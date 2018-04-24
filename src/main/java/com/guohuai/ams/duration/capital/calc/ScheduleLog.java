package com.guohuai.ams.duration.capital.calc;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Data;

/**
 * 资产池每日收益计算日志
 * @author star.zhu
 * 2016年6月13日
 */
@Data
@Entity
@Table(name = "T_GAM_ASSETPOOL_SCHEDULE")
@DynamicInsert
@DynamicUpdate
public class ScheduleLog implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	private String oid;
	// 计算开始时间
	private Timestamp startTime;
	// 计算结束时间
	private Timestamp endTime;
	// 计算任务数(资产池数 )
	private int jobCount;
	// 计算成功数
	private int successCount;
	// 收益基准日
	private Date baseDate;
}
