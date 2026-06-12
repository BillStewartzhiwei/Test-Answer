const api = require('../../utils/request')

Page({
  data: { name: '', description: '' },
  onName(e) { this.setData({ name: e.detail.value }) },
  onDescription(e) { this.setData({ description: e.detail.value }) },
  submit() {
    api.post('/spaces', this.data).then(() => wx.navigateBack())
  }
})
