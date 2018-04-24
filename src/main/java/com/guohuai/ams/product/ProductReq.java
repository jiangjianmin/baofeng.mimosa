package com.guohuai.ams.product;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * mimosa sdk 产品列表查询请求参数
 * @author huyong
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductReq extends MimosaSDKReq {

	private static final long serialVersionUID = -8101912801415702827L;
	/**
	 *  产品类型
	 */
	private String type;
	
	/** 产品oid串 */
	private List<String> oids;
}
