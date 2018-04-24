package com.guohuai.mmp.platform.reserved.account;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.platform.reserved.order.ReservedOrderEntity;



@RestController
@RequestMapping(value = "/mimosa/boot/platform/reservedaccount", produces = "application/json")
public class ReservedAccountBootController extends BaseController {
	
	@Autowired
	private ReservedAccountService reservedAccountService;

	/**
	 * 备付金--中间户代收（超级户）
	 */
	@RequestMapping(value = "collectsuper", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> createHostingCollectTrade4Super(@Valid ReservedAccountReq reservedAccountReq) {
		this.getLoginUser();
		
		reservedAccountReq.setRelatedAcc(ReservedOrderEntity.ORDER_relatedAcc_superAcc);
		BaseRep rep = this.reservedAccountService.reserveCollect(reservedAccountReq);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 备付金--中间户代收（平台户）
	 */
	@RequestMapping(value = "collectbasic", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> createHostingCollectTrade4Basic(@Valid ReservedAccountReq reservedAccountReq) {
		this.getLoginUser();
		
		reservedAccountReq.setRelatedAcc(ReservedOrderEntity.ORDER_relatedAcc_basicAcc);
		BaseRep rep = this.reservedAccountService.reserveCollect(reservedAccountReq);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
//	@RequestMapping(value = "collectBack", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<BaseRep> collectBack(@RequestParam String orderCode, @RequestParam String notifyId) {
//		
//		TradeStatusSync trade = new TradeStatusSync();
//		trade.setOuter_trade_no(orderCode);
//		trade.setNotify_id(notifyId);
//		trade.setInner_trade_no(UUID.randomUUID().toString());
//		this.paymentServiceImpl.onTradeStatus(trade);;
//		BaseRep rep = new BaseRep();
//		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
//	}
	
	/**
	 * 备付金--中间户代付（超级户）
	 */
	@RequestMapping(value = "paysuper", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> createSingleHostingPayTrade4Super(@Valid ReservedAccountReq reservedAccountReq) {
		this.getLoginUser();
		
		reservedAccountReq.setRelatedAcc(ReservedOrderEntity.ORDER_relatedAcc_superAcc);
		BaseRep rep = this.reservedAccountService.reservePay(reservedAccountReq);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 备付金--中间户代付（平台户）
	 */
	@RequestMapping(value = "paybasic", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> createSingleHostingPayTrade4Basic(@Valid ReservedAccountReq reservedAccountReq) {
		this.getLoginUser();
		
		reservedAccountReq.setRelatedAcc(ReservedOrderEntity.ORDER_relatedAcc_basicAcc);
		BaseRep rep = this.reservedAccountService.reservePay(reservedAccountReq);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	
	/**
	 * 备付金--明细信息
	 */
	@RequestMapping(value = "deta", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> detail() {
		this.getLoginUser();
		
		BaseRep rep = this.reservedAccountService.detail();
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "depost", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> deposit(@Valid ReservedAccountTradeReq tReq) {
		this.getLoginUser();
		tReq.setOrderType(ReservedOrderEntity.ORDER_orderType_deposit);
		BaseRep rep = this.reservedAccountService.trade(tReq);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "withdraw", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> withdraw(@Valid ReservedAccountTradeReq tReq) {
		this.getLoginUser();
		tReq.setOrderType(ReservedOrderEntity.ORDER_orderType_withdraw);
		BaseRep rep = this.reservedAccountService.trade(tReq);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	
}
