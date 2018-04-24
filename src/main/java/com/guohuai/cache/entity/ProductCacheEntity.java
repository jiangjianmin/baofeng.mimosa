package com.guohuai.cache.entity;

import java.math.BigDecimal;
import java.sql.Date;

@lombok.Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Builder
public class ProductCacheEntity {
	public static String[] zoomArr = new String[] { "netUnitShare", "investMin", "investAdditional", "investMax",
			"minRredeem", "maxRredeem", "additionalRredeem", "netMaxRredeemDay", "dailyNetMaxRredeem", "maxHold",
			"singleDailyMaxRedeem", "maxSaleVolume", "lockCollectedVolume", "collectedVolume", "currentVolume", };
	/** 
	 * 产品Oid 
	 */
	private String productOid;
	/**
	 * 产品名称
	 */
	private String name;
	
	/** 
	 * 产品类型 
	 */
	private String type;
	
	/**
	 * 收益 计息基数
	 */
	private String incomeCalcBasis;
	
	/**
	 * 募集开始日期
	 */
	private Date raiseStartDate;

	/**
	 * 募集结束日期
	 */
	private Date raiseEndDate;
	
	/**
	 * 预期收益率
	 */
	private BigDecimal expAror;
	private BigDecimal expArorSec;
	
	/** 
	 * 单位份额净值 
	 */
	private BigDecimal netUnitShare;
	
	/** 
	 * 单笔投资最低份额 
	 */
	private BigDecimal investMin;
	/** 
	 * 单笔投资追加份额 
	 */
	private BigDecimal investAdditional;
	/** 
	 * 单笔投资最高份额 
	 */
	private BigDecimal investMax;
	
	/** 
	 * 单笔赎回最低下限 
	 */
	private BigDecimal minRredeem;
	/** 
	 * 单笔赎回最高份额 
	 */
	private BigDecimal maxRredeem;
	/** 
	 * 单笔赎回追加份额 
	 */
	private BigDecimal additionalRredeem;
	
	/** 
	 * 单日净赎回上限 
	 */
	private BigDecimal netMaxRredeemDay;
	/** 
	 * 剩余赎回金额 
	 */
	private BigDecimal dailyNetMaxRredeem;
	
	/** 单人持有上限 */
	private BigDecimal maxHold;
	/** 单人单日赎回上限 */
	private BigDecimal singleDailyMaxRedeem;
	
	/** 是否屏蔽赎回确认 */
	private String isOpenRedeemConfirm;
	
	/**
	 * 产品成立日期(存续期开始日期)
	 */
	private Date setupDate;
	
	/** 产品状态 */
	private String state;
	
	/**
	 * 募集失败日期
	 */
	private Date raiseFailDate;
	
	/**
	 * 存续期结束
	 */
	private Date durationPeriodEndDate;
	
	/** 当前份额(投资者持有份额) */
	private BigDecimal currentVolume;
	
	/**
	 * 已募规模
	 */
	private BigDecimal collectedVolume;
	
	/** 最高可售份额(申请的) */
	private BigDecimal maxSaleVolume;
	
	/** 锁定已募集份额 */
	private BigDecimal lockCollectedVolume;
	
	/**
	 * 还本付息日
	 */
	private Date repayDate;
	/** 开放申购期 */
	private String isOpenPurchase;
	/** 开放赎回期 */
	private String isOpenRemeed;
	/** 开市时间 */
	private String dealStartTime;
	/** 闭市时间 */
	private String dealEndTime;
	/** 产品标签 */
	private String productLabel;
	
	private Integer singleDayRedeemCount; // 单人单日赎回次数
	
	private String guessOid;//关联的竞猜宝oid
}
