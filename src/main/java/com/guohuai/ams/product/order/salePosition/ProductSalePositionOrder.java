package com.guohuai.ams.product.order.salePosition;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
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
@Table(name = "T_GAM_PRODUCT_SALE_POSITION_ORDER")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class ProductSalePositionOrder implements Serializable {

	private static final long serialVersionUID = 4839202649426179728L;
	
	public static final String STATUS_SUBMIT = "SUBMIT";// 待审核
	public static final String STATUS_PASS = "PASS";//审核通过
	public static final String STATUS_FAIL = "FAIL";//审核驳回
	public static final String STATUS_CANCEL = "CANCEL";//取消
	public static final String STATUS_DELETE = "DELETE";//删除
	public static final String STATUS_ACTIVE = "ACTIVE";//已生效
	public static final String STATUS_DEACTIVE = "DEACTIVE";//生效失败
	
	@Id
	private String oid;
	@ManyToOne
	@JoinColumn(name = "productOid")
	private Product product;
	@ManyToOne
	@JoinColumn(name = "schedulingOid")
	private ProductSaleScheduling productSaleScheduling;//关联排期	
	
	private BigDecimal volume;//申请份额
	private Date basicDate;//排期
	private String creator;//申请人
	private Timestamp createTime;//申请时间
	private String auditor;//审批人
	private Timestamp auditTime;//审批时间	
	private String status;//状态
	private String errorMessage;//生效失败原因

}
