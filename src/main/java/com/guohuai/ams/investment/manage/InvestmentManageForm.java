package com.guohuai.ams.investment.manage;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.validation.constraints.NotNull;

import com.guohuai.component.web.parameter.validation.Enumerations;

import lombok.Data;

@Data
public class InvestmentManageForm implements Serializable {

	private static final long serialVersionUID = -8744625712102166340L;
	
	private String oid;
	/**
	 * 标的编号
	 */
	@NotNull(message = "标的编号不能为空")
	private String sn;
	/**
	 * 标的名称
	 */
	@NotNull(message = "标的名称不能为空")
	private String name;
	/**
	 * 付息方式
	 */
	private String accrualType;
	/**
	 * 拟成立日
	 */
	private Date expSetDate;
	/**
	 * 成立日
	 */
	private Date setDate;
	/**
	 * 标的类型
	 */
	@NotNull(message = "标的类型不能为空")
	private String type;
	/**
	 * 投资方向
	 */
	private String investment;
	/**
	 * 预计收益
	 */
	private BigDecimal expIncome;
	/**
	 * 标的规模
	 */
	private BigDecimal raiseScope;
	/**
	 * 标的限期
	 */
	@NotNull
	private Integer life;
	/**
	 * 标的限期单位
	 */
	@NotNull
	@Enumerations(values = { "day", "month", "year" })
	private String lifeUnit;
	/**
	 * 标的限期（日）
	 */
	private Integer lifed;
	/**
	 * 合同年天数
	 */
	@NotNull(message = "合同年天数不能为空")
	private Integer contractDays;
	/**
	 * 起购金额
	 */
	private BigDecimal floorVolume;

	/**
	 * 收益说明
	 */
	private String expArorDesc;
	/**
	 * 付息周期方式
	 */
	private String accrualCycleType;
	/**
	 * 预计年化收益
	 */
	private BigDecimal expAror;
	/**
	 * 预计年化收益区间
	 */
	private String expArorSec;
	/**
	 * 主题评级
	 */
	private String subjectRating;
	/**
	 * 评级机构
	 */
	private String ratingAgency;
	/**
	 * 评级时间
	 */
	private Date ratingTime;
	/**
	 * 内部评级
	 */
	private String irb;
	/**
	 * 优先级
	 */
	private String prior;
	/**
	 * 募集起始日
	 */
	private Date collectStartDate;
	/**
	 * 募集截止日
	 */
	private Date collectEndDate;
	/**
	 * 募集收益
	 */
	private BigDecimal collectIncomeRate;
	/**
	 * 标的优势
	 */
	private String superiority;
	/**
	 * 融资方
	 */
	private String financer;
	/**
	 * 融资方简介
	 */
	private String financerDesc;
	/**
	 * 担保方
	 */
	private String warrantor;
	/**
	 * 担保方简介
	 */
	private String warrantorDesc;
	/**
	 * 资金用途
	 */
	private String usages;
	/**
	 * 还款说明
	 */
	private String repayment;
	/**
	 * 风控措施
	 */
	private String risk;
	/**
	 * 首付息日
	 */
	private Date arorFirstDate;
	/**
	 * 付息日
	 */
	private Integer accrualDate;
	/**
	 * 资产类型名称
	 */
	private String typeName;
	/**
	 * 状态
	 */
	private String state;
	/**
	 * 创建人
	 */
	private String creator;
	/**
	 * 操作员
	 */
	private String operator;
	/**
	 * 风险配置
	 */
	private String riskOption;
	/**
	 * 创建时间
	 */
	private Timestamp createTime;
	/**
	 * 更新时间
	 */
	private Timestamp updateTime;
	
	/**
	 * 标的总评分
	 */
	private Integer collectScore;
	/**
	 * 标的总评分系数
	 */
	private BigDecimal collectScoreWeight;
	/**
	 * 标的风险系数
	 */
	private BigDecimal riskRate;
	
	/**
	 * [N]逾期天数
	 */
	private int overdueDays;
	/**
	 * [N]逾期收益率
	 */
	private BigDecimal overdueRate;
	
	/**
	 * [y]持有目的
	 */
	@NotNull(message = "持有目的不能为空")
	private String holdPorpush;
}
