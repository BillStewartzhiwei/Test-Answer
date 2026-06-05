const request = require("../../utils/request");

Page({
  data: {
    board: { list: [] }
  },

  async onLoad(query) {
    const board = query.quizId
      ? await request.get(`/quizzes/${query.quizId}/leaderboard`)
      : await request.get(`/spaces/${query.spaceId}/leaderboard`);
    this.setData({ board });
  }
});
