const request = require("../../../utils/request");

Page({
  data: {
    name: "",
    description: ""
  },

  onNameChange(event) {
    this.setData({ name: event.detail });
  },

  onDescriptionChange(event) {
    this.setData({ description: event.detail });
  },

  async submit() {
    const space = await request.post("/spaces", this.data);
    wx.redirectTo({ url: `/pages/space/detail/index?id=${space.id}` });
  }
});
