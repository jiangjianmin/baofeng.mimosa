package com.guohuai.ams.duration.fact.income;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.duration.assetPool.AssetPoolEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 收益分配事件表
 * @author wangyan
 *
 */
@Entity
@Table(name = "T_GAM_ASSETPOOL_INCOME_EVENT")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class IncomeEvent implements Serializable {

	private static final long serialVersionUID = 4489598533252483250L;
	
	/**
	 * 状态
	 */
	public static final String STATUS_Create = "CREATE";//待审核
	public static final String STATUS_Allocating = "ALLOCATING";//发放中
	public static final String STATUS_Allocated = "ALLOCATED";//发放完成
	public static final String STATUS_AllocateFail = "ALLOCATEFAIL";//发放失败
	public static final String STATUS_Fail = "FAIL";//驳回
	public static final String STATUS_Delete = "DELETE";//已删除

	@Id
	private String oid;
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assetPoolOid", referencedColumnName = "oid")
	private AssetPoolEntity assetPool;//关联资产池
	private Date baseDate; // 基准日
	private BigDecimal allocateIncome = new BigDecimal(0);//总分配收益
	private String creator;// 申请人
	private Timestamp createTime;  // 申请时间
	private String auditor;  // 审批人
	private Timestamp auditTime; // 审批时间
	private Integer days = 0;//收益分配天数分配天数
	private String status;// (待审核: CREATE;发放中: ALLOCATING;发放完成: ALLOCATED;发放失败: ALLOCATEFAIL;驳回: FAIL;已删除: DELETE) 

}
