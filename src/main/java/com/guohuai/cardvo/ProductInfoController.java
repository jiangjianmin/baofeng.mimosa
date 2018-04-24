package com.guohuai.cardvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.cardvo.req.ProductRep;
import com.guohuai.cardvo.req.ProductReq;
import com.guohuai.cardvo.service.MimosaService;
import com.guohuai.component.web.view.PageResp;


/**
 * @Desc: 	用户信息
 * @author: huyong 
 * @data:   2017.5.11
 */
@RestController
@RequestMapping(value = "/mimosa/cardvo/productinfo",produces = "application/json")
public class ProductInfoController extends BaseController {

	@Autowired
	private MimosaService mimosaService;
	
	/**
	 * @Desc:   查询产品列表和相关信息
	 * @author: huyong 
	 * @data:   2017.5.11
	 */
	@RequestMapping(value = "/getProductList", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PageResp<ProductRep>> getProductList(@RequestBody ProductReq req) {

		return new ResponseEntity<PageResp<ProductRep>>(this.mimosaService.getProductList(req), HttpStatus.OK);
	}
	
	/**
	 * @Desc   根据产品类型和oid查询产品
	 * @author huyong 
	 * @data   2017.5.11
	 */
	@RequestMapping(value = "/getProductCountByOid", method = RequestMethod.POST)
	@ResponseBody
	public Long getProductCountByOid(@RequestBody ProductReq req) {
		return this.mimosaService.getProductCountByOid(req);
	}

}
