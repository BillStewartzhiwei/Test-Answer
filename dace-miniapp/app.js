App({
  globalData: {
    apiBaseUrl: "http://localhost:8080/v1",
    token: "",
    user: null
  },

  onLaunch() {
    this.globalData.token = wx.getStorageSync("token") || "";
    this.globalData.user = wx.getStorageSync("user") || null;
  }
});
