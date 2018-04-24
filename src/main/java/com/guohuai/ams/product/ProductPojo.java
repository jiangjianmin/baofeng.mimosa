package com.guohuai.ams.product;

import com.guohuai.ams.label.LabelRep;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductPojo {

	public static final String ProductPojo_showType_double = "double";
	public static final String ProductPojo_showType_single = "single";
	
	// 普通定期产品
	public static final String DEPOSIT_SUBTYPE = "01";
	// 快活宝
	public static final String DEMAND_SUBTYPE = "02";
	// 天天向上
	public static final String INCREMENT_SUBTYPE = "03";
	// 体验金
	public static final String EXPERIENCE_FUND_SUBTYPE = "04";
	// 0元购
	public static final String ZERO_BUY_SUBTYPE = "05";
	// 快定宝
	public static final String VIRTUAL_SUBTYPE = "07";
	// 快定宝
	public static final String BF_PLUS_SUBTYPE = "07";
	// 散标
	public static final String SCATTER_SUBTYPE = "08";
	// 新手标
	public static final String NEW_BIE_SUBTYPE = "09";


	/**
	 * 产品OID
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
	 * 起息日
	 */
	private String setupDate;
	/**
	 * 起投金额 
	 */
	private BigDecimal investMin;
	/**
	 * 预期收益起始
	 */
	private BigDecimal expAror;
	/**
	 * 预期收益结束
	 */
	private BigDecimal expArorSec;
	
	/**
	 * 收益显示
	 */
	private String expArrorDisp;
	/**
	 * 平台 奖励收益
	 */
	private BigDecimal rewardInterest;
	/**
	 * 存续期天数
	 */
	private Integer durationPeriodDays;
	
	/**
	 * 可售规模
	 */
	private BigDecimal maxSaleVolume;
	
	/**
	 * 募集规模
	 */
	private BigDecimal raisedTotalNumber;
	/**
	 * 已募份额
	 */
	private BigDecimal collectedVolume;
	/**
	 * 锁定已募份额
	 */
	private BigDecimal lockCollectedVolume;
	/**
	 * 状态排序号
	 */
	private String stateOrder;
	/**
	 * 状态
	 */
	private String state;
	/**
	 * 状态显示
	 */
	private String stateDisp;
	/**
	 * 单个收益率还是两个
	 */
	private String showType;
	
	private List<LabelRep> labelList;
	
	private BigDecimal tenThousandIncome;
	
	/**
	 * 已投次数
	 */
	private Integer purchaseNum = 0;
	
	private String subType;
	
	/**
	 * 0元购 折合年化收益率
	 */
	private String expectedArrorDisp;
	
	/**
	 * 活动产品详情标题
	 */
	private String activityDetailTitle;
}
