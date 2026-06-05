const request = require("../../../utils/request");

Page({
  data: {
    spaceId: null,
    title: "",
    description: "",
    timeLimit: 0
  },

  onLoad(query) {
    this.setData({ spaceId: query.spaceId });
  },

  onTitleChange(event) {
    this.setData({ title: event.detail });
  },

  onDescriptionChange(event) {
    this.setData({ description: event.detail });
  },

  onTimeLimitChange(event) {
    this.setData({ timeLimit: Number(event.detail || 0) });
  },

  async submit() {
    const quiz = await request.post(`/spaces/${this.data.spaceId}/quizzes`, {
      title: this.data.title,
      description: this.data.description,
      timeLimit: this.data.timeLimit
    });
    wx.redirectTo({ url: `/pages/quiz/edit/index?id=${quiz.id}` });
  }
});
