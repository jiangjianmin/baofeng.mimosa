package com.guohuai.bfsms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.guohuai.basic.common.DateUtil;
import com.guohuai.basic.message.ShortMessageEntity;
import com.guohuai.bfsms.api.BfSMSApi;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.message.MessageSendUtil;
import com.guohuai.component.util.NumberUtil;
import com.guohuai.component.util.StrRedisUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;

import lombok.extern.slf4j.Slf4j;
@Component
@Slf4j
public class BfSMSUtils {
	
	/** 图形验证码key */
	public static final String IMG_VERICODE_REDIS_KEY = "c:g:u:ic:";

	/** 通过绑卡验证校验的用户  **/
	public static final String PASS_VERI_CODE_USER_KEY = "user.pass.veri:";
	/**忘记密码绕过短信验证redis key前缀 */
	private static final String PASS_VERI_CODE_USER_FORGET_PWD_KEY = "user.pass.forgetpwd.veri:";

	/**短信验证码过期时间*/
	public static final int EXPIRE_SECONDS = 120;
	
	@Autowired
	private RedisTemplate<String, String> redis;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	
	@Value("${bfsms.OperID}")
	private String operID;
	@Value("${bfsms.OperPass}")
	private String operPass;
//	@Value("${bfsms.AppendID}")
//	private String appendID;
//	@Value("${bfsms.ContentType}")
//	private String contentType;
	@Value("${bfsms.types}")
	private String smsTypes;
	@Value("${bfsms.comcontent}")
	private String comcontent;
	@Value("${bfsms.xjfbcomcontent}")
	private String xjfbcomcontent;
	
	@Value("${bfsms.status:}")
	private String smsStatus;
	
	@Value("${bfsms.dayCount:15}")
	private String smsDayCount;
	
	@Autowired
	private BfSMSApi bfSMSApi;
	@Autowired
	private MessageSendUtil messageSendUtil;
	
	public static final Map<String, String > smsTypesMap = new HashMap<String, String>();


	@PostConstruct
	public void initBfSMSTypes() {
		List<BfSMSTypeEntity> list = JSON.parseArray(this.smsTypes, BfSMSTypeEntity.class);
		if (smsTypesMap.size() == 0) {
			for (BfSMSTypeEntity en : list) {
				smsTypesMap.put(en.getSmsType(), en.getContent());
			}
		}
	}
	
//	/**
//	 * 发送短信
//	 * @param phone 手机号码
//	 * @param content 短信内容
//	 */
//	public BaseRep sendSMS(String phone, String content) {
//		BaseRep rep = new BaseRep();
//		
//		String smsXmlRep = bfSMSApi.sendSMS(this.operID, this.operPass, this.appendID, phone, content, this.contentType);
//		
//		if (StringUtil.isEmpty(smsXmlRep)) {
//			log.error("用户：{}，短信发送失败，返回为空。", phone);
//			// 发送短信失败！
//			throw AMPException.getException(120003);
//		}
//		
//		try {
//			XStream xs = new XStream();
//			xs.processAnnotations(BfSMSXmlRep.class);
//			xs.autodetectAnnotations(true);
//			xs.ignoreUnknownElements();
//			BfSMSXmlRep xmlRep = (BfSMSXmlRep) xs.fromXML(smsXmlRep);
//			
//			if (!"03".equals(xmlRep.getCode())) {
//				log.error("用户：{}，短信发送失败，错误代码：{}", phone, xmlRep.getCode());
//				// 发送短信失败！
//				throw AMPException.getException(120003);
//			} else {
//				log.info("用户：{}短信发送成功。", phone);
//			}
//		} catch (Exception e) {
//			log.error("用户：{}，短信发送失败。原因：{}", phone, AMPException.getStacktrace(e));;
//			rep.setErrorCode(BaseRep.ERROR_CODE);
//			rep.setErrorMessage(AMPException.getStacktrace(e));
//			return rep;
//		}
//		
//		return rep;
//	}	
	/**
	 * 发送短信
	 * @param phone 手机号码
	 * @param content 短信内容
	 */
	/**
	* <p>Title: </p>
	* <p>Description: </p>
	* <p>Company: </p> 
	* @param phone
	* @param smsType
	* @param content
	* @return
	* @author 邱亮
	* @date 2017年9月26日 下午2:04:00
	* @since 1.0.0
	*/
	public BaseRep sendSMS(String phone, String smsType,String content) {
		BaseRep rep = new BaseRep();
		
		String smsStrRep = "";
		if (smsStatus != null && "off".equals(smsStatus)) {
			smsStrRep = "X730036171026205714,00";
		}else if (smsStatus != null && "error".equals(smsStatus)) {
			smsStrRep = "X730036171026205714,99";
		}else {
			smsStrRep = bfSMSApi.sendSMS(this.operID, this.operPass, phone, content);
		};
		log.info("手机号:{},短信接口返回:{}",phone,smsStrRep);
		
		if (StringUtil.isEmpty(smsStrRep)) {
			log.error("用户：{}，短信发送失败，返回为空。", phone);
			// 发送短信失败！
			throw AMPException.getException(120003);
		}
		
		try {
			String returnCode = smsStrRep.split(",")[1].trim();
			/**
			 * 短信插入点
			 */
			sendMessage(phone, smsType, content, returnCode);
			if (!"00".equals(returnCode)) {
				log.error("用户：{}，短信发送失败，错误代码：{}", phone, returnCode);
				// 发送短信失败！
				throw AMPException.getException(120003);
			} else {
				log.info("用户：{}短信发送成功。", phone);
			}
		} catch (Exception e) {
			log.error("用户：{}，短信发送失败。原因：{}", phone, AMPException.getStacktrace(e));;
			rep.setErrorCode(BaseRep.ERROR_CODE);
			rep.setErrorMessage(AMPException.getStacktrace(e));
			return rep;
		}
		
		return rep;
	}
	/**
	 * 替换掉{1}格式
	 * @param target 目标
	 * @param strArr 替换的值
	 * @return
	 */
	public String replaceComStrArr(String target, String[] repArr){
		if (null == target) {
			return StringUtil.EMPTY;
		}
		for (int i = 1; i <= repArr.length; i++) {
			target = target.replace("{" + i +"}" , repArr[i - 1]);
		}
		return target;
	}
	
	/**
	 * 将验证码保存到redis
	 * @param phone 手机号
	 * @param smsType 短信类型
	 * @param veriCode 验证码
	 */
	public void sendVC2Redis(String phone, String smsType, String veriCode) {
		boolean result = StrRedisUtil.setSMSEx(redis, StrRedisUtil.VERI_CODE_REDIS_KEY + phone + "_" + smsType, BfSMSUtils.EXPIRE_SECONDS, veriCode);
		if (!result) {
			// 生成验证码失败！
			throw AMPException.getException(120000);
		} 
	}
	
	/**
	 * 获取短信验证码
	 * @param req
	 * @return
	 */
	public BfSMSVeriCodeRep getVeriCode(BfSMSReq req) {
		BfSMSVeriCodeRep rep = new BfSMSVeriCodeRep();
		String veriCode = StrRedisUtil.get(redis, StrRedisUtil.VERI_CODE_REDIS_KEY + req.getPhone() + "_" + req.getSmsType());
		if (!StringUtil.isEmpty(veriCode)) {
			rep.setVeriCode(veriCode);
		} else {
			throw new AMPException("手机号对应的短信类型不存在验证码！");
		}
		return rep;
	}
	
	/**
	 * 校验验证码
	 * @param phoneNumb 手机号
	 * @param smsType 短信类型
	 * @param veriCode 短信码
	 * @return
	 */
	public boolean checkVeriCode(String phone, String smsType, String veriCode) {
		String vericode = StrRedisUtil.get(redis, StrRedisUtil.VERI_CODE_REDIS_KEY + phone + "_" + smsType);
		boolean result = veriCode.equals(vericode);
		if (!result) {
			// 无效的验证码!
			throw AMPException.getException(120001);
		} else {
			if (BfSMSTypeEnum.smstypeEnum.bindbank.toString().equals(smsType)) {
				StrRedisUtil.setEx(redis, PASS_VERI_CODE_USER_KEY + phone, BfSMSUtils.EXPIRE_SECONDS, phone);
			}
			//------------忘记密码增加绕过手机号校验---------------2017.04.10---------------
//			if (BfSMSTypeEnum.smstypeEnum.fogetlogin.toString().equals(smsType)) {
//				StrRedisUtil.setEx(redis, PASS_VERI_CODE_USER_FORGET_PWD_KEY + phone, BfSMSUtils.EXPIRE_SECONDS, phone);
//			}
			//------------忘记密码增加绕过手机号校验---------------2017.04.10---------------
		}
		return result;
	}
	
	/**
	 * 发送短信（由接口传过来的参数值）
	 * @param phone
	 * @param smsType
	 * @param values 短信模板值
	 * @return
	 */
	public BaseRep sendByType(String phone, String smsType, String[] values) {
		BaseRep rep = new BaseRep();
		log.info("手机号：{}，请求的短信类型：{}，请求参数：{}", phone, smsType, values);
		 
		if (BfSMSTypeEnum.smstypeEnum.login.toString().equals(smsType)) {
			// 用户未注册判断
			if (!this.investorBaseAccountService.isPhoneNumExists(phone)) {
				throw new AMPException("用户未注册，请先注册！");
			}
		}
		if(BfSMSTypeEnum.smstypeEnum.changeNewPhone.toString().equals(smsType)){
			this.investorBaseAccountService.checkNewPhoneAccount(phone);
		}
		if (BfSMSTypeEnum.smstypeEnum.regist.toString().equals(smsType)
				||BfSMSTypeEnum.smstypeEnum.xjfb_regist.toString().equals(smsType)) {
			// 判断手机号是否已注册
			if (this.investorBaseAccountService.isPhoneNumExists(phone)) {
				throw new AMPException("该手机号已经注册过，无法再次注册!");
			}
		}
		
		if (BfSMSTypeEnum.smstypeEnum.normal.toString().equals(smsType) ||
				BfSMSTypeEnum.smstypeEnum.fogetlogin.toString().equals(smsType)||
				BfSMSTypeEnum.smstypeEnum.CheckOldPhone.toString().equals(smsType)) {
			// 用户未注册判断
			if (!this.investorBaseAccountService.isPhoneNumExists(phone)) {
				throw new AMPException("用户账号不存在！");
			}
		}
		
		String content = smsTypesMap.get(smsType);;
		if (BfSMSTypeEnum.smstypeEnum.regist.toString().equals(smsType) || 
				BfSMSTypeEnum.smstypeEnum.bindbank.toString().equals(smsType) ||
				BfSMSTypeEnum.smstypeEnum.login.toString().equals(smsType) ||
				BfSMSTypeEnum.smstypeEnum.fogetlogin.toString().equals(smsType) ||
				BfSMSTypeEnum.smstypeEnum.normal.toString().equals(smsType)||
				BfSMSTypeEnum.smstypeEnum.resetLoginPwd.toString().equals(smsType)||
				BfSMSTypeEnum.smstypeEnum.resetPayPwd.toString().equals(smsType)||
				BfSMSTypeEnum.smstypeEnum.womenday.toString().equals(smsType)||
				BfSMSTypeEnum.smstypeEnum.xjfb_regist.toString().equals(smsType)||
				BfSMSTypeEnum.smstypeEnum.xjfb_resetPW.toString().equals(smsType)||
				BfSMSTypeEnum.smstypeEnum.CheckOldPhone.toString().equals(smsType)||
				BfSMSTypeEnum.smstypeEnum.changeNewPhone.toString().equals(smsType)
				) {
//			if (null == values || values.length <= 0) {
			values = new String[]{NumberUtil.randomNumb()};
			log.info("手机号：{}短信类型为：{}的验证码是：{}", phone, smsType, values[0]);
//			}
			// 将短信验证码保存到redis
			this.sendVC2Redis(phone, smsType, values[0]);
			// 校验该手机号当日短信下发次数
			this.checkSmsSendTimes(phone, smsType);
			if(BfSMSTypeEnum.smstypeEnum.xjfb_regist.toString().equals(smsType)||
				BfSMSTypeEnum.smstypeEnum.xjfb_resetPW.toString().equals(smsType)){
				content = this.replaceComStrArr(content, values) + this.xjfbcomcontent;//现金风暴后缀不一样
			}else{
				content = this.replaceComStrArr(content, values) + this.comcontent;
			}
		} else {
			content = this.replaceComStrArr(content, values);
		}
		
		if (!StringUtil.isEmpty(content)) {
			return this.sendSMS(phone,smsType, content);
		} else {
			rep.setErrorCode(BaseRep.ERROR_CODE);
			rep.setErrorMessage("无效的请求发送短信！");
		}
		
		return rep;
	}
	
	/**
	 * 校验手机号发送短信是否超过限制次数
	 * @param phone
	 */
	private void checkSmsSendTimes(String phone, String smsType) {
		String key = StrRedisUtil.VERI_CODE_REDIS_COUNT_KEY + phone + ":" + DateUtil.format(new Date(), DateUtil.datePattern);
		int count = Integer.valueOf(StrRedisUtil.incr(redis, key));
		StrRedisUtil.expire(redis, key, 86400L);
		int dayCount = Integer.valueOf(smsDayCount);
		log.info("key={},count={},dayCount={}", key, count, dayCount);
		if (count > dayCount) {
			StrRedisUtil.del(redis, StrRedisUtil.VERI_CODE_REDIS_KEY + phone + "_" + smsType);
			throw new AMPException("由于您今天使用验证码短信的次数过于频繁，暂无法继续发送，请明天再试");
		}
	}

	/**
	* <p>Title: </p>
	* <p>Description: 短信生产者发送消息</p>
	* <p>Company: </p> 
	* @param message
	* @author 邱亮
	* @date 2017年9月26日 上午10:24:01
	* @since 1.0.0
	*/
	private void sendMessage(String phone,String smsType,String  smsContent,String status) {
		ShortMessageEntity message = new ShortMessageEntity();
		message.setPhone(phone);
		message.setSmsType(smsType);
		message.setSmsContent(smsContent);
		message.setStatus(status);
		message.setSmsTime(new Timestamp(System.currentTimeMillis()));
		messageSendUtil.sendTopicMessage(messageSendUtil.getShortMessageTopic(),messageSendUtil.getShortMessageTopic(),message);
	}
}
