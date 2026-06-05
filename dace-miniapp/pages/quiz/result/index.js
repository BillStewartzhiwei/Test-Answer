const request = require("../../../utils/request");

Page({
  data: {
    attempt: {}
  },

  async onLoad(query) {
    const attempt = await request.get(`/attempts/${query.attemptId}`);
    this.setData({ attempt });
  }
});
