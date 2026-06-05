const request = require("../../utils/request");
const auth = require("../../utils/auth");

Page({
  data: {
    user: {},
    spaces: []
  },

  async onShow() {
    if (!auth.requireLogin()) return;
    const user = auth.getUser() || {};
    const spaces = await request.get("/spaces");
    this.setData({ user, spaces });
  },

  createSpace() {
    wx.navigateTo({ url: "/pages/space/create/index" });
  },

  joinSpace() {
    wx.navigateTo({ url: "/pages/join/index" });
  },

  openSpace(event) {
    wx.navigateTo({ url: `/pages/space/detail/index?id=${event.currentTarget.dataset.id}` });
  }
});
