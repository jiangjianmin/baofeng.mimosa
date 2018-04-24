package com.guohuai.mmp.platform.accment;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.account.api.AccountSdk;
import com.guohuai.account.api.request.AccountQueryRequest;
import com.guohuai.account.api.request.AccountTransRequest;
import com.guohuai.account.api.request.CreateAccountRequest;
import com.guohuai.account.api.request.CreateUserRequest;
import com.guohuai.account.api.request.EnterAccountRequest;
import com.guohuai.account.api.request.TransPublishRequest;
import com.guohuai.account.api.request.TransferAccountRequest;
import com.guohuai.account.api.request.UserQueryRequest;
import com.guohuai.account.api.response.AccountListResponse;
import com.guohuai.account.api.response.AccountTransResponse;
import com.guohuai.account.api.response.BaseResponse;
import com.guohuai.account.api.response.CreateAccountResponse;
import com.guohuai.account.api.response.CreateUserResponse;
import com.guohuai.account.api.response.EnterAccountResponse;
import com.guohuai.account.api.response.TransferAccountResponse;
import com.guohuai.account.api.response.UserListResponse;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.productPackage.ProductPackage;
import com.guohuai.basic.common.StringUtil;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.platform.accment.log.AccLogReq;
import com.guohuai.mmp.platform.accment.log.AccLogService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccmentService {
	@Autowired
	private AccountSdk accountSdk;
	@Autowired
	private AccLogService accLogService;
	
	
	/**
	 * 新增发行人用户
	 */
	public String addUser(String systemUid) {
		
		CreateUserRequest oreq = new CreateUserRequest();
		oreq.setSystemUid(systemUid);
		oreq.setRemark("创建发行人");
		oreq.setUserType(AccParam.UserType.SPV.toString());
		oreq.setSystemSource(AccParam.SystemSource.MIMOSA.toString());
		
//		CreateUserResponse orep = this.accountSdk.addUser(oreq);
//		Assert.notNull(orep, "账户系统返回为空");
//		
//		if (StringUtil.isEmpty(orep.getUserOid())) {
//			throw new IllegalArgumentException("账户系统返回用户ID为空");
//		}
//		BaseRep irep = new BaseRep();
//		irep.setErrorMessage(JSONObject.toJSONString(orep));
//		this.writeLog(irep, oreq, AccInterface.addUser.getInterfaceName());
//		return orep.getUserOid();                                  
		return "test";
	}
	
	/**
	 * 创建产品时
	 * 创建发行人产品户
	 */
	public String createSPVT0Account(Product product) {
		CreateAccRequest ireq = new CreateAccRequest();
		ireq.setUserOid(product.getPublisherBaseAccount().getMemberId());
		ireq.setUserType(AccParam.UserType.SPV.toString());
		ireq.setAccountType(AccParam.AccountType.HQ.toString());
		ireq.setRelationProduct(product.getOid());
		return this.createAccount(ireq);
	}
	
	public String createSPVT0LXAccount(Product product) {
		CreateAccRequest ireq = new CreateAccRequest();
		ireq.setUserOid(product.getPublisherBaseAccount().getMemberId());
		ireq.setUserType(AccParam.UserType.SPV.toString());
		ireq.setAccountType(AccParam.AccountType.HQLX.toString());
		ireq.setRelationProduct(product.getOid());
		return this.createAccount(ireq);
	}
	
	public String createSPVTnAccount(Product product) {
		CreateAccRequest ireq = new CreateAccRequest();
		ireq.setUserOid(product.getPublisherBaseAccount().getMemberId());
		ireq.setUserType(AccParam.UserType.SPV.toString());
		ireq.setAccountType(AccParam.AccountType.DQ.toString());
		ireq.setRelationProduct(product.getOid());
		return this.createAccount(ireq);
	}
	
	public String createSPVTnAccountProductPackage(ProductPackage productPackage) {
		CreateAccRequest ireq = new CreateAccRequest();
		ireq.setUserOid(productPackage.getPublisherBaseAccount().getMemberId());
		ireq.setUserType(AccParam.UserType.SPV.toString());
		ireq.setAccountType(AccParam.AccountType.DQ.toString());
		ireq.setRelationProduct(productPackage.getOid());
		return this.createAccount(ireq);
	}
	
	public String createSPVTnLXAccount(Product product) {
		CreateAccRequest ireq = new CreateAccRequest();
		ireq.setUserOid(product.getPublisherBaseAccount().getMemberId());
		ireq.setUserType(AccParam.UserType.SPV.toString());
		ireq.setAccountType(AccParam.AccountType.DQLX.toString());
		ireq.setRelationProduct(product.getOid());
		return this.createAccount(ireq);
	}
	
	/**
	 * 创建子账户	createAccount
	 */
	
	public String createAccount(CreateAccRequest ireq) {
		CreateAccountRequest oreq = new CreateAccountRequest();
		oreq.setUserOid(ireq.getUserOid());
		oreq.setUserType(ireq.getUserType());
		oreq.setAccountType(ireq.getAccountType());
		oreq.setRelationProduct(ireq.getRelationProduct());
		
//		CreateAccountResponse orep  = accountSdk.createAccount(oreq);
//		Assert.notNull(orep, "账户系统返回为空");
//		if (StringUtil.isEmpty(orep.getAccountNo())) {
//			throw new IllegalArgumentException("账户系统返回用户ID为空");
//		}
//		
//		BaseRep irep = new BaseRep();
//		irep.setErrorMessage(JSONObject.toJSONString(orep));
//		this.writeLog(irep, oreq, AccInterface.createAccount.getInterfaceName());
//		return orep.getAccountNo();
		return "test";
	}
	
	/**
	 * 会员账户交易	trade
	 */
	public BaseRep trade(TradeRequest ireq) {
		return this.trade(ireq, true);
	}
	public BaseRep trade(TradeRequest ireq, boolean isLog) {
		BaseRep irep = new BaseRep();
		AccountTransRequest oreq  = new AccountTransRequest();
		oreq.setUserOid(ireq.getUserOid());
		oreq.setUserType(ireq.getUserType());
		oreq.setOrderType(ireq.getOrderType());
		oreq.setRelationProductNo(ireq.getRelationProductNo());
		oreq.setProductType(ireq.getProductType());
		oreq.setBalance(ireq.getBalance());
		oreq.setRemark(ireq.getRemark());
		oreq.setOrderNo(ireq.getOrderNo());
		oreq.setSystemSource(AccParam.SystemSource.MIMOSA.toString());
		oreq.setRequestNo(StringUtil.uuid());
		
		AccountTransResponse orep = new AccountTransResponse();
		try {
//			orep = accountSdk.trade(oreq);
		} catch (Exception e) {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(AMPException.getStacktrace(e));
		}
		
		setIrep(irep, orep);
		if (isLog) {
			writeLog(irep, ireq, AccInterface.trade.getInterfaceName());
		}
		return irep;
	}

	private void setIrep(BaseRep irep, BaseResponse orep) {
		/** 调用接口异常 */
		if (BaseRep.ERROR_CODE == irep.getErrorCode()) {
			return;
		}

		/** orep == null, 当接口返回为NULL时 */
		if (null == orep) {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage("返回为空");
			return;
		}

		if (AccParam.ReturnCode.RC0000.toString().equals(orep.getReturnCode())) {
			irep.setErrorMessage(orep.getErrorMessage());
		} else {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(orep.getErrorMessage() + "(" + orep.getReturnCode() + ")");
		}
	}
	
	public <T> void writeLog(T sendObj) {
		BaseRep irep = new BaseRep();
		irep.setErrorCode(BaseRep.ERROR_CODE);
		this.writeLog(irep, sendObj, AccInterface.trade.getInterfaceName());
	}
	
	private <T> void writeLog(BaseRep irep, T sendObj, String interfaceName) {
		
		AccLogReq accLogReq =  new AccLogReq();
		accLogReq.setInterfaceName(interfaceName);
		accLogReq.setSendedTimes(1);
		accLogReq.setSendObj(JSONObject.toJSONString(sendObj));
		accLogReq.setErrorCode(irep.getErrorCode());
		accLogReq.setErrorMessage(irep.getErrorMessage());
		accLogService.createEntity(accLogReq);
	}
	
	/**
	 * 平台转账	transferAccount
	 */
	public BaseRep transferAccount(TransferAccRequest ireq) {
		return this.transferAccount(ireq, true);
	}
	
	public BaseRep transferAccount(TransferAccRequest ireq, boolean isLog) {
		BaseRep irep = new BaseRep();
		TransferAccountRequest oreq = new TransferAccountRequest();
		oreq.setInputAccountNo(ireq.getInputAccountNo());
		oreq.setOutpuptAccountNo(ireq.getOutpuptAccountNo());
		oreq.setBalance(ireq.getBalance());
		oreq.setOrderType(ireq.getOrderType());
		oreq.setRemark(ireq.getRemark());
		oreq.setOrderNo(ireq.getOrderNo());
		oreq.setRequestNo(StringUtil.uuid());
		
		TransferAccountResponse orep = new TransferAccountResponse();
		try {
//			orep = accountSdk.transferAccount(oreq);
		} catch (Exception e) {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(AMPException.getStacktrace(e));
		}
		setIrep(irep, orep);
		if (isLog) {
			writeLog(irep, ireq, AccInterface.transferAccount.getInterfaceName());
		}
		
		
		return irep;
	}
	
	
	/**
	 * 平台、发行人账户调账	enterAccount
	 */
	public BaseRep enterAccout(EnterAccRequest ireq) {
		return this.enterAccout(ireq, true);
	}
	
	public BaseRep enterAccout(EnterAccRequest ireq, boolean isLog) {
		BaseRep irep = new BaseRep();
		
		EnterAccountRequest oreq = new EnterAccountRequest();
		oreq.setInputAccountNo(ireq.getInputAccountNo());
		oreq.setBalance(ireq.getBalance());
		oreq.setOrderType(ireq.getOrderType());
		oreq.setRemark(ireq.getRemark());
		oreq.setOrderNo(ireq.getOrderNo());
		oreq.setRequestNo(StringUtil.uuid());
		
		EnterAccountResponse orep = new EnterAccountResponse();
		
		try {
//			orep = accountSdk.enterAccount(oreq);
		} catch (Exception e) {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(AMPException.getStacktrace(e));
		}
		setIrep(irep, orep);
		if (isLog) {
			writeLog(irep, ireq, AccInterface.enterAccout.getInterfaceName());
		}
		
		return irep;
	}
	
	/**
	 * 查询平台用户
	 */
	public UserQueryIRep queryPlatformUser() {
		
		UserQueryIRequest ireq = new UserQueryIRequest();
		ireq.setUserType(AccParam.UserType.PLATFORM.toString());
		
		UserQueryIRep irep = new UserQueryIRep();
		
		UserQueryRequest oreq = new UserQueryRequest();
		oreq.setUserType(ireq.getUserType());
		oreq.setSystemSource(AccParam.SystemSource.MIMOSA.toString());
		
		UserListResponse orep = new UserListResponse();
		try {
//			orep = this.accountSdk.userQueryList(oreq);

			/** orep == null, 当接口返回为NULL时  */
			if (null == orep) {
				irep.setErrorCode(BaseRep.ERROR_CODE);
				irep.setErrorMessage("返回为空");
			}
			
			if (0 == orep.getErrorCode()) {
				irep.setErrorMessage(orep.getErrorMessage());
				if (null == orep.getRows() || orep.getRows().size() != 1 || StringUtil.isEmpty(orep.getRows().get(0).getUserOid())) {
					irep.setErrorCode(BaseRep.ERROR_CODE);
					irep.setErrorMessage("rows is null || rows.size() != 1 || rows[0].userOid is null");
				}
				irep.setUserOid(orep.getRows().get(0).getUserOid());
			} else {
				irep.setErrorCode(BaseRep.ERROR_CODE);
				irep.setErrorMessage(orep.getErrorCode() + "--" + orep.getErrorMessage());
			}
		} catch (Exception e) {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(AMPException.getStacktrace(e));
		}
		this.writeLog(irep, oreq, AccInterface.userQueryList.getInterfaceName());
		return irep;
	}
	
	/**
	 * 查询平台用户下相关账户
	 */
	public AccountQueryIRep accountQueryList(AccountQueryIRequest ireq) {
		
		AccountQueryIRep irep = new AccountQueryIRep();
		AccountQueryRequest oreq = new AccountQueryRequest();
		oreq.setUserOid(ireq.getUserOid());
		oreq.setAccountType(ireq.getAccountType());
		oreq.setUserType(ireq.getUserType());
		
		AccountListResponse orep = new AccountListResponse();
		try {
//			orep = this.accountSdk.accountQueryList(oreq);
			/** orep == null, 当接口返回为NULL时  */
			if (null == orep) {
				irep.setErrorCode(BaseRep.ERROR_CODE);
				irep.setErrorMessage("返回为空");
			}
			if (0 == orep.getErrorCode()) {
				irep.setErrorMessage(orep.getErrorMessage());
				if (null == orep.getRows() || orep.getRows().size() != 1 || StringUtil.isEmpty(orep.getRows().get(0).getUserOid())) {
					irep.setErrorCode(BaseRep.ERROR_CODE);
					irep.setErrorMessage("rows is null || rows.size() != 1 || rows[0].userOid is null");
				}
				irep.setUserOid(orep.getRows().get(0).getAccountNo());
			} else {
				irep.setErrorCode(BaseRep.ERROR_CODE);
				irep.setErrorMessage(orep.getErrorCode() + "--" + orep.getErrorMessage());
			}
		} catch (Exception e) {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(AMPException.getStacktrace(e));
		}
		writeLog(irep, oreq, AccInterface.accountQueryList.getInterfaceName());
		return irep;
	}

	

	public BaseRep tradepublish(TpIntegratedRequest tpIReq) {
		return this.tradepublish(tpIReq, true);
	}
	
	public BaseRep tradepublish(TpIntegratedRequest tpIReq, boolean isLog) {
		BaseRep irep = new BaseRep();
		
		List<TransPublisherRequest> ireq = tpIReq.getTpList();
		List<TransPublishRequest> oreq = new ArrayList<TransPublishRequest>();
		for (TransPublisherRequest itmp : ireq) {
			TransPublishRequest otmp = new TransPublishRequest();
			otmp.setAccountNo(itmp.getAccountNo());
			otmp.setBalance(itmp.getBalance());
			otmp.setOrderNo(itmp.getOrderNo());
			otmp.setOrderType(AccParam.OrderType.CLOSED.toString());
			otmp.setRelationProductNo(itmp.getRelationProductNo());
			otmp.setRequestNo(StringUtil.uuid());
			otmp.setSystemSource(AccParam.SystemSource.MIMOSA.toString());
			oreq.add(otmp);
		}
		AccountTransResponse orep = new AccountTransResponse();
		try {
//			orep = this.accountSdk.tradepublish(oreq);
		} catch (Exception e) {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(AMPException.getStacktrace(e));
		}
		this.setIrep(irep, orep);
		if (isLog) {
			this.writeLog(irep, tpIReq, AccInterface.tradepublish.getInterfaceName());
		}
		return irep;
	}

	public String getProductType(Product product) {
		if (Product.TYPE_Producttype_02.equals(product.getType().getOid())) {
			return AccParam.ProductType.HQ.toString();
		}
		if (Product.TYPE_Producttype_01.equals(product.getType().getOid())) {
			return AccParam.ProductType.DQ.toString();
		}
		
		return null;
	}
	
	
}
