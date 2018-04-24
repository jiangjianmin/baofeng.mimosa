package com.guohuai.mmp.test;

import com.guohuai.ams.companyScatterStandard.CompanyLoanService;
import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.duration.fact.income.schedule.IncomeScheduleService;
import com.guohuai.ams.product.ProductSchedService;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.cycleProduct.CycleProductContinueInvestService;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.cache.service.CacheProductService;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.baseaccount.log.TaskCouponLogService;
import com.guohuai.mmp.investor.baseaccount.statistics.InvestorStatisticsService;
import com.guohuai.mmp.investor.tradeorder.InvestorInvestTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.InvestorRepayCashTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.InvestorRepayInterestTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.platform.accment.AccResendService;
import com.guohuai.mmp.platform.accment.AccSyncService;
import com.guohuai.mmp.platform.payment.PayResendService;
import com.guohuai.mmp.platform.payment.PaymentServiceImpl;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetService;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.publisher.holdapart.snapshot.SnapshotService;
import com.guohuai.mmp.publisher.investor.InterestDistributionService;
import com.guohuai.mmp.publisher.investor.InterestTnRaise;
import com.guohuai.mmp.publisher.product.agreement.ProductAgreementService;
import com.guohuai.mmp.publisher.product.rewardincomepractice.PracticeService;
import com.guohuai.mmp.schedule.OverdueTimesService;
import com.guohuai.mmp.schedule.ResetTodayService;
import com.guohuai.mmp.schedule.cyclesplit.DayCutService;
import com.guohuai.mmp.serialtask.SerialTaskService;
import com.guohuai.settlement.api.request.WriterOffOrderRequest;
import com.guohuai.settlement.api.response.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/mimosa/client/test", produces = "application/json")
public class ClientTestController extends BaseController {
	private Logger logger = LoggerFactory.getLogger(ClientTestController.class);

	@Autowired
	private DayCutService dayCutService;
	@Autowired
	PublisherOffsetService publisherOffsetService;
	@Autowired
	PublisherHoldService publisherHoldService;
	@Autowired
	PracticeService practiceService;
	@Autowired
	SnapshotService snapshotService;
	@Autowired
	ProductSchedService productSchedService;
	@Autowired
	ProductAgreementService productAgreementService;
	@Autowired
	InvestorStatisticsService investorStatisticsService;
	@Autowired
	InvestorRepayInterestTradeOrderService investorRepayInterestTradeOrderService;
	@Autowired
	InvestorInvestTradeOrderService investorInvestTradeOrderService;
	@Autowired
	OverdueTimesService overdueTimesService;
	@Autowired
	InterestTnRaise interestTnRaise;
	@Autowired
	SerialTaskService serialTaskEntityService;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private PaymentServiceImpl paymentServiceImpl;
	@Autowired
	private AccResendService accResendService;
	@Autowired
	private AccSyncService accSyncService;
	@Autowired
	private PayResendService payResendService;
	@Autowired
	private TaskCouponLogService taskCouponLogService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private ResetTodayService resetTodayService;
	@Autowired
	private RedisTemplate<String, String> redis;
	@Autowired
	private InterestDistributionService interestDistributionService;
	@Autowired
	private CacheProductService cacheProductService;
	@Autowired
	private IncomeScheduleService incomeScheduleService;
	@Autowired
	private AssetPoolService assetPoolService;
	@Autowired
	private CycleProductContinueInvestService cycleProductContinueInvestService;
	@Autowired
	private ProductService productService;
	@Autowired
	private InvestorRepayCashTradeOrderService investorRepayCashTradeOrderService;
	@Autowired
	private CompanyLoanService companyLoanService;

	
	@RequestMapping(value = "snapshot", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> snapshot() {
		BaseRep rep = new BaseRep();
		logger.info("<<-----开始计息份额快照----->>");
		try {
			this.snapshotService.snapshot();
		} catch (Throwable e) {
			logger.error("<<-----失败计息份额快照----->>");
			e.printStackTrace();
		}
		logger.info("<<-----成功计息份额快照----->>");
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "unlockredeem", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> unlockRedeem() {
		BaseRep rep = new BaseRep();
		logger.info("<<-----开始解锁赎回锁定份额----->>");
		try {
			this.publisherHoldService.unlockRedeem();
		} catch (Throwable e) {
			logger.error("<<-----失败解锁赎回锁定份额----->>");
			e.printStackTrace();
		}
		logger.info("<<-----成功解锁赎回锁定份额----->>");
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	@RequestMapping(value = "unlockAccrual", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> unlockAccrual() {
		BaseRep rep = new BaseRep();
		logger.info("<<-----开始解锁计息锁定份额----->>");
		try {
			this.publisherHoldService.unlockAccrual();
		} catch (Throwable e) {
			logger.error("<<-----失败解锁计息锁定份额----->>");
			e.printStackTrace();
		}
		logger.info("<<-----成功解锁计息锁定份额----->>");
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "practice", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> practice() {
		BaseRep rep = new BaseRep();
		logger.info("<<-----奖励收益试算 start----->>");
		try {
			practiceService.practice();
		} catch (Throwable e) {
			logger.error("<<-----奖励收益试算 fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----计息success----->>");
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "resetToday", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> resetToday() {
		BaseRep rep = new BaseRep();
		logger.info("<<-----投资者今日统计数据重置 start----->>");
		try {
			this.resetTodayService.resetToday();
		} catch (Throwable e) {
			logger.error("<<-----投资者今日统计数据重置 failed----->>");
			e.printStackTrace();
		}
		logger.info("<<-----投资者今日统计数据重置 success----->>");
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "makeContract", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> makeContract() {
		BaseRep rep = new BaseRep();
		logger.info("<<-----create html start----->>");
		try {
			this.productAgreementService.makeContract();
		} catch (Throwable e) {
			logger.error("<<-----create html fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----pdf success----->>");
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "uploadPDF", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> uploadPDF() {
		BaseRep rep = new BaseRep();
		
		try {
			productAgreementService.uploadPDF();
		} catch (Throwable e) {
			logger.error("<<----- fail----->>");
			e.printStackTrace();
		}
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	
//	
	
	@RequestMapping(value = "createAllNew", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> createAllNew() {
		BaseRep rep = new BaseRep();
		
		try {
			this.publisherOffsetService.createAllNew();
		} catch (Throwable e) {
			logger.error("<<----- fail----->>");
			e.printStackTrace();
		}
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "interest", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> interestTn() {
		BaseRep rep = new BaseRep();
		
		try {
			this.interestTnRaise.interestTnRaise();
		} catch (Throwable e) {
			logger.error("<<----- fail----->>");
			e.printStackTrace();
		}
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "calcSerFee", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> calcSerFee() {
		BaseRep rep = new BaseRep();
		
		try {
			//this.snapshotService.calcSerFee();
		} catch (Throwable e) {
			logger.error("<<----- fail----->>");
			e.printStackTrace();
		}
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	};
	
	
	
	
	
	/**
	 * 剩余赎回金额每日还原;
	 */
	@RequestMapping(value = "scheduleProductDailyMaxRredeem", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public void scheduleProductDailyMaxRredeem() {
		logger.info("<<-----剩余赎回金额每日还原 start----->>");
		try {
			productSchedService.scheduleProductDailyMaxRredeem(); 
		} catch (Exception e) {
			this.logger.error("剩余赎回金额每日还原失败", e);
			e.printStackTrace();
		}
		logger.info("<<-----剩余赎回金额每日还原 end----->>");
	}
	
	
	
	
	@RequestMapping(value = "notstartraiseToRaisingOrRaised", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> notstartraiseToRaisingOrRaised() {
		BaseRep rep = new BaseRep();
		
		logger.info("<<-----产品状态变化----->>");
		try {
			this.productSchedService.notstartraiseToRaisingOrRaised(); 
		} catch (Throwable e) {
			logger.error("<<-----产品状态变化 fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----产品状态变化 success----->>");
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "overdueTimes", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> overdueTimes() {
		BaseRep rep = new BaseRep();
		
		
		try {
			this.overdueTimesService.overdueTimes(); 
		} catch (Throwable e) {
			
			e.printStackTrace();
		}
		
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "executeTask", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> executeTask() {
		BaseRep rep = new BaseRep();
		try {
			this.serialTaskEntityService.executeTask();
		} catch (Throwable e) {
			
			e.printStackTrace();
		}
		
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "pinganRedeem", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<Boolean> pinganRedeem(@RequestParam String orderCode, @RequestParam String returnCode) {
		BaseRep rep = new BaseRep();
		boolean flag = false;
		try {
			
			OrderResponse orderResponse = new OrderResponse();
			orderResponse.setOrderNo(orderCode);
			orderResponse.setReturnCode(returnCode);
			flag = this.paymentServiceImpl.tradeCallback(orderResponse);
		} catch (Throwable e) {
			
			e.printStackTrace();
		}
		
		return new ResponseEntity<Boolean>(flag, HttpStatus.OK);
	}
	
	@RequestMapping(value = "accResendService", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> accResendService() {
		BaseRep rep = new BaseRep();
		try {
			accResendService.resend();
		} catch (Throwable e) {
			
			e.printStackTrace();
		}
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "payResendService", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> payResendService() {
		BaseRep rep = new BaseRep();
		try {
			payResendService.resend();
		} catch (Throwable e) {
			
			e.printStackTrace();
		}
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "accSyncService", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> accSyncService() {
		BaseRep rep = new BaseRep();
		try {
			accSyncService.test();
		} catch (Throwable e) {
			
			e.printStackTrace();
		}
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "writerOffOrder", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> writerOffOrder(@RequestParam String orderCode) {
		BaseRep rep = new BaseRep();
		try {
			WriterOffOrderRequest req = new WriterOffOrderRequest();
			req.setOriginalRedeemOrderCode(orderCode);
			this.paymentServiceImpl.writerOffOrder(req);
		} catch (Throwable e) {
			
			e.printStackTrace();
		}
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 可售份额排期发放;
	 */
	@RequestMapping(value = "scheduleSendProductMaxSaleVolume", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> scheduleSendProductMaxSaleVolume() {
		BaseRep rep = new BaseRep();
		try {
			productSchedService.scheduleSendProductMaxSaleVolume();
		} catch (Throwable e) {
			
			e.printStackTrace();
		}
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 体验金到期自动赎
	 */
	@RequestMapping(value = "flatExpGold", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> flatExpGold() {
		BaseRep rep = new BaseRep();
		try {
			investorTradeOrderService.flatExpGold();
		} catch (Throwable e) {
			
			e.printStackTrace();
		}
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "taskUseCoupon", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> taskUseCoupon() {
		BaseRep rep = new BaseRep();
		try {
			taskCouponLogService.taskUseCoupon();
		} catch (Throwable e) {
			
			e.printStackTrace();
		}
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "onRegister", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> onRegister() {
		BaseRep rep = new BaseRep();
		try {
			investorBaseAccountService.onRegister();
		} catch (Throwable e) {
			
			e.printStackTrace();
		}
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "dealhold", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> dealHold() {
		BaseRep rep = new BaseRep();
		try {
			publisherHoldService.dealHold();
		} catch (Throwable e) {
			
			e.printStackTrace();
		}
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "distributeinterest", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> distributeInterest(@RequestParam String productOid,@RequestParam String incomeOid) {
		BaseRep rep = new BaseRep();
		this.interestDistributionService.distributeInterestByProduct(incomeOid, productOid);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "getProductAlias", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> getProductAlias() {
		BaseRep rep = new BaseRep();
		// this.cacheProductService.getProductAlias("BBB");
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "incomeSchedule", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> incomeScheduleDo() {
		BaseRep rep = new BaseRep();
		this.incomeScheduleService.incomeScheduleDo();
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "incomeNoticeSchedule", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> incomeNoticeSchedule() {
		BaseRep rep = new BaseRep();
		incomeScheduleService.noticeScheduleDo();
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "calcPoolProfitSchedule", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> calcPoolProfitSchedule() {
		BaseRep rep = new BaseRep();
		assetPoolService.calcPoolProfitSchedule();
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "updateStateSchedule", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> updateStateSchedule() {
		BaseRep rep = new BaseRep();
		assetPoolService.updateStateSchedule();
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}

	/**
	 *
	 *
	 * 循环开放产品派息
	 * @author wangpeng
	 * @date 2018/4/11 17:02
	 * @param []
	 * @return org.springframework.http.ResponseEntity<com.guohuai.component.web.view.BaseRep>
	 */
	@RequestMapping(value = "cycleProductInterest", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> cycleProductInterest() {
		BaseRep rep = new BaseRep();
		productService.cycleProductInterest();
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}

	/**
	 *
	 *循环产品还本付息
	 * @author wangpeng
	 * @date 2018/4/11 17:02
	 * @param []
	 * @return org.springframework.http.ResponseEntity<com.guohuai.component.web.view.BaseRep>
	 */
	@RequestMapping(value = "cycleProductRepay", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> cycleProductRepay() {
		BaseRep rep = new BaseRep();
		investorRepayCashTradeOrderService.cycleProductRepay();
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}

	/**
	 *
	 *循环产品续投
	 * @author wangpeng
	 * @date 2018/4/11 17:03
	 * @param []
	 * @return org.springframework.http.ResponseEntity<com.guohuai.component.web.view.BaseRep>
	 */
	@RequestMapping(value = "cycleProductContinueInvest", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseRep> cycleProductContinueInvest() {
		BaseRep rep = new BaseRep();
		productService.deleteToRepayListNoUseData();
		productService.cycleProductAddToOperatingList();
		cycleProductContinueInvestService.continueInvest();
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}

	/**
	 *
	 *转投日切处理逻辑
	 * @author yujianlong
	 * @date 2018/4/11 14:43
	 * @param []
	 * @return java.lang.Object
	 */
	@RequestMapping(value = "doDayCut", method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public Object doDayCut() {
		BaseRep rep = new BaseRep();
		dayCutService.doDayCut(null);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);

	}
	/**
	 *手动触发募集满额
	 *
	 * @author yujianlong
	 * @date 2018/4/23 16:42
	 * @param []
	 * @return java.lang.Object
	 */
	@RequestMapping(value = "handTrigRaiseFullAmountCallBack", method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public Object handTrigRaiseFullAmountCallBack() {
		BaseRep rep = new BaseRep();
		companyLoanService.handTrigRaiseFullAmountCallBack();
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);

	}
}
