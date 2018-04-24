package com.guohuai.ams.product.productChannel;

import java.math.BigDecimal;

import com.guohuai.ams.channel.Channel;
import com.guohuai.component.web.view.BaseResp;

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
public class ChooseChannelResp extends BaseResp {

	public ChooseChannelResp(Channel channel) {
		this.oid = channel.getOid();
		this.channelCode = channel.getChannelCode();
		this.channelName = channel.getChannelName();
		this.channelId = channel.getChannelId();
		this.channelFee = channel.getChannelFee();
		this.joinType = channel.getJoinType();
		this.partner = channel.getPartner();
		this.contactName = channel.getChannelContactName();
		this.channelStatus = channel.getChannelStatus();
	}

	private String oid;
	/**
	 * 渠道编号
	 */
	private String channelCode;
	/**
	 * 渠道名称
	 */
	private String channelName;
	/**
	 * 渠道标识
	 */
	private String channelId;
	/**
	 * 渠道费率
	 */
	private BigDecimal channelFee;
	/**
	 * 接入方式
	 */
	private String joinType;
	/**
	 * 合作方
	 */
	private String partner;
	/**
	 * 渠道联系人
	 */
	private String contactName;
	/**
	 * 渠道状态
	 */
	private String channelStatus;

}
