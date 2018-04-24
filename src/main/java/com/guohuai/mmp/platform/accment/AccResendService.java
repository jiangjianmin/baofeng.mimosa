package com.guohuai.mmp.platform.accment;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.platform.accment.log.AccLogEntity;
import com.guohuai.mmp.platform.accment.log.AccLogService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class AccResendService {

	@Autowired
	private AccLogService accLogService;
	@Autowired
	private AccResendServiceRequireNew accResendServiceRequireNew;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private JobLogService jobLogService;
	
	public void resend() {
		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_accResend)) {
			resendLog();
		}
	}
	
	public void resendLog() {
		JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_accResend);
		try {
			resendDo();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_accResend);
	}

	public void resendDo() throws Exception {

		String lastOid = "0";
		while (true) {

			List<AccLogEntity> entities = accLogService.getResendEntities(lastOid);
			if (entities.isEmpty()) {
				break;
			}
			lastOid = accResendServiceRequireNew.requireNew(lastOid, entities);
		}

	}

	
}
