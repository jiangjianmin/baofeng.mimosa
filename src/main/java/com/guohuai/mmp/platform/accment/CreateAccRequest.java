package com.guohuai.mmp.platform.accment;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class CreateAccRequest {
	/**
	 *  会员ID	userOid	
		用户类型	userType	投资人账户:T1、发行人账户:T2、平台账户:T3
		账户类型	accountType	01为活期，02为活期利息，03为体验金，04为在途，05为冻结户（冻结户，非冻结状态），后期根据需求扩展
		关联产品	relationProduct	该账户对应的理财产品，值取投资人账户的值，目前仅开通活期产品，该字段为未来开通定期等产品预留
	 */
	
	/**
	 * 会员ID
	 */
	private String userOid;
	
	/**
	 * 用户类型
	 */
	private String userType;
	
	/**
	 * 账户类型
	 */
	private String accountType;
	
	/**
	 * 关联产品
	 */
	private String relationProduct;
	
}
