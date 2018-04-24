package com.guohuai.moonBox.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.basic.messageBody.annotations.SerializedField;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.moonBox.FamilyEnum;
import com.guohuai.moonBox.service.FamilyInvestPlanService;
import com.guohuai.moonBox.to.proticalChangeRes;
import com.guohuai.moonBox.to.proticalDateReq;
import com.guohuai.moonBox.to.proticalDateRes;
import com.guohuai.moonBox.to.proticalDeleteReq;
import com.guohuai.moonBox.to.proticalDeleteRes;
import com.guohuai.moonBox.to.proticalLogQueryRes;
import com.guohuai.moonBox.to.proticalQueryReq;
import com.guohuai.moonBox.to.proticalQueryRes;
import com.guohuai.moonBox.to.protocalAddReq;
import com.guohuai.moonBox.to.protocalAddRes;
import com.guohuai.moonBox.to.protocalInvestQueryReq;
import com.guohuai.moonBox.to.protocalInvestQueryRes;
import com.guohuai.moonBox.to.protocalUpdReq;
import com.guohuai.moonBox.to.protocalUpdRes;

import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
@RequestMapping(value = "/mimosa/protocal/moonBox", produces = "application/json")
public class FamilyInvestPlanController extends BaseController {
	
	@Autowired
	private FamilyInvestPlanService familyInvestPlanService;
	
	@RequestMapping(value = "/tradeQuery")
	@ResponseBody
	public ResponseEntity<protocalInvestQueryRes> tradeQuery(@Valid @RequestBody protocalInvestQueryReq req) {
		log.info("理财计划查询，{}",JSONObject.toJSONString(req));
		protocalInvestQueryRes res = new protocalInvestQueryRes();
		String uid = this.getLoginUser();
//		String uid=req.getInvestOid();
		req.setInvestOid(uid);
//		if(req.getInvestOid().equals(uid)){
        	res=familyInvestPlanService.checkInvest(req);
//		}else{
//			res.setReturnCode(FamilyEnum.return9999.getCode());
//			res.setReturnCode(FamilyEnum.return9999.getName());
//		}
		return new ResponseEntity<protocalInvestQueryRes>(res, HttpStatus.OK);
	}
	@RequestMapping(value = "/query")
	@ResponseBody
	public ResponseEntity<proticalQueryRes> query(@Valid @RequestBody proticalQueryReq req) {
		proticalQueryRes res = new proticalQueryRes();
		String uid = this.getLoginUser();
		req.setInvestOid(uid);
//		if(req.getInvestOid().equals(uid)){
			res=familyInvestPlanService.ProtocalQuery(req);
//		}else{
//			res.setReturnCode(FamilyEnum.return9999.getCode());
//			res.setReturnCode(FamilyEnum.return9999.getName());
//		}
		return new ResponseEntity<proticalQueryRes>(res, HttpStatus.OK);
	}
	@RequestMapping(value = "/isAllowChange")
	@ResponseBody
	public ResponseEntity<proticalChangeRes> isAllowChange(){
		String investOid=this.getLoginUser();
		proticalChangeRes res=familyInvestPlanService.isAllowChange(investOid);
		return new ResponseEntity<proticalChangeRes>(res, HttpStatus.OK);
	}
	@RequestMapping(value = "/delProtocal")
	@ResponseBody
	public ResponseEntity<proticalDeleteRes> delProtocal(@Valid @RequestBody proticalDeleteReq req) {
		proticalDeleteRes res = new proticalDeleteRes();
		String uid = this.getLoginUser();
		req.setInvestOid(uid);
//		if(req.getInvestOid().equals(uid)){
			res=familyInvestPlanService.delProtocal(req);
//		}else{
//			res.setReturnCode(FamilyEnum.return9999.getCode());
//			res.setReturnCode(FamilyEnum.return9999.getName());
//		}
		return new ResponseEntity<proticalDeleteRes>(res, HttpStatus.OK);
	}
	@RequestMapping(value = "/addProtocal")
	@ResponseBody
	public ResponseEntity<protocalAddRes> addProtocal(@Valid @RequestBody protocalAddReq req) {
		protocalAddRes res = new protocalAddRes();
		String uid = this.getLoginUser();
		req.setInvestOid(uid);
//		if(req.getInvestOid().equals(uid)){
			res=familyInvestPlanService.addProtocal(req);
//		}else{
//			res.setReturnCode(FamilyEnum.return9999.getCode());
//			res.setReturnCode(FamilyEnum.return9999.getName());
//		}
		return new ResponseEntity<protocalAddRes>(res, HttpStatus.OK);
	}
	@RequestMapping(value = "/updProtocal")
	@ResponseBody
	public ResponseEntity<protocalUpdRes> updProtocal(@Valid @RequestBody protocalUpdReq req) {
		protocalUpdRes res = new protocalUpdRes();
		String uid = this.getLoginUser();
		req.setInvestOid(uid);
//		if(req.getInvestOid().equals(uid)){
			res=familyInvestPlanService.updProtocal(req);
//		}else{
//			res.setReturnCode(FamilyEnum.return9999.getCode());
//			res.setReturnCode(FamilyEnum.return9999.getName());
//		}
		return new ResponseEntity<protocalUpdRes>(res, HttpStatus.OK);
	}
	@RequestMapping(value ="protocalLogQuery")
	@ResponseBody
	public ResponseEntity<PagesRep<proticalLogQueryRes>> protocalLogQuery(@RequestParam String investorOid,@RequestParam int page, @RequestParam int rows){
		page = page < 1 ? 1 : page;
		rows = rows < 1 ? 10 : rows;
		PagesRep<proticalLogQueryRes> pages = this.familyInvestPlanService.protocalLogQuery(investorOid,page,rows);
		return new ResponseEntity<PagesRep<proticalLogQueryRes>>(pages, HttpStatus.OK);
	}
	@RequestMapping(value = "/ProtocalInvestDate")
	@ResponseBody 
	public ResponseEntity<proticalDateRes> ProtocalInvestDate(@Valid @RequestBody proticalDateReq req) {
		proticalDateRes res = new proticalDateRes();
		String uid = this.getLoginUser();
		req.setInvestOid(uid);
//		if(req.getInvestOid().equals(uid)){
			String nextInvestDate=familyInvestPlanService.nextAutoInvestDate(req,req.getProtocalDate());
			if(nextInvestDate!=null){
				res.setNextInvestDate(nextInvestDate);
				res.setReturnCode(FamilyEnum.return0000.getCode());
				res.setReturnMsg(FamilyEnum.return0000.getName());
			}else{
				res.setReturnCode(FamilyEnum.return9999.getCode());
				res.setReturnCode(FamilyEnum.return9999.getName());
			}
//		}else{
//			res.setReturnCode(FamilyEnum.return9999.getCode());
//			res.setReturnCode(FamilyEnum.return9999.getName());
//		}
		return new ResponseEntity<proticalDateRes>(res, HttpStatus.OK);
	}
}
