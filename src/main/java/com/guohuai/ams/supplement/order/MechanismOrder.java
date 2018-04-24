package com.guohuai.ams.supplement.order;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.supplement.mechanism.Mechanism;
import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
/**
 * @author qiuliang
 *
 */
@Entity
@Table(name = "t_money_supplement_order")
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class MechanismOrder  extends UUID  {
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1911914882963391648L;


	/**
	 * 订单状态--已建立
	 */
	public static final String Order_Status_Created = "created";


	/**
	 * 订单类型--补单类型
	 */
	public static final String Order_Type_Supplement = "supplement";
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mechanismOid", referencedColumnName = "oid")
	private Mechanism mechanism;//所属机构
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "productOid", referencedColumnName = "oid")
	private Product product;//所属产品
	private String orderCode;//订单号
	private BigDecimal orderAmount;//订单金额
	private String orderType;//订单类型
	private String orderStatus;//订单状态
	private String productName;//产品名称
	@Temporal(TemporalType.TIMESTAMP)
	private Date fictitiousTime;//虚拟订单时间
	@Temporal(TemporalType.TIMESTAMP)
	private Date operateTime;//操作时间
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;//创建时间
	private String operator;//操作人
	
	

}
