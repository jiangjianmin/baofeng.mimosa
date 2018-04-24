package com.guohuai.ams.duration.order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import com.guohuai.ams.duration.order.trust.TrustIncomeForm;

import lombok.Data;

/**
 * 存续期--信托（计划）转让表单对象
 * @author star.zhu
 * 2016年5月17日
 */
@Data
public class TransForm implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	// 关联投资标的
	private String t_targetOid;
	// 关联资产池
	private String assetPoolOid;
	// 标的名称
	private String t_targetName;
	// 类型
	private String t_targetType;
	// 成立日
	private Date t_setDate;
	// 起息日
	private Date t_incomeDate; 
	// 申购金额
	private BigDecimal applyCash;
	// 申购份额
	private BigDecimal applyVolume;
	// 收益率
	private BigDecimal yield;
	// 预计年化收益区间	
	private String expArorSec;	
	// 首付息日	
	private Date t_arorFirstDate;
	// 付息方式	
	private String t_accrualType;		
	// 付息日	
	private Integer t_accrualDate;	
	// 合同年天数	
	private Integer t_contractDays;	
	// 标的规模	
	private BigDecimal t_raiseScope;	
	// 授信额度
	private BigDecimal t_trustAmount;
	// 剩余授信额度
	private BigDecimal t_restTrustAmount;
	// 标的限期	
	private Integer t_life;	
	// 标的期限单位
	private String t_lifeUnit;
	// 可售额度
	private BigDecimal t_sellScope;
	// 起购金额	
	private BigDecimal t_floorVolume;	
	// 主题评级	
	private String t_subjectRating;	
	// 收益方式（amortized_cost：摊余成本法；book_value：账面价值法）
	private String profitType;
	// 申请人
	private String asker;
	// 审核人
	private String auditor; 
	// 预约人
	private String reserver; 
	// 确认人
	private String confirmer; 
	// 投资日
	private Date investDate; 
	// 审核金额
	private BigDecimal auditCash;
	// 预约金额
	private BigDecimal reserveCash;
	// 确认金额
	private BigDecimal investCash;
	// 审核份额
	private BigDecimal auditVolume;
	// 预约份额
	private BigDecimal reserveVolume;
	// 确认份额
	private BigDecimal investVolume;
	// 状态（审核/预约/确认 的结果 成功/失败）
	private String state;	

	// 转让份额
	private BigDecimal tranVolume;
	// 转让日期
	private Date tranDate; 
	// 转让溢价
	private BigDecimal tranCash; 
	
	// 兑付期数
	private int seq;
	// 实际收益
	private BigDecimal income; 
	// 是否返还本金
	private int capitalFlag;
	// 本金返还
	private BigDecimal capital;
	
	// 类型（申购，本息兑付，转让）
	private String type;
	// 转让类型
	private String transType;
	// 受让方名称
	private String transferee;
	
	// 本息兑付数据
	private List<TrustIncomeForm> incomeFormList;
}
