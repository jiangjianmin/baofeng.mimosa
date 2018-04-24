package com.guohuai.mmp.investor.referprofit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.ams.product.ProductSchedService;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.Response;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @ClassName: ProfitScheduleMngController
 * @Description: 二级邀请定时任务提供controller访问
 * @author yihonglei
 * @date 2017年7月3日 下午2:21:56
 * @version 1.0.0
 */
@RestController
@RequestMapping(value = "/mimosa/boot/profit/schedule", produces = "application/json;charset=utf-8")
@Slf4j
public class ProfitScheduleMngController extends BaseController {
	@Autowired
	private ProfitRuleService profitRuleService;
	@Autowired
	private ProductSchedService productSchedService;
	@Autowired
	private ProfitProvideDetailService profitProvideDetailService;
	
	/** 每月一号对上月活期奖励收益明细汇总  */
	@RequestMapping(value = "t0Profit", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<Response> t0Profit() {
		log.info("<<-----每月一号对上月活期奖励收益明细汇总 start(((手动刷定时任务)))----->>");
		super.getLoginUser();
		Response r = new Response();
		try {
			if (this.profitRuleService.checkProfitRule()) {
				this.productSchedService.t0Profit();
				r.with("success", "t0Profit--执行成功！");
			} else {
				log.info("二级邀请活动已经下架(((手动刷定时任务)))");
			}
		} catch (Throwable e) {
			log.error("<<-----每月一号对上月活期奖励收益明细汇总fail(((手动刷定时任务)))----->>");
			e.printStackTrace();
		}
		log.info("<<-----每月一号对上月活期奖励收益明细汇总 success(((手动刷定时任务)))----->>");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/** 每月15号对上月二级邀请奖励进行发放 */
	@RequestMapping(value = "provideProfit", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<Response> provideProfit() {
		log.info("<<-----每月15号对上月二级邀请奖励进行发放 start(((手动刷定时任务)))----->>");
		super.getLoginUser();
		Response r = new Response();
		try {
			if (this.profitRuleService.checkProfitRule()) {
				this.profitProvideDetailService.provideProfit();
				r.with("success", "provideProfit--执行成功！");
			} else {
				log.info("二级邀请活动已经下架(((手动刷定时任务)))");
			}
		} catch (Throwable e) {
			log.error("<<-----每月15号对上月二级邀请奖励进行发放fail(((手动刷定时任务)))----->>");
			e.printStackTrace();
		}
		log.info("<<-----每月15号对上月二级邀请奖励进行发放 success(((手动刷定时任务)))----->>");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
}
