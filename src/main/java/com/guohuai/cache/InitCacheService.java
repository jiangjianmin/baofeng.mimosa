package com.guohuai.cache;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import com.guohuai.ams.channel.ChannelService;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.productChannel.ProductChannelService;
import com.guohuai.ams.productLabel.ProductLabel;
import com.guohuai.ams.productLabel.ProductLabelService;
import com.guohuai.cache.entity.ProductCacheEntity;
import com.guohuai.cache.entity.SPVHoldCacheEntity;
import com.guohuai.cache.service.CacheProductService;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.util.GHUtil;
import com.guohuai.component.util.HashRedisUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.publisher.hold.PublisherHoldEntity;
import com.guohuai.mmp.publisher.hold.PublisherHoldService;

@Service
@Transactional
public class InitCacheService {
	Logger logger=LoggerFactory.getLogger(InitCacheService.class);
	
	@Autowired
	ProductService productService;
	@Autowired
	PublisherHoldService publisherHoldService;
	@Autowired
	ProductLabelService productLabelService;
	@Autowired
	RedisTemplate<String, String> redis;
	@Autowired
	CacheProductService produceCheckService;
	@Autowired
	ProductChannelService productChannelService;
	@Autowired
	ChannelService channelService;

	/**
	 * 初始化全部的缓存数据
	 * @return 是否初始化成功
	 */
	public boolean initAllCacheData(){
		this.initAllProductCacheData();
		this.initAllSVPHoldCacheData();
		return true;
	}
	

	/**
	 * 初始化全部的产品缓存数据
	 */
	public boolean initAllProductCacheData() {
		boolean result = true;
		List<Product> list = this.productService.findAll();
		for (Product product : list) {
			List<ProductLabel> productLabels = this.productLabelService.findProductLabelsByProduct(product);
			String productLabel = "";
			for (ProductLabel pl : productLabels) {
				productLabel += pl.getLabel().getLabelCode() + ",";
			}
			ProductCacheEntity productCache = getProductCacheEntity(product, productLabel);
			DecimalUtil.zoomOut(productCache);
			boolean thisResult = HashRedisUtil.hmset(redis,
					CacheKeyConstants.PRODUCT_CACHE_KEY + productCache.getProductOid(), GHUtil.obj2Map(productCache));
			if (!thisResult) {
				logger.error("init product={} into cache failed.", productCache.getProductOid());
			}
			result &= thisResult;
		}
		logger.info("initAllProductCacheData: status ={} ", result);
		return result;
	}

	/**
	 * 初始化SVP的持仓缓存数据
	 */
	public boolean initAllSVPHoldCacheData() {
		boolean result = true;

		List<PublisherHoldEntity> list = this.publisherHoldService.getSPVHold();
		if (list.isEmpty()) {
			return false;
		}
		for (PublisherHoldEntity hold : list) {
			SPVHoldCacheEntity holdCache = new SPVHoldCacheEntity();
			if (null != hold.getProduct()) {
				holdCache.setProductOid(hold.getProduct().getOid());
			}
			holdCache.setLockRedeemHoldVolume(hold.getLockRedeemHoldVolume());
			holdCache.setTotalVolume(hold.getTotalVolume());

			DecimalUtil.zoomOut(holdCache);
			boolean thisResult = HashRedisUtil.hmset(redis,
					CacheKeyConstants.SPVHOLD_CACHE_KEY + holdCache.getProductOid(), GHUtil.obj2Map(holdCache));
			if (!thisResult) {
				logger.error("init productOid={} into cache failed.", holdCache.getProductOid(),
						holdCache.getProductOid());
			}
			result &= thisResult;
		}

		logger.info("initSVPHoldCacheData: status ={} ", result);
		return result;
	}
	
	public BaseRep resetProductCacheDataByOid(String oid) {
		BaseRep rep=new BaseRep();
		Product product=this.productService.findByOid(oid);
		if(null == product){
			rep.setErrorCode(-1);
			rep.setErrorMessage(oid+"====该产品不存在!");
			return rep;
		}
		List<ProductLabel> productLabels = this.productLabelService.findProductLabelsByProduct(product);
		List<String> labelCodeList = new ArrayList<String>();
		String productLabel="";
		for (ProductLabel pl : productLabels) {
			labelCodeList.add(pl.getLabel().getLabelCode());
//			productLabel +=pl.getLabel().getLabelCode()+",";
		}
		productLabel = Joiner.on(",").join(labelCodeList);
		
		ProductCacheEntity productCache = getProductCacheEntity(product, productLabel);
		DecimalUtil.zoomOut(productCache);
		boolean thisResult = HashRedisUtil.hmset(redis,
				CacheKeyConstants.PRODUCT_CACHE_KEY + productCache.getProductOid(), GHUtil.obj2Map(productCache));
		if(thisResult){
			rep.setErrorCode(0);
			rep.setErrorMessage(oid+",重置产品数据成功!");
		}
		
		return rep;
	}

	private ProductCacheEntity getProductCacheEntity(Product product, String productLabel) {
		ProductCacheEntity productCache = new ProductCacheEntity();
		productCache.setProductOid(product.getOid());
		productCache.setName(product.getName());
		productCache.setIsOpenPurchase(product.getIsOpenPurchase());
		productCache.setIsOpenRemeed(product.getIsOpenRemeed());
		productCache.setDealStartTime(product.getDealStartTime());
		productCache.setDealEndTime(product.getDealEndTime());
		
		productCache.setNetUnitShare(product.getNetUnitShare());
		productCache.setInvestMin(product.getInvestMin());
		productCache.setInvestAdditional(product.getInvestAdditional());
		productCache.setInvestMax(product.getInvestMax());
		productCache.setMinRredeem(product.getMinRredeem());
		productCache.setMaxRredeem(product.getMaxRredeem());
		productCache.setAdditionalRredeem(product.getAdditionalRredeem());
		productCache.setNetMaxRredeemDay(product.getNetMaxRredeemDay());
		productCache.setDailyNetMaxRredeem(product.getDailyNetMaxRredeem());
		productCache.setMaxHold(product.getMaxHold());
		productCache.setSingleDailyMaxRedeem(product.getSingleDailyMaxRedeem());
		productCache.setMaxSaleVolume(product.getMaxSaleVolume());
		productCache.setProductLabel(productLabel);
		productCache.setType(product.getType().getOid());
		productCache.setState(product.getState());
		productCache.setLockCollectedVolume(product.getLockCollectedVolume());
		productCache.setIsOpenRedeemConfirm(product.getIsOpenRedeemConfirm());
		productCache.setCurrentVolume(product.getCurrentVolume());
		
		productCache.setCollectedVolume(product.getCollectedVolume());
		productCache.setSingleDayRedeemCount(product.getSingleDayRedeemCount());
		
		productCache.setRaiseStartDate(product.getRaiseStartDate());
		productCache.setRaiseEndDate(product.getRaiseEndDate());
		
		productCache.setSetupDate(product.getSetupDate());
		productCache.setDurationPeriodEndDate(product.getDurationPeriodEndDate());
		productCache.setRepayDate(product.getRepayDate());
		productCache.setExpAror(product.getExpAror());
		productCache.setExpArorSec(product.getExpArorSec());
		productCache.setIncomeCalcBasis(product.getIncomeCalcBasis());
		productCache.setRaiseFailDate(product.getRaiseFailDate());
		return productCache;
	}

	public BaseRep resetSVPHoldCacheDataByProductOid(String productOid) {
		BaseRep rep=new BaseRep();
		List<Object[]> objList = this.publisherHoldService.getSPVHoldByProductOid(productOid);
		if(objList.size()==0){
			rep.setErrorCode(-1);
			rep.setErrorMessage(productOid+"====该产品SPV持仓数据不存在");
			return rep;
		}
		boolean resultBoolean=true;
		for(Object[] obj : objList){
			SPVHoldCacheEntity holdCache = new SPVHoldCacheEntity();
			holdCache.setProductOid(obj[0].toString());
			holdCache.setLockRedeemHoldVolume(new BigDecimal(obj[8].toString()));
			holdCache.setTotalVolume(new BigDecimal(obj[9].toString()));

			DecimalUtil.zoomOut(holdCache);
			boolean thisResult = HashRedisUtil.hmset(redis,
					CacheKeyConstants.SPVHOLD_CACHE_KEY + holdCache.getProductOid(), GHUtil.obj2Map(holdCache));
			if (!thisResult) {
				logger.error("reset productOid={} into SPVHoldCache failed.", holdCache.getProductOid(),
						holdCache.getProductOid());
			}
			resultBoolean &= thisResult;
			
		}
		if (resultBoolean) {
			rep.setErrorCode(0);
			rep.setErrorMessage(productOid+",重置产品SPV持有手册数据成功!");
		}
		return rep;
	}
	
}
