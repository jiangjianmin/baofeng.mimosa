package com.guohuai.cardvo.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.basic.message.DealMessageEntity;
import com.guohuai.cardvo.dao.TradeOrderStatisticsDao;
import com.guohuai.cardvo.entity.TradeOrderStatisticsEntity;
import com.guohuai.component.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 中间表统计service
 * @author yujianlong
 *
 */
@Service
@Transactional
@Slf4j
public class TradeOrderStatisticsService {
	@Autowired
	private TradeOrderStatisticsDao tradeOrderStatisticsDao;
	
	/**
	 * 投资时处理中间表
	 * @param messageEntity
	 */
	@Transactional
	public Map<String,String> Handle(DealMessageEntity messageEntity){
		
		String userOid=messageEntity.getTradeorder_stastistics_userOid();
		String phoneNum=messageEntity.getTradeorder_stastistics_phoneNum();
		BigDecimal payAmount=messageEntity.getTradeorder_stastistics_payAmount();
		Timestamp orderTime=new Timestamp(messageEntity.getTradeorder_stastistics_orderTime());
		String orderCode=messageEntity.getTradeorder_stastistics_orderCode();
		return handle(userOid, phoneNum, payAmount, orderTime,orderCode);
		
	}

	private Map<String,String>  handle(String userOid,String phoneNum
			,BigDecimal payAmount,Timestamp orderTime,String orderCode ){
		Map<String,String> result=new HashMap<>();
		TradeOrderStatisticsEntity entity=tradeOrderStatisticsDao.findEntityByOid(userOid);
		
//		int orderCount = tradeOrderStatisticsDao.findByInvestorOid(userOid);
		if(null!=entity) {
			log.info("投资统计数据存在，（用于判断是否重复统计）");
			log.info("更新用户投资统计数据,手机号:{}，金额:{},订单号:{}", phoneNum,payAmount,orderCode);
			try {
				int count=0;
				if (entity.getLastTradeTime().compareTo(orderTime)>0) {
					count=tradeOrderStatisticsDao.updateByInvestorOid(userOid, payAmount,entity.getLastTradeTime());
				}else{
					count=tradeOrderStatisticsDao.updateByInvestorOid(userOid, payAmount,orderTime);
				}
			} catch (Exception e) {
				//TODO 记录失败的日志,记录在消费者端tulip。
				result.put("status", "401");
				result.put("desc", "中间表操作失败,userid:"+userOid+",订单号:"+orderCode);
				log.error("userId:{},payAmount:{},中间表更新失败",userOid,payAmount);
			}
			
		}else {
			TradeOrderStatisticsEntity tradeOrderStatistics = new TradeOrderStatisticsEntity();
			tradeOrderStatistics.setOid(StringUtil.uuid()).setInvestorOid(userOid)
			.setTotalInvestAmount(payAmount).setTotalInvestCount(1).setLastTradeTime(orderTime)
			.setCreateTime(orderTime).setUpdateTime(orderTime);
			log.info("创建用户投资统计数据：{}", JSONObject.toJSONString(tradeOrderStatistics));
			try {
				tradeOrderStatistics=tradeOrderStatisticsDao.save(tradeOrderStatistics);
			} catch (Exception e) {
				//TODO 记录失败的日志
				result.put("status", "401");
				result.put("desc", "中间表操作失败,userid:"+userOid+",订单号:"+orderCode);
				log.error("userId:{},payAmount:{},中间表保存失败",userOid,payAmount);
			}
		}
		result.put("status", "200");
		result.put("desc", "中间表操作成功,userid:"+userOid+",订单号:"+orderCode);
		return result;
	}
	

	
}
