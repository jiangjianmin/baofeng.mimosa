package com.guohuai.ams.product;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_GAM_PRODUCT_DETAIL")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class ProductTypeDetail extends UUID implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String type;
	private String title;
	private String detail;
	private String url;
	private String operator;
	private Timestamp createTime;
	private Timestamp updateTime;
}
