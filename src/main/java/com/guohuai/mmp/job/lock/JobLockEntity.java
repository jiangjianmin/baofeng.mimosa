package com.guohuai.mmp.job.lock;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_JOB_LOCK")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@lombok.Builder
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class JobLockEntity extends UUID {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5109160803296992814L;

	
	public static final String JOB_jobStatus_toRun = "toRun";
	public static final String JOB_jobStatus_processing = "processing";
	
	
	public static final String JOB_jobId_snapshot = "cron.mmp.snapshot";
	public static final String JOB_jobId_practice = "cron.mmp.practice";
	public static final String JOB_jobId_createAllNew = "cron.mmp.publiser_offset";
	public static final String JOB_jobId_createBfPlusOffset = "cron.mmp.bfplus_offset";
	public static final String JOB_jonId_createBankNew = "cron.mmp.bank_offset";
	public static final String JOB_jobId_unlockRedeem = "cron.mmp.unlock_redeem";
	public static final String JOB_jobId_unlockAccrual = "cron.mmp.unlock_accrual";
	public static final String JOB_jobId_resetToday = "cron.mmp.reset_today";
	public static final String JOB_jobId_interestTnRaise = "cron.mmp.interest_tn";
	public static final String JOB_jobId_profitT0 = "cron.mmp.profit_t0";
	public static final String JOB_jobId_provideProfit = "cron.mmp.provide_profit";
	public static final String JOB_jobId_profitRank = "cron.mmp.profit_rank";
	public static final String JOB_jobId_overdueTimes = "cron.mmp.overdue_times";
	public static final String JOB_jobId_uploadPDF = "cron.mmp.upload_pdf";
	public static final String JOB_jobId_createHtml = "cron.mmp.create_html";
	public static final String JOB_jobId_BankStatement = "cron.mmp.bank_statement";
	public static final String JOB_jobId_calcPoolProfitSchedule = "cron.ams.calcPoolProfitSchedule";			// 计算资产池当日收益
	public static final String JOB_jobId_updateStateSchedule = "cron.ams.updateStateSchedule";				// 重置资产池收益计算和收益分配的状态
	public static final String JOB_jobId_updateLifeStateSchedule = "cron.ams.updateLifeStateSchedule";	// 改变标的状态（存续期-->兑付期）
	public static final String JOB_jobId_scheduleProductState = "cron.mmp.product_state";//当<<募集开始日期>>到,募集未开始变为募集中;
	public static final String JOB_jobId_scheduleSendProductMaxSaleVolume = "cron.mmp.product_maxSaleVolume";//可售份额排期发放
	public static final String JOB_jobId_scheduleProductDailyMaxRredeem = "cron.mmp.product_dailyMaxRredeem";//剩余赎回金额每日还原 :dailyNetMaxRredeem 重置为 netMaxRredeemDay
	public static final String JOB_jobId_publishersProductInvestorTop5 = "cron.mmp.publisher_investTop5Product";
	public static final String JOB_jobId_clear = "cron.mmp.clear_order"; // 清盘赎回
	public static final String JOB_jobId_statChannelYesterdayInvestInfo = "cron.mmp.platform_channelinveststat"; 
	public static final String JOB_jobId_statUserYesterdayInvestInfo = "cron.mmp.user_investInfo"; 
	public static final String JOB_jobId_tulipResend = "cron.mmp.tulip_resend";
	public static final String JOB_jobId_accResend = "cron.mmp.acc_resend";
	public static final String JOB_jobId_payResend = "cron.mmp.pay_resend";
	public static final String JOB_jobId_taskUseCoupon = "taskUseCoupon.task"; //体验金异步投资
	public static final String JOB_jobId_scheduleBatchBack = "scheduleBatchBack.task";
	public static final String JOB_jobId_scheduleInsertCompareCheck = "scheduleInsertCompareCheck.task";
	public static final String JOB_jobId_opeschedule = "opeschedule.task"; // 运营查询JOB_jobId_portfolioEstimate
	public static final String JOB_jobId_familySnapShot = "cron.mmp.familySnapShot";//自动理财计划快照
	public static final String JOB_jobId_familyPlanAutoInvest = "cron.mmp.familyPlanAutoInvest";//自动扣款
	
	public static final String JOB_jobId_portfolioEstimate = "portfolio.estimate.task"; // 投资组合每日估值
	
	public static final String JOB_jobId_illiquidStateUpdate = "illiquid.state.update.task"; // 非现金类标的, 状态更新
	
	public static final String JOB_jobId_serital = "serital.task"; // 序列化任务
	
	public static final String JOB_jobId_resetMonth = "cron.mmp.reset_month"; // 投资者提现次数月统计数据重置
	
	public static final String JOB_jobId_cancel = "cancel.task"; // 取消超时序列化任务
		
	public static final String JOB_jobId_illiquidRepaymentStateUpdate = "illiquid.repayment.state.update.task"; // 非现金类标的还款计划, 状态更新
	
	public static final String JOB_jobId_cronCheck = "cron.check.task"; // 平台余额对账
	
	public static final String JOB_jobId_tradeCalendar = "tradeCalendar.task"; // 同步交易日历
	
	public static final String JOB_jobId_scheduleShareSupplement = "scheduleShareSupplement.task";	// 产品补充份额定时通知
	
	public static final String JOB_jobId_createProductFromPackage = "cron.ams.createProductFromPackage";	// 根据产品包上架产品

	public static final String JOB_jobId_createProduct03 = "cron.ams.createProduct03";	// 200人日切上架循环产品子产品
	public static final String JOB_jobId_bfPlusRedeem = "cron.ams.bfPlusRedeem";	// 快定宝赎回定时任务

	public static final String JOB_jobId_incomeDistributionSchedule = "incomeDistributionSchedule.task";	// 收益分配排期执行
	public static final String JOB_jobId_incomeDistributionNotice = "incomeDistributionNotice.task";	// 未收益分配排期通知执行


	public static final String JOB_jobId_incomeDistributionAuditNotice = "incomeDistributionAuditNotice.task";//未收益审核排期通知执行


	public static final String JOB_jobId_t0ProductVolumeInsufficientNotice = "t0ProductVolumeInsufficientNotice.task";//活期份额不足提醒

	// 循环产品到期处理
	public static final String JOB_jobId_cycleProductDurationEnd = "cron.mmp.cycleProductDurationEnd";

	String jobId;
	String jobTime;
	String jobStatus;
	
	private Timestamp createTime;
	private Timestamp updateTime;

}
