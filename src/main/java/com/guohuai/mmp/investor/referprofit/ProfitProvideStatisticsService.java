package com.guohuai.mmp.investor.referprofit;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountRep;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsEntity;
import com.guohuai.plugin.PageVo;

@Service
public class ProfitProvideStatisticsService {
	
	Logger logger = LoggerFactory.getLogger(ProfitProvideStatisticsService.class);
	
	@Autowired
	private ProfitProvideStatisticsDao profitProvideStatisticsDao;
	@Autowired
	private ProfitRuleDao profitRuleDao;
	@Autowired
	private ProfitDetailDao profitDetailDao;
	
	/**
	 * 邀请奖励明细收益统计
	 * @param req
	 * @return
	 */
	public PageVo<Map<String,Object>> queryProfitDetailStatistics(ProfitDetailStatisticsReq req){
		
		logger.info("===queryProfitDetailStatistics begin===");
		
		List<Object[]> profitDetailList = new ArrayList<Object[]>();
		PageVo<Map<String,Object>> pageVo = new PageVo<Map<String,Object>>();
		// 总条数
		int total = 0;
		List<Map<String,Object>> profitDetailAllList = new ArrayList<Map<String,Object>>();
		
		Date createTimeBegin = new Date();
		Date createTimeEnd = new Date();
		try {
			createTimeBegin = new SimpleDateFormat("yyyy-MM-dd").parse(req.getCreateTimeBegin());
			createTimeEnd = new SimpleDateFormat("yyyy-MM-dd").parse(req.getCreateTimeEnd());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		logger.info("===createTimeBegin:{},createTimeEnd:{}===",createTimeBegin,createTimeEnd);
		
		String userName = req.getUserName();
		String inviteType = req.getInviteType();
		String productType = req.getProductType();
		String productShortName = req.getProductShortName();
		
		// 根据条件查询邀请奖励收益详情
		profitDetailList = profitProvideStatisticsDao.getProfitDetailStatisticsList(userName, inviteType, productType, productShortName, createTimeBegin, createTimeEnd, (req.getPage() - 1) * req.getRow(), req.getRow());
		
		total = profitProvideStatisticsDao.getPageCountProfitDetailStatistics(userName, inviteType, productType, productShortName, createTimeBegin, createTimeEnd);
//		total = profitDetailList.size();
		
		logger.info("===profitDetailList===" + JSON.toJSONString(profitDetailList));
		logger.info("===profitDetailList.total:===" + total);
		
		Map<String,Object> mapSum = new HashMap<String,Object>();
		String sortName = "总计";
		BigDecimal totalInterestSum = new BigDecimal(0);
//		for (int i=0;i<profitDetailList.size();i++) {
//			Object[] profitDetail = profitDetailList.get(i);
//			totalInterestSum = totalInterestSum.add(new BigDecimal(profitDetail[2].toString()));
//		}
		// 封装汇总结果为List中的第一个元素
//		mapSum.put("sortName", sortName);
//		mapSum.put("totalInterest", totalInterestSum);
//		profitDetailAllList.add(0, mapSum);
		// 从表t_money_profit_rule查询投资人邀请比例
		ProfitRuleEntity profitRule = profitRuleDao.getProfitRule();
		
		// 数据列表
		for (int i =0;i < profitDetailList.size(); i++) {
			Map<String,Object> map = new HashMap<String,Object>();
			Object[] objprofitDetail = profitDetailList.get(i);
			logger.info("===第{}个收益明细详情{}===",i,JSON.toJSONString(objprofitDetail));
			map.put("interestId", objprofitDetail[0]);       // 收益id
			map.put("createTime", objprofitDetail[1]);     // 创建是时间
//			map.put("totalInterest", new BigDecimal(objprofitDetail[2].toString()));  // 奖励金额
			map.put("orderCode", objprofitDetail[3]);  // 订单编号
			map.put("productShortName", objprofitDetail[4]);  // 产品简称
			if("PRODUCTTYPE_02".equals(objprofitDetail[5])){
				map.put("productType", "活期");  // 产品类型
				map.put("rewardRatio", profitRule.getDemandFactor()+"%");  // 计算参数
				map.put("totalInterest", new BigDecimal(objprofitDetail[2].toString()).multiply(profitRule.getDemandFactor()).divide(new BigDecimal(100)).setScale(2,  BigDecimal.ROUND_HALF_DOWN));  // 奖励金额
				totalInterestSum = totalInterestSum.add(new BigDecimal(objprofitDetail[2].toString()).multiply(profitRule.getDemandFactor()).divide(new BigDecimal(100)).setScale(2,  BigDecimal.ROUND_HALF_DOWN));
			}else{
				map.put("productType", "定期");  // 产品类型
				map.put("rewardRatio", profitRule.getDepositFactor()+"%");  // 计算参数
				map.put("totalInterest", new BigDecimal(objprofitDetail[2].toString()).multiply(profitRule.getDepositFactor()).divide(new BigDecimal(100)).setScale(2,  BigDecimal.ROUND_HALF_DOWN));  // 奖励金额
				totalInterestSum = totalInterestSum.add(new BigDecimal(objprofitDetail[2].toString()).multiply(profitRule.getDepositFactor()).divide(new BigDecimal(100)).setScale(2,  BigDecimal.ROUND_HALF_DOWN));
			}
			map.put("investorName", objprofitDetail[6]);  // 投资人姓名
			map.put("firstInvitorName", objprofitDetail[7]);  // 一级邀请人姓名
			map.put("secondInvitorName", objprofitDetail[8]);  // 二级邀请人姓名
			
			profitDetailAllList.add(map);
		}
		mapSum.put("sortName", sortName);
		mapSum.put("totalInterest", totalInterestSum);
		profitDetailAllList.add(0,mapSum);
		
		pageVo.setRows(profitDetailAllList);
		pageVo.setTotal(total);
		pageVo.setRow(req.getRow());
		pageVo.setPage(req.getPage());
		pageVo.reTotalPage();
		
		logger.info("===queryProfitDetailStatistics end===");
		return pageVo;
	}
	
	/**
	 * 邀请奖励-奖励发放明细
	 * @param req
	 * @return
	 */
	public PageVo<Map<String,Object>> queryProfitProvideDetailStatistics(ProfitProvideDetailStatisticsReq req){
		logger.info("===queryProfitProvideDetailStatistics begin===");
		
		List<Object[]> profitProvideDetailList = new ArrayList<Object[]>();
		
		PageVo<Map<String,Object>> pageVo = new PageVo<Map<String,Object>>();
		// 总条数
		int total = 0;
		List<Map<String,Object>> profitProvideDetailAllList = new ArrayList<Map<String,Object>>();
		
		Date provideTimeBegin = new Date();
		Date provideTimeEnd = new Date();
		try {
			provideTimeBegin = new SimpleDateFormat("yyyy-MM-dd").parse(req.getProvideTimeBegin());
			provideTimeEnd = new SimpleDateFormat("yyyy-MM-dd").parse(req.getProvideTimeEnd());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		logger.info("===provideTimeBegin:{},provideTimeEnd:{}===",provideTimeBegin,provideTimeEnd);
		
		String userName = req.getUserName();
		String provideRewardStatus = req.getProvideRewardStatus();
		String productType = req.getProductType();
		String productShortName = req.getProductShortName();
		
		// 根据条件查询邀请奖励收益详情
		profitProvideDetailList = profitProvideStatisticsDao.getProfitProvideDetailStatisticsList(userName, provideRewardStatus, productType, productShortName, provideTimeBegin, provideTimeEnd, (req.getPage() - 1) * req.getRow(), req.getRow());
		
		total = profitProvideStatisticsDao.getPageCountProfitProvideDetail(userName, provideRewardStatus, productType, productShortName, provideTimeBegin, provideTimeEnd);
//		total = profitProvideDetailList.size();
		
		logger.info("===profitProvideDetailList:{}",JSON.toJSONString(profitProvideDetailList));
		logger.info("===profitProvideDetailList.total:{}===",total);
		
		Map<String,Object> mapSum = new HashMap<String,Object>();
		String sortName = "总计";
		BigDecimal totalInterestSum = new BigDecimal(0);
		for (int i=0;i<profitProvideDetailList.size();i++) {
			Object[] profitDetail = profitProvideDetailList.get(i);
			totalInterestSum = totalInterestSum.add(new BigDecimal(profitDetail[2].toString()));
		}
		// 封装汇总结果为List中的第一个元素
		mapSum.put("sortName", sortName);
		mapSum.put("totalInterest", totalInterestSum);
		profitProvideDetailAllList.add(0, mapSum);
		// 从表t_money_profit_rule查询投资人邀请比例
		ProfitRuleEntity profitRule = profitRuleDao.getProfitRule();
		
		// 数据列表
		for (int i =0;i < profitProvideDetailList.size(); i++) {
			Map<String,Object> map = new HashMap<String,Object>();
			Object[] objProfitProvideDetail = profitProvideDetailList.get(i);
			map.put("rewardId", objProfitProvideDetail[0]);       // 收益id
			map.put("createTime", objProfitProvideDetail[1]);     // 创建是时间
			map.put("totalInterest", new BigDecimal(objProfitProvideDetail[2].toString()));  // 奖励金额
//			map.put("rewardRatio", profitRule.getInvestorFactor()+"%");  // 计算参数
			map.put("provideUserName", objProfitProvideDetail[3]);  // 发放人姓名
			
			// 根据收益明细id（profitId）查询收益明细
			ProfitDetailEntity profitDetail = profitDetailDao.findOne(objProfitProvideDetail[7].toString());
			String investorOid = profitDetail.getInvestorOid();
			String firstInvestorOid = "";
			if (profitDetail.getFirstOid() != null) {
				firstInvestorOid = profitDetail.getFirstOid();
			}
			String secondInvestorOid = "";
			if (profitDetail.getSecondOid() != null) {
				secondInvestorOid = profitDetail.getSecondOid();
			}
			
			logger.info("===investorOid:{},firstInvestorOid:{},secondInvestorOid:{}===",investorOid,firstInvestorOid,secondInvestorOid);
			
			if(investorOid.equals(objProfitProvideDetail[13])){
				map.put("provideInviteType", "投资人");  // 发放人邀请类型
				map.put("rewardRatio", profitRule.getInvestorFactor()+"%");  // 计算参数
			}else if(firstInvestorOid.equals(objProfitProvideDetail[13])){
				map.put("provideInviteType", "一级邀请人");  // 发放人邀请类型
				map.put("rewardRatio", profitRule.getFirstFactor()+"%");  // 计算参数
			}else{
				map.put("provideInviteType", "二级邀请人");  // 发放人邀请类型
				map.put("rewardRatio", profitRule.getSecondFactor()+"%");  // 计算参数
			}
			
			map.put("sourceProvideName", objProfitProvideDetail[4]);  // 来源人
			map.put("provideDate", objProfitProvideDetail[5]);  // 发放日期
			if("toClose".equals(objProfitProvideDetail[6])){
				map.put("provideStatus", "待发放");  // 发放状态
			}else{
				map.put("provideStatus", "已发放");  // 发放状态
			}
			map.put("rewardId", objProfitProvideDetail[7]);  // 收益明细id
			map.put("orderCode", objProfitProvideDetail[8]);  // 订单编号
			map.put("payDate", objProfitProvideDetail[9]);  // 购买日期
			map.put("productShortName", objProfitProvideDetail[10]);  // 产品简称
			if("PRODUCTTYPE_02".equals(objProfitProvideDetail[11])){
				map.put("productType", "活期");  // 产品类型
			}else{
				map.put("productType", "定期");  // 产品类型
			}
			profitProvideDetailAllList.add(map);
		}		
		pageVo.setRows(profitProvideDetailAllList);
		pageVo.setTotal(total);
		pageVo.setRow(req.getRow());
		pageVo.setPage(req.getPage());
		pageVo.reTotalPage();
		
		logger.info("===queryProfitProvideDetailStatistics end===");
		return pageVo;
	}
	
	/**
	 * 累计奖励统计
	 * @param uid
	 * @return
	 */
	public ProfitProvideTotalRep profitProvideTotal() {
		ProfitProvideTotalRep rep = new ProfitProvideTotalRep();
		
		BigDecimal providedTotalAmout = profitProvideStatisticsDao.getProvidedTotalAmout();
		BigDecimal toProvideTotalAmout = profitProvideStatisticsDao.getToProvideTotalAmout();
		int providedTotalNums = profitProvideStatisticsDao.getProvidedTotalNums();
		
		logger.info("===providedTotalAmout:{},toProvideTotalAmout{},providedTotalNums:{}===",providedTotalAmout,toProvideTotalAmout,providedTotalNums);
		
		rep.setProvidedTotalAmout(providedTotalAmout); // 当前已发放总金额
		rep.setToProvideTotalAmout(toProvideTotalAmout); // 当前待发放总金额
		rep.setProvidedTotalNums(providedTotalNums); // 平台累计奖励人数
		return rep;
	}
}
