package com.guohuai.cache.entity;

import java.math.BigDecimal;

@lombok.Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Builder
public class SPVHoldCacheEntity {
	
	/**
	 * SELECT concat('HMSET m:spv:p:', productOid),
ifnull(concat(' productOid ', productOid), ''),
ifnull(concat(' totalVolume ', truncate(totalVolume*10000, 0)), ''),
ifnull(concat(' lockRedeemHoldVolume ',  truncate(lockRedeemHoldVolume*10000, 0)), '')
FROM t_money_publisher_hold 
WHERE accountType='SPV' and productOid is not null and
(oid in ('#oids') or '#xoids'='x');
	 */
	public static String[] zoomArr = new String[] { "lockRedeemHoldVolume", "totalVolume" };

	/** 所属理财产品 */
	private String productOid;
	/** 锁定可赎回份额 */
	private BigDecimal lockRedeemHoldVolume = BigDecimal.ZERO;
	/** 总份额 */
	private BigDecimal totalVolume = BigDecimal.ZERO;
}
