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
import com.guohuai.mmp.platform.statistics.UserBehaviorStatisticsReq;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountRep;
import com.guohuai.plugin.PageVo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/mimosa/boot/profit", produces = "application/json")
public class ProfitBootController {
	
	@Autowired
	private ProfitProvideStatisticsService profitProvideStatisticsService;

	/**
	 * 邀请奖励明细收益统计
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "queryProfitInterestDetail", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageVo<Map<String,Object>>> queryProfitInterestDetail(@Valid @RequestBody ProfitDetailStatisticsReq req) {
		
		log.info("收益明细查询:{}",JSONObject.toJSONString(req));		
		PageVo<Map<String,Object>> res = profitProvideStatisticsService.queryProfitDetailStatistics(req);
		return new ResponseEntity<PageVo<Map<String,Object>>>(res, HttpStatus.OK);
	}
	
	/**
	 * 邀请奖励-奖励发放明细
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "queryProfitProvideInterestDetail", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageVo<Map<String,Object>>> queryProfitProvideInterestDetail(@Valid @RequestBody ProfitProvideDetailStatisticsReq req) {
		
		log.info("奖励发放明细:{}",JSONObject.toJSONString(req));		
		PageVo<Map<String,Object>> res = profitProvideStatisticsService.queryProfitProvideDetailStatistics(req);
		return new ResponseEntity<PageVo<Map<String,Object>>>(res, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @return
	 */
	@RequestMapping(value = "profitProvideTotal", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<ProfitProvideTotalRep> profitProvideTotal() {
		ProfitProvideTotalRep rep = this.profitProvideStatisticsService.profitProvideTotal();
		return new ResponseEntity<ProfitProvideTotalRep>(rep, HttpStatus.OK);
	}
}
