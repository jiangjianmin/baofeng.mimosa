package com.guohuai.ams.companyScatterStandard;

//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileWriter;
//import java.io.IOException;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.basic.message.DealMessageEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


/**
 *
 *企业借款controller
 * @author yujianlong
 * @date 2018/4/21 14:44
 * @param
 * @return
 */
@Slf4j
@RestController
@RequestMapping(value = "/mimosa/companyLoan/",produces = "application/json")
public class CompanyLoanController extends BaseController {
	@Autowired
	CompanyLoanService companyLoanService;

	/**
	 *
	 *募集满额回调方法
	 * @author yujianlong
	 * @date 2018/4/21 14:44
	 * @param [dealMessageEntity]
	 * @return java.util.Map<java.lang.String,java.lang.String>
	 */
	@RequestMapping(value = "/raiseFullAmountCallBack", method = RequestMethod.POST)
	public Map<String,String> raiseFullAmountCallBack(@RequestBody DealMessageEntity dealMessageEntity){
		try {
			return companyLoanService.raiseFullAmountCallBack(dealMessageEntity);
		}catch (Exception e){
			e.printStackTrace();
			log.error("{}{}",e.getCause(),e.getMessage());
			Map<String, String> result = new HashMap<>();
			result.put("status", "400");
			result.put("desc", "募集满额触发失败");
			return result;
		}

	}
	


	
	
	



}
