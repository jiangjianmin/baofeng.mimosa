package com.guohuai.mmp.platform.tulip;

/** 推广平台一些常量定义 */
public class TulipConstants {

	/** 推广平台卡券状态-未使用 */
	public static final String STATUS_COUPON_NOTUSED = "notUsed";
	/** 推广平台卡券状态-已使用 */
	public static final String STATUS_COUPON_USED = "used";
	/** 推广平台卡券状态-已过期 */
	public static final String STATUS_COUPON_EXPIRED = "expired";

	/** 推广平台投资事件状态-投资成功 */
	public static final String STATUS_INVEST_SUCC = "success";
	/** 推广平台投资事件状态-投资失败 */
	public static final String STATUS_INVEST_FAIL = "fail";

	/** 推广平台提现事件状态-提现成功 */
	public static final String STATUS_ONCASH_SUCC = "success";
	/** 推广平台提现事件状态-提现失败 */
	public static final String STATUS_ONCASH_FAIL = "fail";

	/** 空字符串 */
	public static final String BLANK = "";

	/** 推广平台开启的值 */
	public static final String OPENFLAG_TULIPSDK = "1";

	/** 左方括号 */
	public static final String OPEN_BRACKET = "[";
	/** 右方括号 */
	public static final String CLOSE_BRACKET = "]";

	/** 推广平台返回错误 */
	public static final int ERRORCODE_MIMOSA_110001 = 110001;
	/** 访问推广平台异常 */
	public static final int ERRORCODE_MIMOSA_110002 = 110002;
	/** 访问推广平台成功 */
	public static final int ERRORCODE_MIMOSA_0 = 0;
	
	public static final int ERRORCODE_TULIP_SUCC = 0;
	public static final int ERRORCODE_TULIP_FAIL = -1;

	public static final String ERRORMESSAGE_MIMOSA_110002 = " call tulipSdk exception ";
	/** 卡券类型-红包 */
	public static final String COUPON_TYPE_REDPACKETS="redPackets";
	/** 卡券类型-代金券 */
	public static final String COUPON_TYPE_COUPON="coupon";
	/** 卡券类型-加息券 */
	public static final String COUPON_TYPE_RATECOUPON="rateCoupon";
	/** 卡券类型-体验金 */
	public static final String COUPON_TYPE_TASTECOUPON="tasteCoupon";

}
