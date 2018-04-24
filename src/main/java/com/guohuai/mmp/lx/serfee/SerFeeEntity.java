package com.guohuai.mmp.lx.serfee;

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

import com.guohuai.ams.channel.Channel;
import com.guohuai.ams.product.Product;
import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_LX_SERFEE")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
@DynamicInsert
@DynamicUpdate
public class SerFeeEntity extends UUID {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2617078136885944684L;

	
	
	/**
	 * 所属产品
	 */
	@JoinColumn(name = "productOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	Product product;
	
	/**
	 * 所属渠道
	 */
	@JoinColumn(name = "channelOid", referencedColumnName = "oid")
	@ManyToOne(fetch = FetchType.LAZY)
	Channel channel;
	
	
	/**
	 * 日期
	 */
	Date tDay;
	
	/**
	 * $
	 */
	BigDecimal totalVolume;
	
	/**
	 * 
	 */
	BigDecimal fee;
	
	/**
	 * 费率
	 */
	BigDecimal feePercent;
	
	private Timestamp createTime;
	private Timestamp updateTime;
	
}
