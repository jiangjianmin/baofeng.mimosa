package com.guohuai.ams.productLabel;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.label.LabelEntity;
import com.guohuai.ams.product.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 平台-标签-产品
 * 
 * @author wangyan
 *
 */
@Entity
@Table(name = "T_MONEY_PLATFORM_LABEL_PRODUCT")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class ProductLabel implements Serializable {

	private static final long serialVersionUID = -2168908781826361447L;
	
	@Id
	private String oid;
	@ManyToOne
	@JoinColumn(name = "labelOid")
	private LabelEntity label;//所属标签
	@ManyToOne
	@JoinColumn(name = "productOid")
	private Product product;// 所属产品
	private Timestamp updateTime;
	private Timestamp createTime;

}
