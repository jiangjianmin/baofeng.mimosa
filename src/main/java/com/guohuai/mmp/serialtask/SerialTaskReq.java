package com.guohuai.mmp.serialtask;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 串行任务执行
 * 
 * @author yuechao
 *
 */
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
public class SerialTaskReq<T>  {
	
	/**
	 * 任务代码
	 */
	private String taskCode;
	
	/**
	 * 任务参数
	 */
	private T taskParams;
	
	

}
