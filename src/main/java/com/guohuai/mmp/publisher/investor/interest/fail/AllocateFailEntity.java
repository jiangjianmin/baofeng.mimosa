package com.guohuai.mmp.publisher.investor.interest.fail;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.duration.fact.income.IncomeAllocate;
import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发行人-投资人-阶段收益明细
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_GAM_ASSETPOOL_INCOME_ALLOCATE_FAIL")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
@DynamicInsert
@DynamicUpdate
public class AllocateFailEntity extends UUID {
	

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1075290918725891826L;

	/**
	
	
	/**
	 * 关联收益分配
	 */
	@JoinColumn(name = "allocateOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	IncomeAllocate incomeAllocate;
	
	/**
	 * 关联投资者名册
	 * 
	 */
	@JoinColumn(name = "holdOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	PublisherHoldEntity publisherHold;
	
	/**
	 * 失败时间
	 */
	Timestamp failTime;
	
	/**
	 * 失败原因
	 */
	String failComment;
	
	/**
	 * 成功时间
	 */
	Timestamp successTime;
	
	/**
	 * 收益日
	 */
	Date allocateDate;
	
	
	
	Timestamp updateTime, createTime;
}
