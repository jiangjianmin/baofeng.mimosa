package com.guohuai.mmp.investor.referprofit;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.basic.component.ext.web.BaseController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/mimosa/client/investor/myinvite", produces = "application/json")
@Slf4j
public class ProfitClientController extends BaseController{
	
	@Autowired
	private ProfitProvideDetailService profitProvideDetailService;// 奖励发放明细
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: showProvideDetail
	 * @Description: 二级邀请--我的奖励详情列表
	 * @param req
	 * @return ResponseEntity<ProfitClientRep<Map<String,Object>>>
	 * @date 2017年6月15日 下午4:24:58
	 * @since  1.0.0
	 */
	@RequestMapping(value = "showProvideDetail", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<ProfitClientRep<Map<String,Object>>> showProvideDetail(@Valid @RequestBody ProfitClientReq req) {
		log.info("二级邀请--我的奖励详情列表查询参数:{}",JSONObject.toJSONString(req));		
		String uid = this.getLoginUser();
		ProfitClientRep<Map<String,Object>> res = profitProvideDetailService.showProvideDetail(req, uid);
		return new ResponseEntity<ProfitClientRep<Map<String,Object>>>(res, HttpStatus.OK);
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: showProfitProvideRank
	 * @Description: 龙虎榜查询
	 * @param req
	 * @return ResponseEntity<ProfitClientRep<Map<String,Object>>>
	 * @date 2017年6月15日 下午6:15:07
	 * @since  1.0.0
	 */
	@RequestMapping(value = "showProfitProvideRank", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<ProfitClientRep<Map<String,Object>>> showProfitProvideRank(@Valid @RequestBody ProfitClientReq req) {
		log.info("二级邀请--龙虎榜查询参数:{}",JSONObject.toJSONString(req));	
		ProfitClientRep<Map<String,Object>> res = profitProvideDetailService.showProfitProvideRank(req);
		return new ResponseEntity<ProfitClientRep<Map<String,Object>>>(res, HttpStatus.OK);
	}
}
