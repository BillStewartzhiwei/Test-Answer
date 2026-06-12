const api = require('../../utils/request')

Page({
  data: { spaceId: '', validDays: '', maxUses: '', invites: [] },
  onLoad(query) {
    this.setData({ spaceId: query.spaceId })
    this.load()
  },
  onValidDays(e) { this.setData({ validDays: e.detail.value }) },
  onMaxUses(e) { this.setData({ maxUses: e.detail.value }) },
  load() {
    api.get(`/spaces/${this.data.spaceId}/invites`).then(invites => this.setData({ invites }))
  },
  create() {
    api.post(`/spaces/${this.data.spaceId}/invites`, {
      validDays: this.data.validDays ? Number(this.data.validDays) : null,
      maxUses: this.data.maxUses ? Number(this.data.maxUses) : null
    }).then(() => this.load())
  },
  disable(e) {
    api.post(`/invites/${e.currentTarget.dataset.id}/disable`).then(() => this.load())
  },
  remove(e) {
    const { id, code } = e.currentTarget.dataset
    wx.showModal({
      title: '删除邀请码',
      content: `确定删除 ${code} 吗？删除后列表中不再显示。`,
      confirmColor: '#c2412d',
      success: res => {
        if (!res.confirm) return
        api.post(`/invites/${id}/delete`).then(() => this.load())
      }
    })
  }
})
