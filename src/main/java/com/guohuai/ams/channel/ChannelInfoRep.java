package com.guohuai.ams.channel;

import java.math.BigDecimal;

import com.guohuai.component.web.view.BaseResp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ChannelInfoRep extends BaseResp {

	private String cid;
	private String ckey;
	
	/**
	 * 渠道Oid
	 */
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
	private String channelContactName;	
	/**
	 * 渠道联系人邮件
	 */
	private String channelEmail;
	/**
	 * 渠道联系人电话
	 */
	private String channelPhone;
	/**
	 * 渠道地址
	 */
	private String channelAddress;
	/**
	 * 渠道状态
	 */
	private String channelStatus;
	/**
	 * 审核状态
	 */
	private String approvelStatus;
	/**
	 * 删除状态
	 */
	private String deleteStatus;
	/**
	 * 计提费用合计
	 */
	private BigDecimal accruedFeeTotal;
	/**
	 * 支付费用合计
	 */
	private BigDecimal payFeeTotal;
	/**
	 * 支付费用手续费合计
	 */
	private BigDecimal payCouFeeTotal;
}
