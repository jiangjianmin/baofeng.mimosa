package com.guohuai.mmp.platform.channel.statistics;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.guohuai.ams.channel.Channel;
import com.guohuai.ams.channel.ChannelDao;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.BigDecimalUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;

@Service
public class PlatformChannelStatisticsService {

	Logger logger = LoggerFactory.getLogger(PlatformChannelStatisticsService.class);

	@Autowired
	private PlatformChannelStatisticsDao platformChannelStatisticsDao;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderNewService;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private JobLogService jobLogService;
	@Autowired
	private ChannelDao channelDao;
	
	public void statChannelYesterdayInvestInfo() {
		
		
		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_statChannelYesterdayInvestInfo)) {
			statChannelYesterdayInvestInfoLog();
		}
		
		
		
	}
	
	public void statChannelYesterdayInvestInfoLog() {
		JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_statChannelYesterdayInvestInfo);
		try {
			String yesterday = DateUtil.format(DateUtil.addDay(new Date(), -1));
			Date investDate = DateUtil.parseDate(yesterday, "yyyy-MM-dd");
			statChannelYesterdayInvestInfoDo(investDate);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_statChannelYesterdayInvestInfo);
	}

	/** 统计各渠道昨日投资信息 */
	@Transactional
	public void statChannelYesterdayInvestInfoDo(Date date) {

		Timestamp startTime = DateUtil.getTimestampZeroOfDate(date);
		Timestamp endTime = DateUtil.getTimestampLastOfDate(date);

		Map<String, PlatformChannelStatisticsEntity> map = new HashMap<String, PlatformChannelStatisticsEntity>();

		List<Object[]> todayList = this.investorTradeOrderNewService.statInvestAmountByChannel(startTime, endTime);
		if (todayList != null && todayList.size() > 0) {

			for (Object[] arr : todayList) {
				String channelOid = arr[0].toString();// 渠道ID
				String orderType = StringUtils.isEmpty(arr[1]) ? "" : arr[1].toString();// 订单类型
				BigDecimal amount = BigDecimalUtil.parseFromObject(arr[2]);// 交易金额

				if (!map.containsKey(channelOid)) {
					PlatformChannelStatisticsEntity entity = new PlatformChannelStatisticsEntity();
					entity.setOid(StringUtil.uuid());
					entity.setInvestDate(date);
					Channel channel = this.channelDao.findOne(channelOid);
					if (channel != null) {
						entity.setChannel(channel);
					}
					map.put(channelOid, entity);
				}

				// 昨日投资金额
				if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderType)) {
					map.get(channelOid).setTodayInvestAmount(amount);

					// 昨日赎回金额
				} else if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderType)
						|| InvestorTradeOrderEntity.TRADEORDER_orderType_fastRedeem.equals(orderType)
						|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderType)) {
					map.get(channelOid).setTodayRedeemAmount(amount);

					// 昨日还本付息金额
				} else if (InvestorTradeOrderEntity.TRADEORDER_orderType_cash.equals(orderType)) {
					map.get(channelOid).setTodayCashAmount(amount);
				}
			}

		}

		// 统计各渠道截止到昨日累计投资额
		List<Object[]> totalList = this.investorTradeOrderNewService.statInvestTotalAmountByChannel(endTime);
		if (totalList != null && totalList.size() > 0) {

			for (Object[] arr : totalList) {
				String channelOid = arr[0].toString();// 渠道ID
				String orderType = StringUtils.isEmpty(arr[1]) ? "" : arr[1].toString();// 订单类型
				BigDecimal amount = BigDecimalUtil.parseFromObject(arr[2]);// 交易金额

				if (!map.containsKey(channelOid)) {
					PlatformChannelStatisticsEntity entity = new PlatformChannelStatisticsEntity();
					entity.setOid(StringUtil.uuid());
					entity.setInvestDate(date);
					Channel channel = this.channelDao.findOne(channelOid);
					if (channel != null) {
						entity.setChannel(channel);
					}
					map.put(channelOid, entity);
				}

				// 昨日投资金额
				if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderType)) {
					map.get(channelOid).setTotalInvestAmount(amount);

					// 昨日赎回金额
				} else if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderType)
						|| InvestorTradeOrderEntity.TRADEORDER_orderType_fastRedeem.equals(orderType)) {
					map.get(channelOid).setTotalRedeemAmount(amount);

					// 昨日还本付息金额
				} else if (InvestorTradeOrderEntity.TRADEORDER_orderType_cash.equals(orderType)) {
					map.get(channelOid).setTotalCashAmount(amount);
				}
			}

		}

		if (map.size() > 0) {
			Iterator<String> iter = map.keySet().iterator();
			List<PlatformChannelStatisticsEntity> list = new ArrayList<PlatformChannelStatisticsEntity>();
			while (iter.hasNext()) {
				list.add(map.get(iter.next()));
			}
			try{
				this.platformChannelStatisticsDao.deleteByDate(date);
			}catch(Exception e){
				e.printStackTrace();
			}
			this.platformChannelStatisticsDao.save(list);
		}
	}

	/** 昨日渠道累计投资TOP5 */
	public List<Object[]> queryInvestTop5(Date yesterday) {
		return this.platformChannelStatisticsDao.queryInvestTop5(yesterday);
	}

	/** 平台交易额占比分析 */
	public List<Object[]> platformTradeAnalyse(Date yesterday) {
		return this.platformChannelStatisticsDao.platformTradeAnalyse(yesterday);
	}

	/** 平台交易总额各渠道占比 */
	public List<Object[]> queryTotalInvest(Date yesterday) {
		return this.platformChannelStatisticsDao.queryTotalInvest(yesterday);
	}
}
