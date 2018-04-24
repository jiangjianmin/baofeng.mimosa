package com.guohuai.cache;

import java.math.BigDecimal;
import java.util.Date;

import com.guohuai.basic.common.DateUtil;

public class CacheKeyConstants {

	/** 投资者前缀 */
	public static final String INVESTOR_CACHK_KEY = "m:investor:";
	public static String getInvestorHKey(String userOid) {
		return INVESTOR_CACHK_KEY + userOid;
	}

	/** 产品的缓存前缀 */
	public static final String PRODUCT_CACHE_KEY = "m:p:";
	public static String getProductHKey(String productOid) {
		return PRODUCT_CACHE_KEY + productOid;
	}

	/** 投资者合仓 缓存前缀 */
	public static final String INVESTOR_PRODUCT_CACHE_KEY = "m:i:p:";
	public static String getHoldHKey(String investorOid, String productOid) {
		return INVESTOR_PRODUCT_CACHE_KEY + investorOid + ":" + productOid;
	}

	/** 投资者合仓 缓存前缀索引 */
	public static final String INVESTOR_HOLD_INDEX = "m:h:i:";
	public static String getHoldIndexKey(String userOid) {
		return INVESTOR_HOLD_INDEX + userOid;
	}

	/** 渠道缓存前缀 */
	public static final String CHANNEL_CACHE_KEY = "m:c:";
	public static String getChannelHKey(String cid, String ckey, String productOid) {
		return CacheKeyConstants.CHANNEL_CACHE_KEY + cid + ":" + ckey + ":" + productOid;
	}

	/** spv的缓存前缀 */
	public static final String SPVHOLD_CACHE_KEY = "m:spv:p:";
	public static String getSpvHKey(String productOid) {
		return SPVHOLD_CACHE_KEY + productOid;
	}
	
	/**
	 * 投资者资产池申购累计
	 */
	public static final String INVESTOR_purchaseLimit = "m:i:ap:";
	public static String getInvestorPurchaseLimit(String investorOid, String assetPoolOid) {
		return INVESTOR_purchaseLimit + investorOid + ":" + assetPoolOid;
	}
	
	/**
	 * 资产池申购上限
	 */
	public static final String ASSETPOOL_purchaseLimit = "m:ap:";
	public static String getAssetPoolPurchaseLimit(String assetPoolOid) {
		return ASSETPOOL_purchaseLimit + assetPoolOid;
	}
	
	/**
     * 已支付待接收 途中资产
	 */
	public static final String INVESTOR_toAcceptedAmount = "m:i:ps:";
	public static final String getInvestorToAcceptedAmount(String investorOid) {
		return INVESTOR_toAcceptedAmount + investorOid;
	}
	
	/**
     * 单人单日赎回次数和份额
	 */
	public static final String INVESTOR_REDEEM_LIMIT = "redeem:investor:";
	public static final String getDailyInvestorRedeemLimit(String investorOid) {
		return INVESTOR_REDEEM_LIMIT + investorOid + ":" + DateUtil.format(new Date(), DateUtil.datePattern);
	}
	
	/**
	 * 活转定时创建活期赎回订单后将订单号缓存到Redis中
	 */
	public static final String REDEEM_INVEST_REDEEM_ORDER = "ri:r:i:";
	public static final String getRedeemInvestOrder(String investOrderCode){
		return REDEEM_INVEST_REDEEM_ORDER + investOrderCode;
	}
}
