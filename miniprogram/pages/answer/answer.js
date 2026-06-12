const api = require('../../utils/request')

Page({
  data: { quizId: '', quiz: {}, questions: [], startedAt: 0, leftText: '不限时', answers: {}, submitting: false },
  timer: null,
  onLoad(query) {
    this.setData({ quizId: query.quizId, startedAt: Date.now() })
    api.get(`/quizzes/${query.quizId}`).then(detail => {
      this.setData({ quiz: detail.quiz, questions: detail.questions })
      this.tick()
      this.timer = setInterval(() => this.tick(), 1000)
    })
  },
  onUnload() {
    if (this.timer) clearInterval(this.timer)
  },
  tick() {
    const limit = this.data.quiz.timeLimitMinutes || 0
    if (!limit) {
      this.setData({ leftText: '不限时' })
      return
    }
    const used = Math.floor((Date.now() - this.data.startedAt) / 1000)
    const left = Math.max(0, limit * 60 - used)
    const seconds = left % 60
    this.setData({ leftText: `${Math.floor(left / 60)}:${seconds < 10 ? '0' + seconds : seconds}` })
    if (left === 0) this.submit()
  },
  onSelect(e) {
    const answers = this.data.answers
    answers[e.currentTarget.dataset.qid] = e.detail.value.map(Number)
    this.setData({ answers })
  },
  submit() {
    if (this.data.submitting) return
    this.setData({ submitting: true })
    if (this.timer) clearInterval(this.timer)
    const durationSeconds = Math.floor((Date.now() - this.data.startedAt) / 1000)
    const answers = Object.keys(this.data.answers).map(questionId => ({
      questionId: Number(questionId),
      selectedOptionIds: this.data.answers[questionId]
    }))
    api.post(`/quizzes/${this.data.quizId}/attempts`, { durationSeconds, answers }).then(result => {
      wx.setStorageSync('lastResult', result)
      wx.redirectTo({ url: `/pages/result/result?attemptId=${result.attemptId}` })
    })
  }
})
