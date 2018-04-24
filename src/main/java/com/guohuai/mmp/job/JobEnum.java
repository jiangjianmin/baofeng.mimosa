package com.guohuai.mmp.job;

public enum JobEnum {
	JOB_jobId_snapshot("cron.mmp.snapshot", 1, true), // 计息快照
	JOB_jobId_practice("cron.mmp.practice", 1, true), // 收益试算
	JOB_jobId_createAllNew("cron.mmp.publiser_offset", 1, true), //创建新轧差批次
	JOB_jobId_createBfPlusOffset("cron.mmp.bfplus_offset", 1, true), //创建快定宝新轧差批次
	JOB_jobId_bfPlusRedeem("cron.ams.bfPlusRedeem", 1, true), //快定宝赎回定时任务。T+1
	JOB_jobId_unlockRedeem("cron.mmp.unlock_redeem", 1, true), //解锁赎回份额
	JOB_jobId_unlockAccrual("cron.mmp.unlock_accrual", 1, true), //解锁计息份额
	JOB_jobId_resetToday("cron.mmp.reset_today", 1, true), //每日重置
	JOB_jobId_interestTnRaise("cron.mmp.interest_tn", 1, true), //定期募集期收益自动发放
	JOB_jobId_profitT0("cron.mmp.profit_t0", 1, true), // 每月一号汇总上月活期奖励收益明细
	JOB_jobId_provideProfit("cron.mmp.provide_profit", 1, true), // 每月15号对上月二级邀请奖励进行发放
	JOB_jobId_profitRank("cron.mmp.profit_rank", 1, true), // 每日凌晨3点30对二级邀请龙虎榜进行统计
	JOB_jobId_overdueTimes("cron.mmp.overdue_times", 1, true), //逾期统计
	JOB_jobId_uploadPDF("cron.mmp.upload_pdf", 1, true), //生产PDF
	JOB_jobId_createHtml("cron.mmp.create_html", 1, true), //生成HTML
	JOB_jobId_calcPoolProfitSchedule("cron.ams.calcPoolProfitSchedule", 1, true), //计算资产池当日的确认收益
	JOB_jobId_updateStateSchedule("cron.ams.updateStateSchedule", 1, true), //定时初始化资产池的每日收益计算和收益分配的状态
	JOB_jobId_updateLifeStateSchedule("cron.ams.updateLifeStateSchedule", 1, true), // 定时任务 - 更新投资标的生命周期状态 存续期（STAND_UP） --> 兑付期（PAY_BACK)
	/**
	 * 活期: 当<<成立开始日期>>到,募集未开始变为募集中; 定期: 当<<募集开始日期>>到,募集未开始变为募集中; 定期:
	 * 当<<募集結束日期>>到或募集满额,募集中变为募集結束; 定期: 当<<存续期结束日期>>到,存续期变为存续期結束; 定期:
	 * 当募集满额后自动触发成立 募集满额后的第X个自然日后自动成立
	 */
	JOB_jobId_scheduleProductState("cron.mmp.product_state", 1, true), 
	JOB_jobId_scheduleSendProductMaxSaleVolume("cron.mmp.product_maxSaleVolume", 1, true), //可售份额排期发放
	JOB_jobId_scheduleProductDailyMaxRredeem("cron.mmp.product_dailyMaxRredeem", 1, true), //剩余赎回金额每日还原;
	JOB_jobId_publishersProductInvestorTop5("cron.mmp.publisher_investTop5Product", 1, true), //各发行人的投资额TOP5产品排名统计
	JOB_jobId_clear("cron.mmp.clear_order", -1, false), //生成清盘赎回单
	JOB_jobId_statChannelYesterdayInvestInfo("cron.mmp.platform_channelinveststat", 1, true), //平台各渠道昨日投资信息统计
	JOB_jobId_statUserYesterdayInvestInfo("cron.mmp.user_investInfo", 1, true), 
	JOB_jobId_tulipResend("cron.mmp.tulip_resend", -1, false), //推广平台失败请求重发
	JOB_jobId_accResend("cron.mmp.acc_resend", -1, false), //账户系统请求重发
	JOB_jobId_payResend("cron.mmp.pay_resend", -1, false), //结算系统请求重发
	JOB_jobId_taskUseCoupon("taskUseCoupon.task", -1, false), //体验金异步投资
	JOB_jobId_flatExpGold("flat.exp.gold.task", 1, true), //体验金自动赎回
	JOB_jobId_scheduleBatchBack("scheduleBatchBack.task", -1, false), //redis回滚定时任务
	JOB_jobId_scheduleInsertCompareCheck("scheduleInsertCompareCheck.task", 1, true), // 定时生成对账批次
	JOB_jobId_opeschedule("opeschedule.task", -1, false), // 运营查询扫描任务
	JOB_jobId_updateIlliquidState("illiquid.state.update.task", -1, false), //非现金类标的, 状态更新
	JOB_jobId_updateIlliquidRepaymentState("illiquid.repayment.state.update.task", -1, false), //非现金类标的, 状态更新
	JOB_jobId_portfolioEstimate("portfolio.estimate.task", -1, false), //投资组合每日估值
	JOB_jobId_tradeCalendar("tradeCalendar.task", -1, false), //同步交易日历
	JOB_jobId_scheduleShareSupplement("scheduleShareSupplement.task", -1, false), // 产品补充份额定时通知
	JOB_jobId_createProductFromPackage("cron.ams.createProductFromPackage", -1, false),	// 根据产品包上架产品
	JOB_jobId_incomeDistributionSchedule("incomeDistributionSchedule.task", 1, false),	// 收益分配排期执行
	JOB_jobId_incomeDistributionNotice("incomeDistributionNotice.task", -1, false);	// 未收益分配排期通知执行

	private String jobId;
	private int runTimes;
	private boolean isNeedSendMessage;
	
	public static int getRunTimesByJobId(String jobId) {
		for (JobEnum tmp : JobEnum.values()) {
			if (tmp.getJobId().equals(jobId)) {
				return tmp.getRunTimes();
			}
		}
		return -1;
	}
	
	public static boolean isNeedSendMessage(String jobId) {
		for (JobEnum tmp : JobEnum.values()) {
			if (tmp.getJobId().equals(jobId)) {
				return tmp.isNeedSendMessage();
			}
		}
		return false;
	}
	
	
	private JobEnum(String jobId, int runTimes, boolean isNeedSendMessage) {
		this.jobId = jobId;
		this.runTimes = runTimes;
		this.isNeedSendMessage = isNeedSendMessage;
	}
	
	public boolean isNeedSendMessage() {
		return isNeedSendMessage;
	}
	
	public String getJobId() {
		return jobId;
	}
	public int getRunTimes() {
		return runTimes;
	}
}
