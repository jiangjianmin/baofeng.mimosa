package com.guohuai.mmp.investor.bank;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guohuai.basic.common.StringUtil;
import com.guohuai.basic.component.exception.GHException;
import com.guohuai.component.exception.AMPException;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.settlement.api.SettlementSdk;
import com.guohuai.settlement.api.request.ElementValidationRequest;
import com.guohuai.settlement.api.response.ElementValidaResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class BankSyncSettleService {

	@Autowired
	private SettlementSdk settlementSdk;
	@Autowired
	private BankService bankService;
	
	/**
	 * 绑卡的时候验证4要素
	 * @param req
	 * @param account
	 * @return
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public void valid4Ele(BankValid4EleReq req, InvestorBaseAccountEntity account) {
			
		ElementValidationRequest valid4EleRequest = new ElementValidationRequest();
		
		valid4EleRequest.setRealName(req.getName());
		valid4EleRequest.setCertificateNo(req.getIdCardNo());
		valid4EleRequest.setBankCode(req.getBankName());
		valid4EleRequest.setCardNo(req.getCardNo());
		valid4EleRequest.setPhone(req.getPhone());
		valid4EleRequest.setRequestNo(StringUtil.uuid());
		valid4EleRequest.setSystemSource(BankParam.SystemSource.MIMOSA.toString());
		valid4EleRequest.setUserOid(req.getMemberOid());
		
		ElementValidaResponse eleValidResponse;
		try {
			log.info("用户：{}开始验证四要素。", req.getInvestorOid());
			log.info(JSONObject.toJSONString(valid4EleRequest));
			eleValidResponse = settlementSdk.elementValid(valid4EleRequest);
			
		} catch (Exception e) {
			log.error("用户：{}验证四要素失败。原因：{}", req.getInvestorOid(), AMPException.getStacktrace(e));
			throw new AMPException(AMPException.getStacktrace(e));
		}
		
		if (null == eleValidResponse) {
			log.error("用户：{}验证四要素失败，返回为空。", req.getInvestorOid());
			throw new AMPException("结算绑卡，返回为空！");
		}
		
		if (BankParam.ReturnCode.RC0000.toString().equals(eleValidResponse.getReturnCode())) {
			log.info("用户：{}验证四要素成功。", req.getInvestorOid());
			// 业务系统绑卡
			this.bankService.bindBank(req, account);
		} else {
			log.error("用户：{}验证四要素失败。原因：{}", req.getInvestorOid(), eleValidResponse.getErrorMessage() + "(" + eleValidResponse.getReturnCode() + ")");
			throw new AMPException(eleValidResponse.getErrorMessage() + "(" + eleValidResponse.getReturnCode() + ")");
		}
	}
	
	/**
	 * 同步结算系统解绑银行卡
	 * @param account
	 * @param bank
	 * @param operator
	 * @return
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public void syncRemoveSettleBank(InvestorBaseAccountEntity account, BankEntity bank, String operator) {
		
		ElementValidationRequest req = new ElementValidationRequest();
		req.setUserOid(account.getMemberId());
		req.setCardNo(bank.getDebitCard());
		
		ElementValidaResponse elementValidaResponse = null;
		
		try {
			log.info("用户：{}开始在结算系统解绑银行卡，卡号：{}。", account.getOid(), req.getCardNo());
			log.info(JSON.toJSONString(req));
			elementValidaResponse = this.settlementSdk.unlock(req);
		} catch (Exception e) {
			log.error("用户：{}在结算系统解绑银行卡失败。原因：{}", account.getOid(), GHException.getStacktrace(e));
			throw new AMPException(GHException.getStacktrace(e));
		}
		
		if (null == elementValidaResponse) {
			log.error("用户：{}在结算系统解绑银行卡失败，返回为空。", account.getOid());
			throw new AMPException("解绑银行卡返回为空！");
		}
		
		if ("0000".equals(elementValidaResponse.getReturnCode())) {
			log.info("用户：{}在结算系统解绑银行卡成功。", account.getOid());
			this.bankService.removeBank(bank, operator);
		} else {
			log.error("用户：{}在结算系统解绑银行卡失败。原因：{}", account.getOid(), elementValidaResponse.getErrorMessage() + "(" + elementValidaResponse.getReturnCode() + ")");
			throw new AMPException(elementValidaResponse.getErrorMessage() + "(" + elementValidaResponse.getReturnCode() + ")");
		}
	}
	
}
