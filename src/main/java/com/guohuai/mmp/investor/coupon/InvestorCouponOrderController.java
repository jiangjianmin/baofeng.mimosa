package com.guohuai.mmp.investor.coupon;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseRep;

@RestController
@RequestMapping(value = "/mimosa/client/investor/coupon/", produces = "application/json")
public class InvestorCouponOrderController extends BaseController {


	@Autowired
	InvestorCouponOrderService investorCouponOrderService;

	@RequestMapping(value = "useredpacket", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<RedPacketsUseRep> useredpacket(@Valid RedPacketsUseReq req) {
		String uid = this.getLoginUser();
		
		RedPacketsUseRep rep = this.investorCouponOrderService.useRedPackets(uid, req);
		
		return new ResponseEntity<RedPacketsUseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "isredokay", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> isRedOkay(@RequestParam(required = true) String couponOrderOid) {
		this.getLoginUser();
		
		BaseRep rep = this.investorCouponOrderService.isRedOkay(couponOrderOid);
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
}
