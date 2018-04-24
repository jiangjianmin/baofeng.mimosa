package com.guohuai.cache.entity;

@lombok.Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Builder
public class ChannelCacheEntity {
	
	public static String[] zoomArr = new String[] {};
	/**
	 * SELECT concat("HMSET m:c:", b.cid, ":", b.ckey, ":", a.productOid),
  ifnull(concat(" channelOid ", a.channelOid), ""),
  ifnull(concat(" productOid ", a.productOid), ""),
  ifnull(concat(" status ",	 a.status), ""),
  ifnull(concat(" marketState ",	a.marketState), ""),
  ifnull(concat(" cid ",		b.cid), ""),
  ifnull(concat(" ckey ",		b.ckey), ""),
  ifnull(concat(" channelName ",  b.channelName), "") 
FROM
  t_gam_product_channel a 
  LEFT JOIN t_money_platform_channel b 
    ON a.channelOid = b.oid 
WHERE 
a.oid in ('#oids') or '#xoids'='x';
	 */
	
	/**
	 * 渠道Oid
	 */
	private String channelOid;
	/**
	 * 产品Id
	 */
	private String productOid;

	/**
	 * 渠道状态
	 */
	private String status;

	/**
	 * 上下架状态
	 */
	private String marketState;

	/**
	 * cid
	 */
	private String cid;

	/**
	 * ckey
	 */
	private String ckey;

	/**
	 * 渠道名称
	 */
	private String channelName;
}
