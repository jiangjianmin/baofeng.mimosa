package com.guohuai.mmp.investor.baseaccount;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guohuai.bfsms.BfSMSTypeEnum;
import com.guohuai.bfsms.BfSMSUtils;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.Digests;
import com.guohuai.component.util.PwdUtil;
import com.guohuai.component.util.StrRedisUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.mmp.investor.baseaccount.refer.details.InvestoRefErDetailsEntity;
import com.guohuai.mmp.investor.baseaccount.refer.details.InvestoRefErDetailsService;
import com.guohuai.mmp.investor.baseaccount.referee.InvestorRefEreeEntity;
import com.guohuai.mmp.investor.baseaccount.referee.InvestorRefEreeService;
import com.guohuai.mmp.investor.baseaccount.statistics.InvestorStatisticsEntity;
import com.guohuai.mmp.investor.baseaccount.statistics.InvestorStatisticsService;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.platform.tulip.TulipService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class InvestorBaseAccountTwoService {
	
	@Autowired
	private RedisTemplate<String, String> redis;
	@Autowired
	private InvestorBaseAccountDao investorBaseAccountDao;
	@Autowired
	private InvestorStatisticsService investorStatisticsService;
	@Autowired
	private PlatformStatisticsService platformStatisticsService;
	@Autowired
	private InvestorRefEreeService investorRefEreeService;
	@Autowired
	private InvestoRefErDetailsService investoRefErDetailsService;
	@Autowired
	private TulipService tulipNewService;
	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private BfSMSUtils bfSMSUtils;
	@Autowired
	private InvestorBaseAccountSyncAccountService investorBaseAccountSyncAccountService;
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorBaseAccountEntity initBaseAccount(String uid, String mid, String uacc, String racc, String sceneid, String registerChannelId) {
		// 投资人-基本账户
		InvestorBaseAccountEntity account = new InvestorBaseAccountEntity();
		account.setOid(uid);
		account.setUserOid(uid);
		account.setMemberId(mid);
		account.setPhoneNum(uacc);
		// 注册渠道
		account.setRegisterChannelId(registerChannelId);
		// 邀请码标识
		account.setUid(sceneid);
		// 前端注册-投资者
		account.setOwner(InvestorBaseAccountEntity.BASEACCOUNT_owner_investor);
		account.setStatus(InvestorBaseAccountEntity.BASEACCOUNT_status_normal);
		account.setIsFreshMan(InvestorBaseAccountEntity.BASEACCOUNT_isFreshMan_yes);
		account = this.investorBaseAccountService.saveEntity(account);

		// 投资人-统计
		InvestorStatisticsEntity en = new InvestorStatisticsEntity();
		en.setInvestorBaseAccount(account);
		this.investorStatisticsService.saveEntity(en);

		// 资金用户-推荐人统计
		InvestorRefEreeEntity investorRefEreeEntity = new InvestorRefEreeEntity();
		investorRefEreeEntity.setInvestorBaseAccount(account);
		this.investorRefEreeService.saveEntity(investorRefEreeEntity);

		// 资金用户-推荐人
		InvestorBaseAccountEntity recommender = this.investorBaseAccountDao.findByPhoneNum(racc);

		// 如果推荐人存在，则创建资金用户-推荐人明细
		if (recommender != null) {
			log.info("uacc:{},racc:{},创建邀请关系开始！", uacc, racc);
			// 获取推荐人统计信息
			InvestorRefEreeEntity investorRefEree = this.investorRefEreeService
					.getInvestorRefEreeByAccount(recommender);
			InvestoRefErDetailsEntity refErDetails = new InvestoRefErDetailsEntity();
			refErDetails.setInvestorRefEree(investorRefEree);
			refErDetails.setInvestorBaseAccount(account);
			// 创建资金用户-推荐人明细
			this.investoRefErDetailsService.saveEntity(refErDetails);

			// 更新推荐人注册人数
			this.investorRefEreeService.updateRegister(investorRefEree.getOid());
			log.info("uacc:{},racc:{},refErDetail:{},创建邀请关系结束！", uacc, racc,JSONObject.toJSONString(refErDetails));
		}

		// 投资人注册-增加平台注册人数
		this.platformStatisticsService.increaseRegisterAmount();

		// (注册事件)推广平台注册事件
		this.tulipNewService.onRegister(account, recommender);
		
		return account;
	}

	/**
	 * 注册(新)
	 * @param req
	 * @return
	 */
//	@Transactional(value = TxType.REQUIRES_NEW)
//	public InvestorBaseAccountEntity addBaseAccount(InvestorBaseAccountAddReq req) {		
//		if (this.investorBaseAccountService.isPhoneNumExists(req.getUserAcc())) {
//			throw new AMPException("该手机号已经注册过，无法再次注册!");
//		}
//		
//		this.bfSMSUtils.checkVeriCode(req.getUserAcc(), BfSMSTypeEnum.smstypeEnum.regist.toString(), req.getVericode());
//		
//		InvestorBaseAccountEntity account = new InvestorBaseAccountEntity();
//		try {
//			String userOid = StringUtil.uuid();
//			account.setOid(userOid);
//			account.setUserOid(userOid);
//			account.setPhoneNum(req.getUserAcc());
//			if (!StringUtil.isEmpty(req.getUserPwd())) {
//				account.setSalt(Digests.genSalt());
//				account.setUserPwd(PwdUtil.encryptPassword(req.getUserPwd(), account.getSalt()));
//			}
//			// 邀请码标识
//			account.setUid(StrRedisUtil.incr(redis, StrRedisUtil.USER_SCENEID_REDIS_KEY));
//			// 前端注册-投资者
//			account.setOwner(InvestorBaseAccountEntity.BASEACCOUNT_owner_investor);
//			account.setStatus(InvestorBaseAccountEntity.BASEACCOUNT_status_normal);
//			account.setSource(InvestorBaseAccountEntity.BASEACCOUNT_SOURCE_frontEnd);
//			account.setChannelid(req.getChannelid());
//			account.setIsFreshMan(InvestorBaseAccountEntity.BASEACCOUNT_isFreshMan_yes);
//			account = this.investorBaseAccountService.saveEntity(account);
//			req.setInvestorOid(userOid);
//		
//			// 投资人-统计
//			InvestorStatisticsEntity en = new InvestorStatisticsEntity();
//			en.setInvestorBaseAccount(account);
//			this.investorStatisticsService.saveEntity(en);
//	
//			// 资金用户-推荐人统计
//			InvestorRefEreeEntity investorRefEreeEntity = new InvestorRefEreeEntity();
//			investorRefEreeEntity.setInvestorBaseAccount(account);
//			this.investorRefEreeService.saveEntity(investorRefEreeEntity);
//	
//			// 资金用户-推荐人
//			InvestorBaseAccountEntity recommender = this.investorBaseAccountService.findByRecommendId(req.getSceneId());
//	
//			// 如果推荐人存在，则创建资金用户-推荐人明细
//			if (null != recommender) {
//				// 获取推荐人统计信息
//				InvestorRefEreeEntity investorRefEree = this.investorRefEreeService
//						.getInvestorRefEreeByAccount(recommender);
//				InvestoRefErDetailsEntity refErDetails = new InvestoRefErDetailsEntity();
//				refErDetails.setInvestorRefEree(investorRefEree);
//				refErDetails.setInvestorBaseAccount(account);
//				// 创建资金用户-推荐人明细
//				this.investoRefErDetailsService.saveEntity(refErDetails);
//	
//				// 更新推荐人注册人数
//				this.investorRefEreeService.updateRegister(investorRefEree.getOid());
//			}
//			// 记录用户信息到redis，主要是登录信息/个推ID
//			this.investorBaseAccountService.saveAccountRedis(account.getOid(), req.getClientId());
//			
//			// 投资人注册-增加平台注册人数
//			this.platformStatisticsService.increaseRegisterAmount();
//	
//			// (注册事件)推广平台注册事件
//			this.tulipNewService.onRegister(account, recommender);
//			
//			// 同步账务系统
//			this.investorBaseAccountSyncAccountService.syncAccount(req);
//			account.setMemberId(req.getMemberOid());
//		} catch (Exception e) {
//			Throwable cause = e.getCause();
//		    if(cause instanceof ConstraintViolationException) {
//		        String errMsg = ((ConstraintViolationException) cause).getSQLException().getMessage();
//		        if(!StringUtil.isEmpty(errMsg) && errMsg.indexOf("phoneNum") != -1) {
//					throw new AMPException("已经注册成功，请登录！");
//		        }
//		    } 
//		    
//		    if (e instanceof AMPException) {
//	    		throw new AMPException(((AMPException) e).getMessage());
//	    	}
//			log.error("用户：{}注册失败，原因：{}", req.getUserAcc(), e.getMessage());
//			throw new AMPException("注册失败！");
//		}
//		return account;
//	}
}
