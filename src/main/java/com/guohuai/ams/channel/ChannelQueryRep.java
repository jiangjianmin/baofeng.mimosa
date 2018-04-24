package com.guohuai.ams.channel;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ChannelQueryRep {

	public ChannelQueryRep(Channel ench) {
		this.oid = ench.getOid();
		this.cid = ench.getCid();
		this.ckey = ench.getCkey();
		this.channelCode = ench.getChannelCode();
		this.channelName = ench.getChannelName();
		this.channelId = ench.getChannelId();
		this.channelFee = ench.getChannelFee();
		this.joinType = ench.getJoinType();
		this.partner = ench.getPartner();
		this.channelContactName = ench.getChannelContactName();
		this.channelStatus = ench.getChannelStatus();
		this.approvelStatus = ench.getApproveStatus();
		this.deleteStatus = ench.getDeleteStatus();
		this.commentNum = 0;
	}
	
	private String oid, cid, ckey, channelCode, channelName, channelId, joinType, partner, channelContactName, channelStatus, approvelStatus, deleteStatus;
	private BigDecimal channelFee;
	private Integer commentNum;
}
