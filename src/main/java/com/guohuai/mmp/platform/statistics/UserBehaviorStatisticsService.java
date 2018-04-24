package com.guohuai.mmp.platform.statistics;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.plugin.PageVo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class UserBehaviorStatisticsService {
	
	Logger logger = LoggerFactory.getLogger(UserBehaviorStatisticsService.class);

	@Autowired
	private UserBehaviorStatisticsDao userBehaviorStatisticsDao;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private JobLogService jobLogService;
	
	public PageVo<Map<String,Object>> queryUserStatistics(UserBehaviorStatisticsReq req) {
		log.info("mimosa-boot查询统计开始!");
		// 投资统计数据列表
		List<Object[]> objInvestList = new ArrayList<Object[]>();
		// 汇总
		List<Object[]> objStatList = new ArrayList<Object[]>();
		// 总条数
		int total = 0;
		// *****统计汇总******
		objStatList = userBehaviorStatisticsDao.getUserInvestBehaviorStatSum(req.getChannelOid());
		// ******投资统计查询******
		if (req.getDimension() == 0) {// 按日维度统计
			objInvestList = userBehaviorStatisticsDao.getUserInvestBehaviorByDailyStatisticsList(
					req.getStartTime(), req.getEndTime(), req.getIsExperGold(), req.getChannelOid(),
					(req.getPage() - 1) * req.getRow(), req.getRow());
			
			total = userBehaviorStatisticsDao.getPageCountDaily(req.getStartTime(), req.getEndTime(), req.getChannelOid());
		} else {// 按月维度统计
				objInvestList = userBehaviorStatisticsDao.getUserInvestBehaviorByMonthStatisticsList(
						req.getStartTime(), req.getEndTime(), req.getIsExperGold(), req.getChannelOid(),
						(req.getPage() - 1) * req.getRow(), req.getRow());
			total = userBehaviorStatisticsDao.getPageCountMonth(req.getStartTime(), req.getEndTime(), req.getChannelOid());
		}
		
		// 处理注册，绑卡，以及投资相关统计数据
		PageVo<Map<String,Object>> pageVo = dealUserBehaviorStatisticsData(req, objInvestList, objStatList, total);
		
		return pageVo;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: dealUserBehaviorStatisticsData
	 * @Description:处理注册，绑卡，以及投资相关统计数据
	 * @param req
	 * @param objInvestList
	 * @param objRegisterList
	 * @return UserBehaviorStatisticsRes
	 * @date 2017年5月8日 下午6:18:54
	 * @since  1.0.0
	 */
	private PageVo<Map<String,Object>> dealUserBehaviorStatisticsData(UserBehaviorStatisticsReq req,
			List<Object[]> objInvestList, List<Object[]> objStatList, int total) {
		List<Map<String,Object>> userBehaviorStatisticsList = new ArrayList<Map<String,Object>>();
		PageVo<Map<String,Object>> pageVo = new PageVo<Map<String,Object>>();
		
		if (objInvestList.size() > 0 && objStatList.size() > 0) {
			// 汇总计算
			Map<String,Object> mapSum = new HashMap<String,Object>();
			Object[] mapSumData = objStatList.get(0);
			// 封装汇总结果为List中的第一个元素
			mapSum.put("sortTime", "总计");
			mapSum.put("registerNum", StringToInt(mapSumData[0].toString()));
			mapSum.put("bindCardNum", StringToInt(mapSumData[1].toString()));
			mapSum.put("investorPeopleNum", StringToInt(mapSumData[2].toString()));
			mapSum.put("orderAmount", mapSumData[3].toString());
			mapSum.put("investorPenNum", StringToInt(mapSumData[4].toString()));
			mapSum.put("investorAvgAmount", mapSumData[5].toString());
			
			userBehaviorStatisticsList.add(0, mapSum);
			
			// 数据列表
			for (int i =0;i < objInvestList.size(); i++) {
				Map<String,Object> map = new HashMap<String,Object>();
				Object[] objInvest = objInvestList.get(i);
				if (req.getDimension() == 1) {// 按月
					map.put("sortTime", objInvest[0].toString() + "月");
				} else {// 按日
					map.put("sortTime", objInvest[0].toString());
				}
				map.put("registerNum", StringToInt(objInvest[1].toString()));
				map.put("bindCardNum", StringToInt(objInvest[2].toString()));
				map.put("investorPeopleNum", StringToInt(objInvest[3].toString()));
				map.put("orderAmount", new BigDecimal(objInvest[4].toString()));
				map.put("investorPenNum", StringToInt(objInvest[5].toString()));
				map.put("investorAvgAmount", new BigDecimal(objInvest[6].toString()));
				if (req.getIsRatio() == 1) {// 是否看环比(1是，0否)
					double registerRatio = 0;
					double bindCardRatio = 0;
					double investorPeopleRatio = 0;
					BigDecimal orderAmountRatio = new BigDecimal(0);
					double investorPenRatio = 0;
					BigDecimal investorAvgAmountRatio = new BigDecimal(0);
					// 环比计算
					if (objInvestList.size() >= (i+1) && i > 0) {
						Object[] objInvestRatio = objInvestList.get(i-1);
						// registerRatio(注册数环比)
						if (StringToDouble(objInvestRatio[1].toString()) == 0) {
							registerRatio = StringToDouble(objInvest[1].toString())*100;
						} else {
							registerRatio = (StringToDouble(objInvest[1].toString())-StringToDouble(objInvestRatio[1].toString()))
									/StringToDouble(objInvestRatio[1].toString())
									*100;
						}
						// bindCardRatio(绑卡数环比)
						if (StringToDouble(objInvestRatio[2].toString()) == 0) {
							bindCardRatio = StringToDouble(objInvest[2].toString())*100;
						} else {
							bindCardRatio = (StringToDouble(objInvest[2].toString())-StringToDouble(objInvestRatio[2].toString()))
									/StringToDouble(objInvestRatio[2].toString())
									*100;
						}
						// investorPeopleRatio(投资人数环比)
						if (StringToDouble(objInvestRatio[3].toString()) == 0) {
							investorPeopleRatio = StringToDouble(objInvest[3].toString())*100;
						} else {
							investorPeopleRatio = (StringToDouble(objInvest[3].toString())-StringToDouble(objInvestRatio[3].toString()))
									/StringToDouble(objInvestRatio[3].toString())
									*100;
						}
						// orderAmountRatio(投资金额环比)
						if (new BigDecimal(objInvestRatio[4].toString()).compareTo(BigDecimal.ZERO) == 0) {// 分母为0
							orderAmountRatio = new BigDecimal(objInvest[4].toString()).multiply(new BigDecimal(100));
						} else {// 分母不为0
							BigDecimal subtract = new BigDecimal(objInvest[4].toString()).subtract(new BigDecimal(objInvestRatio[4].toString()));
							BigDecimal divideSetScale = subtract.divide(new BigDecimal(objInvestRatio[4].toString()),4,BigDecimal.ROUND_DOWN);
							orderAmountRatio = divideSetScale.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_DOWN);
						}
						// investorPenRatio(投资笔数环比)
						if (StringToDouble(objInvestRatio[5].toString()) == 0) {
							investorPenRatio = StringToDouble(objInvest[5].toString())*100;
						} else {
							investorPenRatio = (StringToDouble(objInvest[5].toString())-StringToDouble(objInvestRatio[5].toString()))
									/StringToDouble(objInvestRatio[5].toString())
									*100;
						}
						// investorAvgAmountRatio(人均客单价环比)
						if (new BigDecimal(objInvestRatio[6].toString()).compareTo(BigDecimal.ZERO) == 0) {// 分母为0
							investorAvgAmountRatio = new BigDecimal(objInvest[6].toString()).multiply(new BigDecimal(100));
						} else {// 分母不为0
							BigDecimal subtract = new BigDecimal(objInvest[6].toString()).subtract(new BigDecimal(objInvestRatio[6].toString()));
							BigDecimal divideSetScale = subtract.divide(new BigDecimal(objInvestRatio[6].toString()),4,BigDecimal.ROUND_DOWN);
							investorAvgAmountRatio = divideSetScale.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_DOWN);
						}
					}
					// 判断正负，加上正负号
					DecimalFormat df = new DecimalFormat("######0.00");
					if (registerRatio > 0) {
						map.put("registerRatio", "+"+df.format(registerRatio)+"%");
					} else if (registerRatio == 0) {
						map.put("registerRatio", "0");
					} else {
						map.put("registerRatio", df.format(registerRatio)+"%");
					}
					if (bindCardRatio > 0) {
						map.put("bindCardRatio", "+"+df.format(bindCardRatio)+"%");
					} else if (bindCardRatio == 0) {
						map.put("bindCardRatio", "0");
					} else {
						map.put("bindCardRatio", df.format(bindCardRatio)+"%");
					}
					if (investorPeopleRatio > 0) {
						map.put("investorPeopleRatio", "+"+df.format(investorPeopleRatio)+"%");
					} else if (investorPeopleRatio == 0) {
						map.put("investorPeopleRatio", "0");
					} else {
						map.put("investorPeopleRatio", df.format(investorPeopleRatio)+"%");
					}
					if (orderAmountRatio.compareTo(BigDecimal.ZERO) > 0) {
						map.put("orderAmountRatio", "+"+orderAmountRatio+"%");
					} else if (orderAmountRatio.compareTo(BigDecimal.ZERO) == 0) {
						map.put("orderAmountRatio", "0");
					} else {
						map.put("orderAmountRatio", orderAmountRatio+"%");
					}
					if (investorPenRatio > 0) {
						map.put("investorPenRatio", "+"+df.format(investorPenRatio)+"%");
					} else if (investorPenRatio == 0) {
						map.put("investorPenRatio", "0");
					} else {
						map.put("investorPenRatio", df.format(investorPenRatio)+"%");
					}
					if (investorAvgAmountRatio.compareTo(BigDecimal.ZERO) > 0) {
						map.put("investorAvgAmountRatio", "+"+investorAvgAmountRatio+"%");
					} else if (investorAvgAmountRatio.compareTo(BigDecimal.ZERO) == 0) {
						map.put("investorAvgAmountRatio", "0");
					} else {
						map.put("investorAvgAmountRatio", investorAvgAmountRatio+"%");
					}
				}
				
				userBehaviorStatisticsList.add(map);
			}
		}
		
		pageVo.setRows(userBehaviorStatisticsList);
		pageVo.setTotal(total);
		pageVo.setRow(req.getRow());
		pageVo.setPage(req.getPage());
		pageVo.reTotalPage();
		
		return pageVo;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryUserRegisterChannelOid
	 * @Description: 用户注册渠道列表
	 * @return ResponseEntity<PageVo<Map<String,Object>>>
	 * @date 2017年6月27日 下午11:58:46
	 * @since  1.0.0
	 */
	public PageVo<Map<String, Object>> queryUserRegisterChannelOid() {
		PageVo<Map<String,Object>> pageVo = new PageVo<Map<String,Object>>();
		List<Object[]> objChannelOidList = userBehaviorStatisticsDao.queryUserRegisterChannelOid();
		List<Map<String,Object>> userChannelOidList = new ArrayList<Map<String,Object>>();
		if (objChannelOidList.size() > 0) {
			for (int i=0;i < objChannelOidList.size(); i++) {
				Map<String,Object> mapData = new HashMap<String,Object>();
				mapData.put("channelOid", objChannelOidList.get(i));
				
				userChannelOidList.add(mapData);
			}
		}
		pageVo.setRows(userChannelOidList);
		return pageVo;
	}
	
	// *****************定时任务处理昨日用户投资信息统计******************
	public void statUserYesterdayInvestInfo() {
		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_statUserYesterdayInvestInfo)) {
			statUserYesterdayInvestInfoLog();
		}
	}

	private void statUserYesterdayInvestInfoLog() {
		JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_statUserYesterdayInvestInfo);
		try {
			String yesterday = DateUtil.format(DateUtil.addDay(new Date(), -1));
			Date investDate = DateUtil.parseDate(yesterday, "yyyy-MM-dd");
			statUserYesterdayInvestInfoDo(investDate);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_statUserYesterdayInvestInfo);
		
	}
	
	private void statUserYesterdayInvestInfoDo(Date investDate) {
		log.info("统计功能--昨日注册，绑卡，投资统计开始，时间:{}", investDate);
		int result = userBehaviorStatisticsDao.initUserBehaviorStatistics(investDate);
		log.info("统计功能--昨日注册，绑卡，投资统计结束，结果为:{}", result>0);
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: StringToInt
	 * @Description:String-->转换-->int
	 * @param str
	 * @return int
	 * @date 2017年5月10日 下午9:31:48
	 * @since  1.0.0
	 */
	private int StringToInt(String str) {
		int i = 0;
		if (!"".equals(str)) {
			double d = new Double(str);
			i = (int)d;
		}
		
		return i;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: StringToDouble
	 * @Description:String to Double
	 * @param str
	 * @return double
	 * @date 2017年5月15日 上午11:06:29
	 * @since  1.0.0
	 */
	private double StringToDouble(String str) {
		double d = 0;
		if (!"".equals(str)) {
			d = new Double(str);
		}
		
		return d;
	}

}
