package com.guohuai.mmp.job.log;

import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.IPUtil;

public class JobLogFactory {
	
	public static JobLogEntity getInstance(String jobId) {
		JobLogEntity jobLog = new JobLogEntity();
		jobLog.setJobId(jobId);
		jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_success);
		jobLog.setBatchStartTime(DateUtil.getSqlCurrentDate());
		jobLog.setMachineIp(IPUtil.getLocalIP());
		return jobLog;
	}
}
