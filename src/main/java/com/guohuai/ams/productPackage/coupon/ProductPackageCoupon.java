package com.guohuai.ams.productPackage.coupon;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.basic.component.ext.hibernate.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "t_gam_product_package_cardvolume")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class ProductPackageCoupon extends UUID{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5689796886544633362L;

	private String productOid;//产品oid
	
	private Integer cardOid;//卡券大类oid
	
	private Integer type;//1、红包 2、加息劵
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;//创建时间
	
}
