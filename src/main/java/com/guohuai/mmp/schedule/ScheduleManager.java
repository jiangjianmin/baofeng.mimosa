package com.guohuai.mmp.schedule;

import com.guohuai.ams.duration.assetPool.AssetPoolService;
import com.guohuai.ams.duration.fact.income.schedule.IncomeScheduleService;
import com.guohuai.ams.investment.pool.InvestmentPoolService;
import com.guohuai.ams.product.ProductSchedService;
import com.guohuai.basic.message.DealMessageEnum;
import com.guohuai.calendar.TradeCalendarService;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.investor.baseaccount.log.TaskCouponLogService;
import com.guohuai.mmp.investor.referprofit.ProfitProvideDetailService;
import com.guohuai.mmp.investor.referprofit.ProfitRuleService;
import com.guohuai.mmp.investor.tradeorder.BfPlusRedeemScheduleService;
import com.guohuai.mmp.investor.tradeorder.InvestorClearTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.platform.baseaccount.statistics.history.PlatformStatisticsHistoryService;
import com.guohuai.mmp.platform.channel.statistics.PlatformChannelStatisticsService;
import com.guohuai.mmp.platform.payment.PayResendService;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetService;
import com.guohuai.mmp.platform.statistics.UserBehaviorStatisticsService;
import com.guohuai.mmp.platform.tulip.TulipResendService;
import com.guohuai.mmp.publisher.baseaccount.statistics.history.PublisherStatisticsHistoryService;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.publisher.holdapart.snapshot.SnapshotService;
import com.guohuai.mmp.publisher.investor.InterestTnRaise;
import com.guohuai.mmp.publisher.product.agreement.ProductAgreementService;
import com.guohuai.mmp.publisher.product.rewardincomepractice.PracticeService;
import com.guohuai.mmp.publisher.product.statistics.PublisherProductStatisticsService;
import com.guohuai.mmp.serialtask.SerialTaskService;
import com.guohuai.moonBox.service.FamilyInvestPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(prefix = "ams", name = "needSchedule", havingValue = "yes")
@Component
@Async("mySimpleAsync")
public class ScheduleManager {

	private Logger logger = LoggerFactory.getLogger(ScheduleManager.class);

	@Autowired
	private PublisherOffsetService publisherOffsetService;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private PracticeService practiceService;
	@Autowired
	private ProductSchedService productSchedService;
	@Autowired
	private ProfitProvideDetailService profitProvideDetailService;
	@Autowired
	private AssetPoolService assetPoolService;
	@Autowired
	private InvestmentPoolService investmentPoolService;
	@Autowired
	private InterestTnRaise interestTnRaise;
	@Autowired
	private ResetTodayService resetTodayService;
	@Autowired
	private PublisherProductStatisticsService publisherProductStatisticsService;
	@Autowired
	private OverdueTimesService overdueTimesService;
	@Autowired
	private PlatformChannelStatisticsService platformChannelStatisticsService;
	@Autowired
	private PlatformStatisticsHistoryService platformStatisticsHistoryService;
	@Autowired
	private PublisherStatisticsHistoryService publisherStatisticsHistoryService;
	@Autowired
	private InvestorClearTradeOrderService investorClearTradeOrderService;
	@Autowired
	private ProductAgreementService productAgreementService;
	@Autowired
	private TulipResendService tulipResendService;
	@Autowired
	private SerialTaskService serialTaskService;
	@Autowired
	private SnapshotService snapshotService;
	@Autowired
	private PayResendService payResendService;
	@Autowired
	private TaskCouponLogService taskCouponLogService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private TradeCalendarService tradeCalendarService;
	@Autowired
	private UserBehaviorStatisticsService userBehaviorStatisticsService;
	@Autowired
	private IncomeScheduleService incomeScheduleService;
	@Autowired
	private FamilyInvestPlanService familyInvestPlanService;
	@Autowired
	private ProfitRuleService profitRuleService;
	@Autowired
	private BfPlusRedeemScheduleService bfPlusRedeemScheduleService;
	
	/**
	 * 计息份额快照
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.snapshot]:10 00 15 * * ?}")
	public void snapshot() {
		logger.info("<<-----开始计息份额快照----->>");
		try {
			this.snapshotService.snapshot();
		} catch (Throwable e) {
			logger.error("<<-----失败计息份额快照----->>");
			e.printStackTrace();
		}
		logger.info("<<-----成功计息份额快照----->>");
	}

	/**
	 * 奖励收益试算
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.practice]:00 1 15 * * ?}")
	public void practice() {
		logger.info("<<-----奖励收益试算 start---->>");
		try {
			practiceService.practice();
		} catch (Throwable e) {

			e.printStackTrace();
		}
		logger.info("<<-----奖励收益试算 end---->>");
	}

	/**
	 * 创建平台-发行人的普通轧差批次
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.publiser_offset]:10 00 15 * * ?}")
	public void createAllNew() {
		logger.info("<<-----开始创建平台-发行人的普通轧差批次----->>");
		try {
			this.publisherOffsetService.createAllNew();
		} catch (Throwable e) {
			logger.error("<<-----失败创建平台-发行人的普通轧差批次----->>");
			e.printStackTrace();
		}
		logger.info("<<-----成功创建平台-发行人的普通轧差批次----->>");
	}

	/**
	 * 生成快定宝轧差定时任务
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.bfplus_offset]:10 00 00 * * ?}")
	public void createBfPlusOffset() {
		/*logger.info("<<-----开始创建生成快定宝轧差批次----->>");
		try {
			this.publisherOffsetService.createBfPlusOffset();
		} catch (Throwable e) {
			logger.error("<<-----失败创建生成快定宝轧差批次----->>");
			e.printStackTrace();
		}
		logger.info("<<-----成功创建创建生成快定宝轧差批次----->>");*/
	}

	/**
	 * 解锁赎回锁定份额
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.unlock_redeem]:00 2 15 * * ?}")
	public void unlockRedeem() {
		logger.info("<<-----开始解锁赎回锁定份额----->>");
		try {
			this.publisherHoldService.unlockRedeem();
		} catch (Throwable e) {
			logger.error("<<-----失败解锁赎回锁定份额----->>");
			e.printStackTrace();
		}
		logger.info("<<-----成功解锁赎回锁定份额----->>");
	}

	/**
	 * 解锁可计息份额
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.unlock_accrual]:10 2 15 * * ?}")
	public void unlockAccrual() {
		logger.info("<<-----开始解锁计息锁定份额----->>");
		try {
			this.publisherHoldService.unlockAccrual();
		} catch (Throwable e) {
			logger.error("<<-----失败计息赎回锁定份额----->>");
			e.printStackTrace();
		}
		logger.info("<<-----成功解锁赎回锁定份额----->>");
	}

	@Scheduled(cron = "${cron.option[cron.mmp.reset_today]:00 00 15 * * ?}")
	public void resetToday() {
		{
			logger.info("<<-----投资者今日统计数据重置 start----->>");
			try {

				this.resetTodayService.resetToday();
			} catch (Throwable e) {
				logger.error("<<-----投资者今日统计数据重置 failed----->>");
				e.printStackTrace();
			}
			logger.info("<<-----投资者今日统计数据重置 success----->>");
		}
	}
	
	/**
	 * 定期募集期收益发放
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.interest_tn]:00 00 15 * * ?}")
	public void interestTnRaise() {
		logger.info("<<-----募集期已确认份额计息 start----->>");
		try {
			this.interestTnRaise.interestTnRaise();
		} catch (Throwable e) {
			logger.error("<<-----募集期已确认份额计息 fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----募集期已确认份额计息 success----->>");
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: t0Profit
	 * @Description: 每月一号对上月活期奖励收益明细汇总
	 * @return void
	 * @date 2017年6月13日 下午9:43:53
	 * @since  1.0.0
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.profit_t0]:0 30 4 1 * ?}")
	public void t0Profit() {
		logger.info("<<-----每月一号对上月活期奖励收益明细汇总 start----->>");
		try {
			if (this.profitRuleService.checkProfitRule()) {
				this.productSchedService.t0Profit();
			} else {
				logger.info("二级邀请活动已经下架");
			}
		} catch (Throwable e) {
			logger.error("<<-----每月一号对上月活期奖励收益明细汇总fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----每月一号对上月活期奖励收益明细汇总 success----->>");
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: provideProfit
	 * @Description: 每月15号对上月二级邀请奖励进行发放
	 * @return void
	 * @date 2017年6月14日 下午2:23:30
	 * @since  1.0.0
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.provide_profit]:0 10 4 15 * ?}")
	public void provideProfit() {
		logger.info("<<-----每月15号对上月二级邀请奖励进行发放 start----->>");
		try {
			if (this.profitRuleService.checkProfitRule()) {
				this.profitProvideDetailService.provideProfit();
			} else {
				logger.info("二级邀请活动已经下架");
			}
		} catch (Throwable e) {
			logger.error("<<-----每月15号对上月二级邀请奖励进行发放fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----每月15号对上月二级邀请奖励进行发放 success----->>");
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: profitRank
	 * @Description: 每日凌晨3点30对二级邀请龙虎榜进行统计
	 * @return void
	 * @date 2017年6月15日 下午9:21:09
	 * @since  1.0.0
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.profit_rank]:0 30 3 * * ?}")
	public void profitRank() {
		logger.info("<<-----每日凌晨3点30对二级邀请龙虎榜进行统计 start----->>");
		try {
			if (this.profitRuleService.checkProfitRule()) {
				this.profitProvideDetailService.profitRank();
			} else {
				logger.info("二级邀请活动已经下架");
			}
		} catch (Throwable e) {
			logger.error("<<-----每日凌晨3点30对二级邀请龙虎榜进行统计 fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----每日凌晨3点30对二级邀请龙虎榜进行统计 success----->>");
	}
	
	@Scheduled(cron = "${cron.option[cron.mmp.overdue_times]:0 0 1 * * ?}")
	public void overdueTimes() {
		logger.info("<<----逾期次数统计 start----->>");
		try {
			overdueTimesService.overdueTimes();
		} catch (Throwable e) {
			logger.error("<<-----逾期次数统计 fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----逾期次数统计 success----->>");
	}
	
	@Scheduled(cron = "${cron.option[cron.mmp.upload_pdf]:0 0 17 * * ?}")
	public void uploadPdf() {
		logger.info("<<----uploadPdf start----->>");
		try {
			productAgreementService.uploadPDF();
		} catch (Throwable e) {
			logger.error("<<-----uploadPdf fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----uploadPdf success----->>");
	}
	
	@Scheduled(cron = "${cron.option[cron.mmp.create_html]:0 0 13 * * ?}")
	public void createHtml() {
		logger.info("<<----createHtml start----->>");
		try {
			this.productAgreementService.makeContract();
		} catch (Throwable e) {
			logger.error("<<-----createHtml fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----createHtml success----->>");
	}
	
	/**
	 * 序列化任务
	 */
	@Scheduled(cron = "${cron.option[serital.task]:0/5 * * * * ?}")
	public void serialTask() {
		logger.info("<<-----serial.task start----->>");
		try {
			this.serialTaskService.executeTask();
		} catch (Exception e) {
			this.logger.error("serial.task fail", e);
			e.printStackTrace();
		}
		logger.info("<<-----serial.task end----->>");
	}
	

	/**
	 * 取消超时序列化任务
	 */
	@Scheduled(cron = "${cron.option[cancel.task]:0/30 * * * * ?}")
	public void cancelTask() {
		logger.info("<<-----cancel.task start----->>");
		try {
			this.serialTaskService.resetTimeoutTask();
		} catch (Exception e) {
			this.logger.error("cancel.task fail", e);
			e.printStackTrace();
		}
		logger.info("<<-----cancel.task end----->>");
	}
	
	/**
	 * 计算资产池当日的确认收益
	 */
	@Scheduled(cron = "${cron.option[cron.ams.calcPoolProfitSchedule]:0 2 15 * * ?}")
	public void calcPoolProfitSchedule() {
		logger.info("<<-----计算资产池当日的确认收益 start----->>");
		try {
			assetPoolService.calcPoolProfitSchedule();
		} catch (Exception e) {
			this.logger.error("计算资产池当日的确认收益失败", e);
			e.printStackTrace();
		}
		logger.info("<<-----计算资产池当日的确认收益 end----->>");
	}
	
	/**
	 * 定时初始化资产池的每日收益计算和收益分配的状态
	 */
	@Scheduled(cron = "${cron.option[cron.ams.updateStateSchedule]:20 1 15 * * ?}")
	public void updateStateSchedule() {
		logger.info("<<-----定时初始化资产池的每日收益计算和收益分配的状态 start----->>");
		try {
			assetPoolService.updateStateSchedule();
		} catch (Exception e) {
			this.logger.error("定时初始化资产池的每日收益计算和收益分配的状态失败", e);
			e.printStackTrace();
		}
		logger.info("<<-----定时初始化资产池的每日收益计算和收益分配的状态 end----->>");
	}
	
	
	/**
	 * 定时任务 - 更新投资标的生命周期状态 存续期（STAND_UP） --> 兑付期（PAY_BACK)
	 */
	@Scheduled(cron = "${cron.option[cron.ams.updateLifeStateSchedule]:0 3 15 * * ?}")
	public void updateLifeStateSchedule() {
		logger.info("<<-----更新投资标的生命周期状态 start----->>");
		try {
			investmentPoolService.updateLifeStateSchedule();
		} catch (Exception e) {
			this.logger.error("更新投资标的生命周期状态失败", e);
			e.printStackTrace();
		}
		logger.info("<<-----更新投资标的生命周期状态 end----->>");
	}
	
	/**
	 * 活期: 当<<成立开始日期>>到,募集未开始变为募集中; 定期: 当<<募集开始日期>>到,募集未开始变为募集中; 定期:
	 * 当<<募集結束日期>>到或募集满额,募集中变为募集結束; 定期: 当<<存续期结束日期>>到,存续期变为存续期結束; 定期:
	 * 当募集满额后自动触发成立 募集满额后的第X个自然日后自动成立
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.product_state]:40 1 15 * * ?}")
	public void scheduleProductState() {
		logger.info("<<-----开始活期: 募集未开始->募集中->募集結束,定期: 募集未开始->募集中->募集結束->存续期->存续期結束生命周期变化----->>");
		try {
			this.productSchedService.notstartraiseToRaisingOrRaised();
		} catch (Exception e) {
			logger.error("<<-----失败活期: 募集未开始->募集中->募集結束,定期: 募集未开始->募集中->募集結束->存续期->存续期結束生命周期变化----->>", e);
			e.printStackTrace();
		}
		logger.info("<<-----成功活期: 募集未开始->募集中->募集結束,定期: 募集未开始->募集中->募集結束->存续期->存续期結束生命周期变化----->>");
	}

	/**
	 * 可售份额排期发放;
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.product_maxSaleVolume]:10 1 15 * * ?}")
	public void scheduleSendProductMaxSaleVolume() {
		logger.info("<<-----可售份额排期发放 start----->>");
		try {
			productSchedService.scheduleSendProductMaxSaleVolume();
		} catch (Exception e) {
			this.logger.error("可售份额排期发放失败", e);
			e.printStackTrace();
		}
		logger.info("<<-----可售份额排期发放 end----->>");
	}

	/**
	 * 剩余赎回金额每日还原;
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.product_dailyMaxRredeem]:0 5 0 * * ?}")
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

	


	/** 各发行人的投资额TOP5产品排名统计 */
	@Scheduled(cron = "${cron.option[cron.mmp.publisher_investTop5Product]:0 0 1 * * ?}")
	public void publishersProductInvestorTop5() {
		logger.info("<<-----各发行人的投资额TOP5产品排名统计 start----->>");
		try {
			
			publisherProductStatisticsService.statPublishersProductInvestInfoByDate();
		} catch (Throwable e) {
			logger.error("<<-----各发行人的投资额TOP5产品排名统计 fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----各发行人的投资额TOP5产品排名统计 success----->>");
	}

	

	@Scheduled(cron = "${cron.option[cron.mmp.clear_order]:0 */10 * * * ?}")
	public void clear() {
		logger.info("<<----生成清盘赎回单 start----->>");
		try {
			investorClearTradeOrderService.clear();
		} catch (Throwable e) {
			logger.error("<<-----生成清盘赎回单 fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----生成清盘赎回单 success----->>");
	}

	

	

	/** 平台各渠道昨日投资信息统计 */
	@Scheduled(cron = "${cron.option[cron.mmp.platform_channelinveststat]:0 0 1 * * ?}")
	public void statChannelYesterdayInvestInfo() {
		logger.info("<<-----平台各渠道昨日投资信息统计 start----->>");
		try {
			
			this.platformChannelStatisticsService.statChannelYesterdayInvestInfo();
		} catch (Throwable e) {
			logger.error("<<-----平台各渠道昨日投资信息统计 fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----平台各渠道昨日投资信息统计 success----->>");
	}
	
	/** 用户昨日--投资信息统计 */
	@Scheduled(cron = "${cron.option[cron.mmp.user_investInfo]:0 0 4 * * ?}")
	public void statUserYesterdayInvestInfo() {
		logger.info("<<-----用户昨日--投资信息统计 start----->>");
		try {
			this.userBehaviorStatisticsService.statUserYesterdayInvestInfo();
		} catch (Throwable e) {
			logger.error("<<-----用户昨日--投资信息统计 fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----用户昨日--投资信息统计 success----->>");
	}
	
	
	// TODO
	/** 平台-统计表和发行人-统计表分表 */
	@Scheduled(cron = "${cron.option[cron.mmp.platformAndPublisher_splittable]:0 0 0 * * ?}")
	public void splitPlatformAndPublisherStatisticsTable() {
		logger.info("<<-----平台-统计表和发行人-统计表分表 start----->>");
		try {
			// 昨日日期
			java.sql.Date splitdate = DateUtil.addSQLDays(DateUtil.getSqlDate(), -1);
			// 平台-统计表分表
			this.platformStatisticsHistoryService.splitTable(splitdate);
			// 发行人-统计表分表
			this.publisherStatisticsHistoryService.splitTable(splitdate);
		} catch (Throwable e) {
			logger.error("<<-----平台-统计表和发行人-统计表分表 fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----平台-统计表和发行人-统计表分表 success----->>");
	}

	/** 推广平台失败请求重发 */
	@Scheduled(cron = "${cron.option[cron.mmp.tulip_resend]:0 0/10 * * * ?}")
	public void tulipResend() {
		logger.info("<<-----推广平台失败请求重发 start----->>");
		try {

			this.tulipResendService.reSendTulipMessage();
		} catch (Throwable e) {
			logger.error("<<-----推广平台失败请求重发 fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----推广平台失败请求重发 success----->>");
	}
	
	/**
	 * 账户系统请求重发
	 */
//	@Scheduled(cron = "${cron.option[cron.mmp.acc_resend]:0 0/30 * * * ?}")
//	public void accResend() {
//		logger.info("<<-----账户系统失败请求重发 start----->>");
//		try {
//			accResendService.resend();
//		} catch (Throwable e) {
//			logger.error("<<-----账户系统失败请求重发 fail----->>");
//			e.printStackTrace();
//		}
//		logger.info("<<-----账户系统失败请求重发 success----->>");
//	}
	
	/**
	 * 结算系统请求重发
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.pay_resend]:0 0/30 * * * ?}")
	public void payResend() {
		logger.info("<<-----结算系统失败请求重发 start----->>");
		try {
			payResendService.resend();
		} catch (Throwable e) {
			logger.error("<<-----结算系统失败请求重发 fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----结算系统失败请求重发 success----->>");
	}
	
	
	
	@Scheduled(cron = "${cron.option[flat.exp.gold.task]:0 0/1 * * * ?}")
	public void flatExpGold() {
		logger.info("<<-----flatExpGold.task start----->>");
		try {
			investorTradeOrderService.flatExpGold();
		} catch (Exception e) {
			this.logger.error("flatExpGold.task fail", e);
			e.printStackTrace();
		}
		logger.info("<<-----flatExpGold.task end----->>");
	}
	
	/**
	 * 体验金异步投资
	 */
	@Scheduled(cron = "${cron.option[taskUseCoupon.task]:0 0/1 * * * ?}")
	public void taskUseCoupon() {
		logger.info("<<-----定时任务执行体验金投资 start----->>");
		try {
			taskCouponLogService.taskUseCoupon();
		} catch (Throwable e) {
			logger.error("<<-----定时任务执行体验金投资 fail----->>");
			e.printStackTrace();
		}
		logger.info("<<-----定时任务执行体验金投资 success----->>");
	}
	
	@Value("${ams.tradeCalendarSchedule:yes}")
	private String tradeCalendarSchedule;
	/**
	 * 同步交易日历
	 */
	@Scheduled(cron = "${cron.option[tradeCalendar.task]:0 0 21 * * ?}")
	public void taskTradeCalendar() {
		if (tradeCalendarSchedule!= null && tradeCalendarSchedule.equals("yes")){
			logger.info("<<-----定时任务执行同步交易日历 start----->>");
			try {
				tradeCalendarService.taskTradeCalendar();
			} catch (Throwable e) {
				logger.error("<<-----定时任务执行同步交易日历 fail----->>");
				e.printStackTrace();
			}
			logger.info("<<-----定时任务执行同步交易日历 success----->>");
		}else{
			logger.info("<<-----定时任务执行同步交易日历配置未启动----->>，ams.tradeCalendarSchedule="+tradeCalendarSchedule);
		}
	}
	
	/**
	 * 份额补充提醒
	 * 
	 */
	@Scheduled(cron = "${cron.option[scheduleShareSupplement.task]:0 0 9 * * ?}")
	public void scheduleShareSupplement() {
		logger.info("<<-----份额补充提醒扫描scheduleShareSupplement.task start----->>");
		try {
			this.productSchedService.scheduleShareSupplement();
		} catch (Exception e) {
			this.logger.error("份额补充提醒扫描scheduleShareSupplement.task fail", e);
			e.printStackTrace();
		}
		logger.info("<<-----份额补充提醒扫描scheduleShareSupplement.task end----->>");
	}
	
	/**
	 * 根据产品包上架产品
	 * 
	 */
	@Scheduled(cron = "${cron.option[cron.ams.createProductFromPackage]:0 0/2 * * * ?}")
	public void createProductFromPackage() {
		logger.info("<<-----根据产品包上架产品cron.ams.createProductFromPackage start----->>");
		try {
			this.productSchedService.createProductFromPackage();
		} catch (Exception e) {
			this.logger.error("根据产品包上架产品cron.ams.createProductFromPackage fail", e);
			e.printStackTrace();
		}
		logger.info("<<-----根据产品包上架产品cron.ams.createProductFromPackage end----->>");
	}

	/**
	 * 开放式循环产品200人日切【每日凌晨2点10分】
	 *
	 */
	@Scheduled(cron = "${cron.option[cron.ams.createProduct03]:0 10 2 * * ?}")
	public void createProduct03() {
		logger.info("<<-----开放式循环产品200人日切cron.ams.createProduct03 start----->>");
		try {
			this.productSchedService.createProduct03();
		} catch (Exception e) {
			this.logger.error("开放式循环产品200人日切cron.ams.createProductFromPackage fail", e);
			e.printStackTrace();
		}
		logger.info("<<-----开放式循环产品200人日切cron.ams.createProduct03 end----->>");
	}

	@Scheduled(cron = "${cron.option[cron.mmp.cycleProductDurationEnd]:0 30 2 * * ?}")
	public void cycleProductDurationEnd() {
		logger.info("<<-----循环开放产品到期处理cron.mmp.cycleProductDurationEnd start----->>");
		try {
			this.productSchedService.cycleProductDurationEnd();
		} catch (Exception e) {
			this.logger.error("循环开放产品到期处理cron.mmp.cycleProductDurationEnd failed", e);
			e.printStackTrace();
		}
		logger.info("<<-----循环开放产品到期处理cron.mmp.cycleProductDurationEnd end----->>");
	}
	
	/**
	 * 收益分配排期执行
	 * 
	 */
	@Scheduled(cron = "${cron.option[incomeDistributionSchedule.task]:0 10 0 * * ?}")
	public void incomeDistributionSchedule() {
		logger.info("<<-----收益分配排期执行incomeDistributionSchedule.task start----->>");
		try {
			this.incomeScheduleService.incomeSchedule();
		} catch (Exception e) {
			this.logger.error("收益分配排期执行incomeDistributionSchedule.task fail", e);
			e.printStackTrace();
		}
		logger.info("<<-----收益分配排期执行incomeDistributionSchedule.task end----->>");
	}
	
	/**
	 * 当天未设置自动收益分配排期，通知
	 * 
	 */
	@Scheduled(cron = "${cron.option[incomeDistributionNotice.task]:0 55 8 * * ?}")
	public void incomeDistributionNotice() {
		logger.info("<<-----活期收益未分配提醒执行incomeDistributionNotice.task start----->>");
		try {
			this.incomeScheduleService.noticeSchedule();
		} catch (Exception e) {
			this.logger.error("活期收益未分配执行incomeDistributionNotice.task fail", e);
			e.printStackTrace();
		}
		logger.info("<<-----活期收益未分配执行incomeDistributionNotice.task end----->>");
	}
	
	/**
	 * @author qiuliang
	 * 当天未审核自动收益分配排期，通知
	 */
	@Scheduled(cron = "${cron.option[incomeDistributionAuditNotice.task]:0 0 14 * * ?}")
	public void incomeDistributionAuditNotice() {
		logger.info("<<-----活期收益未审核提醒执行incomeDistributionAuditNotice.task start----->>");
		try {
			this.incomeScheduleService.noticeAuditSchedule();
		} catch (Exception e) {
			this.logger.error("活期收益未审核执行incomeDistributionAuditNotice.task fail", e);
			e.printStackTrace();
		}
		logger.info("<<-----活期收益未审核执行incomeDistributionAuditNotice.task end----->>");
	}
	/**
	 * @author qiuliang
	 * 快活宝，天天向上份额不足，通知
	 */
	@Scheduled(cron = "${cron.option[t0ProductVolumeInsufficientNotice.task]:0 05 14 * * ?}")
	public void t0ProductVolumeInsufficientNotice() {
		logger.info("<<-----快活宝，天天向上份额不足提醒执行t0ProductVolumeInsufficientNotice.task start----->>");
		try {
			this.productSchedService.t0ProductVolumeInsufficientNotice();
		} catch (Exception e) {
			this.logger.error("快活宝，天天向上份额不足提醒执行t0ProductVolumeInsufficientNotice.task fail", e);
			e.printStackTrace();
		}
		logger.info("<<-----快活宝，天天向上份额不足提醒执行t0ProductVolumeInsufficientNotice.task end----->>");
	}
	
	/**
	 * 筛选出第二天待执行的扣款计划
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.familySnapShot]:0 0 22 * * ?}")
	public void familySnapShot() {
		logger.info("<<-----理财计划待执行筛选 start---->>");
		try {
		    // 快定宝一期暂时停止
//			familyInvestPlanService.familySnapShot();
		} catch (Throwable e) {

			e.printStackTrace();
		}
		logger.info("<<-----理财计划待执行筛选 end---->>");
	}
 
	/**
	 * 自动扣款购买理财协议
	 */
	@Scheduled(cron = "${cron.option[cron.mmp.familyPlanAutoInvest]:0 0 8,10,14 * * ?}")
	public void familyPlanAutoInvest() {
		logger.info("<<-----理财计划自动扣款start---->>");
		try {
            // 快定宝一期暂时停止
//			familyInvestPlanService.familyPlanAutoInvest();
		} catch (Throwable e) {

			e.printStackTrace();
		}
		logger.info("<<-----理财计划自动扣款 end---->>");
	}

	/**
	 * 快定宝赎回定时任务
	 */
	@Scheduled(cron = "${cron.option[cron.ams.bfPlusRedeem]:0 0 3 * * ?}")
	public void bfPlusRedeem() {
		String cycleprocuctname = DealMessageEnum.CYCLEPROCUCTNAME;
		logger.info("<<-----"+cycleprocuctname+"赎回定时任务start---->>");
		try {
			this.bfPlusRedeemScheduleService.bfPlusRedeem();
		} catch (Throwable e) {
			logger.error("", e);
		}
		logger.info("<<-----"+cycleprocuctname+"赎回定时任务end---->>");
	}
}
