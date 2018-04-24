package com.guohuai.mmp.tulip.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONObject;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.coupon.RedPacketsUseRep;
import com.guohuai.mmp.investor.coupon.RedPacketsUseReq;
import com.guohuai.mmp.platform.tulip.TulipConstants;
import com.guohuai.mmp.platform.tulip.log.TuipLogReq;
import com.guohuai.mmp.platform.tulip.log.TulipLogEntity;
import com.guohuai.mmp.platform.tulip.log.TulipLogService;
import com.guohuai.tuip.api.TulipSdk;
import com.guohuai.tuip.api.objs.BaseObj;
import com.guohuai.tuip.api.objs.admin.CheckCouponRep;
import com.guohuai.tuip.api.objs.admin.CheckCouponReq;
import com.guohuai.tuip.api.objs.admin.CouponDetailRep;
import com.guohuai.tuip.api.objs.admin.CouponInterestRep;
import com.guohuai.tuip.api.objs.admin.CouponInterestReq;
import com.guohuai.tuip.api.objs.admin.EventRep;
import com.guohuai.tuip.api.objs.admin.InvestmentReq;
import com.guohuai.tuip.api.objs.admin.IssuedCouponReq;
import com.guohuai.tuip.api.objs.admin.MyCouponRep;
import com.guohuai.tuip.api.objs.admin.MyCouponReq;
import com.guohuai.tuip.api.objs.admin.OrderReq;
import com.guohuai.tuip.api.objs.admin.RefereeRep;
import com.guohuai.tuip.api.objs.admin.TulipListObj;
import com.guohuai.tuip.api.objs.admin.TulipObj;
import com.guohuai.tuip.api.objs.admin.UserReq;
import com.guohuai.tuip.api.objs.admin.VerificationCouponReq;

/**
 * 封装推广平台SDK接口(方便配置是否引入推广平台)
 * 
 * @author wanglei
 *
 */
@Service
public class TulipSDKService {

	Logger logger = LoggerFactory.getLogger(TulipSDKService.class);

	@Autowired
	private TulipSdk tulipSdk;
	@Autowired
	private TulipLogService tulipLogService;

	/** 是否启用推广平台接口(1启用,其他不启用) */
	@Value("${tulip.mmp.sdkopen:0}")
	private String tulipsdkopen;

	/** 获取个人券码列表 */
	public TulipListObj<MyCouponRep> getMyCouponList(MyCouponReq param) {
		TulipListObj<MyCouponRep> rep = new TulipListObj<MyCouponRep>();
		// 不开启时直接返回0条记录
		this.assertSdkEnable();
		try {
			rep = this.getTulipSdk().getMyCouponList(param);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			setCouponExceptionRep(rep);
		}

		return rep;
	}

	/** 获取单个卡券信息 */
	public CouponDetailRep getCouponDetail(MyCouponReq param) {
		CouponDetailRep rep = new CouponDetailRep();
		this.assertSdkEnable();

		try {
			rep = this.getTulipSdk().getCouponDetail(param);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			setCouponExceptionRep(rep);
		}
		return rep;
	}

	/** 可用于购买某产品的卡券 */
	public TulipListObj<MyCouponRep> getCouponList(MyCouponReq param) {
		TulipListObj<MyCouponRep> rep = new TulipListObj<MyCouponRep>();

		this.assertSdkEnable();

		try {
			rep = this.getTulipSdk().getCouponList(param);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			setCouponExceptionRep(rep);

		}

		return rep;
	}

	public void setCouponExceptionRep(BaseObj rep) {
		rep.setErrorCode(BaseRep.ERROR_CODE);
		rep.setErrorMessage("卡券系统 异常");
	}

	public void setCouponUnableRep(BaseObj rep) {
		rep.setErrorCode(BaseRep.ERROR_CODE);
		rep.setErrorMessage("卡券系统 暂停使用");
	}

	/** 锁定卡券 */
	public CheckCouponRep checkCoupon(CheckCouponReq param, TulipLogEntity.TULIP_TYPE type) {
		CheckCouponRep rep = new CheckCouponRep();

		try {
			rep = this.getTulipSdk().checkCoupon(param);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			setCouponExceptionRep(rep);
		}

		TuipLogReq lreq = new TuipLogReq();
		lreq.setErrorCode(rep.getErrorCode());
		lreq.setErrorMessage(rep.getErrorMessage());
		lreq.setInterfaceName(type.getInterfaceName());
		lreq.setInterfaceCode(type.getInterfaceCode());
		lreq.setSendObj(JSONObject.toJSONString(param));
		lreq.setSendedTimes(1);
		this.tulipLogService.createTulipLogEntity(lreq);

		return rep;
	}

	/** 提现 事件 */
	public TulipObj onCash(OrderReq param, TulipLogEntity.TULIP_TYPE type) {

		TulipObj rep = new TulipObj();

		try {
			rep = this.getTulipSdk().onCash(param);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			setCouponExceptionRep(rep);
		}

		TuipLogReq lreq = new TuipLogReq();
		lreq.setInterfaceName(type.getInterfaceName());
		lreq.setInterfaceCode(type.getInterfaceCode());
		lreq.setErrorCode(rep.getErrorCode());
		lreq.setErrorMessage(rep.getErrorMessage());
		lreq.setSendObj(JSONObject.toJSONString(param));
		lreq.setSendedTimes(1);
		this.tulipLogService.createTulipLogEntity(lreq);

		return rep;
	}

	/** 实名认证事件 */
	public BaseRep onSetRealName(UserReq param) {

		BaseRep baseRep = new BaseRep();

		TulipObj rep = new TulipObj();
		if (!this.isSdkEnable()) {
			this.setCouponUnableRep(rep);
		} else {
			try {
				rep = this.getTulipSdk().onSetRealName(param);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				setCouponExceptionRep(rep);
			}
		}

		baseRep.setErrorCode(rep.getErrorCode());
		baseRep.setErrorMessage(rep.getErrorMessage());

		TuipLogReq lreq = new TuipLogReq();
		lreq.setErrorCode(baseRep.getErrorCode());
		lreq.setErrorMessage(baseRep.getErrorMessage());
		lreq.setInterfaceName(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_SETREALNAME.getInterfaceName());
		lreq.setInterfaceCode(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_SETREALNAME.getInterfaceCode());
		lreq.setSendObj(JSONObject.toJSONString(param));
		lreq.setSendedTimes(1);
		this.tulipLogService.createTulipLogEntity(lreq);
		return baseRep;
	}

	/** 注册事件 */
	public BaseRep onRegister(UserReq param) {
		BaseRep baseRep = new BaseRep();

		TulipObj rep = new TulipObj();
		if (!this.isSdkEnable()) {
			this.setCouponUnableRep(rep);
		} else {
			try {
				rep = this.getTulipSdk().onRegister(param);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				setCouponExceptionRep(rep);
			}
		}

		baseRep.setErrorCode(rep.getErrorCode());
		baseRep.setErrorMessage(rep.getErrorMessage());

		TuipLogReq lreq = new TuipLogReq();
		lreq.setErrorCode(baseRep.getErrorCode());
		lreq.setErrorMessage(baseRep.getErrorMessage());
		lreq.setInterfaceName(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_REGISTER.getInterfaceName());
		lreq.setInterfaceCode(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_REGISTER.getInterfaceCode());
		lreq.setSendObj(JSONObject.toJSONString(param));
		lreq.setSendedTimes(1);
		this.tulipLogService.createTulipLogEntity(lreq);

		return baseRep;
	}

	/** 推荐人事件 */
	public BaseRep onReferee(UserReq param) {
		BaseRep baseRep = new BaseRep();

		TulipObj rep = new TulipObj();
		if (!this.isSdkEnable()) {
			this.setCouponUnableRep(rep);
		} else {
			try {
				rep = this.getTulipSdk().onReferee(param);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				setCouponExceptionRep(rep);
			}
		}

		baseRep.setErrorCode(rep.getErrorCode());
		baseRep.setErrorMessage(rep.getErrorMessage());

		TuipLogReq lreq = new TuipLogReq();
		lreq.setErrorCode(baseRep.getErrorCode());
		lreq.setErrorMessage(baseRep.getErrorMessage());
		lreq.setInterfaceCode(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_REFEREE.getInterfaceCode());
		lreq.setInterfaceName(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_REFEREE.getInterfaceName());
		lreq.setSendObj(JSONObject.toJSONString(param));
		lreq.setSendedTimes(1);
		this.tulipLogService.createTulipLogEntity(lreq);

		return baseRep;
	}

	/** 退款事件 */
	public TulipObj onRefund(OrderReq param) {
		TulipObj rep = new TulipObj();
		if (!this.isSdkEnable()) {
			return rep;
		}
		try {
			rep = this.getTulipSdk().onRefund(param);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			setCouponExceptionRep(rep);

		}
		return rep;

	}

	/** 核销卡券 */
	public TulipObj verificationCoupon(TulipListObj<VerificationCouponReq> param) {
		TulipObj rep = new TulipObj();
		if (!this.isSdkEnable()) {
			return rep;
		}
		try {
			rep = this.getTulipSdk().verificationCoupon(param);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			setCouponExceptionRep(rep);

		}
		return rep;
	}

	/** 申购事件 */
	public BaseRep onInvestment(InvestmentReq param, TulipLogEntity.TULIP_TYPE type) {
		BaseRep baseRep = new BaseRep();

		TulipObj rep = new TulipObj();
		if (!this.isSdkEnable()) {
			setCouponExceptionRep(rep);
		} else {
			try {
				rep = this.getTulipSdk().onInvestment(param);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				setCouponExceptionRep(rep);
			}
		}

		baseRep.setErrorCode(rep.getErrorCode());
		baseRep.setErrorMessage(rep.getErrorMessage());

		TuipLogReq lreq = new TuipLogReq();
		lreq.setErrorCode(baseRep.getErrorCode());
		lreq.setErrorMessage(baseRep.getErrorMessage());
		lreq.setInterfaceName(type.getInterfaceName());
		lreq.setInterfaceCode(type.getInterfaceCode());
		lreq.setSendObj(JSONObject.toJSONString(param));
		lreq.setSendedTimes(1);

		this.tulipLogService.createTulipLogEntity(lreq);

		return baseRep;
	}

	/** 赎回事件 */
	public BaseRep onRedeem(OrderReq param) {
		BaseRep baseRep = new BaseRep();

		TulipObj rep = new TulipObj();
		if (!this.isSdkEnable()) {
			setCouponExceptionRep(rep);
		}
		try {
			rep = this.getTulipSdk().onRedeem(param);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			setCouponExceptionRep(rep);
		}

		baseRep.setErrorCode(rep.getErrorCode());
		baseRep.setErrorMessage(rep.getErrorMessage());

		TuipLogReq lreq = new TuipLogReq();
		lreq.setErrorCode(baseRep.getErrorCode());
		lreq.setErrorMessage(baseRep.getErrorMessage());
		lreq.setInterfaceName(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_REDEEM.getInterfaceName());
		lreq.setInterfaceCode(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_REDEEM.getInterfaceCode());
		lreq.setSendObj(JSONObject.toJSONString(param));
		lreq.setSendedTimes(1);

		this.tulipLogService.createTulipLogEntity(lreq);

		return baseRep;
	}

	/** 到期兑付事件 */
	public BaseRep onBearer(OrderReq param) {
		BaseRep baseRep = new BaseRep();

		TulipObj rep = new TulipObj();
		if (!this.isSdkEnable()) {
			setCouponExceptionRep(rep);
		} else {
			try {
				rep = this.getTulipSdk().onBearer(param);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				setCouponExceptionRep(rep);
			}
		}

		baseRep.setErrorCode(rep.getErrorCode());
		baseRep.setErrorMessage(rep.getErrorMessage());

		TuipLogReq lreq = new TuipLogReq();
		lreq.setErrorCode(baseRep.getErrorCode());
		lreq.setErrorMessage(baseRep.getErrorMessage());
		lreq.setInterfaceCode(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_BEARER.getInterfaceCode());
		lreq.setInterfaceName(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_BEARER.getInterfaceName());
		lreq.setSendObj(JSONObject.toJSONString(param));
		lreq.setSendedTimes(1);

		this.tulipLogService.createTulipLogEntity(lreq);
		return baseRep;

	}

	/** 加息券加息金额计算 */
	public CouponInterestRep couponInterest(CouponInterestReq param) {
		CouponInterestRep rep = new CouponInterestRep();
		if (!this.isSdkEnable()) {
			return rep;
		}
		try {
			rep = this.getTulipSdk().couponInterest(param);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			setCouponExceptionRep(rep);

		}
		return rep;
	}

	 public EventRep getFriendEventInfo() {
	        EventRep rep = new EventRep();
	        this.assertSdkEnable();
	        try {
	            rep = this.getTulipSdk().getFriendEventInfo();
	        } catch (Exception e) {
	            logger.error(e.getMessage(), e);
	        }
	        return rep;
	    }
	    public EventRep getRegisterEventInfo() {
	        EventRep rep = new EventRep();
	        this.assertSdkEnable();
	        try {
	            rep = this.getTulipSdk().getRegisterEventInfo();
	        } catch (Exception e) {
	            logger.error(e.getMessage(), e);
	        }
	        return rep;
	    }
	    public EventRep getRefereeEventInfo() {
	        EventRep rep = new EventRep();
	        this.assertSdkEnable();
	        try {
	            rep = this.getTulipSdk().getRegisterEventInfo();
	        } catch (Exception e) {
	            logger.error(e.getMessage(), e);
	        }
	        return rep;
	    }
	    public RefereeRep getFrendIdByUid(String uid) {
	        RefereeRep rep = new RefereeRep();
	        this.assertSdkEnable();
	        try {
	            rep = this.getTulipSdk().getFrendIdByUid(uid);
	        } catch (Exception e) {
	            logger.error(e.getMessage(), e);
	        }
	        return rep;
	    }

	/** 下发卡券 */
	public TulipObj issuedCoupon(IssuedCouponReq param) {
		TulipObj rep = new TulipObj();
		if (!this.isSdkEnable()) {
			return rep;
		}
		try {
			rep = this.getTulipSdk().issuedCoupon(param);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			setCouponExceptionRep(rep);

		}

		return rep;
	}

	public RedPacketsUseRep useRp(RedPacketsUseReq sReq) {

		RedPacketsUseRep sRep = new RedPacketsUseRep();

		MyCouponRep rep = new MyCouponRep();

		MyCouponReq req = new MyCouponReq();
		req.setCouponId(sReq.getCouponId());
		req.setType(sReq.getCouponType());
		try {
			rep = this.getTulipSdk().userRedPackets(req);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			setCouponExceptionRep(rep);
		}
		sRep.setCouponAmount(rep.getAmount());
		sRep.setErrorCode(rep.getErrorCode());
		sRep.setErrorMessage(rep.getErrorMessage());

		TuipLogReq lreq = new TuipLogReq();
		lreq.setErrorCode(sRep.getErrorCode());
		lreq.setErrorMessage(sRep.getErrorMessage());
		lreq.setInterfaceName(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_USE_RP.getInterfaceName());
		lreq.setInterfaceCode(TulipLogEntity.TULIP_TYPE.TULIP_TYPE_USE_RP.getInterfaceCode());
		lreq.setSendObj(JSONObject.toJSONString(req));
		lreq.setSendedTimes(1);

		this.tulipLogService.createTulipLogEntity(lreq);

		return sRep;
	}

	public boolean isUseCoupon(String couponId) {
		return StringUtil.isEmpty(couponId) ? false : true;
	}

	private TulipSdk getTulipSdk() {
		return this.tulipSdk;
	}

	/** 是否启用推广平台(true启用，false不启用) */
	public boolean isSdkEnable() {
		return (TulipConstants.OPENFLAG_TULIPSDK.equals(getSdkOpenConfig())) ? true : false;
	}

	public void assertSdkEnable() {
		if (!isSdkEnable()) {
			throw new AMPException("暂未开通");
		}
	}

	public String getSdkOpenConfig() {
		return this.tulipsdkopen;
	}

	

}
