package com.guohuai.ams.channel.channelapprove;

import java.sql.Timestamp;

import lombok.EqualsAndHashCode;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@lombok.Builder
public class ChannelApproveQueryRep {
	
	String oid, channelOid, channelName, channelApprovelCode, requestType, requester, approvelMan, approvelResult, remark;
	
	Timestamp requestTime, updateTime;
}
