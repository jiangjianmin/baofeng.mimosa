package com.guohuai.mmp.platform.baseaccount;

import java.math.BigDecimal;

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
@RequestMapping(value = "/mimosa/boot/platform/baseaccount", produces = "application/json")
public class PlatformBaseAccountBootController extends BaseController {
	
	@Autowired
	PlatformBaseAccountService platformBaseAccountService;
	
	@RequestMapping(value = "deta", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<PlatformBaseAccountRep> deta() {
		this.getLoginUser();
		PlatformBaseAccountRep rep = this.platformBaseAccountService.deta();
		return new ResponseEntity<PlatformBaseAccountRep>(rep, HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "borrow", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<BaseRep> borrow(@RequestParam(required = true) BigDecimal amount) {
		this.getLoginUser();
		
		BaseRep rep = this.platformBaseAccountService.borrowMoney(amount);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "pay", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<BaseRep> pay(@RequestParam(required = true) BigDecimal amount) {
		this.getLoginUser();
		
		BaseRep rep = this.platformBaseAccountService.payMoney(amount);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	
}
