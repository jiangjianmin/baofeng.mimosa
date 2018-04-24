package com.guohuai.mmp.investor.baseaccount.user;

import com.guohuai.component.web.view.BaseRep;

import feign.Param;
import feign.RequestLine;

public interface UserApi {
	
	@RequestLine("POST /wfduc/boot/investor/baseaccount/setlock?investorOid={investorOid}&isLock={isLock}")
	public BaseRep setLock(@Param("investorOid") String investorOid, @Param("isLock") String isLock);
}
