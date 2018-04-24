package com.guohuai.cache.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.productChannel.ProductChannel;
import com.guohuai.cache.CacheKeyConstants;
import com.guohuai.cache.entity.ChannelCacheEntity;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.HashRedisUtil;

@Service
public class CacheChannelService {
	
	@Autowired
	private RedisTemplate<String, String> redis;

	/**
	 * 校验渠道
	 */
	public void checkChannel(String cid, String ckey, String productOid) {
		ChannelCacheEntity channel = HashRedisUtil.hgetall(redis, CacheKeyConstants.getChannelHKey(cid, ckey, productOid), ChannelCacheEntity.class);
		if (null == channel.getMarketState() || !ProductChannel.MARKET_STATE_Onshelf.equals(channel.getMarketState())) {
			// error.define[30079]=该产品在该渠道尚未发行或发行已被退回(CODE:30079)
			throw new AMPException(30079);
		}
	}
	
	/**
	 * 校验渠道
	 */
	public boolean checkProductChannel(String cid, String ckey, String productOid) {
		ChannelCacheEntity channel = HashRedisUtil.hgetall(redis, CacheKeyConstants.getChannelHKey(cid, ckey, productOid), ChannelCacheEntity.class);
		if (null == channel.getMarketState() || !ProductChannel.MARKET_STATE_Onshelf.equals(channel.getMarketState())) {
			// 该产品在该渠道尚未发行或发行已被退回
			return false;
		}
		return true;
	}
	

}
