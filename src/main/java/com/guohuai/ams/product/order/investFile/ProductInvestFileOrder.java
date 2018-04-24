package com.guohuai.ams.product.order.investFile;

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
@Table(name = "T_GAM_PRODUCT_INVEST_FILE_ORDER")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class ProductInvestFileOrder implements Serializable {

	private static final long serialVersionUID = -5362025759457845731L;

	@Id
	private String oid;
	@ManyToOne
	@JoinColumn(name = "productOid")
	private Product product;
	private String fkey;
	private String creator;
	private Timestamp createTime;
	private String auditor;
	private Timestamp auditTime;
	private String status;

}
