const api = require('../../utils/request')

const types = [
  { label: '单选题', value: 'SINGLE' },
  { label: '多选题', value: 'MULTIPLE' },
  { label: '判断题', value: 'JUDGE' }
]

function blankQuestion() {
  return {
    typeIndex: 0,
    title: '',
    score: 1,
    options: [
      { content: '', correct: true },
      { content: '', correct: false }
    ]
  }
}

Page({
  data: { quizId: '', title: '', timeLimitMinutes: 0, questions: [], types },
  onLoad(query) {
    this.setData({ quizId: query.quizId })
    this.load()
  },
  load() {
    api.get(`/quizzes/${this.data.quizId}`).then(detail => {
      const questions = (detail.questions || []).map(item => {
        const typeIndex = Math.max(0, types.findIndex(type => type.value === item.question.type))
        return {
          typeIndex,
          title: item.question.title,
          score: item.question.score,
          options: item.options.map(option => ({ content: option.content, correct: option.correct }))
        }
      })
      this.setData({
        title: detail.quiz.title,
        timeLimitMinutes: detail.quiz.timeLimitMinutes,
        questions: questions.length ? questions : [blankQuestion()]
      })
    })
  },
  onTitle(e) { this.setData({ title: e.detail.value }) },
  onLimit(e) { this.setData({ timeLimitMinutes: Number(e.detail.value || 0) }) },
  onType(e) {
    const index = e.currentTarget.dataset.index
    const questions = this.data.questions
    questions[index].typeIndex = Number(e.detail.value)
    if (types[questions[index].typeIndex].value === 'JUDGE') {
      questions[index].options = [{ content: '正确', correct: true }, { content: '错误', correct: false }]
    }
    this.setData({ questions })
  },
  onQuestionTitle(e) {
    const questions = this.data.questions
    questions[e.currentTarget.dataset.index].title = e.detail.value
    this.setData({ questions })
  },
  onScore(e) {
    const questions = this.data.questions
    questions[e.currentTarget.dataset.index].score = Number(e.detail.value || 1)
    this.setData({ questions })
  },
  onOptionContent(e) {
    const { q, o } = e.currentTarget.dataset
    const questions = this.data.questions
    questions[q].options[o].content = e.detail.value
    this.setData({ questions })
  },
  toggleCorrect(e) {
    const q = Number(e.currentTarget.dataset.q)
    const o = Number(e.currentTarget.dataset.o)
    const questions = this.data.questions
    const question = questions[q]
    const type = types[question.typeIndex].value
    if (type === 'SINGLE' || type === 'JUDGE') {
      question.options.forEach((option, index) => option.correct = index === o)
    } else {
      question.options[o].correct = !question.options[o].correct
    }
    this.setData({ questions })
  },
  addOption(e) {
    const questions = this.data.questions
    questions[e.currentTarget.dataset.index].options.push({ content: '', correct: false })
    this.setData({ questions })
  },
  addQuestion() {
    this.setData({ questions: this.data.questions.concat(blankQuestion()) })
  },
  removeQuestion(e) {
    const questions = this.data.questions
    questions.splice(e.currentTarget.dataset.index, 1)
    this.setData({ questions })
  },
  save() {
    const body = {
      title: this.data.title,
      timeLimitMinutes: this.data.timeLimitMinutes,
      questions: this.data.questions.map((question, index) => ({
        type: types[question.typeIndex].value,
        title: question.title,
        score: question.score,
        sortOrder: index + 1,
        options: question.options.map((option, optionIndex) => ({
          content: option.content,
          correct: option.correct,
          sortOrder: optionIndex + 1
        }))
      }))
    }
    api.put(`/quizzes/${this.data.quizId}`, body).then(() => {
      wx.showToast({ title: '已保存' })
      wx.navigateBack()
    })
  }
})
