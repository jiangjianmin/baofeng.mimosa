package com.guohuai.mmp.lx.fee;

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

import com.guohuai.ams.product.Product;
import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_LX_FEE")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class FeeEntity extends UUID {
	/**
	 * 
	 */
	private static final long serialVersionUID = -610387651055089525L;
	
	/**
	 * 所属SPV
	 */
	@JoinColumn(name = "spvOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	PublisherBaseAccountEntity spv;
	
	/**
	 * 所属产品
	 */
	@JoinColumn(name = "productOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	Product product;
	
	
	/**
	 * 日期
	 */
	Date tDay;
	
	/**
	 * $
	 */
	BigDecimal money;
	
	/**
	 * 乐信服务费
	 */
	BigDecimal lxFee;
	
	private Timestamp createTime;
	private Timestamp updateTime;
	
}
