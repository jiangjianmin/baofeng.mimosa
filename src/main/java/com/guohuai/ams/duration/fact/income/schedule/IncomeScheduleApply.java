package com.guohuai.ams.duration.fact.income.schedule;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.duration.assetPool.AssetPoolEntity;
import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 资产池收益分配排期申请
 */
@Entity
@Table(name = "T_GAM_ASSETPOOL_INCOMESCHEDULE_APPLY")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class IncomeScheduleApply extends UUID {

	
	private static final long serialVersionUID = 4774716720622694164L;
	/**
	 * 状态
	 */
	public static final String STATUS_toApprove = "toApprove";//待审核
	public static final String STATUS_pass = "pass";//通过
	public static final String STATUS_reject = "reject";//驳回
	public static final String STATUS_delete = "delete";//删除
	public static final String STATUS_lose = "lose";//已失效
	/**
	 * 操作类型 
	 */
	public static final String TYPE_new = "new";//新建
	public static final String TYPE_update = "update";//修改
	public static final String TYPE_delete = "delete";//删除

	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assetPoolOid", referencedColumnName = "oid")
	private AssetPoolEntity assetPool;//关联资产池
	
	private String schedulingOid;//所属排期
	private Date basicDate; // 排期日期
	private BigDecimal annualizedRate = new BigDecimal(0);//年化收益率
	private String creator;// 申请人
	private Timestamp createTime;  // 申请时间
	private String approver;  // 审批人
	private Timestamp approverTime; // 审批时间
	private String status;// 状态    toApprove待审核,pass通过,reject驳回,delete删除,lose已失效
	private String type;//操作类型      new新建，update修改，delete删除

}
