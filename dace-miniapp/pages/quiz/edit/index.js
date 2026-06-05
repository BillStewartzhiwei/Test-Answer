const request = require("../../../utils/request");

Page({
  data: {
    id: null,
    quiz: { questions: [] }
  },

  async onLoad(query) {
    this.setData({ id: query.id });
    await this.loadData();
  },

  async loadData() {
    const quiz = await request.get(`/quizzes/${this.data.id}`);
    this.setData({ quiz });
  },

  addQuestion() {
    wx.showToast({ title: "下一步实现题目表单", icon: "none" });
  },

  async publish() {
    await request.put(`/quizzes/${this.data.id}/publish`);
    wx.navigateBack();
  }
});
