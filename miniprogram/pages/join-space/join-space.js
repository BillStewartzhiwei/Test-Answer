const api = require('../../utils/request')

Page({
  data: { code: '' },
  onCode(e) { this.setData({ code: e.detail.value.toUpperCase() }) },
  join() {
    api.post('/invites/join', { code: this.data.code }).then(() => {
      wx.showToast({ title: '已加入' })
      wx.reLaunch({ url: '/pages/home/home' })
    })
  }
})
