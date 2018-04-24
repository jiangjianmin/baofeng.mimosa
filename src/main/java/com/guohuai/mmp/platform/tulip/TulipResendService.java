package com.guohuai.mmp.platform.tulip;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.platform.tulip.log.TulipLogEntity;
import com.guohuai.mmp.platform.tulip.log.TulipLogService;
import com.guohuai.tuip.api.TulipSdk;

/**
 * 推广平台-重新发送失败的请求
 * 
 */
@Service
public class TulipResendService {

	Logger logger = LoggerFactory.getLogger(TulipResendService.class);
	@Autowired
	private TulipLogService tulipLogService;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private JobLogService jobLogService;
	@Autowired
	private TulipSdk tulipSdk;
	
	
	public void reSendTulipMessage() {
		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_tulipResend)) {
			reSendTulipMessageLog();
		}
	}
	
	public void reSendTulipMessageLog() {
		JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_tulipResend);
		try {
			
			reSendTulipMessageDo();
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_tulipResend);
	}
	
	
	/** 将发送推广平台失败的请求重新发送 */
	public void reSendTulipMessageDo() {

		
		String lastOid = "0";
		while (true) {
			List<TulipLogEntity> list = tulipLogService.getResendEntities(lastOid);
			if (list.isEmpty()) {
				break;
			}
			BaseRep rep=new BaseRep();
			for (TulipLogEntity en : list) {
				for (TulipLogEntity.TULIP_TYPE type : TulipLogEntity.TULIP_TYPE.values()) {
					if (type.getInterfaceCode().equals(en.getInterfaceCode())) {
						try {
							Object obj = this.tulipSdk.getClass().getMethod(type.getInterfaceCode(), Class.forName(type.getIfaceReq()))
									.invoke(this.tulipSdk,JSONObject.parseObject(en.getSendObj(), Class.forName(type.getIfaceReq()).newInstance().getClass()));
						} catch (Exception e) {
							e.printStackTrace();
						} 
					}
				}
				en.setErrorCode(rep.getErrorCode());
				en.setErrorMessage(rep.getErrorMessage());
				en.setSendedTimes(en.getSendedTimes() + 1);
				en.setNextNotifyTime(this.tulipLogService.getNextNotifyTime(en));
				lastOid = en.getOid();
			}
			tulipLogService.batchUpdate(list);
		}

	}

	


}
