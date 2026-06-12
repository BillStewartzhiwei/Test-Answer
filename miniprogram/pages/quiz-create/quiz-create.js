const api = require('../../utils/request')

Page({
  data: { spaceId: '', title: '', timeLimitMinutes: 0 },
  onLoad(query) {
    this.setData({ spaceId: query.spaceId })
  },
  onTitle(e) { this.setData({ title: e.detail.value }) },
  onLimit(e) { this.setData({ timeLimitMinutes: Number(e.detail.value || 0) }) },
  submit() {
    api.post('/quizzes', {
      spaceId: Number(this.data.spaceId),
      title: this.data.title,
      timeLimitMinutes: this.data.timeLimitMinutes,
      questions: []
    }).then(quiz => wx.redirectTo({ url: `/pages/question-edit/question-edit?quizId=${quiz.id}` }))
  }
})
