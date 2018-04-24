package com.guohuai.mmp.investor.bank;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.util.CheckUtil;
import com.guohuai.component.web.view.BaseRep;
@RestController
@RequestMapping(value = "/mimosa/client/investor/bank", produces = "application/json")
public class BankClientController extends BaseController {
	
	@Autowired
	private BankService bankService;		
	
//	/**
//	 * 绑卡的时候验证4要素
//	 * @param req
//	 * @return
//	 */
//	@RequestMapping(value = "valid4ele", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BaseRep> valid4Ele(@Valid @RequestBody BankValid4EleReq req) {
//		CheckUtil.isMobileNO(req.getPhone());
//		req.setInvestorOid(super.getLoginUser());
//		this.bankService.bindBank(req);
//		return new ResponseEntity<BaseRep>(new BaseRep(), HttpStatus.OK);
//	}
	
	/**
	 * 申请代扣协议
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "validagree", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BankAgreeRep> validAgree(@Valid @RequestBody BankAgreeReq req) {
		String userOid = super.getLoginUser();
		BankAgreeRep rep = this.bankService.validAgreement(req, userOid);
		return new ResponseEntity<BankAgreeRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 确认签约代扣协议
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "confirmAgree", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> confirmAgree(@Valid @RequestBody BankAgreeReq req) {
		String userOid = super.getLoginUser();
		BaseRep rep = this.bankService.confirmAgreement(req, userOid);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	
}
