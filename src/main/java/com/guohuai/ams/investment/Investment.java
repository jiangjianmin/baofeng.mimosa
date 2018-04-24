package com.guohuai.ams.investment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 投资标的实体类
 * 
 * @author Administrator
 *
 */
@Entity
@Table(name = "T_GAM_TARGET")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@DynamicInsert
@DynamicUpdate
public class Investment extends UUID implements Serializable {

	private static final long serialVersionUID = 7108314017534718240L;
	/**
	 * 投资标的状态status waitPretrial 等待预审 pretrial 预审中 waitMeeting 待过会 metting 过会中
	 * collecting 募集期 （运营期） reject 驳回 invalid 作废
	 */
	public static final String INVESTMENT_STATUS_waitPretrial = "waitPretrial"; // 待预审
	public static final String INVESTMENT_STATUS_pretrial = "pretrial"; // 预审中
	public static final String INVESTMENT_STATUS_waitMeeting = "waitMeeting"; // 待过会
	public static final String INVESTMENT_STATUS_metting = "metting"; // 过会中
	public static final String INVESTMENT_STATUS_collecting = "collecting"; // 募集期
	public static final String INVESTMENT_STATUS_reject = "reject"; // 驳回
	public static final String INVESTMENT_STATUS_invalid = "invalid"; // 作废
	public static final String INVESTMENT_STATUS_meetingpass = "meetingPass"; // 待准入
	public static final String INVESTMENT_STATUS_metted = "metted"; // 过会成功

	/** add by star.zhu **/
	public static final String INVESTMENT_STATUS_NORMAL_INCOME = "normalIncome"; // 正常兑付
	public static final String INVESTMENT_STATUS_OVERDUE_INCOME = "overdueIncome"; // 逾期兑付
	public static final String INVESTMENT_STATUS_TARGET_CANCEL = "targetCancel"; // 坏账核销
	public static final String INVESTMENT_STATUS_OVERDUE_TRANSFER = "overdueTransfer"; // 逾期转让

	/**
	 * 标的生命周期状态
	 */
	public static final String INVESTMENT_LIFESTATUS_PREPARE = "PREPARE"; // 准备期
	public static final String INVESTMENT_LIFESTATUS_STAND_UP = "STAND_UP"; // 已成立
	public static final String INVESTMENT_LIFESTATUS_STAND_FAIL = "STAND_FAIL"; // 成立失败
	public static final String INVESTMENT_LIFESTATUS_CLOSE = "CLOSE"; // 已结束

	/** add by star.zhu **/
	public static final String INVESTMENT_LIFESTATUS_OVER_TIME = "OVER_TIME"; // 已逾期
	public static final String INVESTMENT_LIFESTATUS_PAY_BACK = "PAY_BACK"; // 兑付期
	public static final String INVESTMENT_LIFESTATUS_RETURN_BACK = "RETURN_BACK"; // 已本息兑付

	/**
	 * 标的编号
	 */
	private String sn;
	/**
	 * 表单名称
	 */
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
	private Integer life;
	/**
	 * 标的限期单位
	 */
	private String lifeUnit;
	/**
	 * 标的限期（日）
	 */
	private Integer lifed;
	/**
	 * 合同年天数
	 */
	private Integer contractDays;
	/**
	 * 起购金额
	 */
	private BigDecimal floorVolume;

	/**
	 * 持有份额
	 */
	private BigDecimal holdAmount = BigDecimal.ZERO;

	/**
	 * 申请份额
	 */
	private BigDecimal applyAmount = BigDecimal.ZERO;

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
	// private String expArorSec;
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
	 * 募集期收益
	 */
	private BigDecimal collectIncomeRate;
	/**
	 * 收益起始日
	 */
	private Date incomeStartDate;
	/**
	 * 收益截止日
	 */
	private Date incomeEndDate;
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
	 * 生命周期
	 */
	private String lifeState;
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
	 * 创建时间
	 */
	private Timestamp createTime;
	/**
	 * 更新时间
	 */
	private Timestamp updateTime;
	/**
	 * 驳回备注
	 */
	private String rejectDesc;

	/**
	 * 风险配置
	 */
	@Transient
	private String riskOption;

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
	 * [N]逾期设定日
	 */
	private Date overdueDay;
	/**
	 * [N]逾期天数
	 */
	private int overdueDays;
	/**
	 * [N]逾期收益率
	 */
	private BigDecimal overdueRate;

	/**
	 * 持有目的
	 */
	private String holdPorpush;
	/**
	 * 授信额度
	 */
	private BigDecimal trustAmount;
	/**
	 * 剩余授信额度
	 */
	private BigDecimal restTrustAmount;

	public Investment(String oid, String name) {
		this.oid = oid;
		this.name = name;
	}
}
