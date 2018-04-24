package com.guohuai.mmp.platform.baseaccount.statistics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.BigDecimalUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.platform.baseaccount.PlatformBaseAccountService;
import com.guohuai.mmp.platform.channel.statistics.PlatformChannelStatisticsService;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
import com.guohuai.mmp.publisher.product.statistics.PublisherProductStatisticsService;
import com.guohuai.mmp.sys.SysConstant;

@Service
@Transactional
public class PlatformStatisticsService {

	Logger logger = LoggerFactory.getLogger(PlatformStatisticsService.class);

	@Autowired
	private PlatformStatisticsDao platformStatisticsDao;
	
	@Autowired
	private PlatformBaseAccountService platformBaseAccountService;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	PlatformChannelStatisticsService platformChannelStatisticsService;
	@Autowired
	PublisherProductStatisticsService publisherProductStatisticsService;





	public PlatformStatisticsEntity findByPlatformBaseAccount() {
		PlatformStatisticsEntity st = this.platformStatisticsDao.findByPlatformBaseAccount(platformBaseAccountService.getPlatfromBaseAccount());
		if (null == st) {
			throw new AMPException("平台统计不存在");
		}
		return st;
	}
	
	/**
	 * 还本
	 */
	public int updateStatistics4RepayLoanConfirm(BigDecimal orderAmount) {
		return this.updateStatistics4RedeemConfirm(orderAmount);
	}
	
	/**
	 * 付息
	 */
	public int updateStatistics4TotalInterestAmount(BigDecimal income) {
		PlatformStatisticsEntity st = this.findByPlatformBaseAccount();
		return this.platformStatisticsDao.updateStatistics4TotalInterestAmount(st.getOid(), income);
	}
	
	/**
	 * 投资单份额确认:累计交易额、累计借款额
	 */
	public int updateStatistics4InvestConfirm(BigDecimal orderAmount) {
		PlatformStatisticsEntity st = this.findByPlatformBaseAccount();
		return this.platformStatisticsDao.updateStatistics4InvestConfirm(st.getOid(), orderAmount);
	}
	
	/**
	 * 赎回单份额确认:累计交易额、累计还款额
	 */
	public int updateStatistics4RedeemConfirm(BigDecimal orderAmount) {
		PlatformStatisticsEntity st = this.findByPlatformBaseAccount();
		return this.platformStatisticsDao.updateStatistics4RedeemConfirm(st.getOid(), orderAmount);
	}

	/**
	 * 投资人充值回调
	 * 更新<<平台-统计>>.<<累计交易总额>><<投资人充值总额>> 
	 * @param orderAmount
	 */
	public int updateStatistics4InvestorDeposit(BigDecimal orderAmount) {
		PlatformStatisticsEntity st = this.findByPlatformBaseAccount();
		return this.platformStatisticsDao.updateStatistics4InvestorDeposit(st.getOid(), orderAmount);
	}

	/**
	 * 发行人充值回调
	 * 更新<<平台-统计>>.<<累计交易总额>><<发行人充值总额>>
	 * @param orderAmount
	 */
	public int updateStatistics4PublisherDeposit(BigDecimal orderAmount) {
		PlatformStatisticsEntity st = this.findByPlatformBaseAccount();
		return this.platformStatisticsDao.updateStatistics4PublisherDeposit(st.getOid(), orderAmount);
	}

	/**
	 * 投资人提现回调
	 * 更新<<平台-统计>>.<<累计交易总额>><<投资人提现总额>>
	 * @param orderAmount
	 */
	public int updateStatistics4InvestorWithdraw(BigDecimal orderAmount) {
		PlatformStatisticsEntity st = this.findByPlatformBaseAccount();
		return this.platformStatisticsDao.updateStatistics4InvestorWithdraw(st.getOid(), orderAmount);
	}

	/**
	 * 发行人提现回调
	 * 更新<<平台-统计>>.<<累计交易总额>><<发行人提现总额>>
	 * @param orderAmount
	 * @return
	 */
	public int updateStatistics4PublisherWithdraw(BigDecimal orderAmount) {
		PlatformStatisticsEntity st = this.findByPlatformBaseAccount();
		return this.platformStatisticsDao.updateStatistics4PublisherWithdraw(st.getOid(), orderAmount);
	}

	/**
	 * 投资人注册
	 * 增加平台注册人数
	 */
	public int increaseRegisterAmount() {
		return this.platformStatisticsDao.increaseRegisterAmount(this.findByPlatformBaseAccount().getOid());
	}
	
	/**
	 * 投资人实名认证
	 * 增加平台实名人数
	 */
	public int increaseVerifiedInvestorAmount() {
		return this.platformStatisticsDao.increaseVerifiedInvestorAmount(this.findByPlatformBaseAccount().getOid());
	}
	
	/**
	 * 投资人数
	 * 更新投资人数、持仓人数
	 */
	public void increaseInvestorAmount(InvestorBaseAccountEntity investorBaseAccount) {
		int i = publisherHoldService.countByInvestorBaseAccount(investorBaseAccount);
		if (i == 1) {
			this.platformStatisticsDao.increaseInvestorAmount(this.findByPlatformBaseAccount().getOid());
		}
	}

	/**
	 * 快赎确认
	 */
	public int updateStatistics4FastRedeemConfirm(BigDecimal orderAmount) {
		PlatformStatisticsEntity st = this.findByPlatformBaseAccount();
		return this.platformStatisticsDao.updateStatistics4FastRedeemConfirm(st.getOid(), orderAmount.multiply(new BigDecimal(2)));
	}
	
	
	/**
	 * 定期产品进入募集期时，增加产品发行数量
	 */
	public int increaseReleasedProductAmount() {
		PlatformStatisticsEntity st = this.findByPlatformBaseAccount();
		int i = this.platformStatisticsDao.increaseReleasedProductAmount(st.getOid());
		return i;
	}
	
	/**
	 * 定期产品进入存续期时，增加在售产品数量
	 */
	public int increaseOnSaleProductAmount() {
		PlatformStatisticsEntity st = this.findByPlatformBaseAccount();
		int i = this.platformStatisticsDao.increaseOnSaleProductAmount(st.getOid());
		return i;
	}
	
	/**
	 * 定期产品存续期结束之后，增加待结算产品数量
	 * @param baseAccount
	 * @return
	 */
	public int increaseToCloseProductAmount() {
		PlatformStatisticsEntity st = this.findByPlatformBaseAccount();
		int i = this.platformStatisticsDao.increaseToCloseProductAmount(st.getOid());
		return i;
	}
	
	/**
	 * 定期产品发起还本付息之后/或定期产品募集失败,增加已结算产品数量
	 */
	public int increaseClosedProductAmount() {
		PlatformStatisticsEntity st = this.findByPlatformBaseAccount();
		int i = this.platformStatisticsDao.increaseClosedProductAmount(st.getOid());
		return i;
	}

	/** 平台首页查询 */
	public BaseRep qryHome() {
		
		PlatformHomeQueryRep rep = new PlatformHomeQueryRep();
		Date yesterday = DateUtil.getBeforeDate();

		// 查询最近3日的平台统计数据
		java.sql.Date today = DateUtil.getSqlDate();
		// 平台备付金账号信息
		List<Object[]> list = this.platformStatisticsDao.platformReservedAccountInfo();
		if (list != null && list.size() > 0) {
			Object[] arr = list.get(0);
			rep.setBalance(BigDecimalUtil.parseFromObject(arr[0]));// 备付金余额(平台备付金账户-余额)
			
			rep.setToCloseProductAmount(parseInteger(arr[2]));// 待还产品数
		}

		java.sql.Date startDate = DateUtil.addSQLDays(today, -2);
		java.sql.Date endDate = DateUtil.addSQLDays(today, 0);
		// 平台统计信息
		List<Object[]> platFormList = this.platformStatisticsDao.platformStatInfo(startDate, endDate);
		if (platFormList != null && platFormList.size() > 0) {
			Object[] todayData = null;// 当日数据
			Object[] yesterdayData = null;// 昨日数据
			Object[] beforeyesterdayData = null;// 前日数据
			for (Object[] objects : platFormList) {
				if (DateUtil.format(today).equals(objects[0])) {
					todayData = objects;// 平台当日统计数据
				} else if (DateUtil.format(DateUtil.addDay(today, -1)).equals(objects[0])) {
					yesterdayData = objects;// 平台昨日统计数据
				} else if (DateUtil.format(DateUtil.addDay(today, -2)).equals(objects[0])) {
					beforeyesterdayData = objects;// 平台前日统计数据
				}
			}

			// 累计数据
			rep.setTotalTradeAmount(BigDecimalUtil.parseFromObject(getFromArr(todayData, 1)));// 累计交易总额
			rep.setTotalInvestAmount(BigDecimalUtil.parseFromObject(getFromArr(todayData, 2)));// 累计投资总额（累计借款）
			rep.setRegisterAmount(parseInteger(getFromArr(todayData, 3)));// 累计注册人数
			rep.setInvestorAmount(parseInteger(getFromArr(todayData, 4)));// 累计投资人数
			rep.setInvestorTotalDepositAmount(BigDecimalUtil.parseFromObject(getFromArr(todayData, 5)));// 投资人充值总额
			rep.setInvestorTotalWithdrawAmount(BigDecimalUtil.parseFromObject(getFromArr(todayData, 6)));// 投资人提现总额
			rep.setTotalReturnAmount(BigDecimalUtil.parseFromObject(getFromArr(todayData, 7)));// 累计还款总额
			rep.setPublisherAmount(BigDecimalUtil.parseFromObject(getFromArr(todayData, 8)));// 企业用户数（发行人数）
			rep.setProductAmount(parseInteger(getFromArr(todayData, 9)));// 发行产品数
			rep.setClosedProductAmount(parseInteger(getFromArr(todayData, 10)));// 已还产品数（已结算产品数）
			rep.setOnSaleProductAmount(parseInteger(getFromArr(todayData, 11)));// 在售产品数

			// 今日数据
			// 今日在线用户数(活跃投资人数)
			rep.setTodayOnline(parseInteger(getFromArr(todayData, 12)));
			// 今日投资金额
			rep.setTodayInvestAmount(rep.getTotalInvestAmount());
			// 今日注册用户数(今日注册总数-昨日注册总数)
			rep.setTodayRegisterAmount(rep.getRegisterAmount() - parseInteger(getFromArr(yesterdayData, 3)));
			// 今日实名认证用户数(今日-昨日)
			rep.setTodayVerifiedInvestorAmount(
					parseInteger(getFromArr(todayData, 13)) - parseInteger(getFromArr(yesterdayData, 13)));
			// 今日投资人数(今日-昨日)
			rep.setTodayInvestorAmount(rep.getInvestorAmount() - parseInteger(getFromArr(yesterdayData, 4)));
			// 今日新增投资(今日-昨日)
			rep.setTodayAddedInvestAmount(rep.getTotalInvestAmount().subtract(BigDecimalUtil.parseFromObject(getFromArr(yesterdayData, 2))));

			// 昨日数据
			// 昨日在线用户数(活跃投资人数)
			rep.setYesterdayOnline(
					parseInteger(getFromArr(yesterdayData, 12)) - parseInteger(getFromArr(beforeyesterdayData, 12)));
			// 昨日投资金额
			rep.setYesterdayInvestAmount(BigDecimalUtil.parseFromObject(getFromArr(yesterdayData, 2)));
			// 昨日注册用户数(昨日注册总数-前日注册总数)
			rep.setYesterdayRegisterAmount(
					parseInteger(getFromArr(yesterdayData, 3)) - parseInteger(getFromArr(beforeyesterdayData, 3)));
			// 昨日实名认证用户数(昨日-前日)
			rep.setYesterdayVerifiedInvestorAmount(
					parseInteger(getFromArr(yesterdayData, 13)) - parseInteger(getFromArr(beforeyesterdayData, 13)));
			// 昨日投资人数(昨日-前日)
			rep.setYesterdayInvestorAmount(
					parseInteger(getFromArr(yesterdayData, 4)) - parseInteger(getFromArr(beforeyesterdayData, 4)));
			// 昨日新增投资(昨日-前日)
			rep.setYesterdayAddedInvestAmount(BigDecimalUtil.parseFromObject(getFromArr(yesterdayData, 2))
					.subtract(BigDecimalUtil.parseFromObject(getFromArr(beforeyesterdayData, 2))));

		}

		// 昨日渠道累计投资TOP5
		List<Object[]> channelTop5 = this.platformChannelStatisticsService.queryInvestTop5(yesterday);
		if (channelTop5 != null && channelTop5.size() > 0) {
			for (Object[] objects : channelTop5) {
				PlatformChartQueryRep an = new PlatformChartQueryRep();
				an.setName(objects[0] == null ? "" : objects[0].toString());// 渠道名称
				if (!StringUtils.isEmpty(objects[1])) {
					an.setValue(BigDecimalUtil.parseFromObject(objects[1].toString()));// 投资额
				}
				rep.getChannelRank().add(an);
			}
		}

		// 昨日产品新增投资TOP5
		List<Object[]> proTop5 = this.publisherProductStatisticsService.findTop5InvestorOfPlatform(yesterday);
		if (proTop5 != null && proTop5.size() > 0) {
			//从数据库里取出来时，金额大的在前面
			//但是，页面上是横向柱状图。所以要将金额大的放在后面，柱状图里金额大的才会显示在上面。
			for (Object[] objects : proTop5) {
				rep.getProInvestorRank().add(0,new PlatformChartQueryRep(objects[0] == null ? "" : objects[0].toString(),
						BigDecimalUtil.parseFromObject(objects[1])));
			}
		}

		// 平台交易额占比分析
		List<Object[]> platformTradeAnalyse = this.platformChannelStatisticsService.platformTradeAnalyse(yesterday);
		if (platformTradeAnalyse != null && platformTradeAnalyse.size() > 0 && platformTradeAnalyse.get(0) != null) {
			Object[] objects = platformTradeAnalyse.get(0);
			rep.getTradeAmountAnalyse()
					.add(new PlatformChartQueryRep("投资", BigDecimalUtil.parseFromObject(objects[0])));
			rep.getTradeAmountAnalyse()
					.add(new PlatformChartQueryRep("赎回", BigDecimalUtil.parseFromObject(objects[1])));
			rep.getTradeAmountAnalyse()
					.add(new PlatformChartQueryRep("还本付息", BigDecimalUtil.parseFromObject(objects[2])));
		}

		// 平台交易总额各渠道占比
		List<Object[]> channelTotalTop5 = this.platformChannelStatisticsService.queryTotalInvest(yesterday);
		if (channelTotalTop5 != null && channelTotalTop5.size() > 0) {
			for (Object[] objects : channelTotalTop5) {
				rep.getChannelAnalyse().add(new PlatformChartQueryRep(objects[0] == null ? "" : objects[0].toString(),
						BigDecimalUtil.parseFromObject(objects[1].toString())));
			}
		}
		// 投资人质量分析
		// -----------平台下投资人质量分析-----------
		List<Object[]> invstorList = this.publisherHoldService.analysePlatformInvestor();
		if (invstorList != null && invstorList.size() > 0) {
			// 取出各个范围的值
			for (Object[] objects : invstorList) {
				rep.getInvestorAnalyse().add(new PlatformChartQueryRep(getLevelName(objects[0]),
						BigDecimalUtil.parseFromObject(objects[1].toString())));
			}
		}


		return rep;
	}

	private Integer parseInteger(Object obj) {
		return obj == null ? SysConstant.INTEGER_defaultValue : Integer.parseInt(obj.toString());
	}

	private Object getFromArr(Object[] arr, int i) {
		if (arr != null && arr.length > i) {
			return arr[i];
		}

		return null;
	}

	private String getLevelName(Object level) {
		if (StringUtils.isEmpty(level)) {
			return "";
		}
		String levelStr = level.toString();
		switch (levelStr) {
		case "1":
			return "5万以下";
		case "2":
			return "5-10万";
		case "3":
			return "10-20万";
		case "4":
			return "20万以上";
		default:
			return "";
		}
	}
	
	/**
	 * 逾期次数
	 */
	public void increaseOverdueTimes(int overdueTimes) {
		if (0 == overdueTimes) {
			
		} else {
			PlatformStatisticsEntity st = this.findByPlatformBaseAccount();
			this.platformStatisticsDao.increaseOverdueTimes(st.getOid(), overdueTimes);
		}
		
	}

	/**
	 * 发行人数
	 */
	public int increatePublisherAmount() {
		return this.platformStatisticsDao.increatePublisherAmount(findByPlatformBaseAccount().getOid());
	}
	
	/**
	 * 活跃投资人数
	 */
	public int syncActiveInvestorAmount(int activeInvestorAmount) {
		return this.platformStatisticsDao.syncActiveInvestorAmount(findByPlatformBaseAccount().getOid(), activeInvestorAmount);
	}

	public List<Object[]> getPlatformStatisticsByBatch(String lastOid) {
		return this.platformStatisticsDao.getPlatformStatisticsByBatch(lastOid);
	}

	public PlatformHomeQueryRep queryAppAndPcHome() {
		PlatformHomeQueryRep rep = new PlatformHomeQueryRep();
		List<Object[]> objStatList = this.platformStatisticsDao.queryHomeStat();
		Object[] objData = objStatList.get(0);
		
		rep.setTotalTradeAmount(new BigDecimal(objData[0].toString()));
		rep.setRegisterAmount(StringToInt(objData[1].toString()));
		
		return rep;
	}
	
	private Integer StringToInt(String str) {
		Integer i = 0;
		if (!"".equals(str)) {
			double d = new Double(str);
			i = (int)d;
		}
		
		return i;
	}
	
	
}
