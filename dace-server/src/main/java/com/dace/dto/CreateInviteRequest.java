package com.dace.dto;

public class CreateInviteRequest {
    private Integer expireDays = 7;
    private Integer maxUses = 50;

    public Integer getExpireDays() { return expireDays; }
    public void setExpireDays(Integer expireDays) { this.expireDays = expireDays; }
    public Integer getMaxUses() { return maxUses; }
    public void setMaxUses(Integer maxUses) { this.maxUses = maxUses; }
}
