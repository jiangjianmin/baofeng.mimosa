package com.guohuai.ams.product;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** 产品信息查询返回 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductSDKRep implements Serializable {

	private static final long serialVersionUID = 3901229909864126344L;

	/** 产品oid */
	private String oid;

	/** 产品名称 */
	private String name;

	/** 产品代码 */
	private String code;

}
