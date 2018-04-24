package com.guohuai.mmp.investor.bank;

import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.guohuai.basic.common.StringUtil;
import com.guohuai.basic.component.exception.GHException;
import com.guohuai.bfsms.BfSMSTypeEnum;
import com.guohuai.bfsms.BfSMSUtils;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.baseaccount.refer.details.InvestoRefErDetailsEntity;
import com.guohuai.mmp.investor.baseaccount.refer.details.InvestoRefErDetailsService;
import com.guohuai.settlement.api.SettlementSdk;
import com.guohuai.settlement.api.request.AuthenticationRequest;
import com.guohuai.settlement.api.response.AuthenticationResponse;

@Service
@Transactional
public class BankService {

	private Logger logger = LoggerFactory.getLogger(BankService.class);
	
	@Autowired
	private BankDao bankDao;
	@Autowired
	private InvestorBaseAccountService baseAccountService;
	@Autowired
	private SettlementSdk settlementSdk;

	private BfSMSUtils bfSMSUtils;
	@Autowired
	private BankSyncSettleService bankSyncSettleService;
//	@Autowired
//	private BankHisService bankHisService;
	@Autowired
	private InvestoRefErDetailsService investoRefErDetailsService;
	
	/**
	 * 新增
	 * @param entity
	 * @return
	 */
	public BankEntity saveEntity(BankEntity entity){
		entity.setCreateTime(DateUtil.getSqlCurrentDate());
		return this.updateEntity(entity);
	}
	
	/**
	 * 修改
	 * @param entity
	 * @return
	 */
	public BankEntity updateEntity(BankEntity entity) {
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		return this.bankDao.save(entity);
	}
	

	public BankEntity findByBaseAccount(InvestorBaseAccountEntity baseAccount) {
		return this.bankDao.findByInvestorBaseAccount(baseAccount);
	}
	
	public BankEntity findByIdCard(String idNumb) {
		return this.bankDao.findByIdCard(idNumb);
	}
	
	public BankEntity findByDebitCard(String debitCard) {
		return this.bankDao.findByDebitCard(debitCard);
	}
	
	/**
	 * 身份证/银行卡号唯一判断（新）
	 * @param req
	 * @return
	 */
	public InvestorBaseAccountEntity isBind(BankValid4EleReq req) {
		InvestorBaseAccountEntity account = this.baseAccountService.findOne(req.getInvestorOid());
		BankEntity bank = this.findByBaseAccount(account);
		
		if (null != bank) {
			if (!bank.getName().contains(req.getName().replace("*", "")) ||
					!bank.getIdCard().contains(req.getIdCardNo().replace("*", ""))){
				throw new AMPException("您提交的姓名或身份证号和之前提交的不一致！");
			}			
		} else {
			if (null != this.findByIdCard(req.getIdCardNo())) {
				throw new AMPException("此身份证已被绑定！");
			}
		}
		
		if (null != this.findByDebitCard(req.getCardNo())) {
			throw new AMPException("此银行卡号已被绑定！");
		}
		req.setMemberOid(account.getMemberId());
		return account;
	}
	
	/**
	 * 绑卡
	 * @param req
	 * @return
	 */
	@Transactional
	public void bindBank(BankValid4EleReq req) {
		this.bfSMSUtils.checkVeriCode(req.getPhone(), BfSMSTypeEnum.smstypeEnum.bindbank.toString(), req.getVericode());
		// 身份证/银行卡号唯一判断
		InvestorBaseAccountEntity account = this.isBind(req);
		try {
			// 四要素验证
			this.bankSyncSettleService.valid4Ele(req, account);
		} catch (Exception e) {
			Throwable cause = e.getCause();
		    if(cause instanceof ConstraintViolationException) {
		        String errMsg = ((ConstraintViolationException) cause).getSQLException().getMessage();
		        if(!StringUtil.isEmpty(errMsg) && errMsg.indexOf("investorOid") != -1) {
					throw new AMPException("您已经绑卡成功，请查看绑卡信息！");
		        }
		    }
			
			if (e instanceof AMPException) {
				throw new AMPException(((AMPException) e).getMessage());
			}
			logger.error("用户：{}，绑卡失败，原因：{}", req.getInvestorOid(), e.getMessage());
			throw new AMPException("绑卡失败！");
		}
	}
	
	/**
	 * 业务系统用户绑卡
	 * @param req
	 * @param account
	 * @return
	 */
	public void bindBank(BankValid4EleReq req, InvestorBaseAccountEntity account) {
		
//		BankEntity bank = this.findByBaseAccount(account);
//		if (null == bank) {
//			bank = new BankEntity();
//		}
//		bank.setInvestorBaseAccount(account);
//		bank.setName(req.getName());
//		bank.setIdCard(req.getIdCardNo());
//		bank.setBankName(req.getBankName());
//		bank.setDebitCard(req.getCardNo());
//		bank.setPhoneNo(req.getPhone());
//		this.saveEntity(bank);
//		
//		// 获取解绑记录，如果有解绑记录，则不再给推荐人送体验金
//		List<BankHisEntity> bankHis = this.bankHisService.findByInvestorOid(account.getOid());
//		
//		if (null == bankHis || bankHis.size() <= 0) {
//			InvestoRefErDetailsEntity refErdetails = this.investoRefErDetailsService.getRefErDetailsByInvestorOid(account.getOid());
//			if (null != refErdetails) {
//				// 绑卡给推荐人送体验金
//				String recommenderOid = refErdetails.getInvestorRefEree().getInvestorBaseAccount().getUserOid();
//				BaseRep rep = this.baseAccountService.addBank(account.getUserOid(), recommenderOid);
//				if (0 != rep.getErrorCode()) {
//					logger.error("用户：{}绑卡，给推荐人：{}送体验金失败，原因：{}", account.getOid(), recommenderOid, rep.getErrorMessage() + "(" + rep.getErrorCode() + ")");
//					throw new AMPException(rep.getErrorMessage() + "(" + rep.getErrorCode() + ")");
//				}
//			}
//		}
		
		// 更新用户实名认证信息
		this.baseAccountService.updateAccountRealName(account, req.getName(), req.getIdCardNo());
		logger.info("用户：{}同步实名认证信息到业务mimosa系统成功。", account.getOid());
	}
	
	/**
	 * 同步结算系统解绑银行卡
	 * @param investorOid
	 * @param operator
	 * @return
	 */
	@Transactional
	public void syncRemoveSettleBank(String investorOid, String operator) {
		
		InvestorBaseAccountEntity account = this.baseAccountService.findOne(investorOid);
		
		BankEntity bank = this.findByBaseAccount(account);
		
		if (null == bank) {
			throw new GHException("用户未绑定银行卡！");
		}
		// 结算解绑卡
		this.bankSyncSettleService.syncRemoveSettleBank(account, bank, operator);
		
	}
	
	/**
	 * 解绑银行卡
	 * @param bank
	 * @param operator
	 * @return
	 */
	public void removeBank(BankEntity bank, String operator) {
		try {
//			BankHisEntity bankHis = new BankHisEntity();
//			bankHis.setInvestorOid(bank.getInvestorBaseAccount().getOid());
//			bankHis.setName(bank.getName());
//			bankHis.setIdNumb(bank.getIdCard());
//			bankHis.setBankName(bank.getBankName());
//			bankHis.setCardNumb(bank.getDebitCard());
//			bankHis.setPhoneNo(bank.getPhoneNo());
//			bankHis.setOperator(operator);
//			this.bankHisService.saveEntity(bankHis);
			
			bank.setBankName(null);
			bank.setDebitCard(null);
			bank.setPhoneNo(null);
			this.updateEntity(bank);
		} catch (Exception e) {
			logger.error("在业务系统解绑失败(" + e.getMessage() + ")");
			throw new AMPException("在业务系统解绑失败(" + e.getMessage() + ")");
		}
	}
	
	/**
	 * 用户银行卡信息
	 * @param investorOid
	 * @return
	 */
	public BankInfoRep getBankInfo(String investorOid) {
		BankInfoRep rep = new BankInfoRep();
		InvestorBaseAccountEntity account = this.baseAccountService.findOne(investorOid);
		
		BankEntity  bank = this.findByBaseAccount(account);
		
		if (null != bank) {
			rep.setName(bank.getName());
			rep.setIdNumb(bank.getIdCard());
			rep.setBankName(bank.getBankName());
			rep.setCardNumb(bank.getDebitCard());
			rep.setPhoneNo(bank.getPhoneNo());
			rep.setCreateTime(DateUtil.formatFullPattern(bank.getCreateTime()));
			rep.setIsbind(StringUtil.isEmpty(bank.getDebitCard()) ? false : true);
		} else {
			rep.setIsbind(false);
		}
		return rep;
	}
	
	/**
	 * 申请代扣协议
	 * @param req
	 * @param userOid
	 * @return
	 */
	public BankAgreeRep validAgreement(BankAgreeReq req, String userOid) {
		BankAgreeRep rep = new BankAgreeRep();
		
		InvestorBaseAccountEntity account = this.baseAccountService.findByUid(userOid);
		
		AuthenticationRequest authRequest = new AuthenticationRequest();
		authRequest.setUserOid(account.getMemberId());
		authRequest.setRealName(req.getRealName());
		authRequest.setCertificateNo(req.getIdCardNo());
		authRequest.setBankCode(req.getBankName());
		authRequest.setCardNo(req.getCardNo());
		authRequest.setPhone(req.getPhone());
		String requestNo = StringUtil.uuid();
		logger.info("用户：{}申请代扣协议 requestNo：{}", userOid, requestNo);
		authRequest.setReuqestNo(requestNo);
		
		AuthenticationResponse authResponse;
		try {
			logger.info("用户：{}开始申请代扣协议。", userOid);
			logger.info(JSON.toJSONString(authRequest));
			authResponse = settlementSdk.applyAgreement(authRequest);
			
		} catch (Exception e) {
			logger.error("用户：{}申请代扣协议失败。原因：{}", userOid, AMPException.getStacktrace(e));
			rep.setErrorCode(BaseRep.ERROR_CODE);
			rep.setErrorMessage(AMPException.getStacktrace(e));
			return rep;
		}
		
		if (null == authResponse) {
			logger.error("用户：{}申请代扣协议失败，返回值为空。", userOid);
			rep.setErrorCode(BaseRep.ERROR_CODE);
			rep.setErrorMessage("返回为空");
			return rep;
		}
		
		if (BankParam.ReturnCode.RC0000.toString().equals(authResponse.getReturnCode())) {
			rep.setReuqestNo(authResponse.getReuqestNo());
			rep.setOrderNo(authResponse.getOrderNo());
			logger.info("用户：{}申请代扣协议成功。", userOid);
		} else {
			int errorCode;
			if ("9901".equals(authResponse.getReturnCode())) {
				errorCode = 9901;
			} else if ("9902".equals(authResponse.getReturnCode())) {
				errorCode = 9902;
			} else if ("9903".equals(authResponse.getReturnCode())) {
				errorCode = 9903;
			} else if ("9904".equals(authResponse.getReturnCode())) {
				errorCode = 9904;
			} else if ("9905".equals(authResponse.getReturnCode())) {
				errorCode = 9905;
			} else if ("9906".equals(authResponse.getReturnCode())) {
				errorCode = 9906;
			} else {
				errorCode = BaseRep.ERROR_CODE;
			}
			logger.error("用户：{}申请代扣协议失败。原因：{}", userOid, authResponse.getErrorMessage());
			rep.setErrorCode(errorCode);
			rep.setErrorMessage(authResponse.getErrorMessage());
			rep.setOrderNo(authResponse.getOrderNo());
		}
	
		return rep;
	}

	/**
	 * 确认签约代扣协议
	 * @param req
	 * @param userOid
	 * @return
	 */
	public BaseRep confirmAgreement(BankAgreeReq req, String userOid) {
		BaseRep baseRep = new BaseRep();
		
		InvestorBaseAccountEntity account = this.baseAccountService.findByUid(userOid);
		
		AuthenticationRequest authRequest = new AuthenticationRequest();
		authRequest.setUserOid(account.getMemberId());
		String requestNo = StringUtil.uuid();
		logger.info("用户：{}确认签约代扣协议 requestNo：{}", userOid, requestNo);
		authRequest.setReuqestNo(requestNo);
		authRequest.setSmsReq(req.getSmscode());
		authRequest.setOrderNo(req.getOrderNo());
		
		AuthenticationResponse authResponse;
		
		try {
			logger.info("用户：{}开始确认签约代扣协议。", userOid);
			logger.info(JSON.toJSONString(authRequest));
			authResponse = settlementSdk.confirmAgreement(authRequest);
			
		} catch (Exception e) {
			logger.error("用户：{}确认签约代扣协议失败。原因：{}", userOid, AMPException.getStacktrace(e));
			baseRep.setErrorCode(BaseRep.ERROR_CODE);
			baseRep.setErrorMessage(AMPException.getStacktrace(e));
			return baseRep;
		}
		
		if (null == authResponse) {
			logger.error("用户：{}确认签约代扣协议失败，返回为空。", userOid);
			baseRep.setErrorCode(BaseRep.ERROR_CODE);
			baseRep.setErrorMessage("返回为空");
			return baseRep;
		}
		
		if (BankParam.ReturnCode.RC0000.toString().equals(authResponse.getReturnCode())) {
			logger.info("用户：{}确认签约代扣协议成功。", userOid);
		} else {
			int errorCode;
			if ("9901".equals(authResponse.getReturnCode())) {
				errorCode = 9901;
			} else if ("9902".equals(authResponse.getReturnCode())) {
				errorCode = 9902;
			} else if ("9903".equals(authResponse.getReturnCode())) {
				errorCode = 9903;
			} else if ("9904".equals(authResponse.getReturnCode())) {
				errorCode = 9904;
			} else if ("9905".equals(authResponse.getReturnCode())) {
				errorCode = 9905;
			} else if ("9906".equals(authResponse.getReturnCode())) {
				errorCode = 9906;
			} else {
				errorCode = BaseRep.ERROR_CODE;
			}
			logger.error("用户：{}确认签约代扣协议失败。原因：{}", userOid, authResponse.getErrorMessage());
			baseRep.setErrorCode(errorCode);
			baseRep.setErrorMessage(authResponse.getErrorMessage());
		}
	
		return baseRep;
	}
}
