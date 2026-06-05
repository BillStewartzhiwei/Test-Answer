const request = require("../../utils/request");
const auth = require("../../utils/auth");

Page({
  data: {
    role: "participant",
    nickname: ""
  },

  onRoleChange(event) {
    this.setData({ role: event.detail });
  },

  chooseRole(event) {
    this.setData({ role: event.currentTarget.dataset.role });
  },

  onNicknameChange(event) {
    this.setData({ nickname: event.detail });
  },

  async submit() {
    const { role, nickname } = this.data;
    if (!nickname) {
      wx.showToast({ title: "请输入昵称", icon: "none" });
      return;
    }
    const user = await request.post("/auth/register", { role, nickname });
    auth.setSession(wx.getStorageSync("token"), user);
    wx.redirectTo({ url: "/pages/home/index" });
  }
});
