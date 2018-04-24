package com.guohuai.mmp.investor.baseaccount.log;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.tradeorder.InvestorInvestTradeOrderExtService;
import com.guohuai.mmp.investor.tradeorder.TradeOrderRep;
import com.guohuai.mmp.investor.tradeorder.TradeOrderReq;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class TaskCouponLogService {
	@Autowired
	private CouponLogService couponLogService;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private JobLogService jobLogService;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private InvestorInvestTradeOrderExtService investorInvestTradeOrderExtService;
	
	public void taskUseCoupon() {

		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_taskUseCoupon)) {
			taskUseCouponLog();
		}
	}
	
	public void taskUseCouponLog() {
		JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_taskUseCoupon);
		try {
			taskUseCouponDo();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_taskUseCoupon);
	}
	
	@Transactional
	public void taskUseCouponDo(){
			List<CouponLogEntity> entites = couponLogService.getCouponLogEntity();
			
			for (CouponLogEntity entity : entites) {
				log.info("体验金userOid={} start", entity.getUserOid());
				String status = CouponLogEntity.STATUS_SUCCESS;
				try {
					TradeOrderReq tradeOrderReq = null;
					//使用体验金投资
					tradeOrderReq = investorBaseAccountService.useTastecoupon(entity.getUserOid());
					tradeOrderReq.setCid(entity.getChannelCid());// 渠道cid
					
					if (null != tradeOrderReq) {
						TradeOrderRep tradeOrderRep = this.investorInvestTradeOrderExtService.expGoldInvest(tradeOrderReq);
						if (BaseRep.ERROR_CODE == tradeOrderRep.getErrorCode()) {
							status = CouponLogEntity.STATUS_FAILED;
						}
					} else {
						status = CouponLogEntity.STATUS_FAILED;
					}
					
				} catch (Exception e) {
					status = CouponLogEntity.STATUS_FAILED;
					e.printStackTrace();
				}
				log.info("体验金userOid={} end", entity.getUserOid());
				
				entity.setSendedTimes(entity.getSendedTimes() + 1);
				entity.setNextNotifyTime(this.couponLogService.getNextNotifyTime(entity));
				entity.setStatus(status);
			}
			this.couponLogService.batchUpdate(entites);
	}

	
}
