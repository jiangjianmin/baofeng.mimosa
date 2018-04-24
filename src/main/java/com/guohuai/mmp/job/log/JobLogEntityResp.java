package com.guohuai.mmp.job.log;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 
 * @author zhangwei
 *
 */

@lombok.Data
public class JobLogEntityResp implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobId;
	private String jobStatus;
	/**
	 * 本批次起始时间
	 */
	private Timestamp batchStartTime;
	/**
	 * 批次结束时间
	 */
	private Timestamp batchEndTime;

	private String jobMessage;
	private String machineIp;
	
	private Timestamp createTime;
	private Timestamp updateTime;
	public JobLogEntityResp(JobLogEntity jobLogEntity) {
		super();
		this.jobId = jobLogEntity.getJobId();
		this.jobStatus = jobLogEntity.getJobStatus();
		this.batchStartTime = jobLogEntity.getBatchStartTime();
		this.batchEndTime = jobLogEntity.getBatchEndTime();
		this.jobMessage = jobLogEntity.getJobMessage();
		this.machineIp = jobLogEntity.getMachineIp();
		this.createTime = jobLogEntity.getCreateTime();
		this.updateTime = jobLogEntity.getUpdateTime();
	}

	
	
	

}
