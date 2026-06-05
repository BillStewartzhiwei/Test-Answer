const request = require("../../utils/request");

Page({
  data: {
    code: ""
  },

  onCodeChange(event) {
    this.setData({ code: event.detail });
  },

  async join() {
    const data = await request.post("/invites/join", { code: this.data.code });
    wx.redirectTo({ url: `/pages/space/detail/index?id=${data.space.id}` });
  }
});
