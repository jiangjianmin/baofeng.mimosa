package com.guohuai.ams.product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * mimosa sdk 产品列表查询响应参数
 * @author huyong
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductRep implements Serializable{
	
	private static final long serialVersionUID = -7828226693401721930L;

	/** 
	 * 产品oid 
	 */
	private String oid = "";
	
	/**
	 *  产品类型
	 */
	private String type;
	
	/** 
	 * 产品名称 
	 */
	private String name;
	
	/**
	 * 预期年化收益率
	 */
	private BigDecimal expAror = new BigDecimal(0);
	
	/**
	 * 创建时间
	 */
	private Timestamp createTime;
	
	/**
	 * 募集总份额
	 */
	private BigDecimal raisedTotalNumber = new BigDecimal(0);
	
	/** 
	 * 基础标签ID
	 */
	private String basicProductLabelOid;
	
	/** 
	 * 基础标签名称
	 */
	private String basicProductLabelName;
	
	/** 
	 * 扩展标签
	 */
	private Map<String,String> expandProductLabels;
}
