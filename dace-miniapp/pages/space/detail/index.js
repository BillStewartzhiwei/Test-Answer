const request = require("../../../utils/request");
const auth = require("../../../utils/auth");

Page({
  data: {
    id: null,
    user: {},
    space: {},
    quizzes: []
  },

  async onLoad(query) {
    this.setData({ id: query.id, user: auth.getUser() || {} });
    await this.loadData();
  },

  async loadData() {
    const space = await request.get(`/spaces/${this.data.id}`);
    const quizzes = await request.get(`/spaces/${this.data.id}/quizzes`);
    this.setData({ space, quizzes });
  },

  createQuiz() {
    wx.navigateTo({ url: `/pages/quiz/create/index?spaceId=${this.data.id}` });
  },

  invite() {
    wx.navigateTo({ url: `/pages/space/invite/index?spaceId=${this.data.id}` });
  },

  answer(event) {
    wx.navigateTo({ url: `/pages/quiz/answer/index?id=${event.currentTarget.dataset.id}` });
  },

  openLeaderboard(event) {
    wx.navigateTo({ url: `/pages/leaderboard/index?quizId=${event.currentTarget.dataset.id}` });
  }
});
