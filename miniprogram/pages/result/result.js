const api = require('../../utils/request')

Page({
  data: { result: {}, accuracyText: '0%' },
  onLoad(query) {
    const cached = wx.getStorageSync('lastResult')
    if (cached && String(cached.attemptId) === String(query.attemptId)) {
      this.setResult(cached)
      return
    }
    api.get(`/attempts/${query.attemptId}`).then(result => this.setResult(result))
  },
  setResult(result) {
    this.setData({
      result,
      accuracyText: `${Math.round((result.accuracy || 0) * 100)}%`
    })
  }
})
