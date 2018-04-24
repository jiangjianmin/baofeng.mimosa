package com.guohuai.mmp.investor.tradeorder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.product.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 产品审核记录实体
 * 
 * @author wangyan
 *
 */
@Entity
@Table(name = "T_GAM_ALLOCATE_INTEREST_AUDIT")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class AllocateInterestAudit implements Serializable {

	private static final long serialVersionUID = -1862368949023169822L;

	// 审核状态
	public static final String AUDIT_STATE_ToAudit = "TOAUDIT";// 待审核
	public static final String AUDIT_STATE_AuditPass = "AUDITPASS";// 审核通过
	public static final String AUDIT_STATE_AuditReject = "AUDITREJECT";// 已驳回
	public static final String AUDIT_STATE_Withdrawed = "WITHDRAWED";// 已撤回

	// 审核类型
	public static final String AUDIT_TYPE_batch = "BATCH"; // 批量审核
	public static final String AUDIT_TYPE_single = "SINGLE"; // 单个审核
	
	@Id
	private String oid;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "productOid", referencedColumnName = "oid")
	private Product product;// 产品
	
	private BigDecimal totalVolume; // 产品总规模
	private BigDecimal totalRepay; // 本金利息总额（元）
	private BigDecimal buyAmount; // 用户申购总额（元）
	private BigDecimal allocateIncomeAmount; // 分配收益总额（元）
	private BigDecimal ratio; // 年化收益率
	private String applicant; // 申请人
	private Timestamp applyTime; // 申请时间
	private String auditStatus; // 待审核:toAudit 已通过:auditPass 已驳回:auditReject 已撤回:withdrawed
	private String auditor; // 审核人
	private Timestamp auditTime; // 审核时间
	private String auditComment; // 申请理由
	private Timestamp updateTime; // 更新时间
}
