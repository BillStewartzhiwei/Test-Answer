const api = require('../../utils/request')

Page({
  data: { title: '排行榜', items: [] },
  onLoad(query) {
    if (query.quizId) {
      this.setData({ title: '单套题排行' })
      api.get(`/quizzes/${query.quizId}/ranking`).then(items => this.setData({ items }))
      return
    }
    this.setData({ title: '空间总排行' })
    api.get(`/spaces/${query.spaceId}/ranking`).then(items => this.setData({ items }))
  }
})
