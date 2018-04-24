package com.guohuai.mmp.investor.referprofit;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.OnSaleT0ProductRep;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.util.ZsetRedisUtil;
import com.guohuai.mmp.investor.tradeorder.InvestorInvestTradeOrderService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.TradeOrderRep;
import com.guohuai.mmp.investor.tradeorder.TradeOrderReq;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;

import lombok.extern.slf4j.Slf4j;
/**
 * 
 * @ClassName: ProfitProvideDetailService
 * @Description: 二级邀请--奖励发放明细
 * @author yihonglei
 * @date 2017年6月13日 下午4:30:04
 * @version 1.0.0
 */
@Service
@Slf4j
@Transactional
public class ProfitProvideDetailService {
	
	@Autowired
	private RedisTemplate<String, String> redis;
	@Autowired
	private ProfitProvideDetailDao profitProvideDetailDao;// 奖励发放明细
	@Autowired
	private ProfitRuleDao profitRuleDao;// 奖励收益规则
	@Autowired
	private ProfitProvideRecordDao profitProvideRecordDao;// 奖励发放记录
	@Autowired
	private InvestorInvestTradeOrderService investorInvestTradeOrderService;
	@Autowired
	private ProductService productService;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private JobLogService jobLogService;
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: initProductProfitProvideDetail
	 * @Description: 根据奖励收益明细和奖励规则生成奖励发放明细
	 * @param product
	 * @return int
	 * @date 2017年6月13日 下午4:39:31
	 * @since  1.0.0
	 */
	public int initProductProfitProvideDetail(String productOid) {
		Product product = productService.findByOid(productOid);// 以上传的产品，取不到产品类型，再根据产品id查一遍产品（?）
		log.info("根据奖励收益明细和奖励规则生成奖励发放明细开始!产品ID:{},产品名称:{},产品类型:{}", product.getOid(), product.getName(), product.getType().getOid());
		// 发放明细处理结果
		int result = 1;
		// 查询奖励收益规则
		List<ProfitRuleEntity> profitRuleList = this.profitRuleDao.findAll();
		if (profitRuleList.size() > 0) {
			ProfitRuleEntity profitRuleEntity = profitRuleList.get(0);
			
			BigDecimal productFactor = profitRuleEntity.getDepositFactor().divide(new BigDecimal(100));// 定期所占比例
			BigDecimal investorFactor = profitRuleEntity.getInvestorFactor().divide(new BigDecimal(100));// 投资人所占比例
			BigDecimal firstFactor = profitRuleEntity.getFirstFactor().divide(new BigDecimal(100));// 一级邀请人所占比例
			BigDecimal secondFactor = profitRuleEntity.getSecondFactor().divide(new BigDecimal(100));// 二级邀请人所占比例
			
			Date t0Date = null;
			if (Product.TYPE_Producttype_02.equals(product.getType().getOid())) {// 如果是活期，需要根据月度去查询
				Calendar ca = Calendar.getInstance();
				ca.add(Calendar.MONTH, -1);// 月份减1
				t0Date = ca.getTime();
				
				productFactor = profitRuleEntity.getDemandFactor().divide(new BigDecimal(100));// 活期所占比例
			}
			/*
			 * 奖励发放明细分为3种情况进行计算:
			 * 1. 投资人奖励发放明细
			 * 2. 一级邀请人奖励发放明细
			 * 3. 二级邀请人奖励发放明细
			 */
			log.info("<--------t0Date:{}--------->", t0Date);
			// 1. 投资人奖励发放明细
			this.profitProvideDetailDao.proInvestorProfitProvideDetail(product.getOid(), 
					productFactor, investorFactor, t0Date, product.getType().getOid());
			// 2. 一级邀请人奖励发放明细
			this.profitProvideDetailDao.proFirstProfitProvideDetail(product.getOid(), 
					productFactor, firstFactor, t0Date, product.getType().getOid());
			// 3. 二级邀请人奖励发放明细
			this.profitProvideDetailDao.proSecondProfitProvideDetail(product.getOid(), 
					productFactor, secondFactor, t0Date, product.getType().getOid());
		} else {
			log.info("二级邀请奖励收益规则为空，不能进行奖励发放明细生成!");
			result = 0;
		}
		
		log.info("根据奖励收益明细和奖励规则生成奖励发放明细,发放结果为:{}", result > 0);
		return result;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: provideProfit
	 * @Description: 二级邀请奖励发放job
	 * @return void
	 * @date 2017年6月16日 上午10:52:55
	 * @since  1.0.0
	 */
	public void provideProfit() {

		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_provideProfit)) {
			this.provideProfitLog();
		}
		
	}

	public void provideProfitLog() {
		JobLogEntity jobLog =  JobLogFactory.getInstance(JobLockEntity.JOB_jobId_provideProfit);
		try {
			this.provideProfitDo();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_provideProfit);
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: provideProfitDo
	 * @Description: 二级邀请奖励收益发放
	 * @return void
	 * @date 2017年6月14日 下午3:52:30
	 * @since  1.0.0
	 */
	public void provideProfitDo() {
		log.info("<------二级邀请奖励收益发放开始------>");
		// 1. 获取可投资活期产品
		OnSaleT0ProductRep prodcutRep = productService.getOnSaleProductOid();
		if(prodcutRep == null) {
			throw new AMPException("暂无可售的活期产品");
		}
		String productOid = prodcutRep.getProductOid();
		// 2.奖励投资
		if (!"".equals(productOid)) {
			String lastYearMonth = ProfitSpecialDateUtil.getLastYearMonth();// 上月月日
			List<Object[]> profitList = this.profitProvideDetailDao.getProfitProvideDetailSum(lastYearMonth);
			if (profitList.size() > 0) {
				for (int i=0; i<profitList.size(); i++) {
					Object[] objData = profitList.get(i);
					TradeOrderReq tradeOrderReq = new TradeOrderReq();
					tradeOrderReq.setUid(objData[0].toString());
					tradeOrderReq.setMoneyVolume(new BigDecimal(objData[1].toString()));
					tradeOrderReq.setProductOid(productOid);
					// 创建二级邀请奖励收益投资订单
					InvestorTradeOrderEntity orderEntity = this.investorInvestTradeOrderService.createInvestProfitTradeOrder(tradeOrderReq);
					// 投资
					TradeOrderRep tradeOrderRep = investorInvestTradeOrderService.investRequireNew(orderEntity.getOrderCode());
					log.info("投资订单Id:{}, 订单状态:{}", tradeOrderRep.getTradeOrderOid(), tradeOrderRep.getOrderStatus());
					if (InvestorTradeOrderEntity.TRADEORDER_orderStatus_toPay.equals(tradeOrderRep.getOrderStatus())) {
						// 按投资人和月份，更新发放状态
						this.updateProfitProvideDetailStatus(objData[0].toString(), objData[2].toString());
						// 存入发放记录
						this.saveProfitProvideRecord(objData,orderEntity);
					}
				}
			} else {
				log.info("无可以进行二级邀请奖励收益发放的明细!");
			}
		}
		log.info("<------二级邀请奖励收益发放结束------>");
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: updateProfitProvideDetailStatus
	 * @Description: 按投资人和月份，更新发放状态
	 * @param provideOid
	 * @param provideMonth
	 * @return void
	 * @date 2017年6月28日 下午8:54:40
	 * @since  1.0.0
	 */
	private void updateProfitProvideDetailStatus(String provideOid, String provideMonth) {
		try {
			log.info(" 按投资人和月份，更新发放状态，provideOid:{}, provideMonth:{}", provideOid, provideMonth);
			int result = this.profitProvideDetailDao.updateProfitProvideDetailStatus(provideOid, provideMonth);
			log.info(" 按投资人和月份，更新发放状态，结果为:{}", result>0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: saveProfitProvideRecord
	 * @Description: 二级邀请奖励发放记录
	 * @param objData
	 * @param orderEntity
	 * @return void
	 * @date 2017年6月15日 上午11:04:17
	 * @since  1.0.0
	 */
	private void saveProfitProvideRecord(Object[] objData, InvestorTradeOrderEntity orderEntity) {
		try {
			log.info("<------二级邀请奖励发放记录开始!------>");
			ProfitProvideRecordEntity profitProvideRecordEntity = new ProfitProvideRecordEntity();
			profitProvideRecordEntity.setOid(StringUtil.uuid());
			profitProvideRecordEntity.setProvideOid(objData[0].toString());
			profitProvideRecordEntity.setOrderOid(orderEntity.getOid());
			profitProvideRecordEntity.setProvideAmount(new BigDecimal(objData[1].toString()));
			profitProvideRecordEntity.setMonth(objData[2].toString());
			profitProvideRecordEntity.setProvideDate(new Date());
			profitProvideRecordEntity.setCreateTime(new Date());
			
			this.profitProvideRecordDao.save(profitProvideRecordEntity);
			log.info("<------二级邀请奖励发放记录结束!------>");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: showProvideDetail
	 * @Description: 二级邀请--我的奖励详情列表
	 * @param req
	 * @param uid
	 * @return ProfitClientRep<Map<String,Object>>
	 * @date 2017年6月15日 下午2:48:46
	 * @since  1.0.0
	 */
	public ProfitClientRep<Map<String, Object>> showProvideDetail(ProfitClientReq req, String uid) {
		// 响应结果
		ProfitClientRep<Map<String, Object>> rep = new ProfitClientRep<Map<String, Object>>();
		// 查询条件处理
		int status = req.getStatus();// 状态
		int timeDemision = this.dealTimeDemision(req.getTimeDemision());// 时间段
		// 奖励汇总查询
		List<Object[]> objProfitStatList = this.profitProvideDetailDao.getProfitProvideDetailStat(uid);
		// 邀请奖励记录查询
		List<Object[]> objProfitInvestList = this.profitProvideDetailDao.getProfitProvideDetailList(uid, status, req.getTimeDemision(), 
				timeDemision, (req.getPage() - 1) * req.getRow(), req.getRow());
		// 邀请奖励总条数
		int total = this.profitProvideDetailDao.getProfitProvideDetailCount(uid, status, req.getTimeDemision(), timeDemision);
		// 返回结果封装
		if (objProfitStatList.size() > 0 ) {
			Object[] objStatSumData = objProfitStatList.get(0);
			rep.setTotalAmount(objToBigdecimal(objStatSumData[0]));// 累计奖金金额
			rep.setToPayAmount(objToBigdecimal(objStatSumData[1]));// 待结算金额
			rep.setPayAmount(objToBigdecimal(objStatSumData[2]));// 已打款金额
			
			List<Map<String,Object>> userProfitInvestList = new ArrayList<Map<String,Object>>();// 邀请奖励详情列表
			if (objProfitInvestList.size() > 0) {
				for (int i=0; i<objProfitInvestList.size(); i++) {
					Object[] objProfitInvestData = objProfitInvestList.get(i);
					Map<String,Object> mapData = new HashMap<String,Object>();
					
					mapData.put("phone", objToString(objProfitInvestData[0]).substring(0, 3)+"****"+objToString(objProfitInvestData[0]).substring(7, 11));// 好友用户名
					mapData.put("payDate", objToString(objProfitInvestData[1]));// 购买时间
					mapData.put("productName", objToString(objProfitInvestData[2]));// 购买产品
					mapData.put("amount", objToString(objProfitInvestData[3]));// 奖金金额
					
					userProfitInvestList.add(mapData);
				}
			}
			
			rep.setRows(userProfitInvestList);
			rep.setTotal(total);
			rep.setRow(req.getRow());
			rep.setPage(req.getPage());
			rep.reTotalPage();
		}
		
		return rep;
	}
	/** 时间维度处理 */
	public int dealTimeDemision (int timeDemision) {
		int time = -1;
		switch (timeDemision) {
		case 0:
			time = 7;
			break;
		case 1:
			time = 30;
			break;
		case 2:
			time = 90;
			break;
		default:
			time = -1;
			break;
		}
		
	    return time;
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: showProfitProvideRank
	 * @Description: 龙虎榜排行查询
	 * @param req
	 * @return ProfitClientRep<Map<String,Object>>
	 * @date 2017年6月15日 下午7:05:56
	 * @since  1.0.0
	 */
	public ProfitClientRep<Map<String, Object>> showProfitProvideRank(ProfitClientReq req) {
		List<String> rankStrList = new ArrayList<String>();
		switch (req.getRank()) {
			case 0: // 日榜
				rankStrList = ZsetRedisUtil.zRange(redis, ZsetRedisUtil.SECOND_LEVEL_PROFIT_RANK+ProfitProvideDetailEntity.SECOND_LEVEN_RANK_DAY, 0, -1);
				break;
			case 1: // 周榜
				rankStrList = ZsetRedisUtil.zRange(redis, ZsetRedisUtil.SECOND_LEVEL_PROFIT_RANK+ProfitProvideDetailEntity.SECOND_LEVEN_RANK_WEEK, 0, -1);
				break;
			case 2: // 月榜
				rankStrList = ZsetRedisUtil.zRange(redis, ZsetRedisUtil.SECOND_LEVEL_PROFIT_RANK+ProfitProvideDetailEntity.SECOND_LEVEN_RANK_MONTH, 0, -1);
				break;
		}
		
		ProfitClientRep<Map<String, Object>> rep = dealRank(rankStrList);
		
		return rep;
	}
	/** 龙虎榜数据处理 */
	public ProfitClientRep<Map<String, Object>> dealRank(List<String> rankStrList) {
		ProfitClientRep<Map<String, Object>> rep = new ProfitClientRep<Map<String, Object>>();
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		if (rankStrList.size() > 0) {
			for (int i=0;i<rankStrList.size();i++) {
				String[] arrStr = rankStrList.get(i).split(",");
				Map<String,Object> mapData = new HashMap<String,Object>();
				mapData.put("rankSort", arrStr[0]);// 名次
				mapData.put("phone", arrStr[1].substring(0, 3)+"****"+arrStr[1].substring(7, 11));// 用户名
				mapData.put("provideAmount", arrStr[2]);// 奖励金额
				
				list.add(mapData);
			}
		}
		rep.setRows(list);
		
		return rep;
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: profitRank
	 * @Description: 龙虎榜统计job
	 * @return void
	 * @date 2017年6月16日 上午10:50:21
	 * @since  1.0.0
	 */
	public void profitRank() {

		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_profitRank)) {
			this.profitRankLog();
		}
		
	}

	public void profitRankLog() {
		JobLogEntity jobLog =  JobLogFactory.getInstance(JobLockEntity.JOB_jobId_profitRank);
		try {
			this.profitRankDo();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_profitRank);
	}
	
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public void profitRankDo() {
		// 获取当前系统时间--周
		Calendar cal=Calendar.getInstance();
		cal.setTime(new Date()); 
		int week=cal.get(Calendar.DAY_OF_WEEK)-1;
		// 获取当前系统时间--号
		SimpleDateFormat sdf = new SimpleDateFormat("dd");
		String dateStr = sdf.format(new Date());
		// 查询昨日榜
		List<Object[]> rankDayList = this.profitProvideDetailDao.getProfitProvideRankDay(ProfitSpecialDateUtil.getLastDay());
		this.saveRankRedis(rankDayList, ProfitProvideDetailEntity.SECOND_LEVEN_RANK_DAY);
	    // 查询上周榜
		if (week == 1) {
			String lastMondayDate = ProfitSpecialDateUtil.getLastWeekDate().get("lastMondayDate");
			String lastSundayDate = ProfitSpecialDateUtil.getLastWeekDate().get("lastSundayDate");
			List<Object[]> rankWeekList = this.profitProvideDetailDao.getProfitProvideRankWeek(lastMondayDate, lastSundayDate);
			this.saveRankRedis(rankWeekList, ProfitProvideDetailEntity.SECOND_LEVEN_RANK_WEEK);
		}
		// 查询上月榜
		if ("01".equals(dateStr)) {
			String lastMonthStart = ProfitSpecialDateUtil.getLastMonthDate().get("lastMonthStart");
			String lastMonthEnd = ProfitSpecialDateUtil.getLastMonthDate().get("lastMonthEnd");
			List<Object[]> rankMonthList = this.profitProvideDetailDao.getProfitProvideRankMonth(lastMonthStart, lastMonthEnd);
			this.saveRankRedis(rankMonthList, ProfitProvideDetailEntity.SECOND_LEVEN_RANK_MONTH);
		}
	}
	
	public void saveRankRedis(List<Object[]> list, String key) {
		if (list.size() > 0) {
			ZsetRedisUtil.zRemRange(redis, ZsetRedisUtil.SECOND_LEVEL_PROFIT_RANK+key, 0, -1);
			for (int i=0;i<list.size();i++) {
				Object[] objData = list.get(i);
				String rankStr = (i+1) + "," + objToString(objData[0]) + "," + objToString(objData[1]);
				ZsetRedisUtil.zAdd(redis, ZsetRedisUtil.SECOND_LEVEL_PROFIT_RANK+key, rankStr, i+1);
			}
		}
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: objToString
	 * @Description: 对象转化为toString,避免空指针转换异常
	 * @param obj
	 * @return String
	 * @date 2017年6月13日 下午9:25:18
	 * @since  1.0.0
	 */
	public String objToString(Object obj) {
		String str = "";
		if (null != obj) {
			str = obj.toString();
		}
		return str;
	}
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: objToBigdecimal
	 * @Description: Object to Bigdecimal
	 * @param objData
	 * @return BigDecimal
	 * @date 2017年6月15日 下午5:00:04
	 * @since  1.0.0
	 */
	public BigDecimal objToBigdecimal(Object obj) {
		String str = "0";
		if (null != obj) {
			str = obj.toString();
		}
		BigDecimal big = new BigDecimal(str);
		return big.setScale(2, BigDecimal.ROUND_UP);
	}
	
}
