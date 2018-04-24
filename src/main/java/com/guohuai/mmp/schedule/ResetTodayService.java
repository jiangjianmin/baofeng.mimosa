package com.guohuai.mmp.schedule;

import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.investor.baseaccount.statistics.InvestorStatisticsEntity;
import com.guohuai.mmp.investor.baseaccount.statistics.InvestorStatisticsService;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsService;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.serialtask.SerialTaskEntity;
import com.guohuai.mmp.serialtask.SerialTaskReq;
import com.guohuai.mmp.serialtask.SerialTaskService;

/**
 * 
 * @author yuechao
 *
 */
@Service
@Transactional
public class ResetTodayService {

	private static final Logger logger = LoggerFactory.getLogger(ResetTodayService.class);
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private InvestorStatisticsService investorStatisticsService;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private PublisherStatisticsService publisherStatisticsService;
	@Autowired
	private JobLogService jobLogService;
	@Autowired
	private SerialTaskService serialTaskService;
	
	public void resetToday() {
		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_resetToday)) {
			this.resetTodayLog();
		}
	}
	
	public void resetTodayLog() {
		JobLogEntity jobLog =  JobLogFactory.getInstance(JobLockEntity.JOB_jobId_resetToday);
		try {
			resetTodayDo();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_resetToday);
	}

	public void resetTodayDo() {
		SerialTaskReq<String> req = new SerialTaskReq<String>();
		req.setTaskCode(SerialTaskEntity.TASK_taskCode_resetToday);
		serialTaskService.createSerialTask(req);
	}
	
	public void resetTodayDb() {
		/** 重围投资者今日持仓数据 */
		this.resetPublisherHoldToday();
		/** 重置投资者今日统计数据*/
		this.resetInvestorStatisticsToday();
		/** 重置发行人每日统计数据 */
		publisherStatisticsService.resetToday();
	}
	
	/** 重置投资者今日持仓数据 **/
	@Transactional(value = TxType.REQUIRES_NEW)
	public void resetPublisherHoldToday(){
		logger.info("=====resetPublisherHoldToday begin=====");
		String lastOid = "0";
		int group = 0;
		while (true) {
			List<PublisherHoldEntity> holds = this.publisherHoldService.getResetTodayHold(lastOid);
			if (holds.isEmpty()) {
				break;
			}
			for (PublisherHoldEntity hold : holds) {
				logger.info("投资人" + hold.getInvestorBaseAccount().getOid() + "的持仓数据");
				publisherHoldService.resetToday(hold.getOid());
				lastOid = hold.getOid();
			}
			group += group;
			logger.info("PublisherHold第" + group + "组的lastOid=" + lastOid);
			try {
				Thread.currentThread().sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		logger.info("=====resetPublisherHoldToday end=====");
	}
	
	/** 重置投资者今日统计数据据 **/
	@Transactional(value = TxType.REQUIRES_NEW)
	public void resetInvestorStatisticsToday(){
		logger.info("=====resetInvestorStatisticsToday begin=====");
		String lastOid = "0";
		int group = 0;
		while (true) {
			List<InvestorStatisticsEntity> investorStatistics = this.investorStatisticsService.getResetTodayInvestorStatistics(lastOid);
			if (investorStatistics.isEmpty()) {
				break;
			}
			for (InvestorStatisticsEntity investorStatistic : investorStatistics) {
				logger.info("重置投资人：" + investorStatistic.getInvestorBaseAccount().getOid() + "的统计数据");
				investorStatisticsService.resetToday(investorStatistic.getOid());
				lastOid = investorStatistic.getOid();
			}
			logger.info("InvestorStatistics第" + group + "组的lastOid=" + lastOid);
			try {
				Thread.currentThread().sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		logger.info("=====resetInvestorStatisticsToday end=====");
	}
}
