package com.guohuai.ams.product;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;

import lombok.extern.slf4j.Slf4j;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

@RestController
@RequestMapping(value = "/mimosa/product/productDetail", produces = "application/json;charset=UTF-8")
@Slf4j
public class ProductTypeDetailController extends BaseController{
	@Autowired
	private ProductTypeDetailService serviceProductTypeDetail;
	
	@RequestMapping(value = "query", name="获取产品详情列表", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PageResp<ProductTypeDetailQueryResp>> ProductTypeDetailQuery(HttpServletRequest request,
			@And({  @Spec(params = "type", path = "type", spec = Equal.class),
					@Spec(params = "updateTime", path = "updateTime", spec = Equal.class)}) 
	         		Specification<ProductTypeDetail> spec,
			@RequestParam int page, 
			@RequestParam int rows) {		
		log.info("---productDetailType+{}",request.getParameter("type"));
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "updateTime")));		
		PageResp<ProductTypeDetailQueryResp> rep = this.serviceProductTypeDetail.ProductTypeDetailQuery(spec, pageable);
		
		return new ResponseEntity<PageResp<ProductTypeDetailQueryResp>>(rep, HttpStatus.OK);
	}
	/**
	 * 新增产品详情
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "add", name="新增产品详情", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> addProductTypeDetailInfo( @RequestBody ProductTypeDetailAddReq req) {	
		log.info("新增产品详情参数：{}", req);
		BaseResp baseRep = new BaseResp();
		String operator = super.getLoginUserName();
		req.setOperator(operator);
		this.serviceProductTypeDetail.addProductTypeDetail(req);
		return new ResponseEntity<BaseResp>(baseRep, HttpStatus.OK);		
	}
	/**
	 * 修改产品详情
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "edit", name="修改产品详情", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> editProductTypeDetailInfo(@Valid ProductTypeDetailAddReq req) {		
		BaseResp baseRep = new BaseResp();
		String operator = super.getLoginUser();
		req.setOperator(operator);
		this.serviceProductTypeDetail.addProductTypeDetail(req);
		return new ResponseEntity<BaseResp>(baseRep, HttpStatus.OK);		
	}
	/**
	 * 根据OID获取产品详情
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "productTypeDetailInfo", name="获取渠道详情", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<ProductTypeDetailInfoResp> getInfo(@RequestParam String oid) {		
		ProductTypeDetailInfoResp rep = this.serviceProductTypeDetail.getProductTypeDetailInfo(oid);
		return new ResponseEntity<ProductTypeDetailInfoResp>(rep, HttpStatus.OK);
	}
	/**
	 *根据类型获取产品详情
	 * @return
	 */
	@RequestMapping(value = "getProductTypeOptions", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<ProductTypeDetailOptionsResp> getProductTypeOptions(){
		ProductTypeDetailOptionsResp rep = this.serviceProductTypeDetail.getProductTypeDetailAll();
		return new ResponseEntity<ProductTypeDetailOptionsResp>(rep, HttpStatus.OK);
	}
}
