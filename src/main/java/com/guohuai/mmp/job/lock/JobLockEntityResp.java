package com.guohuai.mmp.job.lock;

import java.io.Serializable;
import java.sql.Timestamp;
import lombok.Data;


/**
 * 
 * @author zhangwei
 *
 */
@Data
public class JobLockEntityResp implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	String jobId;
	String jobTime;
	String jobStatus;

	private Timestamp createTime;
	private Timestamp updateTime;
	
	private boolean checkRunTimes;
	
	public JobLockEntityResp(JobLockEntity jobLockEntity) {
		this.jobId = jobLockEntity.jobId;
		this.jobTime = jobLockEntity.jobTime;
		this.jobStatus = jobLockEntity.jobStatus;
		this.createTime = jobLockEntity.getCreateTime();
		this.updateTime = jobLockEntity.getUpdateTime();
	}

}
