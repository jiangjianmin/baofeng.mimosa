package com.guohuai.mmp.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;

@Configuration
@ConfigurationProperties(prefix = "cron")
@PropertySource("classpath:crontab.properties")
@Component
public class CrontabOptionConfig {
	
	@Autowired
	private JobLockService jobLockService;
	public static Map<String, String> option = new HashMap<String, String>();

	
	public Map<String, String> getOption() {
		return CrontabOptionConfig.option;
	}

	public void setOption(Map<String, String> option) {
		CrontabOptionConfig.option = option;
	}
	
	@PostConstruct
	public void initJobLock() {
		List<JobLockEntity> lockList = new ArrayList<JobLockEntity>();
		for (Map.Entry<String, String> entry : option.entrySet()) {
			String jobId = entry.getKey();
			String jobTime = entry.getValue();
			JobLockEntity entity = this.jobLockService.findByJobId(jobId);
			if (null == entity) {
				entity = new JobLockEntity();
			}
			entity.setJobId(jobId);
			entity.setJobTime(jobTime);
			entity.setJobStatus(JobLockEntity.JOB_jobStatus_toRun);
			lockList.add(entity);
		}
		jobLockService.batchUpdate(lockList);
	}


}
