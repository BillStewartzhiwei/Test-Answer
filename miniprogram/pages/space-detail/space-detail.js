const api = require('../../utils/request')
const app = getApp()

Page({
  data: { spaceId: '', user: {}, quizzes: [] },
  onLoad(query) {
    this.setData({ spaceId: query.id, user: app.globalData.user || wx.getStorageSync('user') || {} })
  },
  onShow() {
    if (!this.data.spaceId) return
    api.get(`/spaces/${this.data.spaceId}/quizzes`).then(quizzes => this.setData({ quizzes }))
  },
  goInvite() {
    wx.navigateTo({ url: `/pages/invite-manage/invite-manage?spaceId=${this.data.spaceId}` })
  },
  goCreateQuiz() {
    wx.navigateTo({ url: `/pages/quiz-create/quiz-create?spaceId=${this.data.spaceId}` })
  },
  editQuiz(e) {
    wx.navigateTo({ url: `/pages/question-edit/question-edit?quizId=${e.currentTarget.dataset.id}` })
  },
  publish(e) {
    api.post(`/quizzes/${e.currentTarget.dataset.id}/publish`).then(() => this.onShow())
  },
  closeQuiz(e) {
    api.post(`/quizzes/${e.currentTarget.dataset.id}/close`).then(() => this.onShow())
  },
  answer(e) {
    wx.navigateTo({ url: `/pages/answer/answer?quizId=${e.currentTarget.dataset.id}` })
  },
  goQuizRanking(e) {
    wx.navigateTo({ url: `/pages/ranking/ranking?quizId=${e.currentTarget.dataset.id}` })
  },
  goSpaceRanking() {
    wx.navigateTo({ url: `/pages/ranking/ranking?spaceId=${this.data.spaceId}` })
  }
})
