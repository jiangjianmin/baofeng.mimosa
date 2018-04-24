package com.guohuai.ams.product.order.spv;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.ams.order.SPVOrder;
import com.guohuai.ams.order.SPVOrderResp;
import com.guohuai.ams.order.SPVOrderService;
import com.guohuai.component.util.StringUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;

/**
 * 产品SPV订单查询相关接口
 * @author wangyan
 *
 */
@RestController
@RequestMapping(value = "/mimosa/product/duration/spvOrder", produces = "application/json")
public class ProductSpvOrderController extends BaseController {
	
	@Autowired
	private SPVOrderService spvOrderService;
	@Autowired
	private ProductService productService;
	
	/**
	 * 产品对应的spv订单列表
	 * @param request
	 * @param productOid 产品oid
	 * @param page 第几页
	 * @param rows  每页显示多少记录数
	 * @param sort 排序字段 update
	 * @param order 排序规则：升序还是降序 desc
	 * @return {@link ResponseEntity<PagesRep<InvestorOrderResp>>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现 
	 */
	@RequestMapping(value = "/list", name="产品对应的spv订单列表", method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<PageResp<SPVOrderResp>> indetList(HttpServletRequest request,
			@RequestParam String productOid,
			@RequestParam int page, 
			@RequestParam int rows,
			@RequestParam(required = false, defaultValue = "updateTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {
		
		if (page < 1) {
			page = 1;
		}
		if (rows < 1) {
			rows = 1;
		}
		
		PageResp<SPVOrderResp> rep = new PageResp<SPVOrderResp>();
				
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		
		if(!StringUtil.isEmpty(productOid)) {
			
			final Product p = productService.getProductByOid(productOid);
			if(p!=null && p.getAssetPool()!=null) {
				Specification<SPVOrder> spec = new Specification<SPVOrder>() {
					@Override
					public Predicate toPredicate(Root<SPVOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
						return cb.equal(root.get("assetPool").get("oid").as(String.class), p.getAssetPool().getOid());
					}
				};
				spec = Specifications.where(spec);
				
				Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
				rep = this.spvOrderService.list(spec, pageable);
			}
			
		}
		
		return new ResponseEntity<PageResp<SPVOrderResp>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 产品和spv对应的spv订单列表
	 * @param request
	 * @param productOid 产品oid
	 * @param spvOid spv oid
	 * @param page 第几页
	 * @param rows  每页显示多少记录数
	 * @param sort 排序字段 update
	 * @param order 排序规则：升序还是降序 desc
	 * @return {@link ResponseEntity<PagesRep<InvestorOrderResp>>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现 
	 */
	@RequestMapping(value = "/spvlist", name="产品和spv对应的spv订单列表", method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<PageResp<SPVOrderResp>> indetList(HttpServletRequest request,
			@RequestParam(required = true) String productOid,
			@RequestParam(required = true) final String spvOid,
			@RequestParam int page, 
			@RequestParam int rows,
			@RequestParam(required = false, defaultValue = "updateTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {
		
		if (page < 1) {
			page = 1;
		}
		if (rows < 1) {
			rows = 1;
		}
		
		PageResp<SPVOrderResp> rep = new PageResp<SPVOrderResp>();
				
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		
		if(!StringUtil.isEmpty(productOid)) {
			
			final Product p = productService.getProductByOid(productOid);
			if(p!=null && p.getAssetPool()!=null) {
				Specification<SPVOrder> spec = new Specification<SPVOrder>() {
					@Override
					public Predicate toPredicate(Root<SPVOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
						return cb.and(cb.equal(root.get("spv").get("oid").as(String.class), spvOid),
								cb.equal(root.get("assetPool").get("oid").as(String.class), p.getAssetPool().getOid()));
					}
				};
				spec = Specifications.where(spec);
				
				Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
				rep = this.spvOrderService.list(spec, pageable);
			}
			
		}
		
		return new ResponseEntity<PageResp<SPVOrderResp>>(rep, HttpStatus.OK);
	}

}
