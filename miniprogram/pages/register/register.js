const api = require('../../utils/request')
const app = getApp()

Page({
  data: { role: 'PARTICIPANT', nickname: '' },
  onLoad() {
    const user = app.globalData.user || {}
    this.setData({ nickname: user.nickname || '' })
  },
  onNickname(e) {
    this.setData({ nickname: e.detail.value })
  },
  selectRole(e) {
    this.setData({ role: e.currentTarget.dataset.role })
  },
  submit() {
    api.post('/auth/register', { role: this.data.role, nickname: this.data.nickname }).then(user => {
      app.globalData.token = user.token
      app.globalData.user = user
      wx.setStorageSync('token', user.token)
      wx.setStorageSync('user', user)
      wx.reLaunch({ url: '/pages/home/home' })
    })
  }
})
