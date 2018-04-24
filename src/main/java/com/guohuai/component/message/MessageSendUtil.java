package com.guohuai.component.message;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.SendResult;
import com.guohuai.basic.common.StringUtil;
import com.guohuai.basic.common.ThreadPoolUtil;

/**
* <p>Title:MessageSendUtil </p>
* <p>Description: </p>
* <p>Company: 北京暴风诚信科技有限公司</p> 
* @author 邱亮
* @date 2017年9月26日 上午10:17:34
*/
@Component
public class MessageSendUtil {
	private static final Logger logger = LoggerFactory.getLogger(MessageSendUtil.class);
	private static final int RETRY_NUM = 3;

	@Value("${ons.access.key}")
	private String accessKey;
	@Value("${ons.secret.key}")
	private String secretKey;
	@Value("${ons.url}")
	private String onsUrl;

	@Value("${ons.producer.id}")
	private String producerId;
	
	@Value("${ons.deal.topic}")
	private String dealTopic;
	
	@Value("${ons.changephone.topic}")
	private String changePhoneTopic;
	
	@Value("${ons.deal.versionTopic}")
	private String versionTopic;
	
	/**
	 * 短信topic
	 */
	@Value("${ons.deal.shortMessageTopic}")
	private String shortMessageTopic;

	private Producer producer = null;

	private ExecutorService executorService = null;
	
	@Value("${ons.send.latest.hour:21}")
	private Integer latestHour = 21;
	@Value("${ons.send.earliest.hour:7}")
	private Integer earliestHour = 7;
	@Value("${ons.send.adjust.hour:9}")
	private Integer adjustHour = 9;

	@PostConstruct
	public void initProducer() {
		try {
			if (StringUtils.isNotBlank(producerId)) {
				logger.debug("starting message sender");
				Properties properties = new Properties();
				properties.put(PropertyKeyConst.AccessKey, accessKey);
				properties.put(PropertyKeyConst.SecretKey, secretKey);
				properties.put(PropertyKeyConst.ProducerId, producerId);
				if (!StringUtils.isBlank(onsUrl)) {
					properties.put(PropertyKeyConst.ONSAddr, onsUrl);
				}
				producer = ONSFactory.createProducer(properties);
				if (producer == null) {
					logger.error("initialize message sender. failure !!!");
					return;
				}
				producer.start();
				logger.debug("start message sender success !!! ");
				// 开启线程池
				if (null == executorService || executorService.isShutdown()) {
					executorService = ThreadPoolUtil.newTheadPool("MessageSender");
				}
			} else {
				logger.debug("No need to init message sender");
			}
		} catch (Exception ex) {
			logger.error("initialize message sender. failure !!!", ex);
		}
	}

	@PreDestroy
	public void destroyONS() {
		if (producer != null) {
			producer.shutdown();
		}
		logger.info("shutdown message sender ");
		// 关闭线程池
		if(executorService != null) {
			ThreadPoolUtil.shutdown(executorService);
		}
	}

	public void sendTopicMessage(String topic, String tag, Object body) {
		try {
			logger.debug("开始发送消息topic:{} tag:{} body:{}", topic, tag, body);
			Message message = assembleMessage(topic, tag, body);
			sendRetryMessage(message);
		} catch(Exception ex) {
			logger.error("发送消息失败", ex);
		}
	}

	public void sendTimedTopicMessage(String topic, String tag, Object body, long timestamp) {
		try {
			logger.debug("开始发送定时消息topic:{} tag:{} body:{} timestamp:{}", topic, tag, body, timestamp);
			Message message = assembleMessage(topic, tag, body);
			message.setStartDeliverTime(timestamp);
			sendRetryMessage(message);
		} catch (Exception ex) {
			logger.error("发送消息失败", ex);
		}
	}

	public void sendTimedTopicMessage(String topic, String tag, Object body) {
		try {
			logger.debug("开始发送定时消息topic:{} tag:{} body:{}", topic, tag, body);
			Message message = assembleMessage(topic, tag, body);
			setNextNotifyTime(message);
			sendRetryMessage(message);
		} catch (Exception ex) {
			logger.error("发送消息失败", ex);
		}
	}

	/** 拼装消息体 */
	private Message assembleMessage(String topic, String tag, Object body) {
		String strBody = JSONObject.toJSONString(body);
		String strKey = body.getClass().getSimpleName() + "~" + StringUtil.uuid();
		Message message = null;
		try {
			message = new Message(topic, tag, strKey, strBody.getBytes("UTF-8"));
		} catch (Exception e) {
			logger.error("assemble message exception", e);
		}
		return message;
	}

	/** 异步发送消息 */
	private void sendRetryMessage(final Message message) {
		if(producer == null) {
			logger.debug("此服务器不能发送消息，请初始化生产者");
		}
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				sendMessage(message);
			}
		});
	}

	/** 失败重试 发消息 */
	private void sendMessage(Message message) {
		for (int i = 0; i < RETRY_NUM; i++) {
			if (doSendMessage(message)) {
				return;
			}
			logger.info("消息队列发送消息重试[{}]次", i + 1);
		}
		logger.error("消息队列发送消息{}次 失败 , message info :{}", message);
	}

	/** 发送消息 */
	private boolean doSendMessage(Message message) {
		try {
			logger.info("消息队列发送消息内容 : {}", message);
			SendResult sendResult = producer.send(message);
			logger.info("消息队列发送消息结果 :{}", sendResult);
		} catch (Exception e) {
			logger.error("消息队列发送消息异常 ", e);
			return false;
		}
		return true;
	}
	
	//21点以后和8点以前不推送
	public void setNextNotifyTime(Message message) {
		LocalDateTime sendTime = LocalDateTime.now();
		if(sendTime.getHour() >= latestHour) {
			sendTime = sendTime.plusDays(1).withHour(adjustHour).withMinute(0).withSecond(0);
			message.setStartDeliverTime(Timestamp.valueOf(sendTime).getTime());
		} else if(sendTime.getHour() <= earliestHour) {
			sendTime = sendTime.withHour(adjustHour).withMinute(0).withSecond(0);
			message.setStartDeliverTime(Timestamp.valueOf(sendTime).getTime());
		}
	}
	
	public String getDealTopic() {
		return dealTopic;
	}

	public void setDealTopic(String dealTopic) {
		this.dealTopic = dealTopic;
	}

	public String getVersionTopic() {
		return versionTopic;
	}

	public void setVersionTopic(String versionTopic) {
		this.versionTopic = versionTopic;
	}

	public String getShortMessageTopic() {
		return shortMessageTopic;
	}

	public void setShortMessageTopic(String shortMessageTopic) {
		this.shortMessageTopic = shortMessageTopic;
	}

	public String getChangePhoneTopic() {
		return changePhoneTopic;
	}

	public void setChangePhoneTopic(String changePhoneTopic) {
		this.changePhoneTopic = changePhoneTopic;
	}
	
	
	
}
