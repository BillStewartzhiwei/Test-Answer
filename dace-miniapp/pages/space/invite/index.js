const request = require("../../../utils/request");

Page({
  data: {
    spaceId: null,
    invite: {}
  },

  onLoad(query) {
    this.setData({ spaceId: query.spaceId });
  },

  async createInvite() {
    const invite = await request.post(`/spaces/${this.data.spaceId}/invites`, { expireDays: 7, maxUses: 50 });
    this.setData({ invite });
  }
});
