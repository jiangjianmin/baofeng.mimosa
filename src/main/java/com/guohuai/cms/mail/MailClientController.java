package com.guohuai.cms.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.basic.component.ext.web.BaseResp;
import com.guohuai.basic.component.ext.web.PageResp;

@RestController
@RequestMapping(value = "/mimosa/client/mail", produces = "application/json")
public class MailClientController extends BaseController{

	@Autowired
	private MailService mailService;
	
	@RequestMapping(value = "/query", name="前台分页查询站内信",  method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<PageResp<MailCTResp>> query(@RequestParam int page, @RequestParam int rows) {
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "createTime")));
		PageResp<MailCTResp> resp = this.mailService.queryCTPage(pageable, this.getLoginUser());
		
		return new ResponseEntity<PageResp<MailCTResp>>(resp, HttpStatus.OK);
	}
}
