package com.guohuai.mmp.investor.referprofit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.Response;

@RestController
@RequestMapping(value = "/mimosa/boot/profitRule", produces = "application/json;charset=utf-8")
public class ProfitRuleBootController extends BaseController{
	
	@Autowired
	private ProfitRuleService profitRuleService;// 奖励发放规则
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: saveProfitRule
	 * @Description: 奖励发放规则保存
	 * @param form
	 * @return ResponseEntity<Response>
	 * @date 2017年6月16日 下午2:57:56
	 * @since  1.0.0
	 */
	@RequestMapping(value = "saveProfitRule", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<Response> saveProfitRule(ProfitRuleForm form) {
		super.getLoginUser();
		Response r = new Response();
		int result = profitRuleService.saveProfitRule(form);
		if (result > 0) {
			r.with("saveResult", "保存成功!");
		} else {
			r.with("saveResult", "保存失败!");
		}
		
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryProfitRule
	 * @Description: 查询奖励收益规则
	 * @return ResponseEntity<ProfitRuleEntity>
	 * @date 2017年6月26日 下午8:16:02
	 * @since  1.0.0
	 */
	@RequestMapping(value = "queryProfitRule", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<ProfitRuleEntity> queryProfitRule() {
		super.getLoginUser();
		ProfitRuleEntity profitRuleEntity = profitRuleService.queryProfitRule();
		return new ResponseEntity<ProfitRuleEntity>(profitRuleEntity, HttpStatus.OK);
	}
	
}
