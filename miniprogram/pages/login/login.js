const api = require('../../utils/request')
const app = getApp()

Page({
  data: { openId: '' },
  onOpenId(e) {
    this.setData({ openId: e.detail.value })
  },
  login() {
    if (this.data.openId) {
      this.doLogin({ openId: this.data.openId })
      return
    }
    wx.showToast({ title: '请填写测试 openId', icon: 'none' })
  },
  doLogin(payload) {
    api.post('/auth/login', payload).then(user => {
      app.globalData.token = user.token
      app.globalData.user = user
      wx.setStorageSync('token', user.token)
      wx.setStorageSync('user', user)
      wx.reLaunch({ url: user.registered ? '/pages/home/home' : '/pages/register/register' })
    })
  }
})
