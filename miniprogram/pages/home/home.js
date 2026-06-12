const api = require('../../utils/request')
const app = getApp()

Page({
  data: { user: {}, spaces: [], roleText: '' },
  onShow() {
    const user = app.globalData.user || wx.getStorageSync('user') || {}
    this.setData({ user, roleText: user.role === 'CREATOR' ? '创建者' : '参与者' })
    api.get('/spaces').then(spaces => this.setData({ spaces }))
  },
  goCreate() {
    wx.navigateTo({ url: '/pages/space-create/space-create' })
  },
  goJoin() {
    wx.navigateTo({ url: '/pages/join-space/join-space' })
  },
  goSpace(e) {
    wx.navigateTo({ url: `/pages/space-detail/space-detail?id=${e.currentTarget.dataset.id}` })
  }
})
