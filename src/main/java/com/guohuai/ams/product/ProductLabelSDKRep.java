package com.guohuai.ams.product;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** 产品标签 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductLabelSDKRep implements Serializable {

	private static final long serialVersionUID = 3901229909864126344L;

	/** 标签代码 */
	private String code;

	/** 产品标签名称 */
	private String name;

}
