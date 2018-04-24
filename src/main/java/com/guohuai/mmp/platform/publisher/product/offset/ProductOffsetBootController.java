package com.guohuai.mmp.platform.publisher.product.offset;

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
@RequestMapping(value = "/mimosa/boot/productoffset", produces = "application/json")
public class ProductOffsetBootController extends BaseController {

	@Autowired
	ProductOffsetService  productOffsetService;
	
	@RequestMapping(value = "findpid", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> findByProductOid(@RequestParam(required = true) String productOid){
		BaseRep baseRep = this.productOffsetService.findByProductOid(productOid);
		return new ResponseEntity<BaseRep>(baseRep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "findsoid", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> findBySpvOid(@RequestParam(required = true) String offsetOid){
		BaseRep baseRep = this.productOffsetService.findByOffsetOid(offsetOid);
		return new ResponseEntity<BaseRep>(baseRep, HttpStatus.OK);
	}
	
}
