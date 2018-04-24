package com.guohuai.mmp.publisher.product.agreement;

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
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发行人-产品-协议
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PUBLISHER_PRODUCT_AGREEMENT")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
@DynamicInsert
@DynamicUpdate
public class ProductAgreementEntity extends UUID {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4027132899137369552L;
	
	/** 协议类型--合同 */
	public static final String Agreement_agreementType_investing = "investing";
	public static final String Agreement_agreementType_service = "service";

	/**
	 * 所属产品
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "productOid", referencedColumnName = "oid")
	Product product;

	/**
	 * 所属委托单
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "orderOid", referencedColumnName = "oid")
	InvestorTradeOrderEntity investorTradeOrder;

	/**
	 * 协议编号
	 */
	String agreementCode;
	/**
	 * 协议名称
	 */
	String agreementName;
	/**
	 * 协议地址
	 */
	String agreementUrl;
	/**
	 * 协议类型
	 */
	String agreementType;

	private Timestamp updateTime;

	private Timestamp createTime;
}
