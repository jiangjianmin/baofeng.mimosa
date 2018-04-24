package com.guohuai.mmp.job.log;

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
@Table(name = "T_MONEY_JOB_LOG")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@lombok.Builder
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class JobLogEntity extends UUID {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5109160803296992814L;

	
	public static final String JOB_jobStatus_success = "success";
	public static final String JOB_jobStatus_failed = "failed";
	
	

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
