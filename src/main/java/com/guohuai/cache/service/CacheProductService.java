package com.guohuai.cache.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.ams.label.LabelEnum;
import com.guohuai.ams.label.LabelService;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.supplement.order.MechanismOrder;
import com.guohuai.basic.component.exception.GHException;
import com.guohuai.cache.CacheKeyConstants;
import com.guohuai.cache.entity.ProductCacheEntity;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.util.HashRedisUtil;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.platform.redis.RedisSyncService;

@Service
public class CacheProductService {
	Logger logger = LoggerFactory.getLogger(CacheProductService.class);
	@Autowired
	private RedisTemplate<String, String> redis;
	@Autowired
	private RedisExecuteLogExtService redisExecuteLogExtService;
	@Autowired
	private LabelService labelService;
	@Autowired
	private RedisSyncService redisSyncService;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private ProductService productService;
	
	@Value("${redeem.stop.begin.time:}")
	private String redeemStopBeginTime;
	
	@Value("${redeem.stop.end.time:}")
	private String redeemStopEndTime;
	
	/**
	 * 投资校验产品
	 * @param tradeOrderReq
	 */
	public void checkProduct4Invest(InvestorTradeOrderEntity orderEntity) {
		ProductCacheEntity product = getProductCacheEntityById(orderEntity.getProduct().getOid());
		// 活期、定期正常投资，活转定
		boolean isMatchType= Stream.of(Product.TYPE_Producttype_01,Product.TYPE_Producttype_03).anyMatch(type-> Objects.equals(type,product.getType()));
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType())
				|| (InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderEntity.getOrderType())
						&& isMatchType)) {
			if (Product.NO.equals(product.getIsOpenPurchase())) {
				// error.define[30020]=申购开关已关闭(CODE:30020)
				throw new AMPException(30020);
			}

			if (isMatchType) {
				if (!Product.STATE_Raising.equals(product.getState())) {
					// error.define[30017]=定期产品非募集期不能投资(CODE:30017)
					throw new AMPException(30017);
				}
			}

			if (Product.TYPE_Producttype_02.equals(product.getType())) {
				if (!Product.STATE_Durationing.equals(product.getState())) {
					// error.define[30055]=活期产品非存续期不能投资(CODE:30055)
					throw new AMPException(30055);
				}

				isExpGoldInvestable(orderEntity, product);
			}

			// 投资份额需要大于0
			if (orderEntity.getOrderAmount().compareTo(BigDecimal.ZERO) <= 0) {
				// error.define[30040]=金额不能小于等于0(CODE:30040)
				throw new AMPException(30040);
			}

			if (DecimalUtil.isGoRules(product.getInvestMin())) {
			
			isExpGoldInvestable(orderEntity, product);
			}

		// 投资份额需要大于0
		if (orderEntity.getOrderAmount().compareTo(BigDecimal.ZERO) <= 0) {
			// error.define[30040]=金额不能小于等于0(CODE:30040)
			throw new AMPException(30040);
		}
		//---------------增加判断超级用户判断--------20170413---------
		if (DecimalUtil.isGoRules(product.getInvestMin())) {
			if(!investorBaseAccountService.isSuperMan(orderEntity)){
				//不是超级用户
				//--------定期尾单不校验产品投资最小金额且必须一次性买完------2017.06.26----
				if(!this.checkIsLastTnOrder(orderEntity)){
					//不是定期尾单
					if (orderEntity.getOrderAmount().compareTo(product.getInvestMin()) < 0) {
						// error.define[30008]=不能小于产品投资最低金额(CODE:30008)
						throw new AMPException(30008);
					}
				}else{
					//定期尾单，必须一次性买完
					if (orderEntity.getOrderAmount().compareTo(product.getMaxSaleVolume()) != 0) {
						
						throw new GHException("定期尾单，必须一次性买完");
					}
				}
				//--------定期尾单不校验产品投资最小金额且必须一次性买完------2017.06.26----
				
			}
		}
		if (DecimalUtil.isGoRules(product.getInvestMax())) {
			if(!investorBaseAccountService.isSuperMan(orderEntity)){
				if (orderEntity.getOrderAmount().compareTo(product.getInvestMax()) > 0) {
					// error.define[30009]=已超过产品投资最高金额(CODE:30009)
					throw new AMPException(30009);
				}
			}
			
		}
		//---------------增加判断超级用户判断--------20170413---------
			if (DecimalUtil.isGoRules(product.getInvestAdditional())) {
				//-------------------------超级用户--------------2017.04.17-----
				if(!investorBaseAccountService.isSuperMan(orderEntity)){
					//--------不是定期尾单才校验产品投资追加金额------2017.06.26----
					if(!this.checkIsLastTnOrder(orderEntity)){
						if (DecimalUtil.isGoRules(product.getInvestMin())) {
							if (orderEntity.getOrderAmount().subtract(product.getInvestMin()).remainder(product.getInvestAdditional()).compareTo(BigDecimal.ZERO) != 0) {
								// error.define[30010]=不满足产品投资追加金额(CODE:30010)
								throw new AMPException(30010);
							}
						} else {
							if (orderEntity.getOrderAmount().remainder(product.getInvestAdditional()).compareTo(BigDecimal.ZERO) != 0) {
								// error.define[30010]=不满足产品投资追加金额(CODE:30010)
								throw new AMPException(30010);
							}
						}
					}
					//--------不是定期尾单才校验产品投资追加金额------2017.06.26----
				}
				//-------------------------超级用户--------------2017.04.17-----
			}
		}
		
	}



	/**
	 * 体验金投资处理
	 */
	private void isExpGoldInvestable(InvestorTradeOrderEntity orderEntity, ProductCacheEntity product) {
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType())) {
			String productLabels = product.getProductLabel();
			if (this.labelService.isProductLabelHasAppointLabel(productLabels, LabelEnum.tiyanjin.toString())) {
				throw new AMPException(30072);
			}
		}
	}

	public void checkProduct4PlusRedeem(InvestorTradeOrderEntity orderEntity) {
		ProductCacheEntity product = getProductCacheEntityById(orderEntity.getProduct().getOid());
		// 投资份额需要大于0
		if (orderEntity.getOrderVolume().compareTo(BigDecimal.ZERO) <= 0) {
			// error.define[30040]=份额不能小于等于0(CODE:30040)
			throw new AMPException(30040);
		}
		//---------------------去掉超级用户赎回判断----------------------2017.04.17--------
		if (DecimalUtil.isGoRules(product.getMaxRredeem())) {
			if(!investorBaseAccountService.isSuperMan(orderEntity)){
				//----------------------------活转定不校验最高份额条件----2017.05.23---
                if (orderEntity.getOrderVolume().compareTo(product.getMaxRredeem()) > 0) {
                    // error.define[30038]=不满足赎回最高份额条件(CODE:30038)
                    throw new AMPException(30038);
                }
				//----------------------------活转定不校验最高份额条件----2017.05.23---
			}
		}
		//---------------------去掉超级用户赎回判断----------------------2017.04.17--------
        /*if (Product.NO.equals(product.getIsOpenRemeed())) {
            // error.define[30021]=赎回开关已关闭(CODE:30021)
            throw new AMPException(30021);
		}*/

		if (Product.NO.equals(product.getIsOpenRedeemConfirm())) {
			// error.define[30033]=屏蔽赎回确认处于打开状态(CODE:30033)
			throw new AMPException(30033);
		}

	}


	/**
	 * 赎回校验产品
	 * @param tradeOrderReq
	 */
	public void checkProduct4Redeem(InvestorTradeOrderEntity orderEntity) {
		ProductCacheEntity product = getProductCacheEntityById(orderEntity.getProduct().getOid());
		// 投资份额需要大于0
		if (orderEntity.getOrderVolume().compareTo(BigDecimal.ZERO) <= 0) {
			// error.define[30040]=份额不能小于等于0(CODE:30040)
			throw new AMPException(30040);
		}
		//---------------------去掉超级用户赎回判断----------------------2017.04.17--------
		if (DecimalUtil.isGoRules(product.getMaxRredeem())) {
			if(!investorBaseAccountService.isSuperMan(orderEntity)){
				//----------------------------活转定不校验最高份额条件----2017.05.23---
				if(!InvestorTradeOrderEntity.TRADEORDER_orderType_noPayRedeem.equals(orderEntity.getOrderType())){
					if (orderEntity.getOrderVolume().compareTo(product.getMaxRredeem()) > 0) {
						// error.define[30038]=不满足赎回最高份额条件(CODE:30038)
						throw new AMPException(30038);
					}
				}
				//----------------------------活转定不校验最高份额条件----2017.05.23---
			}
		}
		//---------------------去掉超级用户赎回判断----------------------2017.04.17--------
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderEntity.getOrderType())
			|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderEntity.getOrderType())) {
			if (Product.NO.equals(product.getIsOpenRemeed())) {
				// error.define[30021]=赎回开关已关闭(CODE:30021)
				throw new AMPException(30021);
			}
		}
		
		
		if (Product.NO.equals(product.getIsOpenRedeemConfirm()) 
				&& InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderEntity.getOrderType())) {
			// error.define[30033]=屏蔽赎回确认处于打开状态(CODE:30033)
			throw new AMPException(30033);
		}
		
	}
	/**
	 * 锁定产品可售份额
	 */
	public void updateProduct4LockCollectedVolume(InvestorTradeOrderEntity orderEntity) {
		//修改申购缓存锁定份额被在途用户刷没的bug------2017.05.15-----
		/** 产品可售份额 */
		this.productService.updateProduct4LockCollectedVolume(orderEntity);
		//修改申购缓存锁定份额被在途用户刷没的bug------2017.05.15-----
//		BigDecimal orderVolume = orderEntity.getOrderAmount();
//		ProductCacheEntity product = getProductCacheEntityById(orderEntity.getProduct().getOid());
//		BigDecimal valOut = redisExecuteLogExtService.hincrByBigDecimal(
//				CacheKeyConstants.getProductHKey(orderEntity.getProduct().getOid()), "lockCollectedVolume", orderVolume,
//				orderVolume.negate());
//		if (null == product.getMaxSaleVolume() || product.getMaxSaleVolume().subtract(valOut).compareTo(BigDecimal.ZERO) < 0) {
//			redisSyncService.saveEntityRefProductRequireNew(orderEntity.getInvestorBaseAccount().getOid(), 
//					orderEntity.getProduct().getOid(), 
//					orderEntity.getProduct().getAssetPool().getOid());
//			// error.define[30011]=产品可投金额不足(CODE:30011)
//			throw new AMPException(30011);
//
//		}
	}
	
	/**
	 * 根据产品Oid从redis中获取产品
	 */
	public ProductCacheEntity getProductCacheEntityById(String productOid) {
		Map<String, String> mapProduct = HashRedisUtil.hgetall(redis, CacheKeyConstants.getProductHKey(productOid));
		ProductCacheEntity product = JSONObject.parseObject(JSONObject.toJSONString(mapProduct),
				ProductCacheEntity.class);
		logger.info("ProductCacheEntity.zoomIn.key={}{}", CacheKeyConstants.PRODUCT_CACHE_KEY, productOid);
		DecimalUtil.zoomIn(product);
		return product;
	}
	
	/**
	 * 产品单日净赎回上限 
	 */
	public void update4Redeem(InvestorTradeOrderEntity orderEntity) {
		
		ProductCacheEntity product = getProductCacheEntityById(orderEntity.getProduct().getOid());
		if (DecimalUtil.isGoRules(product.getNetMaxRredeemDay())) {
			
			String hkey = CacheKeyConstants.getProductHKey(orderEntity.getProduct().getOid());
			BigDecimal valOut = redisExecuteLogExtService.hincrByBigDecimal(hkey, "dailyNetMaxRredeem", orderEntity.getOrderAmount().negate(), null);
			//-------------------------超级用户--------------2017.04.17-----
			if(!investorBaseAccountService.isSuperMan(orderEntity)){
				if (valOut.compareTo(BigDecimal.ZERO) < 0) {
					redisSyncService.saveEntityRefProductRequireNew(orderEntity.getInvestorBaseAccount().getOid(), 
							orderEntity.getProduct().getOid(), 
							orderEntity.getProduct().getAssetPool().getOid());
					// error.define[30014]=赎回超出产品单日净赎回上限(CODE:30014)
					throw new AMPException(30014);
				}
			}
			//-------------------------超级用户--------------2017.04.17-----
		}
	}

//	public void updatePurchasePeopleNumAndPurchaseNum(String productOid, String batchNo) {
//		String hkey = CacheKeyConstants.getProductHKey(productOid);
//		
//		redisExecuteLogExtService.hincrByLong(batchNo, hkey, "purchasePeopleNum", new Integer(1), new Integer(-1));
//		
//		redisExecuteLogExtService.hincrByLong(batchNo, hkey, "purchaseNum", new Integer(1), new Integer(-1));
//	}
	
	/**
	 * 产品赎回确认
	 */
//	public void update4RedeemConfirm(String productOid,BigDecimal orderVolume,String batchNo){
//		ProductCacheEntity product=getProductCacheEntityById(productOid);
//		String hkey = CacheKeyConstants.getProductHKey(productOid);
//		if(product.getCurrentVolume().compareTo(orderVolume)>=0){
//			//redis操作currentVolume
//			redisExecuteLogExtService.hincrByBigDecimal(batchNo, hkey, "currentVolume", orderVolume.negate(), orderVolume);
//		}else{
//			// error.define[30019]=赎回确认份额异常(CODE:30019)
//			throw new AMPException(30019);
//		}
//	}
	
	/**
	 * 投资次数
	 */
//	public void updatePurchaseNum(String productOid, String batchNo){
//		String hkey = CacheKeyConstants.getProductHKey(productOid);
//		//redis操作purchaseNum
//		redisExecuteLogExtService.hincrByLong(batchNo, hkey, "purchaseNum", new Integer(1), new Integer(-1));
//	}
	
	/** 
	 * 校验产品交易时间 
	 */
	public void isInDealTime(String productOid) {
		ProductCacheEntity product = getProductCacheEntityById(productOid);
		// 交易时间

		if (DateUtil.isLessThanDealTime(product.getDealStartTime())) {
			// error.define[30048]=非交易时间不接收订单(CODE:30048)
			throw AMPException.getException(30048);
		}

		if (DateUtil.isGreatThanDealTime(product.getDealEndTime())) {
			// error.define[30048]=非交易时间不接收订单(CODE:30048)
			throw AMPException.getException(30048);
		}
	}
	
	/**
	 * 校验是否在停赎期间，如10.1、过年
	 */
	public void isInStopRedeemTime() {
		logger.info("【校验是否在停赎期间】redeemStopBeginTime:{}", redeemStopBeginTime );
		logger.info("【校验是否在停赎期间】redeemStopEndTime:{}", redeemStopEndTime );
		if (redeemStopBeginTime == null || "".equals(redeemStopBeginTime)) {
			return;
		}
		if (redeemStopEndTime == null || "".equals(redeemStopEndTime)) {
			return;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			java.util.Date beginTime = simpleDateFormat.parse(redeemStopBeginTime);
			java.util.Date endTime = simpleDateFormat.parse(redeemStopEndTime);
			long currentTime = System.currentTimeMillis();
			logger.info("【校验是否在停赎期间】currentTime:{}", currentTime );
	        if(currentTime>=beginTime.getTime() && currentTime<=endTime.getTime()){
	        	logger.info("【校验是否在停赎期间】交易处于停赎期间不能赎回，停赎开始时间：{}，停赎结束时间：{}", redeemStopBeginTime, redeemStopEndTime);
	        	throw AMPException.getException(30082);
	        }
		} catch (Exception e) {
			logger.error("【校验是否在停赎期间】时间校验异常，停赎开始时间：{}，停赎结束时间：{}", redeemStopBeginTime, redeemStopEndTime);
			throw AMPException.getException(30082);
		}
	}
	
	/**
	 * 份额确认
	 */
//	public void update4InvestConfirm(String productOid, BigDecimal orderVolume, String batchNo) {
//		String hkey = CacheKeyConstants.getProductHKey(productOid);
//
//		BigDecimal valOut = redisExecuteLogExtService.hincrByBigDecimal(batchNo, hkey, "lockCollectedVolume",
//				orderVolume.negate(), orderVolume);
//		// error.define[30012]=解除产品锁定份额异常(CODE:30012)
//		DecimalUtil.isValOutGreatThanOrEqualZero(valOut, 30012);
//
//		redisExecuteLogExtService.hincrByBigDecimal(batchNo, hkey, "maxSaleVolume", orderVolume.negate(), orderVolume);
//		DecimalUtil.isValOutGreatThanOrEqualZero(valOut, 30012);
//
//		redisExecuteLogExtService.hincrByBigDecimal(batchNo, hkey, "currentVolume", orderVolume, orderVolume.negate());
//
//		redisExecuteLogExtService.hincrByBigDecimal(batchNo, hkey, "collectedVolume", orderVolume,
//				orderVolume.negate());
//	}

	/**
	 * 设置产品标签多个以，号隔开
	 */
//	public void syncProductLabel(Product product) {
//		List<ProductLabel> productLabels = this.productLabelService.findProductLabelsByProduct(product);
//		String productLabel = "";
//		for (int i = 0; i < productLabels.size(); i++) {
//			if (i == productLabels.size() - 1) {
//				productLabel += productLabels.get(i).getLabel().getLabelCode();
//			} else {
//				productLabel += productLabels.get(i).getLabel().getLabelCode() + ",";
//			}
//		}
//		ProductCacheEntity cache = new ProductCacheEntity();
//		cache.setProductLabel(productLabel);
//		
//		logger.info("syncProductLabel productOid={}", product.getOid());
//		redisExecuteLogExtService.redisExecuteHMSET(CacheKeyConstants.getSyncProductLabel(),
//				CacheKeyConstants.getProductHKey(product.getOid()), cache);
//	}

	/**
	 * 同步产品maxSaleVolume
	 */
//	public void syncProductMaxSaleVolume(String productOid, BigDecimal orderVolume) {
//		logger.info("syncProductMaxSaleVolume productOid={}", productOid);
//		redisExecuteLogExtService.hincrByBigDecimal(CacheKeyConstants.getSyncProductMaxSaleVolume(),
//				CacheKeyConstants.getProductHKey(productOid), "maxSaleVolume", orderVolume, orderVolume.negate());
//	}
//	
//	/**
//	 * 同步产品申赎开关
//	 */
//	public void syncProductSwitch(Product product) {
//		ProductCacheEntity cache = new ProductCacheEntity();
//		cache.setIsOpenPurchase(product.getIsOpenPurchase());
//		cache.setIsOpenRemeed(product.getIsOpenRemeed());
//		
//		logger.info("syncProductSwitch productOid={}", product.getOid());
//		redisExecuteLogExtService.redisExecuteHMSET(StringUtil.uuid(),
//				CacheKeyConstants.getProductHKey(product.getOid()), cache);
//	}
//	
//	/**
//	 * 同步产品交易规则
//	 */
//	public void syncProductTradingRule(Product product) {
//		ProductCacheEntity cache = new ProductCacheEntity();
//		cache.setNetUnitShare(tZero(product.getNetUnitShare()));
//		cache.setInvestMin(tZero(product.getInvestMin()));
//		cache.setInvestAdditional(tZero(product.getInvestAdditional()));
//		cache.setInvestMax(tZero(product.getInvestMax()));
//		cache.setMinRredeem(tZero(product.getMinRredeem()));
//		cache.setMaxRredeem(tZero(product.getMaxRredeem()));
//		cache.setAdditionalRredeem(tZero(product.getAdditionalRredeem()));
//		cache.setNetMaxRredeemDay(tZero(product.getNetMaxRredeemDay()));
//		cache.setMaxHold(tZero(product.getMaxHold()));
//		cache.setSingleDailyMaxRedeem(tZero(product.getSingleDailyMaxRedeem()));
//		cache.setSingleDayRedeemCount(tZero(product.getSingleDayRedeemCount()));
//		cache.setDealStartTime(null2Kong(product.getDealStartTime()));
//		cache.setDealEndTime(null2Kong(product.getDealEndTime()));
//		
//		
//		
//		logger.info("syncProductTradingRule productOid={}", product.getOid());
//		redisExecuteLogExtService.redisExecuteHMSET(StringUtil.uuid(),
//				CacheKeyConstants.getProductHKey(product.getOid()), cache);
//	}
//	
//	/**
//	 * 同步产品赎回确认开关
//	 */
//	public void syncProductIsOpenRedeemConfirm(Product product) {
//		ProductCacheEntity cache = new ProductCacheEntity();
//		cache.setIsOpenRedeemConfirm(product.getIsOpenRedeemConfirm());
//		
//		logger.info("syncProductIsOpenRedeemConfirm productOid={}", product.getOid());
//		redisExecuteLogExtService.redisExecuteHMSET(StringUtil.uuid(),
//				CacheKeyConstants.getProductHKey(product.getOid()), cache);
//	}
	
//	/**
//	 * 同步产品单人单日赎回上限
//	 */
//	public void syncProductSingleDailyMaxRedeem(Product product) {
//		ProductCacheEntity cache = new ProductCacheEntity();
//		cache.setSingleDailyMaxRedeem(tZero(product.getSingleDailyMaxRedeem()));
//		
//		logger.info("syncProductSingleDailyMaxRedeem productOid={}", product.getOid());
//		redisExecuteLogExtService.redisExecuteHMSET(StringUtil.uuid(),
//				CacheKeyConstants.getProductHKey(product.getOid()), cache);
//	}
//	
//	/**
//	 * 同步产品单日赎回上限 
//	 */
//	public void syncProductDailyNetMaxRredeem(Product product) {
//		ProductCacheEntity cache = new ProductCacheEntity();
//		cache.setDailyNetMaxRredeem(product.getDailyNetMaxRredeem());
//		
//		logger.info("syncProductDailyNetMaxRredeem productOid={}", product.getOid());
//		redisExecuteLogExtService.redisExecuteHMSET(StringUtil.uuid(),
//				CacheKeyConstants.getProductHKey(product.getOid()), cache);
//	}

	
	/**
	 * 产品上架时，同步所有需要缓存 的数据 到redis
	 */
//	public void syncProduct4Upshelf(Product product) {
//		List<ProductLabel> productLabels = this.productLabelService.findProductLabelsByProduct(product);
//		String productLabel = "";
//		for (ProductLabel pl : productLabels) {
//			productLabel += pl.getLabel().getLabelCode() + ",";
//		}
//		ProductCacheEntity cache = new ProductCacheEntity();
//		cache.setProductOid(product.getOid());
//		cache.setName(product.getName());
//		cache.setIsOpenPurchase(product.getIsOpenPurchase());
//		cache.setIsOpenRemeed(product.getIsOpenRemeed());
//		
//		cache.setDealStartTime(null2Kong(product.getDealStartTime()));
//		cache.setDealEndTime(null2Kong(product.getDealEndTime()));
//		
//		cache.setProductLabel(productLabel);
//		cache.setState(product.getState());
//		cache.setType(product.getType().getOid());
//		cache.setIsOpenRedeemConfirm(product.getIsOpenRedeemConfirm());
//		cache.setSpvOid(product.getPublisherBaseAccount().getOid());
//		cache.setNetUnitShare(product.getNetUnitShare());
//		
//		cache.setInvestMin(tZero(product.getInvestMin()));
//		cache.setInvestAdditional(tZero(product.getInvestAdditional()));
//		cache.setInvestMax(tZero(product.getInvestMax()));
//		cache.setMinRredeem(tZero(product.getMinRredeem()));
//		cache.setMaxRredeem(tZero(product.getMaxRredeem()));
//		cache.setAdditionalRredeem(tZero(product.getAdditionalRredeem()));
//		cache.setNetMaxRredeemDay(tZero(product.getNetMaxRredeemDay()));
//		
//		cache.setDailyNetMaxRredeem(product.getDailyNetMaxRredeem());
//		
//		cache.setMaxHold(tZero(product.getMaxHold()));
//		cache.setSingleDailyMaxRedeem(tZero(product.getSingleDailyMaxRedeem()));
//		
//		cache.setSetupDate(product.getSetupDate());
//		cache.setLockCollectedVolume(tZero(product.getLockCollectedVolume()));
//		cache.setCurrentVolume(tZero(product.getCurrentVolume()));
//		cache.setSingleDayRedeemCount(tZero(product.getSingleDayRedeemCount()));
//		cache.setIncomeCalcBasis(product.getIncomeCalcBasis());
//		cache.setExpAror(product.getExpAror());
//		cache.setExpArorSec(product.getExpArorSec());
//		
//		if (Product.TYPE_Producttype_01.equals(product.getType().getOid())) {
//			cache.setMaxSaleVolume(product.getMaxSaleVolume());
//			cache.setRaiseStartDate(product.getRaiseStartDate());
//			cache.setRaiseEndDate(product.getRaiseEndDate());
//			cache.setDurationPeriodEndDate(product.getDurationPeriodEndDate());
//			cache.setRepayDate(product.getRepayDate());
//		}
//		logger.info("syncProduct4Upshelf productOid={}", product.getOid());
//		redisExecuteLogExtService.redisExecuteHMSET(CacheKeyConstants.getSyncProduct4Upshelf(),
//				CacheKeyConstants.getProductHKey(product.getOid()), cache);
//	}
	
//	private Integer tZero(Integer in) {
//		if (null == in) {
//			return 0;
//		}
//		return in;
//	}
	
//	private BigDecimal tZero(BigDecimal in) {
//		if (null == in) {
//			return BigDecimal.ZERO;
//		}
//		return in;
//	}
	
//	private String null2Kong(String in) {
//		if (in == null) {
//			return StringUtil.EMPTY;
//		}
//		return in;
//	}
	
//	/**
//	 * 产品清盘
//	 * @param productOid
//	 */
//	public void productCleared(Product product){
//		ProductCacheEntity cache = new ProductCacheEntity();
//		cache.setState(product.getState());
//		logger.info("productCleared productOid={}", product.getOid());
//		redisExecuteLogExtService.redisExecuteHMSET(StringUtil.uuid(),
//				CacheKeyConstants.getProductHKey(product.getOid()), cache);
//	}
//	/**
//	 * 产品清盘接口:
//	 * 清盘中的产品, 不可开启申购/开启赎回, 并在该事件中自动关闭申购/赎回, 在产品表中记录清盘操作人, 清盘操作时间
//	 * 产品状态调整为清盘中: clearing
//	 * 其他事件: 调用dubbo接口 LEOrderService.productClear()
//	 */
//	public void productClearing(Product product){
//		ProductCacheEntity cache = new ProductCacheEntity();
//		cache.setState(product.getState());
//		cache.setIsOpenPurchase(product.getIsOpenPurchase());
//		cache.setIsOpenRemeed(product.getIsOpenRemeed());
//		redisExecuteLogExtService.redisExecuteHMSET(StringUtil.uuid(),
//				CacheKeyConstants.getProductHKey(product.getOid()), cache);
//	}
//	/**
//	 * 修改产品状态
//	 * @param product
//	 */
//	public void updateProductState(Product product){
//		ProductCacheEntity cache = new ProductCacheEntity();
//		cache.setState(product.getState());
//		cache.setRaiseFailDate(product.getRaiseFailDate());
//		redisExecuteLogExtService.redisExecuteHMSET(StringUtil.uuid(),
//				CacheKeyConstants.getProductHKey(product.getOid()), cache);
//	}
//	/**
//	 * 新增当前份额
//	 * @param productOid
//	 * @param currentVolume
//	 * @param basicRatio
//	 */
//	public void incomeAllocateAdjustCurrentVolume(String productOid, BigDecimal currentVolume, BigDecimal basicRatio) {
//		
//		String hkey = CacheKeyConstants.PRODUCT_CACHE_KEY + productOid;
//		// redis操作currentVolume
//		redisExecuteLogExtService.hincrByBigDecimal(StringUtil.uuid(), hkey, "currentVolume",
//				currentVolume, currentVolume.negate());
//		//设置basicRatio
//		redisExecuteLogExtService.redisExecuteHSET(StringUtil.uuid(), hkey, "basicRatio", basicRatio, basicRatio.negate());
//
//	}
//	/**
//	 * 批量修改产品状态
//	 * @param products
//	 */
//	public void batchUpdateProductState(List<Product> products) {
//		for(Product product : products){
//			ProductCacheEntity cache = new ProductCacheEntity();
//			cache.setState(product.getState());
//			redisExecuteLogExtService.redisExecuteHMSET(StringUtil.uuid(),
//					CacheKeyConstants.getProductHKey(product.getOid()), cache);
//		}
//	}
	
	/**
	 * 解锁产品锁定已募份额 
	 */
//	public void update4InvestAbandon(InvestorTradeOrderEntity orderEntity, String batchNo) {
//		BigDecimal valOut = redisExecuteLogExtService.hincrByBigDecimal(batchNo, CacheKeyConstants.getProductHKey(orderEntity.getProduct().getOid()), 
//				"lockCollectedVolume", orderEntity.getOrderVolume().negate(), orderEntity.getOrderVolume());
//		// error.define[30034]=废申购单时产品锁定份额异常(CODE:30034)
//		DecimalUtil.isValOutGreatThanOrEqualZero(valOut, 30034);
//	}
	
	
	/**
	 * 定期产品期号编码规则：定期产品名+期数
    定期产品名称：根据系统定义定期产品名称；
    期数：以200个用户为一期（该定期产品下，涵盖全部期数用户是不能重复的），期号用数字编码自动顺序，格式0000。如果期号大于9999，请自动增加期号。
如，定期产品定投宝，期数为第三期；
那么，期号应该展示为“定投宝0003”。
	 * @param product
	 * @return
	 */
	public String getProductAlias(Product product) {

		Long productAliasCounter = HashRedisUtil.hincrByLong(redis, CacheKeyConstants.getProductHKey(product.getOid()),
				"productAliasCounter", 1);
		Long productAlias;
		if (((productAliasCounter - 1) % 200) == 0) {
			productAlias = HashRedisUtil.hincrByLong(redis, CacheKeyConstants.getProductHKey(product.getOid()),
					"productAlias", 1);
		} else {
			productAlias = Long
					.parseLong(HashRedisUtil.hget(redis, CacheKeyConstants.getProductHKey(product.getOid()), "productAlias"));
		}
		String tmp = String.valueOf(productAlias);
		while (tmp.length() < 3) {
			tmp = "0" + tmp;
		}
		return product.getName() + tmp;
	}
	
	/**校验机构订单是否处于募集期
	 * @param mechanismOrder
	 */
	public void checkProductIsRaisingOrRaisend(MechanismOrder mechanismOrder) {
		ProductCacheEntity product = getProductCacheEntityById(mechanismOrder.getProduct().getOid());

			if (Product.TYPE_Producttype_01.equals(product.getType())) {
				if (!Product.STATE_Raising.equals(product.getState())
						&&!Product.STATE_Raiseend.equals(product.getState())) {
					throw new GHException("只有募集中和募集结束才可以补单");
				}
			}
		
	}

	/**检查是否为定期尾单
	 * @param orderEntity
	 */
	public boolean checkIsLastTnOrder(InvestorTradeOrderEntity orderEntity) {
		ProductCacheEntity product = getProductCacheEntityById(orderEntity.getProduct().getOid());
		// 定期正常投资，活转定
		if ((InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType())
						|| InvestorTradeOrderEntity.TRADEORDER_orderType_noPayInvest.equals(orderEntity.getOrderType()))
						&& Product.TYPE_Producttype_01.equals(product.getType())) {
			BigDecimal investMin = product.getInvestMin();
			BigDecimal maxSaleVolume =  product.getMaxSaleVolume();
			if(maxSaleVolume.compareTo(investMin)<0){
				return true;
			}
		}
		return false;
	}
}
