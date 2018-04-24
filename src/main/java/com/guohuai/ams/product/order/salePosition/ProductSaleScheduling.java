package com.guohuai.ams.product.order.salePosition;

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

import com.guohuai.ams.product.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 可售份额排期
 * @author wangyan
 *
 */
@Entity
@Table(name = "T_GAM_PRODUCT_SALE_SCHEDULING")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class ProductSaleScheduling implements Serializable {

	private static final long serialVersionUID = 1346251430674106885L;
	@Id
	private String oid;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "productOid", referencedColumnName = "oid")
	private Product product;//关联产品	
	private Date basicDate;//排期
	private BigDecimal auditAmount;//	审批份额	
	private BigDecimal applyAmount;//申请份额
	private BigDecimal approvalAmount;//生效份额
	private Timestamp syncTime;//同步时间
	private Timestamp createTime;
	private Timestamp updateTime;

}
