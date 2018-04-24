package com.guohuai.ams.product.dataTransmission;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSException;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.plugin.PageVo;
import com.jcraft.jsch.SftpException;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/mimosa/boot/product/dataTransmission", produces = "application/json")
@Slf4j
public class ProductDataTransmissionController extends BaseController {
	
	@Autowired
	private ProductDataTransmissionService productDataTransmissionService;
	
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryUserStatistics
	 * @Description: 查询募集期结束，有投资成功记录并且募集结束日期小于当前系统时间的定期产品列表
	 * @param req
	 * @return ResponseEntity<PageVo<Map<String,Object>>>
	 * @throws UnsupportedEncodingException 
	 * @date 2017年5月23日 下午6:52:25
	 * @since  1.0.0
	 */
	@RequestMapping(value = "queryProductInfo", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageVo<Map<String,Object>>> queryUserStatistics(@Valid @RequestBody ProductDataTransmissionReq req) throws UnsupportedEncodingException {
		log.info("产品列表信息查询参数:{}",JSONObject.toJSONString(req));
		super.getLoginUser();
		
		PageVo<Map<String,Object>> res = productDataTransmissionService.queryProductInfo(req);
		return new ResponseEntity<PageVo<Map<String,Object>>>(res, HttpStatus.OK);
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryProductInvestorInfo
	 * @Description: 根据产品Id查询某个产品对应投资人投资信息列表
	 * @param req
	 * @return ResponseEntity<PageVo<Map<String,Object>>>
	 * @date 2017年5月23日 下午6:54:26
	 * @since  1.0.0
	 */
	@RequestMapping(value = "queryProductInvestorInfo", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageVo<Map<String,Object>>> queryProductInvestorInfo(@Valid @RequestBody ProductDataTransmissionReq req) {
		log.info("产品对应投资信息查询参数:{}",JSONObject.toJSONString(req));
		super.getLoginUser();
		
		PageVo<Map<String,Object>> res = productDataTransmissionService.queryProductInvestorInfo(req);
		return new ResponseEntity<PageVo<Map<String,Object>>>(res, HttpStatus.OK);
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: queryProductDataTransRecord
	 * @Description: 查询某个产品对应数据传输记录列表--需求已去掉
	 * @param req
	 * @return ResponseEntity<PageVo<Map<String,Object>>>
	 * @date 2017年5月23日 下午6:55:46
	 * @since  1.0.0
	 */
	@RequestMapping(value = "queryProductDataTransRecord", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageVo<Map<String,Object>>> queryProductDataTransRecord(@Valid @RequestBody ProductDataTransmissionReq req) {
		log.info("产品记录传输记录:{}",JSONObject.toJSONString(req));
		super.getLoginUser();
		
		PageVo<Map<String,Object>> res = productDataTransmissionService.queryProductDataTransRecord(req);
		return new ResponseEntity<PageVo<Map<String,Object>>>(res, HttpStatus.OK);
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: productDataTransBySftp
	 * @Description:通过sftp方式，进行数据传输(包含产品基本信息，产品投资人信息，分成两个文件传输)
	 * @param req
	 * @return ResponseEntity<PageVo<Map<String,Object>>>
	 * @throws IOException 
	 * @throws ClientException 
	 * @throws OSSException 
	 * @throws SftpException 
	 * @date 2017年5月23日 下午6:58:34
	 * @since  1.0.0
	 */
	@RequestMapping(value = "productDataTransBySftp", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<Map<String,Object>> productDataTransBySftp() {
		String operator = super.getLoginUser();
		Map<String,Object> res = productDataTransmissionService.productDataTransBySftp(operator);
		
		return new ResponseEntity<Map<String,Object>>(res, HttpStatus.OK);
	}
	
}
