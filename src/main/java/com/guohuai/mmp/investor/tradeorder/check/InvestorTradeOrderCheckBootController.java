package com.guohuai.mmp.investor.tradeorder.check;

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
import com.guohuai.mmp.investor.tradeorder.InvestorInvestTradeOrderExtService;

@RestController
@RequestMapping(value = "/mimosa/boot/tradeorder/check", produces = "application/json")
public class InvestorTradeOrderCheckBootController extends BaseController {

	@Autowired
	private InvestorAbandonTradeOrderService investorAbandonTradeOrderService;
	@Autowired
	private InvestorRefundTradeOrderService investorRefundTradeOrderService;
	@Autowired
	private InvestorInvestTradeOrderExtService investorInvestTradeOrderExtService;

	@RequestMapping(value = "abandon", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<BaseRep> abandon(@Valid AbandonReq req) {

		BaseRep rep = investorAbandonTradeOrderService.abandon(req);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}

	@RequestMapping(value = "refund", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<BaseRep> refund(@Valid RefundTradeOrderReq req) {

		BaseRep rep = investorRefundTradeOrderService.refund(req);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "rsinvest", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<BaseRep> resumitInvestOrder( @Valid CheckOrderReq req) {

		BaseRep rep = investorInvestTradeOrderExtService.resumitInvestOrder(req);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "rsredeem", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<BaseRep> resumitRedeemOrder( @Valid CheckOrderReq redeemTradeOrderReq) {

		BaseRep rep = investorInvestTradeOrderExtService.resumitRedeemOrder(redeemTradeOrderReq);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
}
