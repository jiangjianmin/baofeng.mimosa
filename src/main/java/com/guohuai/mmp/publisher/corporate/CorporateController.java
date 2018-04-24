package com.guohuai.mmp.publisher.corporate;

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
import com.guohuai.component.web.view.PagesRep;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = { "/mimosa/corporate"}, produces = "application/json;charset=utf-8")
@Slf4j(topic = "com.guohuai")
public class CorporateController extends BaseController {

	@Autowired
	private CorporateService corporateService;

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<BaseRep> create(CorporateReq corporateReq) {
		String operator = this.getLoginUser();
		log.debug(corporateReq.toString());
		this.corporateService.create(corporateReq, operator);
		BaseRep rep = new BaseRep();
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}

	@RequestMapping(value = "/query", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<PagesRep<CorporateQueryRep>> query(@RequestParam(defaultValue = "") String account, 
			@RequestParam(defaultValue = "") String begin, @RequestParam(defaultValue = "") String end,
			@RequestParam(defaultValue = "0") int status) {
		PagesRep<CorporateQueryRep> rep = this.corporateService.query(account, begin, end, status);

		return new ResponseEntity<PagesRep<CorporateQueryRep>>(rep, HttpStatus.OK);
	}

	@RequestMapping(value = "/read", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<CorporateDetailRep> read(@RequestParam String oid) {
		CorporateDetailRep rep = this.corporateService.detail(oid);

		return new ResponseEntity<CorporateDetailRep>(rep, HttpStatus.OK);
	}

	@RequestMapping(value = "/lockin", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<BaseRep> lockin(@RequestParam String oid) {
		super.getLoginUser();
		this.corporateService.lockin(oid, super.getLoginUser());
		BaseRep rep = new BaseRep();
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}

}
