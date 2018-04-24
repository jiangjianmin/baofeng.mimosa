package com.guohuai.ams.duration.assetPool.chargefee;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AssetPoolFeeSettingResp {

	public AssetPoolFeeSettingResp(AssetPoolFeeSetting s) {
		this.oid = s.getOid();
		this.startAmount = s.getStartAmount() == null ? null : s.getStartAmount().divide(new BigDecimal("10000"));
		this.endAmount = s.getEndAmount() == null ? null : s.getEndAmount().divide(new BigDecimal("10000"));
		this.feeRatio = s.getFeeRatio().multiply(new BigDecimal("100"));
	}

	private String oid;
	private BigDecimal startAmount;
	private BigDecimal endAmount;
	private BigDecimal feeRatio;

}
