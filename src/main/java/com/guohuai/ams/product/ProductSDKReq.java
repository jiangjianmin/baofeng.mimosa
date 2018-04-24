package com.guohuai.ams.product;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** 产品信息查询请求 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductSDKReq extends MimosaSDKReq {

	private static final long serialVersionUID = -3673063314257511819L;

	/** 产品oid */
	private String oid;
	
	/** 产品类型(0:活期产品，1:定期产品，空:所有产品) */
	private String type;

	/**
	 * 产品标签(参考productLabel的定义)
	 * 
	 * @see Product
	 */
	private String label;
}
