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
 * 资产池收益分配排期
 */
@Entity
@Table(name = "T_GAM_ASSETPOOL_INCOMESCHEDULE_SCHEDULING")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class IncomeSchedule extends UUID {

	
	private static final long serialVersionUID = 4774716720622694164L;
	/**
	 * 状态
	 */
	public static final String STATUS_toApprove = "toApprove";//待审核
	public static final String STATUS_pass = "pass";//待执行
	public static final String STATUS_finish = "finish";//已完成
	public static final String STATUS_fail = "fail";//失败
	public static final String STATUS_lose = "lose";//已失效

	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assetPoolOid", referencedColumnName = "oid")
	private AssetPoolEntity assetPool;//关联资产池
	
	private Date basicDate; // 排期日期
	private BigDecimal annualizedRate = new BigDecimal(0);//年化收益率
	private String errorMes;//失败原因
	private String status;// 状态    toApprove待审核，reject驳回，pass待执行，finish已完成，fail失败,lose已失效
	private Timestamp createTime;  
	private Timestamp updateTime; 

}
