package com.guohuai.mmp.investor.baseaccount;

import com.guohuai.ams.acct.account.AccountDao;
import com.guohuai.ams.label.LabelEnum;
import com.guohuai.ams.label.LabelService;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.basic.cardvo.CardVoStatus;
import com.guohuai.basic.message.ChangePhoneMessageEntity;
import com.guohuai.basic.message.MessageConstant;
import com.guohuai.bfsms.BfSMSTypeEnum;
import com.guohuai.bfsms.BfSMSUtils;
import com.guohuai.cache.entity.ProductCacheEntity;
import com.guohuai.cache.service.CacheHoldService;
import com.guohuai.cache.service.CacheProductService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.message.MessageSendUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StrRedisUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.investor.bank.BankEntity;
import com.guohuai.mmp.investor.bank.BankService;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountQueryRep.InvestorBaseAccountQueryRepBuilder;
import com.guohuai.mmp.investor.baseaccount.changephone.ChangePhoneEntity;
import com.guohuai.mmp.investor.baseaccount.changephone.ChangePhoneForm;
import com.guohuai.mmp.investor.baseaccount.changephone.ChangePhoneService;
import com.guohuai.mmp.investor.baseaccount.log.CouponLogEntity;
import com.guohuai.mmp.investor.baseaccount.log.CouponLogReq;
import com.guohuai.mmp.investor.baseaccount.log.CouponLogService;
import com.guohuai.mmp.investor.baseaccount.refer.details.InvestoRefErDetailsEntity;
import com.guohuai.mmp.investor.baseaccount.refer.details.InvestoRefErDetailsService;
import com.guohuai.mmp.investor.baseaccount.statistics.InvestorStatisticsEntity;
import com.guohuai.mmp.investor.baseaccount.statistics.InvestorStatisticsService;
import com.guohuai.mmp.investor.baseaccount.user.UserApi;
import com.guohuai.mmp.investor.fund.YiLuService;
import com.guohuai.mmp.investor.tradeorder.InvestorOpenCycleDao;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.TradeOrderReq;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.platform.redis.RedisSyncService;
import com.guohuai.mmp.platform.tulip.TulipConstants;
import com.guohuai.mmp.platform.tulip.TulipService;
import com.guohuai.mmp.tulip.rep.MyAllCouponRep;
import com.guohuai.mmp.tulip.sdk.TulipSDKService;
import com.guohuai.moonBox.util.SetRedisUtil;
import com.guohuai.tuip.api.objs.admin.UserReq;
import com.guohuai.usercenter.api.UserCenterSdk;
import com.guohuai.usercenter.api.obj.Response;
import com.guohuai.usercenter.api.obj.UcPayPwdReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@Transactional
public class InvestorBaseAccountService {
	
	private Logger logger = LoggerFactory.getLogger(InvestorBaseAccountService.class);
	
	@Autowired
	private RedisTemplate<String, String> redis;
	@Autowired
	private InvestorBaseAccountDao investorBaseAccountDao;
	@Autowired
	private InvestorOpenCycleDao investorOpenCycleDao;
	@Autowired
	private InvestorStatisticsService investorStatisticsService;
	@Autowired
	private PlatformStatisticsService platformStatisticsService;
	@Autowired
	private TulipService tulipService;
	@Autowired
	private ProductService productService;
	@Autowired
	private TulipSDKService tulipSDKService;
	@Autowired
	private InvestorBaseAccountTwoService investorBaseAccountTwoService;
	@Autowired
	private LabelService labelService;
	@Autowired
	private BankService bankService;
	@Autowired
	private CouponLogService couponLogService;
	@Autowired
	private InvestoRefErDetailsService investoRefErDetailsService;
	@Autowired
	private CacheProductService cacheProductService;
	@Autowired
	private BfSMSUtils bfSMSUtils;
	@Autowired
	private UserApi userApi;
	@Autowired
	private CacheHoldService cacheHoldService;
	@Autowired
	private RedisSyncService redisSyncService;
	@Autowired
	private UserCenterSdk userCenterSdk;
	@Autowired
	private MessageSendUtil messageSendUtil;
	@Autowired
	private ChangePhoneService changePhoneService;
	@Autowired
	private YiLuService yiLuService;
	@Autowired
	private AccountDao accountDao;
	/**
	 * 更新用户手机号，仅用于测试人员使用
	 * @param phoneNum 手机号
	 */
	public void changeAccPhoneNum(String phoneNum) {
		InvestorBaseAccountEntity account = this.investorBaseAccountDao.findByPhoneNum(phoneNum);
		if (null != account) {
			account.setPhoneNum(StringUtil.uuid());
			this.updateEntity(account);
		} else {
			//error.define[80000]=账户不存在!(CODE:80000)
			throw new AMPException(80000);
		}
	}
	
	@Transactional
	public PagesRep<InvestorBaseAccountQueryRep> accountQuery(Specification<InvestorBaseAccountEntity> spec, Pageable pageable) {		
		Page<InvestorBaseAccountEntity> accounts = this.investorBaseAccountDao.findAll(spec, pageable);
		PagesRep<InvestorBaseAccountQueryRep> pagesRep = new PagesRep<InvestorBaseAccountQueryRep>();		
		
		for (InvestorBaseAccountEntity en : accounts) {
			InvestorStatisticsEntity st = investorStatisticsService.findByInvestorBaseAccount(en);
				
			InvestorBaseAccountQueryRep rep = new InvestorBaseAccountQueryRepBuilder()
					.oid(en.getOid())
					.userOid(en.getUserOid())
//					.phoneNum(StringUtil.kickstarOnPhoneNum(en.getPhoneNum()))
//					.realName(StringUtil.kickstarOnRealname(en.getRealName()))
					.phoneNum(en.getPhoneNum())
					.realName(en.getRealName())
					.status(en.getStatus())
					.statusDisp(statusEn2Ch(en.getStatus()))
					.owner(en.getOwner())
					.ownerDisp(ownerEn2Ch(en.getOwner()))
					.balance(en.getBalance())
					.totalInvestAmount(st.getTotalInvestAmount())
					.totalIncomeAmount(st.getTotalIncomeAmount())
					.createTime(en.getCreateTime())
					.build();
			pagesRep.add(rep);
		}
		pagesRep.setTotal(accounts.getTotalElements());	
		return pagesRep;
	}
	
	private String statusEn2Ch(String status) {
		if (InvestorBaseAccountEntity.BASEACCOUNT_status_normal.equals(status)) {
			return "正常";
		}
		if (InvestorBaseAccountEntity.BASEACCOUNT_status_forbidden.equals(status)) {
			return "禁用";
		}
		return status;
	}
	
	private String ownerEn2Ch(String owner) {
		if (InvestorBaseAccountEntity.BASEACCOUNT_owner_investor.equals(owner)) {
			return "投资者 ";
		}
		if (InvestorBaseAccountEntity.BASEACCOUNT_owner_platform.equals(owner)) {
			return "平台";
		}
		return owner;
	}
	
	public InvestorBaseAccountEntity findByUid(String uid) {
		InvestorBaseAccountEntity baseAccount = investorBaseAccountDao.findOne(uid);
		if (null == baseAccount) {
			//error.define[80000]=账户不存在!(CODE:80000)
			throw new AMPException(80000);
		}
		return baseAccount;
	}
	
	public InvestorBaseAccountEntity findOne(String oid) {
		InvestorBaseAccountEntity baseAccount = investorBaseAccountDao.findOne(oid);
		if (null == baseAccount) {
			//error.define[80000]=账户不存在!(CODE:80000)
			throw new AMPException(80000);
		}
		return baseAccount;
	}

	public InvestorBaseAccountEntity findByPhone(String phone) {
		InvestorBaseAccountEntity baseAccount = investorBaseAccountDao.findByPhoneNum(phone);
		if (null == baseAccount) {
			//error.define[80000]=账户不存在!(CODE:80000)
			throw new AMPException(80000);
		}
		return baseAccount;
	}
	
	public InvestorBaseAccountEntity findByPhoneNum(String phoneNum) {		
		return investorBaseAccountDao.findByPhoneNum(phoneNum);
	}
	
	// 推荐邀请码
	public InvestorBaseAccountEntity findByRecommendId(String uid) {		
		return investorBaseAccountDao.findByUid(uid);
	} 
	
	public InvestorBaseAccountEntity findByMemberId(String mid) {
		InvestorBaseAccountEntity baseAccount = investorBaseAccountDao.findByMemberId(mid);
		if (null == baseAccount) {
			//error.define[80000]=账户不存在!(CODE:80000)
			throw new AMPException(80000);
		}
		return baseAccount;
	}


	public int balanceEnough(BigDecimal orderVolume, String uid) {
		this.findByUid(uid);
		int i = investorBaseAccountDao.balanceEnough(orderVolume, uid);
		if (i < 1) {
			//error.define[80002]=投资人-基本账户的余额不足!(CODE:80002)
			throw new AMPException(80002);
		}
		return i;
	}

	/**
	 * PC端
	 * @param uid
	 * @return
	 */
	public BaseAccountRep userInfoPc(String userOid) {
		
		return userInfo(userOid, false);
	}

	/**
	 * 判断手机号是否已经注册
	 * @param phoneNum
	 * @return
	 */
	public InvestorBaseAccountIsRegistRep isRegist(String phoneNum) {
		InvestorBaseAccountIsRegistRep rep = new InvestorBaseAccountIsRegistRep();
		InvestorBaseAccountEntity account = this.investorBaseAccountDao.findByPhoneNum(phoneNum);
		if (null != account) {
			rep.setRegist(true);
//			rep.setAccountOid(account.getOid());
		} else {
			rep.setRegist(false);
		}		
		return rep;
	}
	
	/**
	 * 判断手机号是否已经注册
	 * @param phoneNum
	 * @return
	 * 后端调用
	 */
	public InvestorBaseAccountIsRegistRep isRegistSDK(String phoneNum) {
		InvestorBaseAccountIsRegistRep rep = new InvestorBaseAccountIsRegistRep();
		InvestorBaseAccountEntity account = this.investorBaseAccountDao.findByPhoneNum(phoneNum);
		if (null != account) {
			rep.setRegist(true);
			rep.setAccountOid(account.getOid());
		} else {
			rep.setRegist(false);
		}		
		return rep;
	}
	
	/**
	 * 判断手机号是否已注册
	 * @param phoneNum
	 * @return
	 */
	public boolean isPhoneNumExists(String phoneNum) {
		InvestorBaseAccountEntity account = this.investorBaseAccountDao.findByPhoneNum(phoneNum);
		if (null == account) {
			return false;
		}
		return true;
	}
	
	/**
	 * APP端
	 * @param uid
	 * @param isClient 是后台还是客户端
	 * @return
	 */
	public BaseAccountRep userInfo(String uid, boolean isClient) {
		
		InvestorBaseAccountEntity baseAccount = this.findByUid(uid);
		InvestorStatisticsEntity sta = investorStatisticsService.findByInvestorBaseAccount(baseAccount);
		BankEntity bank = this.bankService.findByBaseAccount(baseAccount);
		
		BaseAccountRep rep = new BaseAccountRep();
		
		//查询用户基金总资产
		String ylUid = null;
		List<String> list = accountDao.selectYiLuOid(uid);
		if(null!=list&&list.size()>0) {
			ylUid = list.get(0);
		}
		BigDecimal fundAmount = new BigDecimal(0);
		Map<String,Object> map = new HashMap<String,Object>();
		map = yiLuService.myFund(ylUid);
		if(map.get("fundWealthAll")!=null) {
			fundAmount = new BigDecimal(map.get("fundWealthAll").toString());
			
		}
		// 安全处理不返回用户id相关信息
//		rep.setUserOid(baseAccount.getUserOid());
//		rep.setMemberId(baseAccount.getMemberId());
		// 客户端显示*处理信息
		if (isClient) {
			rep.setPhone(StringUtil.kickstarOnPhoneNum(baseAccount.getPhoneNum()));
			rep.setPhoneNum(StringUtil.kickstarOnPhoneNum(baseAccount.getPhoneNum()));
			rep.setRealName(StringUtil.kickstarOnRealname(baseAccount.getRealName()));
			rep.setBankName(null == bank ? "" : StringUtil.kickstarOnRealname(bank.getBankName()));
			rep.setCardNum(null == bank ? "" : StringUtil.kickstarOnCardNum(bank.getDebitCard()));
		}else {
			rep.setPhone(baseAccount.getPhoneNum());
			rep.setPhoneNum(baseAccount.getPhoneNum());
			rep.setRealName(baseAccount.getRealName());
			rep.setBankName(null == bank ? "" : bank.getBankName());
			rep.setCardNum(null == bank ? "" : bank.getDebitCard());
		}
		rep.setStatus(baseAccount.getStatus());
		rep.setFundAmount(fundAmount);
		rep.setStatusDisp(statusEn2Ch(baseAccount.getStatus()));
		rep.setBalance(baseAccount.getBalance());
		rep.setOwner(baseAccount.getOwner());
		rep.setOwner(ownerEn2Ch(baseAccount.getOwner()));
		rep.setIsFreshman(baseAccount.getIsFreshMan());
		rep.setIsFreshmanDisp(isFreshmanEn2Ch(baseAccount.getIsFreshMan()));
		rep.setTotalDepositAmount(sta.getTotalDepositAmount());
		rep.setTotalWithdrawAmount(sta.getTotalWithdrawAmount());
		rep.setTotalInvestAmount(sta.getTotalInvestAmount());
		rep.setTotalRedeemAmount(sta.getTotalRedeemAmount());
		rep.setTotalIncomeAmount(sta.getTotalIncomeAmount());
		rep.setTotalRepayLoan(sta.getTotalRepayLoan());
		rep.setT0YesterdayIncome(sta.getT0YesterdayIncome());
		rep.setTnTotalIncome(sta.getTnTotalIncome());
		rep.setT0TotalIncome(sta.getT0TotalIncome());
		rep.setT0CapitalAmount(sta.getT0CapitalAmount());
		rep.setTnCapitalAmount(sta.getTnCapitalAmount());
		rep.setTotalInvestProducts(sta.getTotalInvestProducts());
		rep.setTotalDepositCount(sta.getTotalDepositCount());
		rep.setTotalWithdrawCount(sta.getTotalWithdrawCount());
		rep.setTotalInvestCount(sta.getTotalInvestCount());
		rep.setTotalRedeemCount(sta.getTotalRedeemCount());
		
		rep.setTodayDepositCount(sta.getTodayDepositCount());
		rep.setTodayWithdrawCount(sta.getTodayWithdrawCount());
		rep.setTodayInvestCount(sta.getTodayInvestCount());
		rep.setTodayRedeemCount(sta.getTodayRedeemCount());
		
		rep.setTodayDepositAmount(sta.getTodayDepositAmount());
		rep.setTodayWithdrawAmount(sta.getTodayWithdrawAmount());
		rep.setTodayInvestAmount(sta.getTodayInvestAmount());
		rep.setTodayRedeemAmount(sta.getTodayRedeemAmount());
		
		rep.setFirstInvestTime(sta.getFirstInvestTime());
		rep.setIncomeConfirmDate(sta.getIncomeConfirmDate());
		rep.setUpdateTime(sta.getUpdateTime());
		rep.setCreateTime(sta.getCreateTime());
		return rep;
	}


	private String isFreshmanEn2Ch(String isFreshMan) {
		if (InvestorBaseAccountEntity.BASEACCOUNT_isFreshMan_yes.equals(isFreshMan)) {
			return "是新手";
		}
		if (InvestorBaseAccountEntity.BASEACCOUNT_isFreshMan_no.equals(isFreshMan)) {
			return "非新手";
		}
		
		return isFreshMan;
	}

	
	public int updateBalancePlusPlus(BigDecimal orderAmount, InvestorBaseAccountEntity investorBaseAccount) {
		return this.investorBaseAccountDao.updateBalancePlusPlus(
				investorBaseAccount.getOid(), orderAmount);
	}
	
	public int updateBalanceMinusMinus(BigDecimal orderAmount, InvestorBaseAccountEntity investorBaseAccount) {
		int i = this.investorBaseAccountDao.updateBalanceMinusMinus(
				investorBaseAccount.getOid(), orderAmount);
		return i;
		
	}
	
	
	
	
	/**
	 * 获取超级投资用户
	 * @return
	 */
	public InvestorBaseAccountEntity getSuperInvestor(){
		InvestorBaseAccountEntity baseAccount = this.investorBaseAccountDao.findByOwner(InvestorBaseAccountEntity.BASEACCOUNT_owner_platform);
		if (null == baseAccount) {
			throw new AMPException("超级投资用户不存在!");
		}
		return baseAccount;
	}

	public BaseAccountRep supermanInfo() {
		InvestorBaseAccountEntity baseAccount = this.getSuperInvestor();
		if (null == baseAccount) {
			throw new AMPException("投资人-基本账户不存在!");
		}
		return this.userInfo(baseAccount.getUserOid(), false);
		
	}
	
	/**
	 * 同步用户中心数据，来源于前端注册的用户的信息
	 * @param uid userOid
	 * @param mid 会员OID
	 * @param uacc 手机号
	 * @param racc 推荐人手机号
	 * @param sceneid 邀请码
	 * @return
	 */
	public BaseRep initBaseAccount(String uid, String mid, String uacc, String racc, String sceneid, String registerChannelId){
		InvestorBaseAccountEntity returnAccount=investorBaseAccountTwoService.initBaseAccount(uid, mid, uacc, racc, sceneid, registerChannelId);
		//使用体验金投资活期产品
		logger.info("=======================uid:"+returnAccount.getOid());
		CouponLogReq entity=new CouponLogReq();
		entity.setUserOid(uid);
		entity.setType(CouponLogEntity.TYPE_REGISTER);
		couponLogService.createEntity(entity);
//		TradeOrderReq tradeOrderReq = this.useRegisterTastecoupon(uid);
//		if(null != tradeOrderReq){
//			this.investorInvestTradeOrderExtService.expGoldInvest(tradeOrderReq);
//		}
		return new BaseRep();
	}
	
	/**
	 * 注册重新发送体验金
	 * @param phone
	 * @return
	 */
	public BaseRep reSendCoupon(String phone) {
		BaseRep rep = new BaseRep();
		logger.info("给用户：{}重新发送注册体验金。", phone);
		InvestorBaseAccountEntity account = this.investorBaseAccountDao.findByPhoneNum(phone);
		if (null == account) {
			//error.define[80000]=账户不存在!(CODE:80000)
			throw new AMPException(80000);
		}
		
		InvestoRefErDetailsEntity refDetails = this.investoRefErDetailsService.findByAccount(account);
		
		
		UserReq req = new UserReq();
		req.setUserId(account.getUserOid());
		req.setPhone(account.getPhoneNum());// 手机号
		if (refDetails != null) {
			req.setFriendId(refDetails.getInvestorRefEree().getInvestorBaseAccount().getUserOid());// 推荐人
		}

		// 请求推广平台结果
		rep = this.tulipSDKService.onRegister(req);
		
		if (0 != rep.getErrorCode()) {
			return rep;
		}
		
		//使用体验金投资活期产品
		logger.info("用户：{}，注册体验金投资活期产品。", account.getOid());
		CouponLogReq entity = new CouponLogReq();
		entity.setUserOid(account.getUserOid());
		entity.setType(CouponLogEntity.TYPE_REGISTER);
		couponLogService.createEntity(entity);
		logger.info("给用户：{}重新发送注册体验金成功。", phone);
		return rep;
	}
	
	/**
	 * 注册送体验金
	 * @param uid
	 * @return
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public TradeOrderReq useTastecoupon(String uid) {
		TradeOrderReq tradeOrderReq = null;
		// 查询当前用户未使用的体验金
		PagesRep<MyAllCouponRep> myAllCoupons = this.tulipService.getMyAllCouponList(uid,
				TulipConstants.STATUS_COUPON_NOTUSED, TulipConstants.COUPON_TYPE_TASTECOUPON, 1, 10);
		// 如果用户没有体验金
		if (myAllCoupons.getTotal() <= 0) {
			logger.info("=======================uid:{},体验金未发放,等待下次发放", uid);
			throw new AMPException("体验金未发放,等待下次发放");
		}
		List<MyAllCouponRep> couponList = myAllCoupons.getRows();
		MyAllCouponRep myCouponRep = couponList.get(0);

		Product usedProduct = null;
		List<Product> productList = this.productService.findOnSaleProducts();
		for (Product p : productList) {
			// 根据产品ID获取基本标签
			Integer labelCount = this.labelService.findLabelByProductId(p.getOid());
			if (labelCount > 0) {
				usedProduct = p;
				break;
			}
		}
		if (usedProduct == null) {
			logger.info("=======================uid:{},体验金产品未创建。", uid);
			throw new AMPException("请新建体验金产品");
		}

		// 可用
		tradeOrderReq = new TradeOrderReq();
		// 产品ID
		tradeOrderReq.setProductOid(usedProduct.getOid());
		// 申购金额
		tradeOrderReq.setMoneyVolume(myCouponRep.getAmount());
		// 卡券ID
		tradeOrderReq.setCouponId(myCouponRep.getOid());
		// 卡券类型
		tradeOrderReq.setCouponType(myCouponRep.getType());
		// 卡券实际抵扣金额
		tradeOrderReq.setCouponDeductibleAmount(myCouponRep.getAmount());
		// 卡券金额
		tradeOrderReq.setCouponAmount(myCouponRep.getAmount());
		// 投资者实付金额
		tradeOrderReq.setPayAmouont(BigDecimal.ZERO);
		// 投资者用户Id
		tradeOrderReq.setUid(uid);
		return tradeOrderReq;
	}
	/**
	 * 用户绑卡成功之后推荐人投资产品
	 * @param uid
	 * @return
	 */
	public BaseRep addBank(String uid, String refereeId) {
		// 投资人-基本账户
//		InvestorBaseAccountEntity account = investorBaseAccountDao.findByUserOid(uid);
//		InvestorBaseAccountEntity recommender = investorBaseAccountDao.findByUserOid(refereeId);
		// (注册事件)推广平台注册事件
//		this.tulipService.onReferee(account, recommender);
		// 使用体验金投资活期产品
//		logger.info("发送推荐人事件,给uid:"+ uid+"下发体验金");

//		CouponLogReq entity = new CouponLogReq();
//		entity.setUserOid(recommender.getUserOid());
//		entity.setType(CouponLogEntity.TYPE_REFEREE);
//		couponLogService.createEntity(entity);

		return new BaseRep();
	}


	public InvestorBaseAccountEntity saveEntity(InvestorBaseAccountEntity entity) {
		entity.setCreateTime(DateUtil.getSqlCurrentDate());
		return this.updateEntity(entity);
	}

	

	public InvestorBaseAccountEntity updateEntity(InvestorBaseAccountEntity entity) {
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		return this.investorBaseAccountDao.save(entity);
	}

	public void updateAccountRealName(InvestorBaseAccountEntity entity, String name, String idNum) {
		entity.setRealName(name);
		entity.setIdNum(idNum);
		this.updateEntity(entity);
		
		if (!StringUtil.isEmpty(name) && !StringUtil.isEmpty(idNum)) {
			this.platformStatisticsService.increaseVerifiedInvestorAmount();
			
			this.tulipService.onSetRealName(entity);
		}
	}
	
	/**
	 * 同步用户真实姓名
	 * @param uid
	 * @param name
	 * @param idNum
	 * @return
	 */
	public BaseRep syncucUserName(String uid, String name, String idNum){
		InvestorBaseAccountEntity entity = this.findByUid(uid);
		entity.setRealName(name);
		entity.setIdNum(idNum);
		this.updateEntity(entity);
		
		if (!StringUtil.isEmpty(name) && !StringUtil.isEmpty(idNum)) {
			this.platformStatisticsService.increaseVerifiedInvestorAmount();
			
			
			this.tulipService.onSetRealName(entity);
		}
		
		return new BaseRep();
	}
	
	/**
	 * 用户基本信息
	 */
	public InvestorBaseAccountInfoRep getUserInfo(String oid){
		InvestorBaseAccountInfoRep rep = new InvestorBaseAccountInfoRep();
		InvestorBaseAccountEntity account = this.findOne(oid);
		
		InvestorStatisticsEntity st = investorStatisticsService.findByInvestorBaseAccount(account);
		
		rep.setPhoneNum(account.getPhoneNum());
		rep.setRealName(account.getRealName());
		rep.setStatus(account.getStatus());
		rep.setBalance(account.getBalance());
		rep.setCreateTime(account.getCreateTime());
		rep.setFirstInvestTime(st.getFirstInvestTime());
		return rep;
	}
	
	/**
	 * 锁定与解锁用户
	 * @param uoid
	 * @param islock is/not
	 * @return
	 */
	public BaseRep lockUser(String oid, String islock){
		BaseRep rep = new BaseRep();
		InvestorBaseAccountEntity account = this.findOne(oid);
		String status = InvestorBaseAccountEntity.BASEACCOUNT_status_normal;
		// 锁定
		if("is".equals(islock)){
			status = InvestorBaseAccountEntity.BASEACCOUNT_status_forbidden;
		}
		account.setStatus(status);
		this.updateEntity(account);
		
		userApi.setLock(oid, islock);
		
		return rep;
	}

//	public QueryAccountDetailsResp queryAccountDetails(QueryAccountDetailsReq req) {
//		req.setIdentityId(this.investorBaseAccountDao.findByPhoneNum(req.getPhoneNum()).getUserOid());
//		
//		return this.platformBalanceService.queryAccountDetails(req);
//	}

	public int borrowFromPlatform(BigDecimal amount) {
		int i = this.investorBaseAccountDao.borrowFromPlatform(amount);
		if (i < 1) {
			//error.define[30052]=借款失败(CODE:30051)
			throw new AMPException(30052);
		}
		return i;
		
	}

	public int payToPlatform(BigDecimal amount) {
		int i = this.investorBaseAccountDao.payToPlatform(amount);
		if (i < 1) {
			//error.define[30053]=还款失败(CODE:30053)
			throw new AMPException(30053);
		}
		return i;
		
	}

	public List<InvestorBaseAccountEntity> query4Bonus(String lastOid) {
		
		return this.investorBaseAccountDao.query4Bonus(lastOid);
	}

	public String findMemberIdByUserOid(String userOid) {
		String memeberId = this.investorBaseAccountDao.findMemberIdByUserOid(userOid);
		return memeberId;
	}
	
	public void onRegister() {
		List<InvestorBaseAccountEntity> list =  this.investorBaseAccountDao.onRegister();
		for (InvestorBaseAccountEntity entity : list) {
			UserReq req = new UserReq();
			req.setUserId(entity.getUserOid());
			req.setPhone(entity.getPhoneNum());// 手机号
			this.tulipSDKService.onRegister(req);
			logger.info("phoneNum={},注册事件发送成功", entity.getPhoneNum());
		}
	}
	/**
	 * 手动补偿被推荐人帮卡未下发体验金
	 * @return
	 */
	public BaseRep compensateRefereeCoupon() {
		List<Object[]> list=investorBaseAccountDao.getNeedCompensateRefereeCoupon();
		for(Object[] objs : list){
			int eachSize=Integer.parseInt(objs[5].toString());
			String refereeId=objs[2].toString();
			for(int i=0;i<eachSize;i++){
				InvestorBaseAccountEntity recommender = investorBaseAccountDao.findByUserOid(refereeId);
				// (注册事件)推广平台注册事件
				InvestorBaseAccountEntity account=new InvestorBaseAccountEntity();
				account.setUserOid("111");
				this.tulipService.onReferee(account, recommender, null);
				// 使用体验金投资活期产品
				logger.info("=======================uid:{}", recommender);

				CouponLogReq entity = new CouponLogReq();
				entity.setUserOid(recommender.getUserOid());
				entity.setType(CouponLogEntity.TYPE_REFEREE);
				couponLogService.createEntity(entity);
			}
			
		}
		return new BaseRep();
	}

	public List<InvestorBaseAccountEntity> findByWriteOffStatus() {
		return investorBaseAccountDao.findByWriteOffStatus();
	}
	
	public void updateBaseAccountStatus(String userOid) {
		int i = this.investorBaseAccountDao.updateBaseAccountStatus(userOid);
		if (i < 1) {
			// error.define[30014]=赎回超出产品单日净赎回上限(CODE:30014)
			throw new AMPException(30014);
		}
	}
		
	/**
	 * 新手否
	 */
	public void isNewBie(InvestorTradeOrderEntity orderEntity) {
		ProductCacheEntity cache = this.cacheProductService
				.getProductCacheEntityById(orderEntity.getProduct().getOid());
		int i = this.investorBaseAccountDao.updateFreshman(orderEntity.getInvestorBaseAccount().getOid());
		if (i < 1) {
			boolean isMatchType= Stream.of(Product.TYPE_Producttype_01,Product.TYPE_Producttype_03).anyMatch(type-> Objects.equals(type,cache.getType()));
			if (isMatchType && labelService
					.isProductLabelHasAppointLabel(cache.getProductLabel(), LabelEnum.newbie.toString())) {
				logger.info("用户账户Oid:{},是否新手:{}", orderEntity.getInvestorBaseAccount().getOid(), i);
				redisSyncService.saveEntityRefInvestorHoldRequireNew(orderEntity.getInvestorBaseAccount().getOid(),
						orderEntity.getProduct().getOid(), orderEntity.getProduct().getAssetPool().getOid());
				// error.define[30077]=新手产品只提供初次购买(CODE:30077)
				throw new AMPException(30077);
			}
		}
	}


	/**
	 * 注册(新)
	 * @param req
	 * @return
	 */
//	@Transactional
//	public BaseRep addBaseAccount(InvestorBaseAccountAddReq req){
//		
//		this.investorBaseAccountTwoService.addBaseAccount(req);
//		
//		//使用体验金投资活期产品
//		logger.info("=======================uid:" + req.getInvestorOid());
//		CouponLogReq entity=new CouponLogReq();
//		entity.setUserOid(req.getInvestorOid());
//		entity.setType(CouponLogEntity.TYPE_REGISTER);
//		couponLogService.createEntity(entity);
////		TradeOrderReq tradeOrderReq = this.useRegisterTastecoupon(uid);
////		if(null != tradeOrderReq){
////			this.investorInvestTradeOrderExtService.expGoldInvest(tradeOrderReq);
////		}
//		return new BaseRep();
//	}
//	
//	/**
//	 * 登录
//	 * @param req
//	 * @return
//	 */
//	public String login(InvestorBaseAccountLoginReq req){
//		InvestorBaseAccountEntity account = this.findByPhone(req.getUserAcc());
//		if (StringUtil.isEmpty(account.getUserPwd()) || StringUtil.isEmpty(account.getSalt())) {
//			throw new AMPException("您未设置登录密码，建议使用快捷登录！");
//		}
//		InvestorBaseAccountRedisInfo accountRedis = this.saveAccountRedis(account.getOid(), req.getClientId());
//		
//		logger.info("用户：{}使用密码进行登录。", account.getPhoneNum());
//		// 获取锁定状态
//		this.getLockState(account, accountRedis, true);
//		
//		if (PwdUtil.checkPassword(req.getUserPwd(), account.getUserPwd(), account.getSalt())) {
//			// 更新用户登录错误次数，清零
//			accountRedis.setPwdErrorTimes(0);
//			this.updateAccountRedis(account.getOid(), accountRedis);
//			return account.getOid();
//		} else {
//			// 错误次数累计
//			int pwdErrorTimes = accountRedis.getPwdErrorTimes() + 1;
//			if(pwdErrorTimes == 5){
//				// 设置锁定时间
//				accountRedis.setLockTime(DateUtil.getSqlDate());
//			}
//			// 更新用户登录错误次数
//			accountRedis.setPwdErrorTimes(pwdErrorTimes);
//			this.updateAccountRedis(account.getOid(), accountRedis);
//			if(pwdErrorTimes == 5){
//				logger.info("用户：{}，密码输入错误：5次，时间：{}", account.getPhoneNum(), DateUtil.getSqlDate());
//				throw GHException.getException("密码连续输入错误超过五次，账号已被锁定24小时！");
//			}
//			logger.info("用户：{}，密码输入错误：{}次，时间：{}", account.getPhoneNum(), pwdErrorTimes, DateUtil.getSqlDate());
//			throw new GHException("登录名和密码不匹配，连续输错超过5次账号当天将会被锁定，剩余" + (5- pwdErrorTimes) + "次机会");
//		}
//	}
	
//	/**
//	 * 快速登录
//	 * @param fu
//	 * @return
//	 */
//	public String fastLogin(InvestorBaseAccountLoginReq req) {
//		InvestorBaseAccountEntity account = this.findByPhone(req.getUserAcc());
//		this.bfSMSUtils.checkVeriCode(req.getUserAcc(), BfSMSTypeEnum.smstypeEnum.login.toString(), req.getVericode());
//		this.saveAccountRedis(account.getOid(), req.getClientId());
//		return account.getOid();
//	}
	
	/**
	 * 初始化登录信息 redis
	 * @param accountOid
	 * @param clientId 可为空
	 * @return
	 */
	public InvestorBaseAccountRedisInfo saveAccountRedis(String investorOid, String clientId) {
		InvestorBaseAccountRedisInfo accountRedis = InvestorBaseAccountRedisUtil.get(redis, investorOid);
		
		if (null == accountRedis) {
			accountRedis = new InvestorBaseAccountRedisInfo();
		}
		if (!StringUtil.isEmpty(clientId)) {
			accountRedis.setClientId(clientId);
		}
		accountRedis = InvestorBaseAccountRedisUtil.set(redis, investorOid, accountRedis);
		return accountRedis;
	}
	
	/**
	 * 更新登录信息 redis
	 * @param investorOid
	 * @param accountRedis
	 * @return
	 */
	public InvestorBaseAccountRedisInfo updateAccountRedis(String investorOid, InvestorBaseAccountRedisInfo accountRedis) {
		return InvestorBaseAccountRedisUtil.set(redis, investorOid, accountRedis);
	}
	
//	/**
//	 * 判断用户的锁定状态
//	 * @param userAcc
//	 */
//	public void checkLockState(String userAcc) {
//		InvestorBaseAccountEntity account = this.findByPhone(userAcc);
//		InvestorBaseAccountRedisInfo accountRedis = this.saveAccountRedis(account.getOid(), "");
//		// 获取登录状态
//		this.getLockState(account, accountRedis, false);
//	}
	
	/**
	 * 获取当前用户锁定状态
	 * @param account
	 * @param accountRedis
	 * @param isLogin
	 */
	public void getLockState(InvestorBaseAccountEntity account, InvestorBaseAccountRedisInfo accountRedis, boolean isLogin){
		
		if (InvestorBaseAccountEntity.BASEACCOUNT_status_forbidden.equals(account.getStatus())) {
			throw new AMPException("此用户已被冻结!");
		}
		
		if (null != accountRedis.getLockTime()) {
			// 不是同一天，更新用户登录错误次数，清零
			if (!DateUtil.same(DateUtil.getSqlDate(), accountRedis.getLockTime())){
				accountRedis.setPwdErrorTimes(0);
				accountRedis.setLockTime(DateUtil.getSqlDate());
				InvestorBaseAccountRedisUtil.set(redis, account.getOid(), accountRedis);
			}
		}
		
		if (accountRedis.getPwdErrorTimes() > 4) {
			if (isLogin) {
				throw new AMPException("密码连续输入错误超过五次，账号已被锁定24小时！");
			} else {
				throw new AMPException("您的账号已被锁定，不能找回密码！！");
			}
		}
	}
	
	/**
	 * 校验输入的原登录密码是否正确
	 * @param req
	 * @return
	 */
//	public BaseRep checkLoginPassword(InvestorBaseAccountPasswordReq req) {
//		InvestorBaseAccountEntity account = this.findOne(req.getInvestorOid());
//		BaseRep rep = new BaseRep();
//		if (!PwdUtil.checkPassword(req.getUserPwd(), account.getUserPwd(), account.getSalt())) {
//			rep.setErrorCode(-1);
//			rep.setErrorMessage("原登录密码验证错误！");
//		}
//		return rep;
//	}
//	
//	/**
//	 * 设置/修改登录密码
//	 * @param req
//	 * @return
//	 */
//	public BaseRep editLoginPassword(InvestorBaseAccountPasswordReq req) {
//		InvestorBaseAccountEntity account = this.findOne(req.getInvestorOid());
//		BaseRep rep = new BaseRep();
//		if (PwdUtil.checkPassword(req.getUserPwd(), account.getPayPwd(), account.getPaySalt())) {
//			rep.setErrorCode(-1);
//			rep.setErrorMessage("登录密码和交易密码不能一致！");
//			return rep;
//		}
//		this.updatePasswordEntity(account, req);
//		return rep;
//	}
//	
//	/**
//	 * 忘记登录密码
//	 * @param req
//	 * @return
//	 */
//	public BaseRep forgetLoginPassword(InvestorBaseAccountPasswordReq req) {
//		InvestorBaseAccountEntity account = this.findByPhone(req.getUserAcc());
//		this.bfSMSUtils.checkVeriCode(req.getUserAcc(), BfSMSTypeEnum.smstypeEnum.fogetlogin.toString(), req.getVericode());
//		BaseRep rep = new BaseRep();
//		this.updatePasswordEntity(account, req);
//		return rep;
//	}
//	
//	public void updatePasswordEntity(InvestorBaseAccountEntity account, InvestorBaseAccountPasswordReq req) {
//		account.setSalt(Digests.genSalt());
//		account.setUserPwd(PwdUtil.encryptPassword(req.getUserPwd(), account.getSalt()));
//		this.updateEntity(account);
//	}
//	
//	/**
//	 * 校验支付密码是否正确
//	 * @param req
//	 * @return
//	 */
//	public BaseRep checkPayPwd(InvestorBaseAccountPayPwdReq req) {
//		InvestorBaseAccountEntity account = this.findOne(req.getInvestorOid());
//		BaseRep rep = new BaseRep();
//		if (!PwdUtil.checkPassword(req.getPayPwd(), account.getPayPwd(), account.getPaySalt())) {
//			rep.setErrorCode(-1);
//			rep.setErrorMessage("交易密码验证错误！");
//		}
//		return rep;
//	}
//	
//	/**
//	 * 设置/修改支付密码
//	 * @param req
//	 * @return
//	 */
//	public BaseRep editPayPwd(InvestorBaseAccountPayPwdReq req) {
//		InvestorBaseAccountEntity account = this.findOne(req.getInvestorOid());
//		BaseRep rep = new BaseRep();
//		if (PwdUtil.checkPassword(req.getPayPwd(), account.getUserPwd(), account.getSalt())) {
//			rep.setErrorCode(-1);
//			rep.setErrorMessage("交易密码和登录密码不能一致！");
//			return rep;
//		}
//		account.setPaySalt(Digests.genSalt());
//		account.setPayPwd(PwdUtil.encryptPassword(req.getPayPwd(), account.getPaySalt()));
//		this.updateEntity(account);
//		return rep;
//	}
//	
//	/**
//	 * 用户信息
//	 * @param investorOid
//	 * @return
//	 */
//	public BaseAccountInfoRep getAccountInfo(String investorOid) {
//		InvestorBaseAccountEntity account = this.findOne(investorOid);
//		
//		BaseAccountInfoRep rep = new BaseAccountInfoRep();
//		rep.setIslogin(true);
//		rep.setInvestorOid(account.getOid());
//		rep.setUserOid(account.getUserOid());
//		rep.setUserAcc(account.getPhoneNum());
//		rep.setUserPwd(StringUtil.isEmpty(account.getUserPwd()) ? false : true);
//		rep.setPaypwd(StringUtil.isEmpty(account.getPayPwd()) ? false : true);
//		rep.setSceneid(account.getUid());
//		rep.setStatus(account.getStatus()); // 冻结状态
//		rep.setSource(account.getSource()); // 注册来源
//		rep.setChannelid(account.getChannelid()); // 渠道来源
//		rep.setCreateTime(DateUtil.formatFullPattern(account.getCreateTime())); // 注册时间
//		
//		BankEntity bank = this.bankService.findByBaseAccount(account);
//		
//		if (null != bank) {
//			rep.setName(StringUtil.kickstarOnRealname(bank.getName())); // 姓名
//			rep.setFullName(bank.getName()); // 全姓名
//			rep.setIdNumb(StringUtil.kickstarOnIdNum(bank.getIdCard())); // 身份证号
//			rep.setFullIdNumb(bank.getIdCard()); // 全身份证号
//			rep.setBankName(bank.getBankName()); // 银行名称
//			rep.setBankCardNum(StringUtil.kickstarOnCardNum(bank.getDebitCard())); //　银行卡号
//			rep.setFullBankCardNum(bank.getDebitCard()); // 全银行卡号
//			rep.setBankPhone(StringUtil.kickstarOnPhoneNum(bank.getPhoneNo())); // 预留手机号
//		}		
//		return rep;
//	}
	
//	/**
//	 * 解锁登录锁定
//	 * @param investorOid
//	 */
//	public void cancelLoginLock(String investorOid) {
//		this.findOne(investorOid);
//		InvestorBaseAccountRedisInfo accountRedis = this.saveAccountRedis(investorOid, "");
//		accountRedis.setPwdErrorTimes(0);
//		this.updateAccountRedis(investorOid, accountRedis);
//	}

	/**判断是否是超级用户的订单
	 * @param orderEntity
	 * @return
	 */
	public boolean isSuperMan(InvestorTradeOrderEntity orderEntity) {
		logger.info("<------------orderEntity.getInvestorBaseAccount().getOwner():{}--------------->",orderEntity.getInvestorBaseAccount().getOwner());
		if(InvestorBaseAccountEntity.SUPER_MAN_STATUS.equals(orderEntity.getInvestorBaseAccount().getOwner())){
			logger.info("<------------is superman--------------->");
			return true;
		}
		return false;
	}

	
//	/**
//	 * 新手否
//	 */
//	public void isNewBie(InvestorTradeOrderEntity orderEntity) {
//		ProductCacheEntity cache = this.cacheProductService.getProductCacheEntityById(orderEntity.getProduct().getOid());
//		if (Product.TYPE_Producttype_01.equals(cache.getType()) && labelService.isProductLabelHasAppointLabel(cache.getProductLabel(), LabelEnum.newbie.toString())) {
//			int i = this.investorBaseAccountDao.updateFreshman(orderEntity.getInvestorBaseAccount().getOid());
//			if (i < 1) {
//				// error.define[30077]=新手产品只提供初次购买(CODE:30077)
//				throw new AMPException(30077);
//			}
//		}
//	}
	
	/**
	 * 注册(新)
	 * @param req
	 * @return
	 */
//	@Transactional
//	public BaseRep addBaseAccount(InvestorBaseAccountAddReq req){
//		
//		this.investorBaseAccountTwoService.addBaseAccount(req);
//		
//		//使用体验金投资活期产品
//		logger.info("=======================uid:" + req.getInvestorOid());
//		CouponLogReq entity=new CouponLogReq();
//		entity.setUserOid(req.getInvestorOid());
//		entity.setType(CouponLogEntity.TYPE_REGISTER);
//		couponLogService.createEntity(entity);
////		TradeOrderReq tradeOrderReq = this.useRegisterTastecoupon(uid);
////		if(null != tradeOrderReq){
////			this.investorInvestTradeOrderExtService.expGoldInvest(tradeOrderReq);
////		}
//		return new BaseRep();
//	}
//	
//	/**
//	 * 登录
//	 * @param req
//	 * @return
//	 */
//	public String login(InvestorBaseAccountLoginReq req){
//		InvestorBaseAccountEntity account = this.findByPhone(req.getUserAcc());
//		if (StringUtil.isEmpty(account.getUserPwd()) || StringUtil.isEmpty(account.getSalt())) {
//			throw new AMPException("您未设置登录密码，建议使用快捷登录！");
//		}
//		InvestorBaseAccountRedisInfo accountRedis = this.saveAccountRedis(account.getOid(), req.getClientId());
//		
//		logger.info("用户：{}使用密码进行登录。", account.getPhoneNum());
//		// 获取锁定状态
//		this.getLockState(account, accountRedis, true);
//		
//		if (PwdUtil.checkPassword(req.getUserPwd(), account.getUserPwd(), account.getSalt())) {
//			// 更新用户登录错误次数，清零
//			accountRedis.setPwdErrorTimes(0);
//			this.updateAccountRedis(account.getOid(), accountRedis);
//			return account.getOid();
//		} else {
//			// 错误次数累计
//			int pwdErrorTimes = accountRedis.getPwdErrorTimes() + 1;
//			if(pwdErrorTimes == 5){
//				// 设置锁定时间
//				accountRedis.setLockTime(DateUtil.getSqlDate());
//			}
//			// 更新用户登录错误次数
//			accountRedis.setPwdErrorTimes(pwdErrorTimes);
//			this.updateAccountRedis(account.getOid(), accountRedis);
//			if(pwdErrorTimes == 5){
//				logger.info("用户：{}，密码输入错误：5次，时间：{}", account.getPhoneNum(), DateUtil.getSqlDate());
//				throw GHException.getException("密码连续输入错误超过五次，账号已被锁定24小时！");
//			}
//			logger.info("用户：{}，密码输入错误：{}次，时间：{}", account.getPhoneNum(), pwdErrorTimes, DateUtil.getSqlDate());
//			throw new GHException("登录名和密码不匹配，连续输错超过5次账号当天将会被锁定，剩余" + (5- pwdErrorTimes) + "次机会");
//		}
//	}
	
	/**
	 * 快速登录
	 * @param fu
	 * @return
	 */
	public String fastLogin(InvestorBaseAccountLoginReq req) {
		InvestorBaseAccountEntity account = this.findByPhone(req.getUserAcc());
		this.bfSMSUtils.checkVeriCode(req.getUserAcc(), BfSMSTypeEnum.smstypeEnum.login.toString(), req.getVericode());
		this.saveAccountRedis(account.getOid(), req.getClientId());
		return account.getOid();
	}
	
//	/**
//	 * 初始化登录信息 redis
//	 * @param accountOid
//	 * @param clientId 可为空
//	 * @return
//	 */
//	public InvestorBaseAccountRedisInfo saveAccountRedis(String investorOid, String clientId) {
//		InvestorBaseAccountRedisInfo accountRedis = InvestorBaseAccountRedisUtil.get(redis, investorOid);
//		
//		if (null == accountRedis) {
//			accountRedis = new InvestorBaseAccountRedisInfo();
//		}
//		if (!StringUtil.isEmpty(clientId)) {
//			accountRedis.setClientId(clientId);
//		}
//		accountRedis = InvestorBaseAccountRedisUtil.set(redis, investorOid, accountRedis);
//		return accountRedis;
//	}
	
//	/**
//	 * 更新登录信息 redis
//	 * @param investorOid
//	 * @param accountRedis
//	 * @return
//	 */
//	public InvestorBaseAccountRedisInfo updateAccountRedis(String investorOid, InvestorBaseAccountRedisInfo accountRedis) {
//		return InvestorBaseAccountRedisUtil.set(redis, investorOid, accountRedis);
//	}
	
	/**
	 * 判断用户的锁定状态
	 * @param userAcc
	 */
	public void checkLockState(String userAcc) {
		InvestorBaseAccountEntity account = this.findByPhone(userAcc);
		InvestorBaseAccountRedisInfo accountRedis = this.saveAccountRedis(account.getOid(), "");
		// 获取登录状态
		this.getLockState(account, accountRedis, false);
	}
	
//	/**
//	 * 获取当前用户锁定状态
//	 * @param account
//	 * @param accountRedis
//	 * @param isLogin
//	 */
//	public void getLockState(InvestorBaseAccountEntity account, InvestorBaseAccountRedisInfo accountRedis, boolean isLogin){
//		
//		if (InvestorBaseAccountEntity.BASEACCOUNT_status_forbidden.equals(account.getStatus())) {
//			throw new AMPException("此用户已被冻结!");
//		}
//		
//		if (null != accountRedis.getLockTime()) {
//			// 不是同一天，更新用户登录错误次数，清零
//			if (!DateUtil.same(DateUtil.getSqlDate(), accountRedis.getLockTime())){
//				accountRedis.setPwdErrorTimes(0);
//				accountRedis.setLockTime(DateUtil.getSqlDate());
//				InvestorBaseAccountRedisUtil.set(redis, account.getOid(), accountRedis);
//			}
//		}
//		
//		if (accountRedis.getPwdErrorTimes() > 4) {
//			if (isLogin) {
//				throw new AMPException("密码连续输入错误超过五次，账号已被锁定24小时！");
//			} else {
//				throw new AMPException("您的账号已被锁定，不能找回密码！！");
//			}
//		}
//	}
	
//	/**
//	 * 校验输入的原登录密码是否正确
//	 * @param req
//	 * @return
//	 */
//	public BaseRep checkLoginPassword(InvestorBaseAccountPasswordReq req) {
//		InvestorBaseAccountEntity account = this.findOne(req.getInvestorOid());
//		BaseRep rep = new BaseRep();
//		if (!PwdUtil.checkPassword(req.getUserPwd(), account.getUserPwd(), account.getSalt())) {
//			rep.setErrorCode(-1);
//			rep.setErrorMessage("原登录密码验证错误！");
//		}
//		return rep;
//	}
//	
//	/**
//	 * 设置/修改登录密码
//	 * @param req
//	 * @return
//	 */
//	public BaseRep editLoginPassword(InvestorBaseAccountPasswordReq req) {
//		InvestorBaseAccountEntity account = this.findOne(req.getInvestorOid());
//		BaseRep rep = new BaseRep();
//		if (PwdUtil.checkPassword(req.getUserPwd(), account.getPayPwd(), account.getPaySalt())) {
//			rep.setErrorCode(-1);
//			rep.setErrorMessage("登录密码和交易密码不能一致！");
//			return rep;
//		}
//		this.updatePasswordEntity(account, req);
//		return rep;
//	}
//	
//	/**
//	 * 忘记登录密码
//	 * @param req
//	 * @return
//	 */
//	public BaseRep forgetLoginPassword(InvestorBaseAccountPasswordReq req) {
//		InvestorBaseAccountEntity account = this.findByPhone(req.getUserAcc());
//		this.bfSMSUtils.checkVeriCode(req.getUserAcc(), BfSMSTypeEnum.smstypeEnum.fogetlogin.toString(), req.getVericode());
//		BaseRep rep = new BaseRep();
//		this.updatePasswordEntity(account, req);
//		return rep;
//	}
//	
//	public void updatePasswordEntity(InvestorBaseAccountEntity account, InvestorBaseAccountPasswordReq req) {
//		account.setSalt(Digests.genSalt());
//		account.setUserPwd(PwdUtil.encryptPassword(req.getUserPwd(), account.getSalt()));
//		this.updateEntity(account);
//	}
//	
//	/**
//	 * 校验支付密码是否正确
//	 * @param req
//	 * @return
//	 */
//	public BaseRep checkPayPwd(InvestorBaseAccountPayPwdReq req) {
//		InvestorBaseAccountEntity account = this.findOne(req.getInvestorOid());
//		BaseRep rep = new BaseRep();
//		if (!PwdUtil.checkPassword(req.getPayPwd(), account.getPayPwd(), account.getPaySalt())) {
//			rep.setErrorCode(-1);
//			rep.setErrorMessage("交易密码验证错误！");
//		}
//		return rep;
//	}
//	
//	/**
//	 * 设置/修改支付密码
//	 * @param req
//	 * @return
//	 */
//	public BaseRep editPayPwd(InvestorBaseAccountPayPwdReq req) {
//		InvestorBaseAccountEntity account = this.findOne(req.getInvestorOid());
//		BaseRep rep = new BaseRep();
//		if (PwdUtil.checkPassword(req.getPayPwd(), account.getUserPwd(), account.getSalt())) {
//			rep.setErrorCode(-1);
//			rep.setErrorMessage("交易密码和登录密码不能一致！");
//			return rep;
//		}
//		account.setPaySalt(Digests.genSalt());
//		account.setPayPwd(PwdUtil.encryptPassword(req.getPayPwd(), account.getPaySalt()));
//		this.updateEntity(account);
//		return rep;
//	}
//	
//	/**
//	 * 用户信息
//	 * @param investorOid
//	 * @return
//	 */
//	public BaseAccountInfoRep getAccountInfo(String investorOid) {
//		InvestorBaseAccountEntity account = this.findOne(investorOid);
//		
//		BaseAccountInfoRep rep = new BaseAccountInfoRep();
//		rep.setIslogin(true);
//		rep.setInvestorOid(account.getOid());
//		rep.setUserOid(account.getUserOid());
//		rep.setUserAcc(account.getPhoneNum());
//		rep.setUserPwd(StringUtil.isEmpty(account.getUserPwd()) ? false : true);
//		rep.setPaypwd(StringUtil.isEmpty(account.getPayPwd()) ? false : true);
//		rep.setSceneid(account.getUid());
//		rep.setStatus(account.getStatus()); // 冻结状态
//		rep.setSource(account.getSource()); // 注册来源
//		rep.setChannelid(account.getChannelid()); // 渠道来源
//		rep.setCreateTime(DateUtil.formatFullPattern(account.getCreateTime())); // 注册时间
//		
//		BankEntity bank = this.bankService.findByBaseAccount(account);
//		
//		if (null != bank) {
//			rep.setName(StringUtil.kickstarOnRealname(bank.getName())); // 姓名
//			rep.setFullName(bank.getName()); // 全姓名
//			rep.setIdNumb(StringUtil.kickstarOnIdNum(bank.getIdCard())); // 身份证号
//			rep.setFullIdNumb(bank.getIdCard()); // 全身份证号
//			rep.setBankName(bank.getBankName()); // 银行名称
//			rep.setBankCardNum(StringUtil.kickstarOnCardNum(bank.getDebitCard())); //　银行卡号
//			rep.setFullBankCardNum(bank.getDebitCard()); // 全银行卡号
//			rep.setBankPhone(StringUtil.kickstarOnPhoneNum(bank.getPhoneNo())); // 预留手机号
//		}		
//		return rep;
//	}
	
	/**
	 * 解锁登录锁定
	 * @param investorOid
	 */
	public void cancelLoginLock(String investorOid) {
		this.findOne(investorOid);
		InvestorBaseAccountRedisInfo accountRedis = this.saveAccountRedis(investorOid, "");
		accountRedis.setPwdErrorTimes(0);
		this.updateAccountRedis(investorOid, accountRedis);
	}
	/**
	 * 修改注册手机号
	 * 校验用户是否超过失败次数
	 */
	public void isLock(String investorOid, String type){
		InvestorBaseAccountEntity account = this.investorBaseAccountDao.findOne(investorOid);
		if (null != account) {
			String key = "";
			if(type!=null && ChangePhoneForm.PHONE_CAN_USE.equals(type)){
				key=SetRedisUtil.CHANGE_PHONENUM_USER_LOCK_Y+account.getOid();
			}else if(type!=null&&ChangePhoneForm.PHONE_CAN_NOT_USE.equals(type)){
				key=SetRedisUtil.CHANGE_PHONENUM_USER_LOCK_N+account.getOid();
			}else {
				throw new AMPException("参数有误！");
			}
			
			if(StrRedisUtil.exists(redis, key)){
					throw new AMPException("您已连续错误5次，请2小时后尝试或选择其他方式更换手机号");
			}
		} else {
			//error.define[80000]=账户不存在!(CODE:80000)
			throw new AMPException(80000);
		}
	}
	/**
	 * 修改用户手机号-原手机号可用-第一步手机号验证码验证
	 *  
	 */
	public void checkOldPhoneAccount(String userOid, String vericodes){
		InvestorBaseAccountEntity account = this.investorBaseAccountDao.findOne(userOid);
		if (null != account) {
			if(!SetRedisUtil.exists(redis, SetRedisUtil.CHANGE_PHONENUM_USER_LOCK_Y+account.getOid())){
				String vericode = StrRedisUtil.get(redis, StrRedisUtil.VERI_CODE_REDIS_KEY + account.getPhoneNum() + "_" + BfSMSTypeEnum.smstypeEnum.CheckOldPhone.toString());
				boolean result = vericodes.equals(vericode);
				if(!result){
					Long countSet=SetRedisUtil.IncrBy(redis, SetRedisUtil.CHANGE_PHONENUM_FAIL_COUNT_Y+account.getOid(), Long.valueOf(1));
					if(countSet<5L){
						throw new AMPException("错误"+countSet+"次，连续5次错误需2小时后尝试");
					}else{
						SetRedisUtil.SetNxOnTimes(redis, SetRedisUtil.CHANGE_PHONENUM_USER_LOCK_Y+account.getOid(), account.getOid());
						SetRedisUtil.del(redis, SetRedisUtil.CHANGE_PHONENUM_FAIL_COUNT_Y+account.getOid());
						throw new AMPException(15005);
					}
				}else{
					SetRedisUtil.del(redis, SetRedisUtil.CHANGE_PHONENUM_FAIL_COUNT_Y+account.getOid());
					SetRedisUtil.hSet(redis, SetRedisUtil.CHANGE_PHONENUM_USER_INFO+account.getOid(), SetRedisUtil.CHANGE_PHONENUM_REALNAME,account.getRealName() + "");
					SetRedisUtil.hSet(redis, SetRedisUtil.CHANGE_PHONENUM_USER_INFO+account.getOid(), SetRedisUtil.CHANGE_PHONENUM_IDCARENO,account.getIdNum() + "");
					SetRedisUtil.expire(redis, SetRedisUtil.CHANGE_PHONENUM_USER_INFO+account.getOid(), 5);
				}
			}else{
				// error.define[15005]=您已连续错误5次，请2小时后尝试(CODE:15005)
				throw new AMPException(15005);
			}
		} else {
			//error.define[80000]=账户不存在!(CODE:80000)
			throw new AMPException(80000);
		}
	}
	
	/**
	 * 修改用户手机号-原手机号可用-往新手机号发送短信验证码-检查新手机号是否可用
	 */
	public void checkNewPhoneAccount(String phoneNum){
		InvestorBaseAccountEntity account = this.investorBaseAccountDao.findByPhoneNum(phoneNum);
		if (null != account) {
			// error.define[80021]=该手机号已注册，请重新输入!(CODE:80021)
			throw new AMPException(80021);
		}
	}
	/**
	 * 修改用户手机号
	 *  
	 */
	@Transactional
	public void changePhone(String phoneNum,String vericodes,String userOid,String type){
		if(type == null || (!ChangePhoneForm.PHONE_CAN_USE.equals(type) && !ChangePhoneForm.PHONE_CAN_NOT_USE.equals(type))){
			throw new AMPException("参数错误！");
		}
		InvestorBaseAccountEntity account = this.investorBaseAccountDao.findByPhoneNum(phoneNum);
		String lockKey = ChangePhoneForm.PHONE_CAN_USE.equals(type) ? SetRedisUtil.CHANGE_PHONENUM_USER_LOCK_Y+userOid : SetRedisUtil.CHANGE_PHONENUM_USER_LOCK_N+userOid;
		String countKey = ChangePhoneForm.PHONE_CAN_USE.equals(type) ?  SetRedisUtil.CHANGE_PHONENUM_FAIL_COUNT_Y + userOid :  SetRedisUtil.CHANGE_PHONENUM_FAIL_COUNT_N + userOid;
		if (null != account) {
			// error.define[80021]=改手机号已注册，请重新输入!(CODE:80021)
			throw new AMPException(80021);
		} else {
			if(!SetRedisUtil.exists(redis, lockKey)){
				String vericode = StrRedisUtil.get(redis, StrRedisUtil.VERI_CODE_REDIS_KEY + phoneNum + "_" + BfSMSTypeEnum.smstypeEnum.changeNewPhone.toString());
				boolean result = vericodes.equals(vericode);
				if(!result){
					Long countSet=SetRedisUtil.IncrBy(redis, countKey, Long.valueOf(1));
					if(countSet<5L){
						throw new AMPException("错误"+countSet+"次，连续5次错误需2小时后尝试");
					}else{
						SetRedisUtil.SetNxOnTimes(redis, lockKey, userOid);
						SetRedisUtil.del(redis, countKey);
						throw new AMPException(15005);
					}
				}else{
					if(SetRedisUtil.exists(redis, SetRedisUtil.CHANGE_PHONENUM_USER_INFO+userOid)){
						if(!SetRedisUtil.hExists(redis, SetRedisUtil.CHANGE_PHONENUM_USER_INFO+userOid, SetRedisUtil.CHANGE_PHONENUM_REALNAME)||
								!SetRedisUtil.hExists(redis, SetRedisUtil.CHANGE_PHONENUM_USER_INFO+userOid, SetRedisUtil.CHANGE_PHONENUM_IDCARENO)){
							 throw new AMPException("您的信息好像过期啦，请先完成上一步操作！");
						 }else{
							SetRedisUtil.del(redis, countKey);
							SetRedisUtil.del(redis, SetRedisUtil.CHANGE_PHONENUM_USER_INFO+userOid);
							//修改注册手机号
							InvestorBaseAccountEntity accountEntity = investorBaseAccountDao.findOne(userOid);
							if (null == accountEntity) {
								throw new AMPException("账户不存在！");
							}
							String oldPhone = accountEntity.getPhoneNum();
							
							// 保存修改日志
							ChangePhoneEntity changePhoneEntity = new ChangePhoneEntity();
							changePhoneEntity.setOldPhone(oldPhone);
							changePhoneEntity.setNewPhone(phoneNum);
							changePhoneEntity.setInvestorOid(userOid);
							changePhoneEntity.setStatus(0); // initial status 0 failure
							changePhoneEntity = changePhoneService.save(changePhoneEntity);
							logger.info("【用户修改手机号】日志：{}", changePhoneEntity);
							
							int updateResult = investorBaseAccountDao.updateBaseAccountPhoneNum(userOid, oldPhone, phoneNum);
							if (updateResult < 1) {
								throw new AMPException("系统错误，请稍后重试！");
							}
							//更新中间表手机号
							updateResult = investorOpenCycleDao.updateOpencycleRelationPhonenum(userOid, oldPhone, phoneNum);
//							if (updateResult < 1) {
//								throw new AMPException("系统错误，请稍后重试！");
//							}

							ChangePhoneMessageEntity messageEntity = new ChangePhoneMessageEntity();
							messageEntity.setDataId(changePhoneEntity.getOid());
							messageEntity.setInvestorOid(userOid);
							messageEntity.setNewPhone(phoneNum);
							messageEntity.setOldPhone(oldPhone);
							
							Map<String, Object> ucResult = userCenterSdk.changePhone(messageEntity);
							if (CardVoStatus.SUCCESS.equals((Object) ucResult.get("status"))) {
								updateResult = 1;
							}else {
								updateResult = 0;
								throw new AMPException("系统错误，请稍后重试！");
							}
							
							changePhoneService.updateStatus(changePhoneEntity.getOid(), updateResult);
							
							// 发送消息 其他服务器订阅处理
							messageSendUtil.sendTopicMessage(messageSendUtil.getChangePhoneTopic(), MessageConstant.MESSAGE_QUEUE_DEAL_CHANGEPHONE_TAG, messageEntity);
						 }
					 }else{
						 throw new AMPException("您的信息好像过期啦，请先完成上一步操作！");
					 }
				}
			}else{
				throw new AMPException(15005);
			}
		}
	}
	/**
	 * 修改用户手机号-原手机号不可用-第一步交易密码验证
	 *  
	 */
	public void checkPayPwd(String payPassWord,String userOid){
		InvestorBaseAccountEntity account = this.investorBaseAccountDao.findOne(userOid);
		if (null == account) {
			//error.define[80000]=账户不存在!(CODE:80000)
			throw new AMPException(80000);
		} else {
			if(!SetRedisUtil.exists(redis, SetRedisUtil.CHANGE_PHONENUM_USER_LOCK_N+account.getOid())){
				UcPayPwdReq req=new UcPayPwdReq();
				req.setInvestorOid(userOid);
				req.setPayPwd(payPassWord);
				Response res = null;
				try{
					res=userCenterSdk.checkPayPwd(req);
				}catch(Exception e){
					logger.error("用户{}修改密码错误，错误信息为：{}",account.getOid(),e);
					res = new Response();
					res.setErrorCode(-1);
				}
				
				if(res.getErrorCode()!=0){
					Long countSet=SetRedisUtil.IncrBy(redis, SetRedisUtil.CHANGE_PHONENUM_FAIL_COUNT_N+account.getOid(), Long.valueOf(1));
					if(countSet<5L){
						throw new AMPException("错误"+countSet+"次，连续5次错误需2小时后尝试");
					}else{
						SetRedisUtil.SetNxOnTimes(redis, SetRedisUtil.CHANGE_PHONENUM_USER_LOCK_N+account.getOid(), account.getOid());
						SetRedisUtil.del(redis, SetRedisUtil.CHANGE_PHONENUM_FAIL_COUNT_N+account.getOid());
						throw new AMPException(15005);
					}
				}else{
					SetRedisUtil.del(redis, SetRedisUtil.CHANGE_PHONENUM_FAIL_COUNT_N+account.getOid());
					SetRedisUtil.hSet(redis, SetRedisUtil.CHANGE_PHONENUM_USER_INFO+account.getOid(), SetRedisUtil.CHANGE_PHONENUM_PAYPASSWOED, payPassWord);
					SetRedisUtil.expire(redis, SetRedisUtil.CHANGE_PHONENUM_USER_INFO+account.getOid(), 5);
				}
			}else{
				throw new AMPException(15005);
			}
		}
	}
	/**
	 * 原手机号不可用-第二步-验证用户实名信息
	 *  
	 */
	public void checkUserInfo(String RealName,String IdCardNo,String userOid){
		InvestorBaseAccountEntity account = this.investorBaseAccountDao.findOne(userOid);
		if (null == account) {
			//error.define[80000]=账户不存在!(CODE:80000)
			throw new AMPException(80000);
		} else {
			if (StringUtil.isEmpty(account.getRealName()) || StringUtil.isEmpty(account.getIdNum())) {
				throw new AMPException("请完成绑定银行卡后重试");
			}
			if(!SetRedisUtil.exists(redis, SetRedisUtil.CHANGE_PHONENUM_USER_LOCK_N+account.getOid())){
				if(!account.getRealName().equals(RealName)||!account.getIdNum().equalsIgnoreCase(IdCardNo)){
					Long countSet=SetRedisUtil.IncrBy(redis, SetRedisUtil.CHANGE_PHONENUM_FAIL_COUNT_N+account.getOid(), Long.valueOf(1));
					if(countSet<5L){
						throw new AMPException("姓名或身份证号错误"+countSet+"次，连续5次错误需2小时后尝试");
					}else{
						SetRedisUtil.SetNxOnTimes(redis, SetRedisUtil.CHANGE_PHONENUM_USER_LOCK_N + account.getOid(), account.getOid());
						SetRedisUtil.del(redis, SetRedisUtil.CHANGE_PHONENUM_FAIL_COUNT_N+account.getOid());
						throw new AMPException(15005);
					}
				}else{
					if(SetRedisUtil.exists(redis, SetRedisUtil.CHANGE_PHONENUM_USER_INFO+account.getOid())){
						if(!SetRedisUtil.hExists(redis, SetRedisUtil.CHANGE_PHONENUM_USER_INFO+account.getOid(), SetRedisUtil.CHANGE_PHONENUM_PAYPASSWOED)){
							 throw new AMPException("请先完成上一步操作！");
						 }else{
							 SetRedisUtil.del(redis, SetRedisUtil.CHANGE_PHONENUM_FAIL_COUNT_N+account.getOid());
							 SetRedisUtil.hSet(redis, SetRedisUtil.CHANGE_PHONENUM_USER_INFO+account.getOid(), SetRedisUtil.CHANGE_PHONENUM_REALNAME, RealName);
							 SetRedisUtil.hSet(redis, SetRedisUtil.CHANGE_PHONENUM_USER_INFO+account.getOid(), SetRedisUtil.CHANGE_PHONENUM_IDCARENO, IdCardNo);
						 }
					 }else{
						 throw new AMPException("请先完成上一步操作！");
					 }
				}
			}else{
				throw new AMPException(15005);
			}
		}
	}
}
