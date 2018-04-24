package com.guohuai.component.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.guohuai.ams.product.SaveCurrentProductForm;
import com.guohuai.ams.product.SavePeriodicProductForm;
import com.guohuai.ams.productPackage.SavePeriodicProductPackageForm;
import com.guohuai.basic.component.exception.GHException;
import com.guohuai.component.exception.AMPException;

/**
 * check util
 * @author Jeffrey.Wong
 * 2015年7月18日下午2:02:36
 */
public class CheckUtil {
	
	/**
	 * 校验参数是否为空
	 * @param param
	 * @param tip
	 */
	public static void checkParams(String param, String tip){
		if (param == null || "".equals(param)) {
			throw new AMPException(tip);
		}
	}
	
	/**
	 * 校验手机号(为兼容批量发)
	 * @param mobile
	 */
	public static void isMobileNO(String mobile){
		String[] phones = mobile.split(",");
		String phone = "";
		for (int i = 0 ;i < phones.length ; i++) {
			phone = phones[i];
			Pattern p = Pattern.compile("^1[3-9][0-9]{9}$");
			Matcher m = p.matcher(phone);
			if (!m.matches()) {
				throw new AMPException("手机号格式不正确！");
			}
		}
	}
	
	/**
	 * 校验短信验证码
	 * @param veriCode
	 */
	public static void checkVeriCode(String veriCode){
		Pattern p = Pattern.compile("^\\d{6}$");
		Matcher m = p.matcher(veriCode);
		if (!m.matches()) {
			throw new AMPException("验证码格式不正确！");
		}
	}

	/**
	* <p>Title: </p>
	* <p>Description:校验产品包卡券相关参数 </p>
	* <p>Company: </p> 
	* @param form
	* @author 邱亮
	* @date 2017年9月21日 下午9:01:12
	* @since 1.0.0
	*/
	public static void checkProductPackageParams(SavePeriodicProductPackageForm form) {
		Integer relateGuess = form.getRelateGuess();//0未关联，1已关联
		Integer useRed = form.getUseRedPackages();//2不能使用
		Integer useRaise = form.getUseraiseRateCoupons();//2不能使用
		if(relateGuess==1&&useRed!=2&&useRaise!=2){
			throw new GHException("产品包不能同时关联竞猜宝和卡券");
		}
		Integer useRed1 = form.getUseRedPackages();//2不能使用
		Integer useRaise1 = form.getUseraiseRateCoupons();//2不能使用
		String[] reds = form.getRedPackages();
		String[] raises = form.getRaiseRateCoupons();
		if((useRed1==1&&reds==null)
				||(useRaise1==1&&raises==null)){
			throw new GHException("自定义卡券必须选择卡券");
		}
	}

	/**
	* <p>Title: </p>
	* <p>Description: 校验活期卡券信息</p>
	* <p>Company: </p> 
	* @param form
	* @author 邱亮
	* @date 2017年9月21日 下午9:09:19
	* @since 1.0.0
	*/
	public static void checkCouponParams(SaveCurrentProductForm form) {
		Integer useRed = form.getUseRedPackages();//2不能使用
		Integer useRaise = form.getUseraiseRateCoupons();//2不能使用
		if(useRed!=2||useRaise!=2){
			throw new GHException("活期不能使用卡券");
		}
	}

	/**
	* <p>Title: </p>
	* <p>Description:校验卡券 </p>
	* <p>Company: </p> 
	* @param form
	* @author 邱亮
	* @date 2017年9月28日 下午5:27:29
	* @since 1.0.0
	*/
	public static void checkCouponParams(SavePeriodicProductForm form) {
		Integer useRed = form.getUseRedPackages();//2不能使用
		Integer useRaise = form.getUseraiseRateCoupons();//2不能使用
		String[] reds = form.getRedPackages();
		String[] raises = form.getRaiseRateCoupons();
		if((useRed==1&&reds==null)
				||(useRaise==1&&raises==null)){
			throw new GHException("自定义卡券必须选择卡券");
		}
		
		
	}
	
}
