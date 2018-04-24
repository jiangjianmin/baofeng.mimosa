package com.guohuai.mmp.publisher.baseaccount;

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

@RestController
@RequestMapping(value = "/mimosa/boot/publisher/baseaccount", produces = "application/json")
public class PublisherBaseAccountBootController extends BaseController {
	
	@Autowired
	private PublisherBaseAccountService publisherBaseAccountService;
	
	@RequestMapping(value = "userinfo", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PublisherBaseAccountRep> userinfo() {
		String uid = this.getLoginUser();
		
		PublisherBaseAccountRep rep = this.publisherBaseAccountService.userInfo(uid);
		return new ResponseEntity<PublisherBaseAccountRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "open", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> open(@Valid BaseAccountOpenReq req) {
		this.getLoginUser();
		
		BaseRep rep = this.publisherBaseAccountService.open(req);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	
	
	
}
