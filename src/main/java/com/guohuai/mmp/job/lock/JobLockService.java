package com.guohuai.mmp.job.lock;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.guohuai.ams.product.ProductSchedService;
import com.guohuai.cache.service.RedisExecuteLogExtService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.IPUtil;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.mmp.investor.tradeorder.InvestorClearTradeOrderService;
import com.guohuai.mmp.job.JobEnum;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.platform.accment.AccResendService;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetService;
import com.guohuai.mmp.platform.tulip.TulipResendService;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.publisher.holdapart.snapshot.SnapshotService;
import com.guohuai.mmp.publisher.investor.InterestTnRaise;
import com.guohuai.mmp.publisher.product.agreement.ProductAgreementService;
import com.guohuai.mmp.publisher.product.rewardincomepractice.PracticeService;
import com.guohuai.mmp.publisher.product.statistics.PublisherProductStatisticsService;
import com.guohuai.mmp.schedule.OverdueTimesService;
import com.guohuai.mmp.schedule.ResetTodayService;
import com.guohuai.mmp.serialtask.SerialTaskService;
import lombok.extern.slf4j.Slf4j;


@Service
@Transactional
@Slf4j
public class JobLockService {
	private Logger logger = LoggerFactory.getLogger(JobLockService.class);
	
	public static String needSchedule;
	@Autowired
	private JobLogService jobLogService;
	@Autowired
	private PublisherOffsetService publisherOffsetService;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private PracticeService practiceService;
	@Autowired
	private ProductSchedService productSchedService;
	@Autowired
	private InterestTnRaise interestTnRaise;
	@Autowired
	private ResetTodayService resetTodayService;
	/*@Autowired
	private ResetMonthService resetMonthService;*/
	@Autowired
	private PublisherProductStatisticsService publisherProductStatisticsService;
	@Autowired
	private OverdueTimesService overdueTimesService;
	@Autowired
	private InvestorClearTradeOrderService investorClearTradeOrderService;
	@Autowired
	private ProductAgreementService productAgreementService;
	@Autowired
	private TulipResendService tulipResendService;
	@Autowired
	private SerialTaskService serialTaskService;
	// @Autowired
	// private CouponCashDetailsService couponCashDetailsService;
	@Autowired
	private SnapshotService snapshotService;
	@Autowired
	private AccResendService accResendService;
	@Autowired
	private RedisExecuteLogExtService redisExecuteLogExtService;
	
	public static final String JOB_needSchedule_no = "no";
	
	@Autowired
	private JobLockDao jobLockDao;
	@Value("${ams.needSchedule:}")
	public void setNeedSchedule(String v){
		needSchedule=v;
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public JobLockEntity save(JobLockEntity jobLock) {
		return this.jobLockDao.save(jobLock);
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public boolean getRunPrivilege(String jobId) {
		if (JOB_needSchedule_no.equals(needSchedule)) {
			log.info("ip={},此机器 不需要执行定时任务", IPUtil.getLocalIP());
			return false;
		}
		
		int i = this.jobLockDao.updateStatus4Lock(jobId);
		if (i < 1) {
			log.info("ip={},jobId={}, getRunPrivilege failed", IPUtil.getLocalIP(), jobId);
			return false;
		} else {
			log.info("ip={},jobId={}, getRunPrivilege success", IPUtil.getLocalIP(), jobId);
			boolean isTimesOk = checkRunTimes(jobId);
			if (!isTimesOk) {
				this.resetJob(jobId);
			}
			return isTimesOk;
		}
	}
	
	public boolean checkRunTimes(String jobId) {
		int runTimes = JobEnum.getRunTimesByJobId(jobId);
		if (runTimes == -1) {
			
		} else {
			Timestamp date = new Timestamp(System.currentTimeMillis());
			String begin = DateUtil.getDaySysBeginTime(date);
			String end = DateUtil.getDaySysEndTime(date);
			log.info("jobId={},begin={},end={}", jobId, begin, end);
			int runedTimes = this.jobLogService.queryRunedTimes(jobId, begin, end);
			if (runedTimes >= runTimes) {
				log.info("{}:已超过运行次数", jobId);
				return false;
			}
		}
		return true;
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void resetJob(String jobId) {
		int i = this.jobLockDao.resetJob(jobId);
		if (i < 1) {
			log.info("jobId={}, resetJob failed", jobId);
			throw new AMPException(jobId + " resetJob failed");
		} else {
			log.info("jobId={}, resetJob success", jobId);
		}
	}
	
	public JobLockEntity findByJobId(String jobId) {
		return this.jobLockDao.findByJobId(jobId);
	}


	public void batchUpdate(List<JobLockEntity> entities) {
		this.jobLockDao.save(entities);
	}
	
	public List<JobLockEntity> findAll() {
		return this.jobLockDao.findAll();
	}
	
	public PageResp<JobLockEntityResp> findAll(int page,int rows) {
		if (page < 1) {
			page = 1;
		}
		if (rows < 1) {
			rows = 1;
		}
		
		Pageable pageable = new PageRequest(page - 1, rows);
		
		PageResp<JobLockEntityResp> pagesRep = new PageResp<JobLockEntityResp>();
		Page<JobLockEntity> cas = jobLockDao.findAll(pageable);
	
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			List<JobLockEntityResp> rowss = new ArrayList<JobLockEntityResp>();
			for (JobLockEntity j : cas) {
				JobLockEntityResp queryRep = new JobLockEntityResp(j);
				if("toRun".equals(j.jobStatus)){
					queryRep.setCheckRunTimes(this.checkRunTimes(j.getJobId()));
				}else{
					queryRep.setCheckRunTimes(false);
				}
				rowss.add(queryRep);
			}
			pagesRep.setRows(rowss);
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}
	
	public void snapshot(Date inDate,String jobId) {
		logger.info("<<-----start----->>");
		switch (jobId) {
				case JobLockEntity.JOB_jobId_snapshot:
					 snapshotService.snapshot();
					 break;
				case JobLockEntity.JOB_jobId_practice:
					 practiceService.practice();
					 break;
				case JobLockEntity.JOB_jobId_createAllNew:
					 publisherOffsetService.createAllNew();
					 break;
				case JobLockEntity.JOB_jobId_unlockRedeem:
					 publisherHoldService.unlockRedeem();
					 break;
				case JobLockEntity.JOB_jobId_unlockAccrual:
					 publisherHoldService.unlockAccrual();
					 break;	 
				case JobLockEntity.JOB_jobId_resetToday:
					 resetTodayService.resetToday();
					 break;
				case JobLockEntity.JOB_jobId_interestTnRaise:
					 interestTnRaise.interestTnRaise();
					 break;
				case JobLockEntity.JOB_jobId_overdueTimes:
					overdueTimesService.overdueTimes();
					 break;
				case JobLockEntity.JOB_jobId_scheduleProductState:
					productSchedService.notstartraiseToRaisingOrRaised();
					break;	
				case JobLockEntity.JOB_jobId_scheduleSendProductMaxSaleVolume:
					productSchedService.scheduleSendProductMaxSaleVolume();
					 break;	 
				case JobLockEntity.JOB_jobId_scheduleProductDailyMaxRredeem:
					productSchedService.scheduleProductDailyMaxRredeem();
					 break;
				case JobLockEntity.JOB_jobId_publishersProductInvestorTop5:
					publisherProductStatisticsService.statPublishersProductInvestInfoByDate();
					 break;
				case JobLockEntity.JOB_jobId_clear:
					investorClearTradeOrderService.clear();
					 break;
				case JobLockEntity.JOB_jobId_tulipResend:
					this.tulipResendService.reSendTulipMessage();
					break;
				case JobLockEntity.JOB_jobId_accResend:
					accResendService.resend();
					break;
				/*case JobLockEntity.JOB_jobId_scheduleBatchBack:
					this.redisExecuteLogExtService.scheduleBatchBack();
					break;
				case JobLockEntity.JOB_jobId_opeschedule:
					this.opeScheduleService.scheduler();
					break;	
				case JobLockEntity.JOB_jobId_portfolioEstimate:
					this.portfolioEstimateService.batchEstimate();
					break;
				case JobLockEntity.JOB_jobId_illiquidStateUpdate:
					this.illiquidAssetUpdateStateScheduleService.updateState(new java.sql.Date(System.currentTimeMillis()));
					break;*/
				case JobLockEntity.JOB_jobId_serital:
					this.serialTaskService.executeTask();
					break;
				/*case JobLockEntity.JOB_jobId_resetMonth:
					final Calendar c = Calendar.getInstance();
					if (c.get(Calendar.DATE) == c.getActualMaximum(Calendar.DATE)) {
						this.resetMonthService.resetMonth();
					}
					break;*/
				case JobLockEntity.JOB_jobId_cancel:
					this.serialTaskService.resetTimeoutTask();
					break;
				/*case JobLockEntity.JOB_jobId_illiquidRepaymentStateUpdate:
					this.portfolioIlliquidHoldRepaymentScheduleService.updateState(new java.sql.Date(System.currentTimeMillis()));
					break;*/
				/*case JobLockEntity.JOB_jobId_cronCheck:
					String currentCheckTime = DateUtil.getCurrStrDate();
					String checkTimeEnd = DateUtil.afterDate(currentCheckTime);
					this.checkService.generateCheckOrders(currentCheckTime, checkTimeEnd);
					this.detailCheckService.generateDetailCheck(checkTimeEnd);
					break;	*/		
		}
		logger.info("<<-----end----->>");
				
	}
//	
//	@Transactional(value = TxType.REQUIRES_NEW)
//	public JobLockEntity findByBatchCodeAndJobId(String batchCode, String jobId) {
//		if ("no".equals(needSchedule)) {
//			throw new AMPException("不需要执行定时任务");
//		}
//		JobLockEntity lock = this.jobLockDao.findByBatchCodeAndJobId(batchCode, jobId);
//		if (null == lock) {
//			lock = new JobLockEntity();
//			lock.setBatchCode(batchCode);
//			lock.setJobId(jobId);
//			lock.setJobStatus(JobLockEntity.JOB_jobStatus_processing);
//			lock.setBatchStartTime(DateUtil.getSqlCurrentDate());
//			this.jobLockDao.save(lock);
//		} else {
//			if (!JobLockEntity.JOB_jobStatus_fail.equals(lock.getJobStatus())) {
//				throw new AMPException("任务非失败状态");
//			} else {
//				int i = this.jobLockDao.updateStatus4Lock(lock.getOid(), JobLockEntity.JOB_jobStatus_processing);
//				if (i < 1) {
//					throw new AMPException("任务已被锁定");
//				}
//			}
//		}
//		 
//		return lock;
//	}

//	public void isSnapshotVolume(String batchCode, String jobId) {
//		JobLockEntity lock = this.jobLockDao.findByBatchCodeAndJobId(batchCode, jobId);
//		if (null == lock) {
//			throw new RuntimeException("计息快照没尚未完成");
//		}
//		if (!lock.getJobStatus().equals(JobLockEntity.JOB_jobStatus_done)) {
//			throw new RuntimeException("计息快照没尚未完成");
//		}
//		
//	}

}
