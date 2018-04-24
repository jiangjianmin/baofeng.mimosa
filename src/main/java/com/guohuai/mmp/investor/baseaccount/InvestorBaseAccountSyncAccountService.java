package com.guohuai.mmp.investor.baseaccount;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.account.api.AccountSdk;
import com.guohuai.account.api.request.CreateUserRequest;
import com.guohuai.account.api.response.CreateUserResponse;
import com.guohuai.component.exception.AMPException;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class InvestorBaseAccountSyncAccountService {

	@Autowired
	private AccountSdk accountSdk;
	
	/**
	 * 同步账务系统
	 * @param req
	 * @return
	 */
	public void syncAccount(InvestorBaseAccountAddReq req) {				
		// 同步账务系统账号
		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setSystemUid(req.getInvestorOid());
		createUserRequest.setSystemSource(InvestorBaseAccountEntity.BASEACCOUNT_systemSource_mimosa);
		createUserRequest.setPhone(req.getUserAcc());
		createUserRequest.setUserType(InvestorBaseAccountEntity.BASEACCOUNT_userType_T1);
		
		// 在账务系统创建账号
		CreateUserResponse createUserResponse = null;
		
		try {
			log.info("用户：{}注册，开始同步到账务系统。", req.getUserAcc());
			log.info(JSONObject.toJSONString(createUserRequest));
			createUserResponse = accountSdk.addUser(createUserRequest);
			
		} catch (Exception e) {
			log.error("用户：{}注册时，同步到账务系统失败，原因：{}", req.getUserAcc(), e.getMessage());
			if (e instanceof FeignException) {
				throw new AMPException("注册失败！");
			}
			throw new AMPException(AMPException.getStacktrace(e));
		}
		
		if (null == createUserResponse) {
			log.error("用户：{}同步到账务系统返回为空!", req.getUserAcc());
			throw new AMPException("账务系统返回为空!");
		}
		
		if ("0000".equals(createUserResponse.getReturnCode())) {
			// 获取会员OID
			req.setMemberOid(createUserResponse.getUserOid());
			log.info("用户：{}注册，成功同步到账务系统。", req.getUserAcc());
		} else {
			log.error("用户：{}同步到账务系统失败，原因：{}", req.getUserAcc(), createUserResponse.getErrorMessage() + "(" + createUserResponse.getReturnCode() + ")");
			throw new AMPException(createUserResponse.getErrorMessage() + "(" + createUserResponse.getReturnCode() + ")");
		}
	}
}
