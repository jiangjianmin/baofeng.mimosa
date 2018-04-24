package com.guohuai.mmp.publisher.product.client;

import com.guohuai.component.web.view.BaseResp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author wanglei 体验金可购买的产品
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ExperienceProductResp extends BaseResp {

	/** 产品oid */
	private String oid;

}
