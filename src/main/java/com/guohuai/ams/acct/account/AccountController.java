package com.guohuai.ams.acct.account;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.ams.acct.account.fund.Base64;
import com.guohuai.ams.acct.account.fund.RSAEncrypt;
import com.guohuai.ams.acct.account.fund.RSASignature;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.mmp.investor.fund.YiLuConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/mimosa/acct/account", produces = "application/json;charset=utf-8")
public class AccountController extends BaseController {
	
	
	
	@Autowired
	private AccountService accountService;
	
	private RSASignature rSASignature = new RSASignature();
	
	@Autowired
	YiLuConfig config;

	@RequestMapping(value = "/search", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<List<AccountResp>> search() {
		super.getLoginUser();
		List<Account> list = this.accountService.search();
		List<AccountResp> view = new ArrayList<AccountResp>();
		for (Account a : list) {
			view.add(new AccountResp(a));
		}
		return new ResponseEntity<List<AccountResp>>(view, HttpStatus.OK);
	}

	@RequestMapping(value = "/update", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<AccountResp> update(AccountForm form) {
		super.getLoginUser();
		Account a = this.accountService.update(form);
		AccountResp view = new AccountResp(a);
		return new ResponseEntity<AccountResp>(view, HttpStatus.OK);
	}
	
	
	//一路财富查询四要素
	@RequestMapping(value = "/selectFourElement", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody Map<String,String> selectFourElement(HttpServletResponse response, HttpServletRequest request)  {
		log.info("开始接受查询四要素参数");
        log.info("开始校验签名");
        Map<String,String> resultMap = new HashMap<String,String>();
        //获取参数
        String content = request.getParameter("uuid");
        String sign_data = request.getParameter("sign_data");
        boolean result = false;
		//校验签名
        try {
        	Map<String,String> map = new HashMap<String,String>();
        	map.put("uuid", content);
        	map.put("sign_key",config.getFund_sign_key());
        	String signdata = rSASignature.sign(map);
			if(sign_data!=null) {
				result = sign_data.equals(signdata);
			}
		} catch (Exception e) {
			resultMap.put("respStatus", "0003");
    		resultMap.put("respMessage", "失败！信息获取失败！");
    		log.info("失败！验签出现异常！");
    		return resultMap;
		}
        if(result) {
        	//签名验证成功，获取四要素
        	log.info("签名校验成功，开始获取四要素");
        	Object[] obj = this.accountService.selectFourElement(content);
        	FourElementVo fev = null;
			try {
				if(obj!=null&&obj.length==4&&obj[0]!=null&&obj[1]!=null&&obj[2]!=null&&obj[3]!=null) {
					fev = new FourElementVo(obj[0].toString(),obj[1].toString(),new String(obj[2].toString().getBytes(),"utf-8"),obj[3].toString());
				}else {
		        	resultMap.put("respStatus", "0001");
		        	resultMap.put("respMessage", "失败！找不到该uid对应的四要素信息！");
		        	log.info("找不到该uid对应的四要素信息(或不全)!,uid为："+content);
		        	return resultMap;
				}
			} catch (UnsupportedEncodingException e) {
				resultMap.put("respStatus", "0003");
	    		resultMap.put("respMessage", "失败！信息获取失败！");
	    		log.info("失败！编码转换错误！");
	    		return resultMap;
			}
        	resultMap.put("name", fev.getName());
        	resultMap.put("idNumb", fev.getIdNumb());
        	resultMap.put("cardNumb", fev.getCardNumb());
        	resultMap.put("phoneNo", fev.getPhoneNo());
        	String resultContent = RSAEncrypt.map2LinkString(resultMap);
        	resultMap.remove("name");
        	resultMap.remove("idNumb");
        	resultMap.remove("cardNumb");
        	resultMap.remove("phoneNo");
        	//签名
        	log.info("开始添加签名");
        	String signstr = null;
			try {
				signstr = RSASignature.sign(resultContent,config.getFund_privatekey(),"utf-8");
			} catch (Exception e) {
				resultMap.put("respStatus", "0003");
	    		resultMap.put("respMessage", "失败！信息获取失败！");
	    		log.info("失败！添加签名失败！");
	    		return resultMap;
			}
        	signstr = Base64.encode(signstr.getBytes());
        	log.info("添加签名成功，签名串为："+signstr);
        	resultMap.put("sign_data", signstr);        	
        	//加密
        	log.info("开始加密");
        	byte[] cipherData = null;
			try {
				cipherData = RSAEncrypt.encrypt(RSAEncrypt.loadPrivateKeyByStr(config.getFund_privatekey()),resultContent.getBytes());
			} catch (Exception e) {
				resultMap.put("respStatus", "0003");
	    		resultMap.put("respMessage", "失败！信息获取失败！");
	    		log.info("失败！加密失败！");
	    		return resultMap;
			}
        	String cipher=Base64.encode(cipherData);  
        	log.info("加密成功，密文为："+cipher);
        	resultMap.put("encrypt_data", cipher);
        }else {
			resultMap.put("respStatus", "0002");
    		resultMap.put("respMessage", "失败！签名验证不正确！");
    		log.info("失败！签名验证不正确！");
    		return resultMap;
		}
        log.info("成功返回:"+resultMap);
        resultMap.put("respStatus", "0000");
		resultMap.put("respMessage", "成功！");
		return resultMap;
	}

}
