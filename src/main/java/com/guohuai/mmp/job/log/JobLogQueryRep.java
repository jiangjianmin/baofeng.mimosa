package com.guohuai.mmp.job.log;

import java.sql.Timestamp;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 定时任务日志
 * 
 * @author yuechao
 *
 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class JobLogQueryRep {

	


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
}
