package com.guohuai.ams.productPackage;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

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
@Table(name = "T_GAM_PRODUCT_LOG")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class ProductPackageLog implements Serializable {

	private static final long serialVersionUID = -1862368949023169822L;

	// 审核类型
	public static final String AUDIT_TYPE_Auditing = "AUDITING";// 审核
	public static final String AUDIT_TYPE_Reviewing = "REVIEWING";// 复核
	public static final String AUDIT_TYPE_Approving = "APPROVING";// 准入

	public static final String AUDIT_STATE_Commited = "COMMITED";// 提交
	public static final String AUDIT_STATE_Approval = "APPROVAL";// 批准
	public static final String AUDIT_STATE_Reject = "REJECT";// 驳回
	public static final String AUDIT_STATE_Invalid = "INVALID";// 失效

	@Id
	private String oid;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "productOid", referencedColumnName = "oid")
	private ProductPackage product;// 产品
	private String auditType;// 审核类型
	private String auditComment;// 审核备注
	private String auditState;// 审核状态
	private String auditor;// 审核人
	private Timestamp auditTime;// 审核时间

}
