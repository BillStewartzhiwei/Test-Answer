const request = require("../../utils/request");
const auth = require("../../utils/auth");

Page({
  login() {
    wx.login({
      success: async ({ code }) => {
        const data = await request.post("/auth/login", { code });
        auth.setSession(data.token, data.user || { openid: data.openid });
        wx.redirectTo({ url: data.need_register ? "/pages/register/index" : "/pages/home/index" });
      }
    });
  }
});
