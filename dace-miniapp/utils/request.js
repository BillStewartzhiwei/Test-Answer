const app = getApp();

function request(options) {
  const token = wx.getStorageSync("token");
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.apiBaseUrl}${options.url}`,
      method: options.method || "GET",
      data: options.data || {},
      header: {
        "content-type": "application/json",
        Authorization: token ? `Bearer ${token}` : ""
      },
      success(res) {
        const body = res.data || {};
        if (body.code === 0) {
          resolve(body.data);
          return;
        }
        wx.showToast({ title: body.msg || "请求失败", icon: "none" });
        reject(body);
      },
      fail(err) {
        wx.showToast({ title: "网络异常", icon: "none" });
        reject(err);
      }
    });
  });
}

module.exports = {
  get(url, data) {
    return request({ url, data });
  },
  post(url, data) {
    return request({ url, method: "POST", data });
  },
  put(url, data) {
    return request({ url, method: "PUT", data });
  },
  del(url, data) {
    return request({ url, method: "DELETE", data });
  }
};
