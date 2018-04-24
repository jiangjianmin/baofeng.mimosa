package com.guohuai.mmp.platform.redis;


import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class RedisSyncService {

	@Autowired
	private RedisSyncDao redisSyncDao;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;

	@Transactional(value = TxType.REQUIRES_NEW)
	public RedisSyncEntity saveEntityRefProductRequireNew(String investorOid, String productOid, String assetPoolOid) {
		RedisSyncEntity entity = new RedisSyncEntity();
		entity.setSyncOid(investorOid);
		entity.setProductOid(productOid);
		entity.setAssetPoolOid(assetPoolOid);
		entity.setSyncOidType(RedisSyncEntity.SYNC_syncOidType_investor);
		return this.saveEntity(entity);
	}
	
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public RedisSyncEntity saveEntityRefInvestorHoldRequireNew(String investorOid, String productOid, String assetPoolOid) {
		RedisSyncEntity entity = new RedisSyncEntity();
		entity.setSyncOid(investorOid);
		entity.setProductOid(productOid);
		entity.setAssetPoolOid(assetPoolOid);
		entity.setSyncOidType(RedisSyncEntity.SYNC_syncOidType_investor);
		return this.saveEntity(entity);
	}
	
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public RedisSyncEntity saveEntityRefSpvHoldRequireNew(String investorOid, String productOid, String assetPoolOid) {
		RedisSyncEntity entity = new RedisSyncEntity();
		entity.setSyncOid(investorOid);
		entity.setProductOid(productOid);
		entity.setAssetPoolOid(assetPoolOid);
		entity.setSyncOidType(RedisSyncEntity.SYNC_syncOidType_investor);
		return this.saveEntity(entity);
	}
	

	public RedisSyncEntity saveEntity(RedisSyncEntity entity) {
		return this.redisSyncDao.save(entity);
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public RedisSyncEntity investRedisRevert(String orderCode) {
		InvestorTradeOrderEntity orderEntity = investorTradeOrderService.findByOrderCode(orderCode);
		log.info("investRedisRevert.orderEntity{}", JSON.toJSONString(orderEntity));
		RedisSyncEntity entity = new RedisSyncEntity();
		entity.setSyncOid(orderEntity.getInvestorBaseAccount().getOid());
		entity.setProductOid(orderEntity.getProduct().getOid());
		entity.setAssetPoolOid(orderEntity.getProduct().getAssetPool().getOid());
		entity.setSyncOidType(RedisSyncEntity.SYNC_syncOidType_investor);
		return this.saveEntity(entity);
		
	}
	
}
