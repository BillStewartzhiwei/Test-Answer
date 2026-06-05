const request = require("../../utils/request");
const auth = require("../../utils/auth");

Page({
  login() {
    wx.login({
      success: async ({ code }) => {
        const data = await request.post("/auth/login", { code });
        auth.setSession(data.token, { openid: data.openid });
        wx.redirectTo({ url: "/pages/register/index" });
      }
    });
  }
});
