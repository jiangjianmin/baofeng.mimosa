package com.guohuai.fund;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.ApplicationBootstrap;
import com.guohuai.ams.acct.account.fund.Base64;
import com.guohuai.ams.acct.account.fund.RSAEncrypt;
import com.guohuai.ams.acct.account.fund.RSASignature;
import com.guohuai.ams.acct.account.fund.WyHttpClientUtil;
import com.guohuai.mmp.investor.fund.YiLuConfig;
import com.guohuai.mmp.investor.fund.YiLuService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApplicationBootstrap.class)
public class Test {
	 public static void main(String[] args) throws Exception {  
	        String filepath="C:/temp/temp";  
	  
	       // RSAEncrypt.genKeyPair(filepath);  
	          
	          
//	        System.out.println("--------------公钥加密私钥解密过程-------------------");  
//	        String plainText="ihep_公钥加密私钥解密";  
//	        //公钥加密过程  
//	        byte[] cipherData=RSAEncrypt.encrypt(RSAEncrypt.loadPublicKeyByStr(RSAEncrypt.loadPublicKeyByFile(filepath)),plainText.getBytes());  
//	        String cipher=Base64.encode(cipherData);  
//	        //私钥解密过程  
//	        byte[] res=RSAEncrypt.decrypt(RSAEncrypt.loadPrivateKeyByStr(RSAEncrypt.loadPrivateKeyByFile(filepath)), Base64.decode(cipher));  
//	        String restr=new String(res);  
//	        System.out.println("原文："+plainText);  
//	        System.out.println("加密："+cipher);  
//	        System.out.println("解密："+restr);  
//	        System.out.println();  
//	          
//	        System.out.println("--------------私钥加密公钥解密过程-------------------");  
//	       String plainText="ihep_私钥加密公钥解密";  
//	        //私钥加密过程  
//	       byte[] cipherData=RSAEncrypt.encrypt(RSAEncrypt.loadPrivateKeyByStr(RSAEncrypt.loadPrivateKeyByFile(filepath)),plainText.getBytes());  
//	       String cipher=Base64.encode(cipherData);  
//	        //公钥解密过程  
//	       byte[] res=RSAEncrypt.decrypt(RSAEncrypt.loadPublicKeyByStr(RSAEncrypt.loadPublicKeyByFile(filepath)), Base64.decode(cipher));  
//	        String restr=new String(res);  
//	        System.out.println("原文："+plainText);  
//	        System.out.println("加密："+cipher);  
//	        System.out.println("解密："+restr);  
//	        System.out.println();  
	          
//	        System.out.println("---------------私钥签名过程------------------");  
//	        String content="74324abab";  
//	        String signstr=RSASignature.sign(content,"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCxQhjSzugu5kszouM8UZgP4MNzjWtUIltBbstME4uGpA2zBCWvTOr152mqODNaYmgH0xRvSbnjFho7bheyCCxVGvoWivycgiwyn19dHUYCT5KuitjSuGdMdR7vpu+/JviRYhvqdt3aO8XN7pidwkFuye/ZqkeaBCkHsp/fmxJUbwIDAQAB","UTF-8");
//	        signstr = Base64.encode(signstr.getBytes());
//	        System.out.println("签名原串："+content);  
//	        System.out.println("签名串："+signstr);  
//	        System.out.println();  
//	          
//	        //公钥加密过程  
//	        String plainText=content;
//	        byte[] cipherData=RSAEncrypt.encrypt(RSAEncrypt.loadPublicKeyByStr(RSAEncrypt.loadPublicKeyByFile(filepath)),plainText.getBytes());  
//	        String cipher=Base64.encode(cipherData);  
	        
	        
//	        //私钥解密过程  
//	        byte[] res=RSAEncrypt.decrypt(RSAEncrypt.loadPublicKeyByStr(RSAEncrypt.loadPublicKeyByFile(filepath)), Base64.decode("RoBLJTePgNkoLllu1VqytdvVGyM0VN0FescfSeHjzojadJkK38Lb2JRfE5+1/LDpvhn5n39nJGMDUi0bzaAkYXMmDWQH3cv9PakyU9uT6ZAS1aVJD0mP4OGhBzv/bwE9oBJotSzP3hxnF6mA6GYCdAmagyWr5xyoJ2sr5zc2XzI="));  
//	        String restr=new String(res);  
////	        System.out.println("原文："+plainText);  
////	        System.out.println("加密："+cipher);  
//	        System.out.println("解密："+restr);  
//	        System.out.println();  
	        
//	        System.out.println("---------------公钥校验签名------------------");  
//	        System.out.println("签名原串："+content);  
//	        System.out.println("签名串："+signstr);  
//	          
//	        System.out.println("验签结果："+RSASignature.doCheck(content, new String(Base64.decode(signstr)), "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBALFCGNLO6C7mSzOi4zxRmA/gw3ONa1QiW0Fuy0wTi4akDbMEJa9M6vXnaao4M1piaAfTFG9JueMWGjtuF7IILFUa+haK/JyCLDKfX10dRgJPkq6K2NK4Z0x1Hu+m778m+JFiG+p23do7xc3umJ3CQW7J79mqR5oEKQeyn9+bElRvAgMBAAECgYEAi7Vy50SZD84HW614uKP+3BTlOAAwpeFmV45BwbZt9DL9cSlFooNXdXVtfPJK12RUZkZiBszvLCLRjiHZn0J93Uz5BkH4zSOIKRX+A4V+2UFxwQiOLIaslcP2pHui4H6Ftnuq32A8n6qeUKLKufY8uIHv5bMqlIXqAuFrVgY0KeECQQDpJ6AmSjpwyIAQtMEfPe19yEHkshbu4POwOl6VhjE7DBNH166UTdK7Jxnf+ttymaDVC6dg2c8wgj7nBsxaawg5AkEAwqBhYnijuV0Nphfc7dBDNOqtEPfVgUKQyiTsfGqsflWinzN1HK1jst1P2aEYff2/q4Ucg7sCV48mY2QRaUMx5wJBAMNZHge23awtcY+NwtloS5m9teflhu1ysPqQjTr+ijUM8wKYpX9AjTWdl0JgrokgSu71qrGtDLl6BzYOLh3725ECQQCGBTyqO1q6xRol+p624Ee3Q7ajTZYnKXhcyqpSTn6zjDKsoBmQAtH5lC5tNWKRN8/pw4LML1XAh6GupXTb4FqXAkAb7NrIVGtZupO5hKIHTHsYYg2mfkeBS4QzfFz1DMR7GgsYfSQk8G4RaSJrO+GjMm5SK80O2sRYD0wZOLKhxd2e"));  
//	        System.out.println();  
	          
	        //发送请求
	        WyHttpClientUtil util = new WyHttpClientUtil();
	        Map<String,String> map = new HashMap<String,String>();
	        map.put("uuid","9fe92b4879e4465b874ae7178ec6342d");
			map.put("sign_key", "b3dqebaquaa4gnadcbiqkbgqcpoypnw4j3O1");
			String sign_data = RSASignature.sign(map);
			map.remove("sign_key");
	        map.put("sign_data", sign_data);
	        System.out.println(sign_data);
	        String str = util.postSSLUrlWithParams("http://huidu.baofengzd.com:81/mimosa/acct/account/selectFourElement",map,"utf-8","");
//	        Map<String,String> rmap = JSONObject.parseObject(str,Map.class);
//	        //公钥解密过程  
//	        System.out.println(rmap);
//	        String cipher = rmap.get("encrypt_data");
//	        byte[] res=RSAEncrypt.decrypt(RSAEncrypt.loadPublicKeyByStr("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCxQhjSzugu5kszouM8UZgP4MNzjWtUIltBbstME4uGpA2zBCWvTOr152mqODNaYmgH0xRvSbnjFho7bheyCCxVGvoWivycgiwyn19dHUYCT5KuitjSuGdMdR7vpu+/JviRYhvqdt3aO8XN7pidwkFuye/ZqkeaBCkHsp/fmxJUbwIDAQAB"), Base64.decode(cipher));  
//	        String restr=new String(res);  
//	        System.out.println("解密："+restr);  
//	        System.out.println();
//	        //验签
//	        System.out.println("验签结果："+RSASignature.doCheck(restr, new String(Base64.decode(rmap.get("sign_data"))), "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCxQhjSzugu5kszouM8UZgP4MNzjWtUIltBbstME4uGpA2zBCWvTOr152mqODNaYmgH0xRvSbnjFho7bheyCCxVGvoWivycgiwyn19dHUYCT5KuitjSuGdMdR7vpu+/JviRYhvqdt3aO8XN7pidwkFuye/ZqkeaBCkHsp/fmxJUbwIDAQAB"));
//	        YiLuService service = new YiLuService();
//	        Map<String,Object> map = service.myFund("18513131314");
//	        List<HashMap<String,Object>> list = (List<HashMap<String,Object>>) map.get("fundTradingList");
//			BigDecimal bd = new BigDecimal(0);
//			for(HashMap<String,Object> m : list) {
//				bd = bd.add(new BigDecimal(m.get("applicationamount").toString()));
//			}
//			System.out.println(bd);
//	        System.out.println(new BigDecimal(map.get("fundWealth").toString()));
	        
//	        Map<String,String> map = new HashMap<String,String>();
//			map.put("uuid", "192d81a53c8811e8b64986b3e1fe9391");
//			map.put("sign_key", "fdjia456789fasfjiehfuejianm1554874545aiemlg");
//			String sign_data = RSASignature.sign(map);
//			System.out.println(sign_data);
//	        
//	        Map<String,String> map = new HashMap<String,String>();
//			map.put("cust_id", "9fe92b4879e4465b874ae7178ec6342d");
//			map.put("customkey", "baofeng");
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//			String time = sdf.format(new Date());
//			System.out.println(time);
//			map.put("timeStamp", time);
//			map.put("seckey", "nzktnfrjcxnxt99kn02qpm1ykpock0bfh3dw");
//			
//			RSASignature rsaSign = new RSASignature();
//			System.out.println(rsaSign.sign(map));
//			String sign = rsaSign.sign(map);
//			map.put("sign", sign);
//			System.out.println(sign);
//			map.remove("seckey");
//			WyHttpClientUtil util = new WyHttpClientUtil();
//	        String str = util.postSSLUrlWithParams("http://baofeng.yilucaifu.com/api2/myWealth/fund/transaccountCloseAcctApi.html",map,"utf-8","");
//	        Map<String,Object> rmap = JSONObject.parseObject(str,Map.class);
//	        String result = rmap.get("respmsg").toString();
//			System.out.println(result);
	        
	 }         
	 
//	 @Autowired
//	 private YiLuService yiLuService;
//	 
//	 @Autowired
//	 private YiLuConfig yiLuConfig;
//	 
//	 @org.junit.Test
//	 public void test() {
//	 Map<String,String> map = new HashMap<String,String>();
//			map.put("partnerid", yiLuConfig.getFund_customkey());
//			map.put("businesscode", yiLuConfig.getYilu_fund_getAccountStatusByuuid());
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//			String time = sdf.format(new Date());
//			map.put("custReqSerialno", time);
//			map.put("localtime", time);
//			map.put("custid", "18513131314");
//			map.put("seckey", yiLuConfig.getFund_sign());
//			RSASignature rsaSign = new RSASignature();
//			String sign = rsaSign.sign(map);
//			map.put("sign", sign);
//			map.remove("seckey");
//			WyHttpClientUtil util = new WyHttpClientUtil();
//	        String str = util.postSSLUrlWithParams(yiLuConfig.getYilu_fund_getAccountStatusByuuid(),map,"utf-8","");
//	        Map<String,Object> rmap = JSONObject.parseObject(str,Map.class);
//	        String result = rmap.get("isSign").toString();
//			System.out.println(result);
//	 }
}
