package com.guohuai.mmp.investor.fund;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.ams.acct.account.fund.RSASignature;
import com.guohuai.ams.acct.account.fund.WyHttpClientUtil;
import com.guohuai.mmp.investor.fund.YiLuConfig;

@Service
@Transactional
public class YiLuService {
	
	
	@Autowired
	private YiLuConfig config;
	
	
	/**
	 * 查询我的基金
	 * @param uid
	 * @param mobiletelno
	 * @return
	 */
	public Map<String,Object> myFund(String uid) {
		Map<String,String> map = new HashMap<String,String>();
		map.put("partnerid",config.getFund_customkey() );
		map.put("businesscode", config.getFund_myfund_code());
		String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		map.put("custReqSerialno", time);
		map.put("localtime", time);
		map.put("custid", uid);
		map.put("showfundWealth", "1");
		map.put("seckey", config.getFund_sign());
		//加签
		RSASignature rsaSign = new RSASignature();
		String sign = rsaSign.sign(map);
		map.put("sign", sign);
		map.remove("seckey");
		WyHttpClientUtil util = new WyHttpClientUtil();
        String str = util.postSSLUrlWithParams(config.getFund_myfund(),map,"utf-8","") ;
        System.out.println(str);
        Map<String,Object> rmap = JSONObject.parseObject(str,Map.class);
        return rmap;
	}
	
	/**
	 * 查询正在进行中的交易
	 * @param yiluoid
	 * @param mobiletelno
	 * @return
	 */

//	public Map<String, Object> fundTrading(String yiluoid) {
//		Map<String,String> map = new HashMap<String,String>();
//		map.put("partnerid","baofeng" );
//		map.put("businesscode", "fw0002");
//		String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//		map.put("custReqSerialno", time);
//		map.put("localtime", time);
//		map.put("custid", yiluoid);
//		map.put("seckey", "48gbivqjisifjjrcha0frrwfuqcm7mtcj49v");
//		RSASignature rsaSign = new RSASignature();
//		String sign = rsaSign.sign(map);
//		map.put("sign", sign);
//		map.remove("seckey");
//		WyHttpClientUtil util = new WyHttpClientUtil();
//		String str = util.postSSLUrlWithParams("http://baofeng.yilucaifu.com/api2/myWealth/fund/fundTrading.html",map,"utf-8","") ;
//        System.out.println(str);
//        Map<String,Object> rmap = JSONObject.parseObject(str,Map.class);
//        return rmap;
//	}
}
