package com.guohuai.ams.product.order.operating;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
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

@Entity
@Table(name = "T_GAM_PRODUCT_OPERATING_ORDER")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class ProductOperatingOrder implements Serializable {
	
	private static final long serialVersionUID = -273382927227327489L;
	
	// 开启申购 PURCHASE_ON
	public static final String TYPE_PURCHASE_ON = "PURCHASE_ON";
	// 关闭申购 PURCHASE_OFF
	public static final String TYPE_PURCHASE_OFF = "PURCHASE_OFF";
	// 开启赎回 REDEEM_ON
	public static final String TYPE_REDEEM_ON = "REDEEM_ON";
	// 关闭赎回 REDEEM_OFF
	public static final String TYPE_REDEEM_OFF = "REDEEM_OFF";
	
	// 待审核
	public static final String STATUS_SUBMIT = "SUBMIT";
	//审核通过
	public static final String STATUS_PASS = "PASS";
	//审核驳回
	public static final String STATUS_FAIL = "FAIL";
	//取消
	public static final String STATUS_CANCEL = "CANCEL";
	//删除
	public static final String STATUS_DELETE = "DELETE";
	
	@Id
	private String oid;
	@ManyToOne
	@JoinColumn(name = "productOid")
	private Product product;
	private String type;
	private String creator;
	private Timestamp createTime;
	private String auditor;
	private Timestamp auditTime;
	private String status;

}
