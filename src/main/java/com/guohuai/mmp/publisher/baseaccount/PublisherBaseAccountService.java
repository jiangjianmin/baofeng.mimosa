package com.guohuai.mmp.publisher.baseaccount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.platform.accment.AccmentService;
//import com.guohuai.mmp.platform.payment.PlatformBalanceService;
import com.guohuai.mmp.publisher.baseaccount.loginacc.PublisherLoginAccService;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsEntity;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsService;
import com.guohuai.mmp.publisher.corporate.Corporate;
import com.guohuai.mmp.publisher.corporate.CorporateService;

@Service
@Transactional
public class PublisherBaseAccountService {
	@Autowired
	private PublisherStatisticsService publisherStatisticsService;
	@Autowired
	private PublisherBaseAccountDao publisherBaseAccountDao;
	@Autowired
	private CorporateService corporateService;
	@Autowired
	private PublisherLoginAccService publisherLoginAccService;
	@Autowired
	private AccmentService accmentService;
	
	
	public PublisherBaseAccountEntity findByCorperateOid(String corperateOid) {
		PublisherBaseAccountEntity entity = publisherBaseAccountDao.findByCorperateOid(corperateOid);
		if (null == entity) {
			//error.define[30056]=发行人账户不存在(CODE:30056)
			throw new AMPException(30056);
		}
		return entity;
	}
	
	public PublisherBaseAccountEntity findByLoginAcc(String uid) {
		PublisherBaseAccountEntity baseAccount = publisherLoginAccService.findByLoginAcc(uid);
		return baseAccount;
	}
	
	public PublisherBaseAccountRep userInfo(String uid) {
		PublisherBaseAccountRep rep = new PublisherBaseAccountRep();
		PublisherBaseAccountEntity baseAccount = this.findByLoginAcc(uid);
		
		PublisherStatisticsEntity sta = publisherStatisticsService.findByPublisherBaseAccount(baseAccount);
		rep.setPublisherOid(baseAccount.getOid()); // 发行人OID
		rep.setAccountBalance(baseAccount.getAccountBalance()); //账户余额
		rep.setTotalDepositAmount(sta.getTotalDepositAmount()); //累计充值总额
		rep.setTotalWithdrawAmount(sta.getTotalWithdrawAmount()); //累计提现总额
		rep.setTotalLoanAmount(sta.getTotalLoanAmount()); //累计借款总额
		rep.setTotalReturnAmount(sta.getTotalReturnAmount()); //累计还款总额
		rep.setTotalInterestAmount(sta.getTotalInterestAmount()); //累计付息总额
		
		rep.setTodayInvestAmount(sta.getTodayInvestAmount()); //今日投资总额
		rep.setTodayT0InvestAmount(sta.getTodayT0InvestAmount()); //今日活期投资额
		rep.setTodayTnInvestAmount(sta.getTodayTnInvestAmount()); //今日定期投资总额
		rep.setTodayRedeemAmount(sta.getTodayRedeemAmount()); //今日赎回金额
		rep.setTodayRepayInvestAmount(sta.getTodayRepayInvestAmount()); //今日还本金额
		rep.setTodayRepayInterestAmount(sta.getTodayRepayInterestAmount()); //今日付息金额
		
		rep.setOverdueTimes(sta.getOverdueTimes()); //逾期次数
		rep.setProductAmount(sta.getProductAmount()); //发行产品总数
		rep.setClosedProductAmount(sta.getClosedProductAmount()); //已结算产品数
		rep.setToCloseProductAmount(sta.getToCloseProductAmount()); //待结算产品数
		rep.setInvestorAmount(sta.getInvestorAmount()); //总投资人数
		rep.setInvestorHoldAmount(sta.getInvestorHoldAmount()); //现持仓人数
		rep.setTodayT0InvestorAmount(sta.getTodayT0InvestorAmount()); //今日活期投资人数
		rep.setTodayTnInvestorAmount(sta.getTodayTnInvestorAmount()); //今日定期投资人数
		rep.setTodayInvestorAmount(sta.getTodayInvestorAmount()); //今日投资人数
		return rep;
	}
	
	
	
	public int updateBalancePlusPlus(PublisherBaseAccountEntity publisher, BigDecimal orderAmout) {
		return publisherBaseAccountDao.updateBalancePlusPlus(publisher.getOid(), orderAmout);
	}
	
	public int updateBalanceMinusMinus(PublisherBaseAccountEntity publisher, BigDecimal orderAmout) {
		return publisherBaseAccountDao.updateBalanceMinusMinus(publisher.getOid(), orderAmout);
	}

	
	public List<PublisherBaseAccountEntity> findAll(){
		return this.publisherBaseAccountDao.findAll();
	}

	public int balanceEnough(BigDecimal orderAmount, String uid) {
		
		int i = publisherBaseAccountDao.balanceEnough(orderAmount, uid);
		if (i < 1) {
			// error.define[30057]=账户余额不足(CODE:30057)
			throw new AMPException(30057);
		}
		return i;
		
	}
	
	public PublisherBaseAccountEntity openDo(Corporate corporate) {
		
		
		
		PublisherBaseAccountEntity entity = publisherBaseAccountDao.findByCorperateOid(corporate.getOid());
		if (null != entity) {
			throw new AMPException("企业已经开户，请不要重复开户");
		}
		entity = new PublisherBaseAccountEntity();
		entity.setCorperateOid(corporate.getOid());
		
		entity.setStatus(PublisherBaseAccountEntity.PUBLISHER_BASE_ACCOUNT_STATUS_normal);
		this.saveEntity(entity);
		
		entity.setMemberId(accmentService.addUser(entity.getOid()));
		this.saveEntity(entity);
		
		PublisherStatisticsEntity st = new PublisherStatisticsEntity();
		st.setPublisherBaseAccount(entity);
		this.publisherStatisticsService.saveEntity(st);
		return entity;
	}
	

	public BaseRep open(BaseAccountOpenReq req) {
		
		Corporate corporate = corporateService.read(req.getCorperateOid());
		corporate.setIsOpen(Corporate.YES);
		corporateService.saveEntity(corporate);
		
		return new BaseRep();
	}

	private PublisherBaseAccountEntity saveEntity(PublisherBaseAccountEntity entity) {
		entity.setCreateTime(DateUtil.getSqlCurrentDate());
		return this.updateEntity(entity);
	}

	private PublisherBaseAccountEntity updateEntity(PublisherBaseAccountEntity entity) {
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		return this.publisherBaseAccountDao.save(entity);
	}

	public PublisherBaseAccountEntity findOne(String oid) {
		PublisherBaseAccountEntity entity = this.publisherBaseAccountDao.findOne(oid);
		if (null == entity) {
			throw new AMPException("发行人不存在");
		}
		return entity;
	}

	/**
	 * 发行人选择列表
	 * @author star.zhu
	 * @return
	 */
	@Transactional
	public List<JSONObject> getAllSPV() {
		List<JSONObject> objList = Lists.newArrayList();
		List<Corporate> list = this.corporateService.getCorportateList();
		if (null != list && !list.isEmpty()) {
			JSONObject obj = null;
			for (Corporate entity : list) {
				obj = new JSONObject();
				obj.put("spvId", entity.getOid());
				obj.put("spvName", entity.getName());
				objList.add(obj);
			}
		}

		return objList;
	}
	
	public List<PublisherDetailRep> options() {
		List<Corporate> list = this.corporateService.getCorportateList();
		List<PublisherDetailRep> r = new ArrayList<PublisherDetailRep>();
		if (null != list && list.size() > 0) {
			for (Corporate corporate : list) {
				r.add(new PublisherDetailRep(corporate));
			}
		}
		return r;
	}

	

	public List<Object[]> findOneOid(){
		return this.publisherBaseAccountDao.findOneOid();
	}

	
}
