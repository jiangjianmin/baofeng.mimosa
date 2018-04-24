package com.guohuai.ams.clientVersion;

import java.sql.Timestamp;
import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.guohuai.basic.common.RedisOps;
import com.guohuai.basic.component.exception.GHException;
import com.guohuai.component.message.MessageSendUtil;
import com.guohuai.component.util.StrRedisUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.util.ZsetRedisUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class ClientVersionService {
	
	/** 版本号key */
	public static final String VERSION_REDIS_KEY = "m:c:c:";
	
	public static final String UNIQUE_CLIENT_VERSION_KEY = "unique.client.version.";
	
	public static final String VERSIONS = "versions";
	
	@Autowired
	private RedisTemplate<String, String> redis;
	@Autowired
	private MessageSendUtil messageSendUtil;

	public String getClientVersion(String versionKey){
		String versionInRedis = "";
		versionInRedis = StrRedisUtil.get(redis, versionKey);
		return versionInRedis;
	}
	
	public void handleClientVersion(String clientId,String clientType,String version){
		String versionKey = "";
		if(StringUtil.isEmpty(clientId) || StringUtil.isEmpty(clientType) || StringUtil.isEmpty(version)){
			log.info("====clientId:{},clientType:{},version:{},存在空值====",clientId,clientType,version);
		}else{
			ZsetRedisUtil.zAdd(redis, VERSIONS, version, new Date().getTime());
			String msgKey = UNIQUE_CLIENT_VERSION_KEY + clientId + "." + clientType + "." + version;
			if(RedisOps.setnx(redis, msgKey, msgKey)){
				versionKey = VERSION_REDIS_KEY + clientId + ":" + clientType;
				String versionInRedis = StrRedisUtil.get(redis, versionKey);
				if(version.equals(versionInRedis)){
					log.info("{},version:{}已存在",versionKey,version);
				}else{
					StrRedisUtil.set(redis, versionKey, version);
					Timestamp now = new Timestamp(System.currentTimeMillis());
					ClientVersionEntity clientVersionEntity = new ClientVersionEntity();
					clientVersionEntity.setClientId(clientId);
					clientVersionEntity.setClientType(clientType);
					clientVersionEntity.setVersion(version);
					clientVersionEntity.setCreateTime(now);
					sendMessage(clientVersionEntity);
				}
			} else {
				log.info("===用户clientId:{},clientType:{},version:{},消息相同，不再次发送===",clientId,clientType,version);
			}
		}
	}
	
	private void sendMessage(ClientVersionEntity clientVersionEntity) {
		log.info("====send message,clientVersionEntity:{}====",JSON.toJSONString(clientVersionEntity));
		messageSendUtil.sendTopicMessage(messageSendUtil.getVersionTopic(), messageSendUtil.getVersionTopic(), clientVersionEntity);
	}
}
