package com.guohuai.mmp.platform.baseaccount;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;

@RestController
@RequestMapping(value = "/mimosa/client/platform/baseaccount", produces = "application/json")
public class PlatformBaseAccountClientController extends BaseController {
	
	@Autowired
	PlatformBaseAccountService platformBaseAccountService;
	
	@RequestMapping(value = "deta", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<PlatformBaseAccountRep> deta() {
		
		
		PlatformBaseAccountRep rep = this.platformBaseAccountService.deta();
		return new ResponseEntity<PlatformBaseAccountRep>(rep, HttpStatus.OK);
	}
	
	
	
}
