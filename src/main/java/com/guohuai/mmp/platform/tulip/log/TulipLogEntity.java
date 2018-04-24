package com.guohuai.mmp.platform.tulip.log;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.alibaba.fastjson.TypeReference;
import com.guohuai.component.persist.UUID;
import com.guohuai.tuip.api.objs.admin.InvestmentReq;
import com.guohuai.tuip.api.objs.admin.IssuedCouponReq;
import com.guohuai.tuip.api.objs.admin.OrderReq;
import com.guohuai.tuip.api.objs.admin.TulipListObj;
import com.guohuai.tuip.api.objs.admin.UserReq;
import com.guohuai.tuip.api.objs.admin.VerificationCouponReq;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 推广平台-请求发送日志
 * 
 * @author wanglei
 *
 */
@Entity
@Table(name = "T_MONEY_TULIP_LOG")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class TulipLogEntity extends UUID {

	private static final long serialVersionUID = -5183984182066265200L;

	public static enum TULIP_TYPE {
		
		//红包相关
		TULIP_TYPE_VALID_RP("valid_rp", "红包校验事件", 1),
		TULIP_TYPE_LOCK_RP("lock_rp", "红包锁定事件", 1),
		TULIP_TYPE_USE_RP("ues_rp", "红包使用事件", 1),
		TULIP_TYPE_CASH_FAIL_RP("cash_fail_rp", "红包提现失败事件", 10),
		TULIP_TYPE_CASH_OK_RP("cash_ok_rp", "红包提现成功事件", 10),
		
		TULIP_TYPE_CHECKCOUPON("checkCoupon", "锁定卡券", 1,"com.guohuai.tuip.api.objs.admin.CheckCouponReq"),
		TULIP_TYPE_CHECKREDPACK("validMyCoupon", "锁定新版卡券", 1,"com.guohuai.basic.cardvo.req.cardreq.ValidCardReq"),
		TULIP_TYPE_INVESTMENT("onInvestment", "申购事件", 10, "com.guohuai.tuip.api.objs.admin.InvestmentReq"),
		
		TULIP_TYPE_GET_MYCOUPON_LIST("getMyCouponList", "查询某人的卡券列表", 10),
		TULIP_TYPE_GET_PROCOUPON_LIST("getProCouponList", "可购买某产品的卡券列表", 10),
		TULIP_TYPE_GET_COUPON_DETAIL("getCouponDetail", "查询卡券详情", 10),
		TULIP_TYPE_COUPON_INTEREST("couponInterest", "计算定期产品加息券加息金额", 10),
		
		TULIP_TYPE_REDEEM("onRedeem", "赎回事件", 10),
		TULIP_TYPE_BEARER("onBearer", "到期兑付事件", 10),
		
		TULIP_TYPE_REGISTER("onRegister", "注册事件", 10 , "com.guohuai.tuip.api.objs.admin.UserReq"),
		
		TULIP_TYPE_REFEREE("onReferee", "推荐人事件",10 , "com.guohuai.tuip.api.objs.admin.UserReq"),
		
		TULIP_TYPE_SETREALNAME("onSetRealName", "实名认证事件 ", 10 ,"com.guohuai.tuip.api.objs.admin.UserReq"),
		
		TULIP_TYPE_CASH("onCash", "用户提现到银行卡事件", 10),
		
		TULIP_TYPE_REFUND("onRefund", "退款事件", 10),
		TULIP_TYPE_VERIFICATION("verification", "卡券核销事件", 10),
		TULIP_TYPE_ISUEDCOUPON("isuedCoupon", "下发卡券", 10);
		
		
		String interfaceCode;
		String interfaceName;
		int limitSendTimes;
		String ifaceReq;
		private TULIP_TYPE(String interfaceCode, String interfaceName, int limitSendTimes,String ifaceReq) {
			this.interfaceCode = interfaceCode;
			this.interfaceName = interfaceName;
			this.limitSendTimes = limitSendTimes;
			this.ifaceReq = ifaceReq;
		}
		private TULIP_TYPE(String interfaceCode, String interfaceName, int limitSendTimes) {
			this.interfaceCode = interfaceCode;
			this.interfaceName = interfaceName;
			this.limitSendTimes = limitSendTimes;
		}
		
		public String getIfaceReq() {
			return ifaceReq;
		}
		public String getInterfaceCode() {
			return interfaceCode;
		}
		
		public int getLimitSendTimes() {
			return limitSendTimes;
		}
		
		public String getInterfaceName() {
			return interfaceName;
		}
	}
	
	public static int getTimes(String interfaceCode) {
		for (TULIP_TYPE tmp : TULIP_TYPE.values()) {
			if (tmp.getInterfaceCode().equals(interfaceCode)) {
				return tmp.getLimitSendTimes();
			}
		}
		return 111;
	}
	public static String getName(String interfaceCode) {
		for (TULIP_TYPE tmp : TULIP_TYPE.values()) {
			if (tmp.getInterfaceCode().equals(interfaceCode)) {
				return tmp.getInterfaceName();
			}
		}
		return "exeption";
	}
	
	
	/** ==============json转推广平台Object对象的类型定义========START=========== */
	/** 注册、实名认证 */
	public static final TypeReference<UserReq> typeReference_UserReq = new TypeReference<UserReq>() {
	};
	/** 投资 */
	public static final TypeReference<InvestmentReq> typeReference_InvestmentReq = new TypeReference<InvestmentReq>() {
	};
	/** 赎回、到期兑付、 提现、退款、 */
	public static final TypeReference<OrderReq> typeReference_OrderReq = new TypeReference<OrderReq>() {
	};
	/** 下发卡券 */
	public static final TypeReference<IssuedCouponReq> typeReference_IssuedCouponReq = new TypeReference<IssuedCouponReq>() {
	};
	/** 卡券核销 */
	public static final TypeReference<TulipListObj<VerificationCouponReq>> typeReference_VerificationCouponReq 
		= new TypeReference<TulipListObj<VerificationCouponReq>>() {};


	/** 接口代码 */
	private String interfaceCode;

	/** 接口中文名 */
	private String interfaceName;

	/** 接口返回码 */
	private Integer errorCode;

	/** 接口错误消息 */
	private String errorMessage;

	/** 已发送次数 */
	private Integer sendedTimes;
	
	/**
	 * 最多发送次数
	 */
	private Integer limitSendTimes;
	
	/**
	 * 下次调用时间
	 */
	private Timestamp nextNotifyTime;

	/** 发送消息内容 */
	private String sendObj;

	private Timestamp createTime;

	private Timestamp updateTime;
}
