package com.guohuai.cardvo.req;

import java.io.Serializable;

import lombok.Data;

/**
 * @desc mimosa sdk 产品列表查询响应参数
 * @author huyong
 * @date 2017.5.11
 */
@Data
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
	private String expAror;
	
	/**
	 * 创建时间
	 */
	private String createTime;
	
	/**
	 * 募集总份额
	 */
	private String raisedTotalNumber ;
	
	/** 
	 * 基础标签名称
	 */
	private String basicProductLabelName;
	
	/** 
	 * 扩展标签
	 */
	private String expandProductLabelName;
}
