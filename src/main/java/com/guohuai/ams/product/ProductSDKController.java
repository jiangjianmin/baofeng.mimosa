package com.guohuai.ams.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.PageResp;

/**
 * mimosa提供的SDK接口
 * 
 * @author wanglei
 *
 */
@RestController
@RequestMapping(value = "/mimosa/sdk/product", produces = "application/json")
public class ProductSDKController extends BaseController {

	@Autowired
	private ProductSDKService productSDKService;

	/**
	 * 所有产品列表查询
	 */
	@RequestMapping(value = "/queryall", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PageResp<ProductSDKRep>> queryProductList(@RequestBody ProductSDKReq req) {
		return new ResponseEntity<PageResp<ProductSDKRep>>(this.productSDKService.queryProductList(req), HttpStatus.OK);
	}
	
	/**
	 * 产品标签查询
	 */
	@RequestMapping(value = "/proLabel", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PageResp<ProductLabelSDKRep>> proLabel() {

		return new ResponseEntity<PageResp<ProductLabelSDKRep>>(this.productSDKService.queryProductLabelList(),
				HttpStatus.OK);
	}
	
}
