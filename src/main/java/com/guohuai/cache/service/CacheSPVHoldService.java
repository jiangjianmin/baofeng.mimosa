package com.guohuai.cache.service;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.basic.common.StringUtil;
import com.guohuai.cache.CacheKeyConstants;
import com.guohuai.cache.entity.SPVHoldCacheEntity;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.util.HashRedisUtil;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.platform.redis.RedisSyncService;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;

@Service
public class CacheSPVHoldService {
	
	Logger logger = LoggerFactory.getLogger(CacheSPVHoldService.class);
	@Autowired
	private RedisTemplate<String, String> redis;
	@Autowired
	private RedisExecuteLogExtService redisExecuteLogExtService;
	@Autowired
	private RedisSyncService redisSyncService;

	/**
	 * 校验SPV持仓
	 */
	public void checkSpvHold4Invest(InvestorTradeOrderEntity orderEntity) {
		BigDecimal orderVolume = orderEntity.getOrderAmount();

		BigDecimal valOut = redisExecuteLogExtService.hincrByBigDecimal(
				CacheKeyConstants.getSpvHKey(orderEntity.getProduct().getOid()), "lockRedeemHoldVolume", orderVolume,
				orderVolume.negate());
		SPVHoldCacheEntity spvHold = getSPVHoldCacheEntityByProductId(orderEntity.getProduct().getOid());
		if (spvHold.getTotalVolume().compareTo(valOut) < 0) {
			redisSyncService.saveEntityRefSpvHoldRequireNew(orderEntity.getInvestorBaseAccount().getOid(), 
					orderEntity.getProduct().getOid(), 
					orderEntity.getProduct().getAssetPool().getOid());
			// error.define[30069]=SPV可售份额不足(CODE:30069)
			throw new AMPException(30069);
		}
	}

	/**
	 * 根据产品Oik获取SPV持仓
	 * 
	 * @param productOid
	 * @return
	 */
	private SPVHoldCacheEntity getSPVHoldCacheEntityByProductId(String productOid) {
		Map<String, String> mapSPV = HashRedisUtil.hgetall(redis, CacheKeyConstants.SPVHOLD_CACHE_KEY + productOid);
		SPVHoldCacheEntity svpHold = JSONObject.parseObject(JSONObject.toJSONString(mapSPV), SPVHoldCacheEntity.class);
		logger.info("SPVHoldCacheEntity.zoomIn.key={}{}:{}", CacheKeyConstants.SPVHOLD_CACHE_KEY, productOid);
		DecimalUtil.zoomIn(svpHold);
		return svpHold;
	}

	/**
	 * 更新SPV持仓
	 */
//	public void update4InvestConfirm(String productOid, BigDecimal orderVolume, String batchNo) {
//		
//		String hkey = CacheKeyConstants.SPVHOLD_CACHE_KEY + productOid;
//		// redis操作lockRedeemHoldVolume
//		BigDecimal valOut = redisExecuteLogExtService.hincrByBigDecimal(batchNo, hkey,
//							"lockRedeemHoldVolume", orderVolume.negate(), orderVolume);
//		
//		if (valOut.compareTo(BigDecimal.ZERO) < 0) {
//			// error.define[30024]=针对SPV持仓份额确认失败(CODE:30024)
//			throw new AMPException(30024);
//		}
//		
//		valOut = redisExecuteLogExtService.hincrByBigDecimal(batchNo, hkey,
//				"totalVolume", orderVolume.negate(), orderVolume);
//		if (valOut.compareTo(BigDecimal.ZERO) < 0) {
//		
//			// error.define[30024]=针对SPV持仓份额确认失败(CODE:30024)
//			throw new AMPException(30024);
//		}
//		
//	}

	/**
	 * SPV赎回确认
	 */
//	public void update4RedeemConfirm(String productOid, BigDecimal orderVolume, String batchNo) {
//		SPVHoldCacheEntity spvHold = getSPVHoldCacheEntityByProductId(productOid);
//		String hkey = CacheKeyConstants.SPVHOLD_CACHE_KEY + productOid;
//		if (spvHold.getTotalVolume().compareTo(orderVolume) >= 0) {
//			// redis操作totalVolume
//			redisExecuteLogExtService.hincrByBigDecimal(batchNo, hkey, "totalVolume",
//					orderVolume, orderVolume.negate());
//		} else {
//			// error.define[30024]=针对SPV持仓份额确认失败(CODE:30024)
//			throw new AMPException(30024);
//		}
//	}

	/**
	 * 新建SPV持仓
	 */
	public void createSPVHoldCache(PublisherHoldEntity holdEntity) {
		SPVHoldCacheEntity cacheEntity = new SPVHoldCacheEntity();
		cacheEntity.setProductOid(holdEntity.getProduct().getOid());
		cacheEntity.setTotalVolume(holdEntity.getTotalVolume());
		cacheEntity.setLockRedeemHoldVolume(holdEntity.getLockRedeemHoldVolume());
		
		logger.info("createSPVHoldCache productOid={}", holdEntity.getProduct().getOid());
		redisExecuteLogExtService.redisExecuteHMSET(StringUtil.uuid(),
				CacheKeyConstants.getSpvHKey(cacheEntity.getProductOid()), cacheEntity);
	}
	
	/**
	 * 同步totalVolume到redis
	 */
//	public void syncSpvHoldTotalVolume(Product product, BigDecimal orderVolume) {
//		if (null != product) {
//			logger.info("syncSpvHoldTotalVolume productOid={}", product.getOid());
//			redisExecuteLogExtService.hincrByBigDecimal(CacheKeyConstants.getSyncSpvHoldTotalVolume(),
//					CacheKeyConstants.getSpvHKey(product.getOid()), "totalVolume", orderVolume, orderVolume.negate());
//		}
//	}
//	
//	/**
//	 * 废单：解锁SPV锁定份额 
//	 */
//	public void updateSpvHold4InvestAbandon(InvestorTradeOrderEntity orderEntity, String batchNo) {
//		BigDecimal valOut = redisExecuteLogExtService.hincrByBigDecimal(batchNo,
//				CacheKeyConstants.getSpvHKey(orderEntity.getProduct().getOid()), "lockRedeemHoldVolume",
//				orderEntity.getOrderVolume().negate(), orderEntity.getOrderVolume());
//		
//		// error.define[30035]=废申购单时SPV持仓锁定份额异常(CODE:30035)
//		DecimalUtil.isValOutGreatThanOrEqualZero(valOut, 30035);
//	}
	
}
