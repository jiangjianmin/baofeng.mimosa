package com.guohuai.cache.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.cache.CacheConfig;
import com.guohuai.cache.CacheKeyConstants;
import com.guohuai.cache.entity.HoldCacheEntity;
import com.guohuai.cache.entity.ProductCacheEntity;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.util.HashRedisUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.util.ZsetRedisUtil;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.platform.redis.RedisSyncService;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;
@Service
public class CacheHoldService {
	Logger logger = LoggerFactory.getLogger(CacheHoldService.class);
	@Autowired
	private RedisTemplate<String, String> redis;
	@Autowired
	private CacheProductService cacheProductService;
	@Autowired
	private RedisExecuteLogExtService redisExecuteLogExtService;
	@Autowired
	private RedisSyncService redisSyncService;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private CacheConfig cacheConfig;
	@Autowired
	private ProductService productService;
	@Autowired
	private PublisherHoldService publisherHoldService;
	@Autowired
	private ProductIncomeRewardCacheService rewardCacheService;
	
	/**
	 * 产品单人单日赎回上限
	 * 产品单日赎回次数
	 */
	public void redeemDayRules(InvestorTradeOrderEntity orderEntity) {
		BigDecimal orderVolume = orderEntity.getOrderAmount();
		Product product = orderEntity.getProduct();
		String hkey = CacheKeyConstants.getHoldHKey(orderEntity.getInvestorBaseAccount().getOid(), orderEntity.getProduct().getOid());
		
		if (DecimalUtil.isGoRules(product.getSingleDailyMaxRedeem())) {
			BigDecimal valOut = redisExecuteLogExtService.hincrByBigDecimal(hkey,
					"dayRedeemVolume", orderVolume, orderVolume.negate());
			//-------------------------超级用户--------------2017.04.17-----
			if(!investorBaseAccountService.isSuperMan(orderEntity)){
				if (valOut.compareTo(product.getSingleDailyMaxRedeem()) > 0) {
					redisSyncService.saveEntityRefInvestorHoldRequireNew(orderEntity.getInvestorBaseAccount().getOid(), 
							orderEntity.getProduct().getOid(), 
							orderEntity.getProduct().getAssetPool().getOid());
					// error.define[30032]=超过产品单人单日赎回上限(CODE:30032)
					throw AMPException.getException(30032);
				}
			}
		}
		if (DecimalUtil.isGoRules(product.getSingleDayRedeemCount())) {
			Long valOut = redisExecuteLogExtService.hincrByLong(hkey, "dayRedeemCount", 1, -1);
			//-------------------------超级用户--------------2017.04.17-----
			if(!investorBaseAccountService.isSuperMan(orderEntity)){
				if (valOut > product.getSingleDayRedeemCount()) {
					redisSyncService.saveEntityRefInvestorHoldRequireNew(orderEntity.getInvestorBaseAccount().getOid(), 
							orderEntity.getProduct().getOid(), 
							orderEntity.getProduct().getAssetPool().getOid());
					throw new AMPException("超过单日赎回次数" + product.getSingleDayRedeemCount() + "次");
				}
			}
		}
		
	}
	/**
	 * 校验所购产品最大持仓
	 */
	public void checkMaxHold4Invest(InvestorTradeOrderEntity orderEntity) {
		if (DecimalUtil.isGoRules(orderEntity.getProduct().getMaxHold())) { // 等于0，表示无限制
			//------------------------超级用户扫尾判断--------20170414--------
			if(!investorBaseAccountService.isSuperMan(orderEntity)){
				if (orderEntity.getOrderAmount().compareTo(orderEntity.getProduct().getMaxHold()) > 0) {
					// error.define[30031]=您所购金额已超过产品最大持有上限(CODE:30031)
					throw new AMPException(30031);
				}
			}
			//------------------------超级用户扫尾判断--------20170414--------
			String hkey = CacheKeyConstants.getHoldHKey(orderEntity.getInvestorBaseAccount().getOid(),
					orderEntity.getProduct().getOid());
			BigDecimal valOut = redisExecuteLogExtService.hincrByBigDecimal(hkey, "maxHoldVolume", orderEntity.getOrderVolume(),
					orderEntity.getOrderVolume().negate());
			
			//------------------------超级用户扫尾判断--------20170414--------
			if(!investorBaseAccountService.isSuperMan(orderEntity)){
				if (valOut.compareTo(orderEntity.getProduct().getMaxHold()) > 0) {
					// error.define[30031]=您所购金额已超过产品最大持有上限(CODE:30031)
					redisSyncService.saveEntityRefInvestorHoldRequireNew(orderEntity.getInvestorBaseAccount().getOid(), 
							orderEntity.getProduct().getOid(), 
							orderEntity.getProduct().getAssetPool().getOid());
					
					throw new AMPException(30031);
				}
			}
			//------------------------超级用户扫尾判断--------20170414--------
		}
	}
	/**
	 * 赎回锁定
	 */
	public void redeemLock(InvestorTradeOrderEntity orderEntity) {
		/**
		 *
		 */
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_cash.equals(orderEntity.getOrderType()) 
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_cashFailed.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderEntity.getOrderType())
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderEntity.getOrderType())) {
			return;
		}
		BigDecimal orderVolume = orderEntity.getOrderAmount();
		String hkey = CacheKeyConstants.getHoldHKey(orderEntity.getInvestorBaseAccount().getOid(),
				orderEntity.getProduct().getOid());

		BigDecimal valOut = redisExecuteLogExtService.hincrByBigDecimal(hkey, "redeemableHoldVolume",
				orderVolume.negate(), orderVolume);
		logger.info("redeemLock.valOut:" + valOut);
		if (null == valOut || valOut.compareTo(BigDecimal.ZERO) < 0) {
			redisSyncService.saveEntityRefInvestorHoldRequireNew(orderEntity.getInvestorBaseAccount().getOid(), 
					orderEntity.getProduct().getOid(), 
					orderEntity.getProduct().getAssetPool().getOid());
			// error.define[20004]=赎回锁定份额异常(CODE:20004)
			throw AMPException.getException(20004);
		}
		
		redisExecuteLogExtService.hincrByBigDecimal(hkey, "toConfirmRedeemVolume", orderVolume,
					orderVolume.negate());
	}

	/**
	 * 投资失败解锁锁定份额
	 */
	public void redeemUnlock(InvestorTradeOrderEntity orderEntity) {
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_cash.equals(orderEntity.getOrderType()) 
				|| InvestorTradeOrderEntity.TRADEORDER_orderType_cashFailed.equals(orderEntity.getOrderType())) {
			return;
		}
		BigDecimal orderVolume = orderEntity.getOrderAmount();
		String hkey = CacheKeyConstants.getHoldHKey(orderEntity.getInvestorBaseAccount().getOid(),
				orderEntity.getProduct().getOid());

		BigDecimal valOut = redisExecuteLogExtService.hincrByBigDecimal(hkey, "redeemableHoldVolume", orderVolume,
				orderVolume.negate());
		
		if (null == valOut || valOut.compareTo(BigDecimal.ZERO) < 0) {
			redisSyncService.saveEntityRefInvestorHoldRequireNew(orderEntity.getInvestorBaseAccount().getOid(), 
					orderEntity.getProduct().getOid(), 
					orderEntity.getProduct().getAssetPool().getOid());
			// error.define[20004]=赎回锁定份额异常(CODE:20004)
			throw AMPException.getException(20004);
		}
		
		redisExecuteLogExtService.hincrByBigDecimal(hkey, "toConfirmRedeemVolume", orderVolume.negate(), 
				orderVolume);
	}
	
	/**
	 * 产品赎回份额张约束
	 * 查看总持仓份额是否等于可赎回份额,并且是全部赎回
	 */
	public void update4MinRedeem(InvestorTradeOrderEntity orderEntity) {
		BigDecimal orderVolume = orderEntity.getOrderAmount();
		ProductCacheEntity product = cacheProductService.getProductCacheEntityById(orderEntity.getProduct().getOid());
		HoldCacheEntity holdCacheEntity = getHoldCacheEntityByInvestorOidAndProductOid(
				orderEntity.getInvestorBaseAccount().getOid(), orderEntity.getProduct().getOid());
		
		boolean isRedeemAllFlag = false;
		if (DecimalUtil.isGoRules(product.getMinRredeem())) {// 如果是全部赎回
			if (holdCacheEntity.getRedeemableHoldVolume().compareTo(orderVolume) == 0) {
				isRedeemAllFlag = true;
			}
		}
		if (!isRedeemAllFlag) {
			if (DecimalUtil.isGoRules(product.getMinRredeem())) {
				//-------------------------超级用户--------------2017.04.17-----
				if(!investorBaseAccountService.isSuperMan(orderEntity)){
					if (orderVolume.compareTo(product.getMinRredeem()) < 0) {
						// error.define[30013]=不满足单笔赎回下限(CODE:30013)
						throw new AMPException(30013);
					}
				}
			}

			if (DecimalUtil.isGoRules(product.getAdditionalRredeem())) {
				if (DecimalUtil.isGoRules(product.getMinRredeem())) {
					//-------------------------超级用户--------------2017.04.17-----
					if(!investorBaseAccountService.isSuperMan(orderEntity)){
						if (orderVolume.subtract(product.getMinRredeem()).remainder(product.getAdditionalRredeem())
								.compareTo(BigDecimal.ZERO) != 0) {
							// error.define[30039]=不满足赎回追加份额(CODE:30039)
							throw new AMPException(30039);
						}
					}
				} else {
					//-------------------------超级用户--------------2017.04.17-----
					if(!investorBaseAccountService.isSuperMan(orderEntity)){
						if (orderVolume.remainder(product.getAdditionalRredeem()).compareTo(BigDecimal.ZERO) != 0) {
							// error.define[30039]=不满足赎回追加份额(CODE:30039)
							throw new AMPException(30039);
						}
					}
				}
			}
		}
	}

	/**
	 * 线上缓存不一致问题，我的账户
	 * 5s
	 * yujianlong sunjian
	 * @param investorOid
	 * @param productId
	 * @return
	 */
	public HoldCacheEntity getHoldCacheEntityByInvestorOidAndProductOid(String investorOid,String productId){
		Map<String, String> mapHold = HashRedisUtil.hgetall(redis,
				CacheKeyConstants.getHoldHKey(investorOid, productId));
		HoldCacheEntity hold = JSONObject.parseObject(JSONObject.toJSONString(mapHold),HoldCacheEntity.class);
		String holdJson = "";
		if(hold != null) {
			holdJson = JSON.toJSONString(hold);
		}
		logger.info("user:{},product:{},hold:{},", investorOid, productId, holdJson);
		logger.info("HoldCacheEntity.zoomIn.key={}{}:{}", CacheKeyConstants.INVESTOR_PRODUCT_CACHE_KEY, investorOid, productId);
		String latestReadTimeStr=mapHold.get("latestReadTime");
		if (StringUtils.isBlank(latestReadTimeStr)||System.currentTimeMillis()-Long.valueOf(latestReadTimeStr).longValue()>5000||!holdJson.contains("redeemableHoldVolume") || !holdJson.contains("holdVolume")
				 || !holdJson.contains("toConfirmInvestVolume") || !holdJson.contains("totalInvestVolume")
				 || !holdJson.contains("dayRedeemVolume") || !holdJson.contains("dayInvestVolume")
				 || !holdJson.contains("maxHoldVolume") || !holdJson.contains("lockRedeemHoldVolume")
				 || !holdJson.contains("toConfirmRedeemVolume") || !holdJson.contains("totalVolume")
				 || !holdJson.contains("accruableHoldVolume") || !holdJson.contains("expGoldVolume")
				 || !holdJson.contains("holdYesterdayIncome") || !holdJson.contains("holdTotalIncome")
				 || !holdJson.contains("expectIncome") || !holdJson.contains("expectIncomeExt")) {
			logger.info("====用户持仓读数据库====");
			Map<String, Object> map = publisherHoldService.getHoldByInvestorAndProduct(investorOid, productId);
			hold = JSONObject.parseObject(JSONObject.toJSONString(map),HoldCacheEntity.class);
			DecimalUtil.zoomIn(hold);
			// 同步持仓缓存
			HashRedisUtil.hmset(redis, CacheKeyConstants.getHoldHKey(investorOid, productId), map);
		} else {
			logger.info("====hold is not null====");
			DecimalUtil.zoomIn(hold);
		}
		return hold;
	}
	
	/**
	 * 根据用户查询所有活期产品合仓
	 */
	public List<HoldCacheEntity> findByInvestorOid(String investorOid) {
		String zkey = CacheKeyConstants.getHoldIndexKey(investorOid);
		List<String> list = ZsetRedisUtil.zRange(redis, zkey, 0, -1);

		List<HoldCacheEntity> returnList = new ArrayList<HoldCacheEntity>();
		HoldCacheEntity hold = null;
		for (String productOid : list) {
			hold = getHoldCacheEntityByInvestorOidAndProductOid(investorOid, productOid);
			logger.info("==用户{},产品{},持仓{}==", investorOid, productOid, hold);
			returnList.add(hold);
		}
		return returnList;
	}
	
	public List<HoldCacheEntity> findByInvestorOidSort(String investorOid) {
		String zkey = CacheKeyConstants.getHoldIndexKey(investorOid);
		List<String> list = ZsetRedisUtil.zRevRange(redis, zkey, 0, -1);
		List<HoldCacheEntity> returnList = new ArrayList<HoldCacheEntity>();
		HoldCacheEntity hold = null;
		String demandProductOid = productService.getOnSaleProductOid().getProductOid();
		for (String productOid : list) {
			hold = getHoldCacheEntityByInvestorOidAndProductOid(investorOid, productOid);
			logger.info("==用户{},产品{},持仓{}==", investorOid, productOid, hold);
			if(productOid.equals(demandProductOid)) {
				returnList.add(0, hold);
			} else if (rewardCacheService.hasRewardIncome(productOid)) {
				if (demandProductOid != null && returnList.size() > 0 && demandProductOid.equals(returnList.get(0).getProductOid())) {
					returnList.add(1, hold);
				}else{
					returnList.add(0, hold);
				}
			} else {
				returnList.add(hold);
			}
		}
		return returnList;
	}
	
	/**
	 * 资产池申购上限检验
	 */
	public void checkAssetPoolPurchaseLimit(InvestorTradeOrderEntity orderEntity) {
		
		String hkey = CacheKeyConstants.getAssetPoolPurchaseLimit(orderEntity.getProduct().getAssetPool().getOid());
		String val = HashRedisUtil.hget(redis, hkey, "purchaseLimit");
		BigDecimal purchaseLimit = new BigDecimal(val);
		if (DecimalUtil.isGoRules(purchaseLimit)) {
			hkey = CacheKeyConstants.getInvestorPurchaseLimit(orderEntity.getInvestorBaseAccount().getOid(), orderEntity.getProduct().getAssetPool().getOid());
			this.redisExecuteLogExtService.hincrByBigDecimal(hkey, orderEntity.getProduct().getOid(), orderEntity.getOrderAmount(), null);
			
			List<String> vals = HashRedisUtil.hVals(redis, hkey);
			BigDecimal total = BigDecimal.ZERO;
			for (String tVal : vals) {
				total  = total.add(new BigDecimal(tVal));
			}
			//------------------------超级用户扫尾判断--------20170414--------
			if(!investorBaseAccountService.isSuperMan(orderEntity)){
				//不是超级用户
				if (total.compareTo(purchaseLimit) > 0) {
					// 反向操作用户的资产池申购上限
					this.redisExecuteLogExtService.hincrByBigDecimal(hkey, orderEntity.getProduct().getOid(), orderEntity.getOrderAmount().negate(), null);
					this.redisSyncService.saveEntityRefInvestorHoldRequireNew(orderEntity.getInvestorBaseAccount().getOid(),
							orderEntity.getProduct().getOid(), orderEntity.getProduct().getAssetPool().getOid());
					// error.define[30078]=超过资产池申购上限(CODE:30078)
					throw new AMPException(30078);
				}
			}
			//------------------------超级用户扫尾判断--------20170414--------
		}
	}
	
	/**
	 * 获取已支付待接收资产
	 */
	public BigDecimal getToAcceptedAmount(String investorOid) {
		String hkey = CacheKeyConstants.getInvestorToAcceptedAmount(investorOid);
		List<String> vals = HashRedisUtil.hVals(redis, hkey);
		BigDecimal toAcceptedAmount = BigDecimal.ZERO;
		for (String val : vals) {
			toAcceptedAmount = toAcceptedAmount.add(new BigDecimal(val));
		}
		return toAcceptedAmount;
	}
	
	/**
	 * 校验单人单日赎回上限
	 * 校验单日赎回次数
	 */
	public void validDailyInvestorRedeemLimit(InvestorTradeOrderEntity orderEntity) {
		BigDecimal orderVolume = orderEntity.getOrderAmount();
		String hkey = CacheKeyConstants.getDailyInvestorRedeemLimit(orderEntity.getInvestorBaseAccount().getOid());
		
		BigDecimal amount = redisExecuteLogExtService.hincrByBigDecimal(hkey, "dayRedeemVolume", orderVolume,
				orderVolume.negate());
		//如果没有设置超时时间
		if(redisExecuteLogExtService.getExpire(hkey) < 0) {
			redisExecuteLogExtService.expire(hkey, 86400);
		}
		// -------------------------超级用户--------------2017.04.17-----
		if (!investorBaseAccountService.isSuperMan(orderEntity)) {
			if (amount.compareTo(cacheConfig.DAILY_REDEEM_AMOUNT) > 0) {
				//如果验证失败，回滚已提现金额
				redisExecuteLogExtService.hincrByBigDecimal(hkey, "dayRedeemVolume", orderVolume.negate(),
						orderVolume);
				// error.define[30032]=超过产品单人单日赎回上限(CODE:30032)
				throw new AMPException("超过单日赎回金额" + cacheConfig.DAILY_REDEEM_AMOUNT + "元");
			}
		}
		Long count = redisExecuteLogExtService.hincrByLong(hkey, "dayRedeemCount", 1, -1);
		// -------------------------超级用户--------------2017.04.17-----
		if (!investorBaseAccountService.isSuperMan(orderEntity)) {
			if (count > cacheConfig.DAILY_REDEEM_COUNT) {
				//如果验证失败，回滚已提现次数
				redisExecuteLogExtService.hincrByLong(hkey, "dayRedeemCount", -1, 1);
				throw new AMPException("超过单日赎回次数" + cacheConfig.DAILY_REDEEM_COUNT + "次");
			}
		}

	}
	
	public int getDailyRedeemCount(String investorOid) {
		String hkey = CacheKeyConstants.getDailyInvestorRedeemLimit(investorOid);
		String redeemCountStr = HashRedisUtil.hget(redis, hkey, "dayRedeemCount");
		if(StringUtils.isBlank(redeemCountStr)) {
			return 0;
		} else {
			return Integer.valueOf(redeemCountStr);
		}
	}
}
