package com.guohuai.ams.product.productChannel;

import com.guohuai.component.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductChannelView {

	
	public ProductChannelView(ProductChannel c) {
		this.oid = c.getOid();
		this.channelOid = c.getChannel().getOid();
		this.cid = c.getChannel().getCid();
		this.ckey = c.getChannel().getCkey();
		this.channelName = c.getChannel().getChannelName();
		this.productOid = c.getProduct().getOid();
		this.productName = c.getProduct().getName();
		this.productCode = c.getProduct().getCode();
		this.productStatus = c.getProduct().getState();
		this.publishStatus = c.getStatus();
		this.productType = c.getProduct().getType().getOid();
		this.productTypeName = c.getProduct().getType().getName();
		this.marketState = c.getMarketState();
		this.rackTime = c.getRackTime()!=null?DateUtil.formatDatetime(c.getRackTime().getTime()):"";
		this.downTime = c.getDownTime()!=null?DateUtil.formatDatetime(c.getDownTime().getTime()):"";
	}
	
	private String oid;
	private String channelOid;
	private String cid;
	private String ckey;
	private String channelName;
	private String productOid;
	private String productName;
	private String productCode;
	
	private String productType;
	private String productTypeName;
	private String productStatus;
	private String marketState;
	private String rackTime;// 上架时间
	private String downTime;// 下架时间
	
	private String publishStatus;
}
