package com.guohuai.mmp.publisher.product.rewardincomepractice;

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
@RequestMapping(value = "/mimosa/boot/practice", produces = "application/json")
public class PracticeController extends BaseController {

	@Autowired
	PracticeService practiceService;


	@RequestMapping(value = "deta", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseRep> detail(@RequestParam(required = true) String productOid) throws Exception {
		this.getLoginUser();
		BaseRep rep = this.practiceService.detail(productOid);

		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}

	@RequestMapping(value = "query", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseRep> query(@RequestParam(required = false) String productOid) throws Exception {
		
		
		BaseRep rep = this.practiceService.findByProduct(productOid, null);

		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
}
