package com.guohuai.mmp.serialtask;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.IPUtil;
import com.guohuai.mmp.job.lock.JobLockService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class SerialTaskService {

	@Autowired
	private SerialTaskDao serialTaskDao;
	@Autowired
	private SerialTaskRequireNewService serialTaskRequireNewService;
	
	@Value("${serialTask.timeout:30}")
	private int timeout;

	/**
	 * 添加待执行子任务
	 */
	public void createSerialTask(SerialTaskReq<?> req) {
		SerialTaskEntity en = serialTaskRequireNewService.createEntity(req);
		log.info("createSerialTask>>taskOid={}, taskCode={}, taskStatus={}, createTime={}任务添加成功", 
				en.getOid(), en.getTaskCode(),en.getTaskStatus(), en.getCreateTime());
//		/** 执行任务 */
//		this.executeTask();
	}

	/**
	 * 执行
	 */
	public void executeTask() {
		if (JobLockService.JOB_needSchedule_no.equals(JobLockService.needSchedule)) {
			log.info("ip={},此机器 不需要执行定时任务", IPUtil.getLocalIP());
			return;
		}
		List<SerialTaskEntity> list=new ArrayList<SerialTaskEntity>();
		boolean need=false;
		try{
			int currentCount = this.serialTaskDao.getOneTask();
			log.info("本次需要执行{}个任务", currentCount);
			if (currentCount>0) {
				need=this.serialTaskRequireNewService.beginTask();
				if (need) {
					log.info("begin to handle executeTask");
					list = this.serialTaskDao.getToDoTasks();
					for (SerialTaskEntity en : list) {
						log.info("开始执行 序列化任务 id:{} type:{}", en.getOid(), en.getTaskCode());
						this.doOneTask(en);
						log.info("执行 序列化任务 id:{} type:{}完成", en.getOid(), en.getTaskCode());
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if (need) {
				this.serialTaskRequireNewService.endTask();
			}
		}
		
	}
	
	/**
	 *重置超时任务
	 */
	public void resetTimeoutTask(){
		if (JobLockService.JOB_needSchedule_no.equals(JobLockService.needSchedule)) {
			return;
		}
		if (this.serialTaskDao.hasTimeoutChildTask(timeout)>0 && this.serialTaskRequireNewService.endTask()) {
				log.info("reset timeout-task successfully.");
		}
	}

	public void doOneTask(SerialTaskEntity en) {
		String taskStatus = SerialTaskEntity.TASK_taskStatus_running;
		String taskError = "";
		log.info("begin to handle task {}", en.getTaskCode());
		this.serialTaskRequireNewService.updateBegin(en.getOid(), DateUtil.getSqlCurrentDate(), taskStatus);
		try {
			this.serialTaskRequireNewService.taskDo(en);
			taskStatus = SerialTaskEntity.TASK_taskStatus_done;
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			taskStatus = SerialTaskEntity.TASK_taskStatus_failed;
			/** 赎回处理失败，重跑 */
			taskError = AMPException.getStacktrace(e);
		}
		log.info("the taskStatus of task {} is {} and taskError {}.", en.getTaskStatus(), taskStatus, taskError);
		this.serialTaskRequireNewService.updateFinish(en.getOid(), DateUtil.getSqlCurrentDate(), taskStatus, taskError);
	}

	/**
	 * 去重
	 */
	public void findByTaskCodeAndTaskParam(SerialTaskReq<?> req) {
		List<String> taskStatus = new ArrayList<String>();
		taskStatus.add(SerialTaskEntity.TASK_taskStatus_toRun);
		taskStatus.add(SerialTaskEntity.TASK_taskStatus_running);
		taskStatus.add(SerialTaskEntity.TASK_taskStatus_done);
		SerialTaskEntity en = this.serialTaskDao.findByTaskCodeAndTaskParamsAndTaskStatusIn(req.getTaskCode(),
				JSONObject.toJSONString(req.getTaskParams()), taskStatus);
		if (null == en) {
			return;
		} else {
			if (SerialTaskEntity.TASK_taskStatus_toRun.equals(en.getTaskStatus())) {
				throw new AMPException("任务正等待执行");
			}
			if (SerialTaskEntity.TASK_taskStatus_running.equals(en.getTaskStatus())) {
				throw new AMPException("任务正在执行");
			}
			if (SerialTaskEntity.TASK_taskStatus_done.equals(en.getTaskStatus())) {
				throw new AMPException("任务已执行成功");
			}

		}
	}
}
