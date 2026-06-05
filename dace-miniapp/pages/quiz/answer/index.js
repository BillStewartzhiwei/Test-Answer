const request = require("../../../utils/request");

Page({
  data: {
    id: null,
    quiz: { questions: [] },
    answers: []
  },

  async onLoad(query) {
    this.setData({ id: query.id });
    await request.post(`/quizzes/${query.id}/start`);
    const quiz = await request.get(`/quizzes/${query.id}`);
    this.setData({ quiz });
  },

  async submit() {
    const result = await request.post(`/quizzes/${this.data.id}/submit`, { answers: this.data.answers });
    wx.redirectTo({ url: `/pages/quiz/result/index?attemptId=${result.attempt_id}` });
  }
});
