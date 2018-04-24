package com.guohuai.ams.product.productChannel;

import com.guohuai.component.util.DateUtil;
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
public class ProductChannelResp extends BaseResp {

	public ProductChannelResp(ProductChannel pc) {
		this.oid = pc.getOid();
		this.productOid = pc.getProduct().getOid();// 产品
		this.productName = pc.getProduct().getName();// 产品
		this.productCode = pc.getProduct().getCode();// 产品
		this.productFullName = pc.getProduct().getFullName();// 产品
		this.productStatus = pc.getProduct().getState();// 产品
		this.operator = pc.getOperator();// 申请人
		this.channelOid = pc.getChannel().getChannelName();// 渠道
		this.channelName = pc.getChannel().getOid();// 渠道
		this.marketState = pc.getMarketState();// 上下架状态
		this.createTime = pc.getCreateTime() != null ? DateUtil.formatDate(pc.getCreateTime().getTime()) : "";// 申请渠道销售时间
		this.rackTime = pc.getRackTime() != null ? DateUtil.formatDate(pc.getRackTime().getTime()) : "";// 上架时间
		this.downTime = pc.getDownTime() != null ? DateUtil.formatDate(pc.getDownTime().getTime()) : "";// 下架时间
	}

	private String oid;
	private String productOid;// 产品
	private String productName;// 产品
	private String productCode;// 产品编号
	private String productFullName;// 产品全称
	private String productStatus;// 产品状态
	private String operator;// 申请人
	private String createTime;// 申请渠道销售时间
	private String marketState;// 上下架状态
	private String rackTime;// 上架时间
	private String downTime;// 下架时间
	private String channelOid;// 渠道
	private String channelName;

}
