package com.guohuai.cardvo.req;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * @desc mimosa sdk 产品列表查询请求参数
 * @author huyong
 * @date 2017.5.11
 */
@Data
public class ProductReq implements Serializable{

	private static final long serialVersionUID = 6877611015363086311L;

	/**
	 *  产品类型
	 */
	private String type;
	
	/** 产品oid */
	private List<String> oids;
	
	/** 产品状态   */
	private List<String> productStatusList;
	
	/**
	 * 默认页数
	 */
	private int 		page = 1;
	
	/**
	 * 默认行数
	 */
	private int 		rows = 10;
}
