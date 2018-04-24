package com.guohuai.mmp.serialtask;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SerialTaskDao extends JpaRepository<SerialTaskEntity, String>, JpaSpecificationExecutor<SerialTaskEntity> {
	
	@Query(value = "select * from T_MONEY_SERIALTASK where taskStatus in ('toRun') and oid != 'mainTask' order by priority desc,createTime limit 200", nativeQuery = true)
	List<SerialTaskEntity> getToDoTasks();
	
	@Query(value = "select count(1) from T_MONEY_SERIALTASK where taskStatus in ('toRun') and oid != 'mainTask' limit 1", nativeQuery = true)
	int getOneTask();
	
	@Query(value = "select count(1) from T_MONEY_SERIALTASK a WHERE a.taskStatus='running' AND TIMESTAMPDIFF(MINUTE,a.updateTime,NOW()) > ?1 and oid != 'mainTask'", nativeQuery = true)
	int hasTimeoutChildTask(int timeout);
	
	/**
	 * 开始主任务
	 * @return
	 */
	@Modifying
	@Query(value = "update SerialTaskEntity set taskStatus = 'running' where oid = 'mainTask' and taskStatus = 'toRun' ")
	int beginTask();

	/** 终结主任务 */
	@Modifying
	@Query(value = "update SerialTaskEntity set taskStatus = 'toRun' where oid = 'mainTask' and taskStatus = 'running' ")
	int endTask();

	@Modifying
	@Query(value = "update SerialTaskEntity set updateTime = sysdate() where oid = ?1 and taskStatus = 'running' ")
	int updateTime(String taskOid);
	
	@Modifying
	@Query(value = "update SerialTaskEntity set executeStartTime = ?2, taskStatus = ?3 where oid = ?1 and taskStatus = 'toRun' ")
	int updateBegin(String taskOid, Timestamp executeStartTime, String taskStatus);
	
	@Modifying
	@Query(value = "update T_MONEY_SERIALTASK set executeEndTime = ?2, taskStatus = if(taskStatus = 'running', ?3, taskStatus), taskError = ?4 where oid = ?1", nativeQuery = true)
	int updateFinish(String taskOid, Timestamp executeEndTime, String taskStatus, String taskError);
	
	@Modifying
	@Query(value = "update T_MONEY_SERIALTASK set executeEndTime = sysdate(), taskStatus = if(taskStatus = 'running', ?2, taskStatus) where oid = ?1", nativeQuery = true)
	int timed(String taskOid, String taskStatus);

	SerialTaskEntity findByTaskCodeAndTaskParams(String taskCode, String taskParams);

	SerialTaskEntity findByTaskCodeAndTaskParamsAndTaskStatusIn(String taskCode, String taskParams, List<String> taskStatus);
}
 