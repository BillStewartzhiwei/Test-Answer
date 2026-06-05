function setSession(token, user) {
  wx.setStorageSync("token", token);
  wx.setStorageSync("user", user);
}

function getUser() {
  return wx.getStorageSync("user") || null;
}

function requireLogin() {
  const token = wx.getStorageSync("token");
  if (!token) {
    wx.redirectTo({ url: "/pages/login/index" });
    return false;
  }
  return true;
}

module.exports = {
  setSession,
  getUser,
  requireLogin
};
