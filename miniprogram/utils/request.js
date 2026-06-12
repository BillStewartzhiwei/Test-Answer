const app = getApp()

const KEY = 'mockDb'

function now() {
  return new Date().toISOString().replace('T', ' ').slice(0, 19)
}

function loadDb() {
  const db = wx.getStorageSync(KEY)
  if (db) return db
  const fresh = {
    ids: { user: 1, space: 1, invite: 1, quiz: 1, question: 1, option: 1, attempt: 1 },
    users: [],
    spaces: [],
    invites: [],
    members: [],
    quizzes: [],
    questions: [],
    options: [],
    attempts: [],
    answers: []
  }
  saveDb(fresh)
  return fresh
}

function saveDb(db) {
  wx.setStorageSync(KEY, db)
}

function nextId(db, name) {
  const id = db.ids[name]
  db.ids[name] += 1
  return id
}

function currentUser() {
  return app.globalData.user || wx.getStorageSync('user') || {}
}

function authResult(user) {
  return {
    token: `mock-token-${user.id}`,
    userId: user.id,
    role: user.role,
    nickname: user.nickname,
    avatarUrl: user.avatarUrl,
    registered: !!user.role
  }
}

function ok(data) {
  return Promise.resolve(clone(data))
}

function fail(message) {
  wx.showToast({ title: message, icon: 'none' })
  return Promise.reject({ code: 400, message })
}

function clone(data) {
  return data == null ? data : JSON.parse(JSON.stringify(data))
}

function code6() {
  const alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  let value = ''
  for (let i = 0; i < 6; i++) value += alphabet[Math.floor(Math.random() * alphabet.length)]
  return value
}

function getQuizDetail(db, quizId, hideCorrect) {
  const quiz = db.quizzes.find(item => item.id === quizId)
  if (!quiz) return null
  const questions = db.questions
    .filter(item => item.quizId === quizId)
    .sort((a, b) => a.sortOrder - b.sortOrder)
    .map(question => ({
      question,
      options: db.options
        .filter(option => option.questionId === question.id)
        .sort((a, b) => a.sortOrder - b.sortOrder)
        .map(option => hideCorrect ? Object.assign({}, option, { correct: null }) : option)
    }))
  return { quiz, questions }
}

function saveQuizQuestions(db, quizId, questions) {
  const oldQuestionIds = db.questions.filter(item => item.quizId === quizId).map(item => item.id)
  db.questions = db.questions.filter(item => item.quizId !== quizId)
  db.options = db.options.filter(item => oldQuestionIds.indexOf(item.questionId) === -1)

  let totalScore = 0
  ;(questions || []).forEach((item, index) => {
    const question = {
      id: nextId(db, 'question'),
      quizId,
      type: item.type,
      title: item.title,
      score: Number(item.score || 1),
      sortOrder: item.sortOrder || index + 1,
      createdAt: now(),
      updatedAt: now()
    }
    totalScore += question.score
    db.questions.push(question)
    ;(item.options || []).forEach((option, optionIndex) => {
      db.options.push({
        id: nextId(db, 'option'),
        questionId: question.id,
        content: option.content,
        correct: !!option.correct,
        sortOrder: option.sortOrder || optionIndex + 1,
        createdAt: now(),
        updatedAt: now()
      })
    })
  })
  const quiz = db.quizzes.find(item => item.id === quizId)
  if (quiz) {
    quiz.totalScore = totalScore
    quiz.updatedAt = now()
  }
}

function rankAttempts(attempts) {
  return attempts
    .slice()
    .sort((a, b) => b.score - a.score || a.durationSeconds - b.durationSeconds || String(a.submittedAt).localeCompare(String(b.submittedAt)))
}

function rankingItem(db, attempt, rank) {
  const user = db.users.find(item => item.id === attempt.userId) || {}
  return {
    rank,
    userId: attempt.userId,
    nickname: user.nickname || '',
    avatarUrl: user.avatarUrl || '',
    score: attempt.score,
    totalScore: attempt.totalScore,
    durationSeconds: attempt.durationSeconds,
    submittedAt: attempt.submittedAt
  }
}

function handle(method, url, data = {}) {
  const db = loadDb()
  const user = currentUser()

  if (method === 'POST' && url === '/auth/login') {
    const openId = data.openId || `mock-${data.code || Date.now()}`
    let found = db.users.find(item => item.openId === openId)
    if (!found) {
      found = { id: nextId(db, 'user'), openId, role: '', nickname: openId, avatarUrl: '', createdAt: now(), updatedAt: now() }
      db.users.push(found)
      saveDb(db)
    }
    return ok(authResult(found))
  }

  if (method === 'POST' && url === '/auth/register') {
    const found = db.users.find(item => item.id === user.userId)
    if (!found) return fail('请先登录')
    if (found.role) return fail('角色注册后不可更改')
    found.role = data.role
    found.nickname = data.nickname || found.nickname
    found.avatarUrl = data.avatarUrl || ''
    found.updatedAt = now()
    saveDb(db)
    return ok(authResult(found))
  }

  if (method === 'GET' && url === '/spaces') {
    if (user.role === 'CREATOR') return ok(db.spaces.filter(item => item.creatorId === user.userId && item.status !== 'ARCHIVED'))
    const spaceIds = db.members.filter(item => item.userId === user.userId).map(item => item.spaceId)
    return ok(db.spaces.filter(item => spaceIds.indexOf(item.id) >= 0 && item.status !== 'ARCHIVED'))
  }

  if (method === 'POST' && url === '/spaces') {
    const space = { id: nextId(db, 'space'), creatorId: user.userId, name: data.name, description: data.description, status: 'ACTIVE', createdAt: now(), updatedAt: now() }
    db.spaces.push(space)
    saveDb(db)
    return ok(space)
  }

  const spaceUpdate = url.match(/^\/spaces\/(\d+)$/)
  if (method === 'PUT' && spaceUpdate) {
    const space = db.spaces.find(item => item.id === Number(spaceUpdate[1]))
    if (!space) return fail('空间不存在')
    space.name = data.name
    space.description = data.description
    space.updatedAt = now()
    saveDb(db)
    return ok(space)
  }

  const inviteList = url.match(/^\/spaces\/(\d+)\/invites$/)
  if (inviteList && method === 'GET') {
    const spaceId = Number(inviteList[1])
    return ok(db.invites.filter(item => item.spaceId === spaceId).sort((a, b) => b.id - a.id))
  }
  if (inviteList && method === 'POST') {
    const spaceId = Number(inviteList[1])
    let code = code6()
    while (db.invites.some(item => item.code === code)) code = code6()
    const expire = new Date(Date.now() + (Number(data.validDays || 7) * 86400000))
    const invite = {
      id: nextId(db, 'invite'),
      spaceId,
      creatorId: user.userId,
      code,
      expireAt: expire.toISOString().slice(0, 19).replace('T', ' '),
      maxUses: Number(data.maxUses || 50),
      usedCount: 0,
      status: 'ACTIVE',
      createdAt: now(),
      updatedAt: now()
    }
    db.invites.push(invite)
    saveDb(db)
    return ok(invite)
  }

  const disableInvite = url.match(/^\/invites\/(\d+)\/disable$/)
  if (method === 'POST' && disableInvite) {
    const invite = db.invites.find(item => item.id === Number(disableInvite[1]))
    if (invite) invite.status = 'DISABLED'
    saveDb(db)
    return ok(null)
  }

  const deleteInvite = url.match(/^\/invites\/(\d+)\/delete$/)
  if (method === 'POST' && deleteInvite) {
    const inviteId = Number(deleteInvite[1])
    db.invites = db.invites.filter(item => item.id !== inviteId)
    saveDb(db)
    return ok(null)
  }

  if (method === 'POST' && url === '/invites/join') {
    const invite = db.invites.find(item => item.code === data.code)
    if (!invite || invite.status !== 'ACTIVE') return fail('邀请码无效')
    if (invite.usedCount >= invite.maxUses) return fail('邀请码使用次数已满')
    if (db.members.some(item => item.spaceId === invite.spaceId && item.userId === user.userId)) return fail('已加入该空间')
    const member = { id: db.members.length + 1, spaceId: invite.spaceId, userId: user.userId, joinedAt: now() }
    db.members.push(member)
    invite.usedCount += 1
    saveDb(db)
    return ok(member)
  }

  const quizList = url.match(/^\/spaces\/(\d+)\/quizzes$/)
  if (method === 'GET' && quizList) {
    const spaceId = Number(quizList[1])
    let quizzes = db.quizzes.filter(item => item.spaceId === spaceId)
    if (user.role === 'PARTICIPANT') quizzes = quizzes.filter(item => item.status === 'PUBLISHED')
    return ok(quizzes.sort((a, b) => b.id - a.id))
  }

  if (method === 'POST' && url === '/quizzes') {
    const quiz = {
      id: nextId(db, 'quiz'),
      spaceId: Number(data.spaceId),
      creatorId: user.userId,
      title: data.title,
      timeLimitMinutes: Number(data.timeLimitMinutes || 0),
      totalScore: 0,
      status: 'DRAFT',
      createdAt: now(),
      updatedAt: now()
    }
    db.quizzes.push(quiz)
    saveQuizQuestions(db, quiz.id, data.questions)
    saveDb(db)
    return ok(quiz)
  }

  const quizIdOnly = url.match(/^\/quizzes\/(\d+)$/)
  if (quizIdOnly && method === 'GET') {
    const detail = getQuizDetail(db, Number(quizIdOnly[1]), user.role === 'PARTICIPANT')
    return detail ? ok(detail) : fail('题目套不存在')
  }
  if (quizIdOnly && method === 'PUT') {
    const quiz = db.quizzes.find(item => item.id === Number(quizIdOnly[1]))
    if (!quiz) return fail('题目套不存在')
    quiz.title = data.title
    quiz.timeLimitMinutes = Number(data.timeLimitMinutes || 0)
    quiz.updatedAt = now()
    saveQuizQuestions(db, quiz.id, data.questions)
    saveDb(db)
    return ok(quiz)
  }

  const publishQuiz = url.match(/^\/quizzes\/(\d+)\/publish$/)
  if (method === 'POST' && publishQuiz) {
    const quiz = db.quizzes.find(item => item.id === Number(publishQuiz[1]))
    if (!quiz) return fail('题目套不存在')
    if (!db.questions.some(item => item.quizId === quiz.id)) return fail('至少需要一道题')
    quiz.status = 'PUBLISHED'
    quiz.publishedAt = now()
    saveDb(db)
    return ok(null)
  }

  const closeQuiz = url.match(/^\/quizzes\/(\d+)\/close$/)
  if (method === 'POST' && closeQuiz) {
    const quiz = db.quizzes.find(item => item.id === Number(closeQuiz[1]))
    if (quiz) quiz.status = 'CLOSED'
    saveDb(db)
    return ok(null)
  }

  const submitAttempt = url.match(/^\/quizzes\/(\d+)\/attempts$/)
  if (method === 'POST' && submitAttempt) {
    const quizId = Number(submitAttempt[1])
    const quiz = db.quizzes.find(item => item.id === quizId)
    if (!quiz || quiz.status !== 'PUBLISHED') return fail('只能作答已发布题目')
    if (db.attempts.some(item => item.quizId === quizId && item.userId === user.userId)) return fail('每套题只能作答一次')
    const questions = db.questions.filter(item => item.quizId === quizId).sort((a, b) => a.sortOrder - b.sortOrder)
    const answerMap = {}
    ;(data.answers || []).forEach(answer => { answerMap[answer.questionId] = answer.selectedOptionIds || [] })
    let score = 0
    let correctCount = 0
    const details = []
    questions.forEach(question => {
      const correctIds = db.options.filter(option => option.questionId === question.id && option.correct).map(option => option.id).sort()
      const selectedIds = (answerMap[question.id] || []).slice().sort()
      const correct = JSON.stringify(correctIds) === JSON.stringify(selectedIds)
      const questionScore = correct ? question.score : 0
      if (correct) {
        score += question.score
        correctCount += 1
      }
      details.push({ questionId: question.id, title: question.title, score: questionScore, correct, selectedOptionIds: selectedIds, correctOptionIds: correctIds })
    })
    const attempt = {
      id: nextId(db, 'attempt'),
      quizId,
      spaceId: quiz.spaceId,
      userId: user.userId,
      score,
      totalScore: quiz.totalScore,
      correctCount,
      questionCount: questions.length,
      durationSeconds: Number(data.durationSeconds || 0),
      submittedAt: now(),
      details
    }
    db.attempts.push(attempt)
    saveDb(db)
    const ranked = rankAttempts(db.attempts.filter(item => item.quizId === quizId))
    const rank = ranked.findIndex(item => item.id === attempt.id) + 1
    return ok(Object.assign({}, attempt, { attemptId: attempt.id, accuracy: questions.length ? correctCount / questions.length : 0, rank }))
  }

  const attemptResult = url.match(/^\/attempts\/(\d+)$/)
  if (method === 'GET' && attemptResult) {
    const attempt = db.attempts.find(item => item.id === Number(attemptResult[1]))
    if (!attempt) return fail('作答记录不存在')
    const ranked = rankAttempts(db.attempts.filter(item => item.quizId === attempt.quizId))
    const rank = ranked.findIndex(item => item.id === attempt.id) + 1
    return ok(Object.assign({}, attempt, { attemptId: attempt.id, accuracy: attempt.questionCount ? attempt.correctCount / attempt.questionCount : 0, rank }))
  }

  const quizRanking = url.match(/^\/quizzes\/(\d+)\/ranking$/)
  if (method === 'GET' && quizRanking) {
    const attempts = rankAttempts(db.attempts.filter(item => item.quizId === Number(quizRanking[1])))
    return ok(attempts.map((attempt, index) => rankingItem(db, attempt, index + 1)))
  }

  const spaceRanking = url.match(/^\/spaces\/(\d+)\/ranking$/)
  if (method === 'GET' && spaceRanking) {
    const spaceId = Number(spaceRanking[1])
    const grouped = {}
    db.attempts.filter(item => item.spaceId === spaceId).forEach(attempt => {
      if (!grouped[attempt.userId]) grouped[attempt.userId] = { userId: attempt.userId, score: 0, totalScore: 0, durationSeconds: 0, submittedAt: '' }
      grouped[attempt.userId].score += attempt.score
      grouped[attempt.userId].totalScore += attempt.totalScore
      grouped[attempt.userId].durationSeconds += attempt.durationSeconds
      grouped[attempt.userId].submittedAt = attempt.submittedAt
    })
    const items = Object.keys(grouped).map(id => grouped[id]).sort((a, b) => b.score - a.score || a.durationSeconds - b.durationSeconds)
    return ok(items.map((item, index) => rankingItem(db, item, index + 1)))
  }

  return fail(`未实现的本地接口：${method} ${url}`)
}

function request(method, url, data = {}) {
  return handle(method, url, data)
}

module.exports = {
  get: (url, data) => request('GET', url, data),
  post: (url, data) => request('POST', url, data),
  put: (url, data) => request('PUT', url, data)
}
