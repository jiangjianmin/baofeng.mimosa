package com.guohuai.ams.order;

import java.math.BigDecimal;
import java.text.ParseException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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

import com.guohuai.ams.product.ProductDetailResp;
import com.guohuai.component.util.StringUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.PageResp;

//import io.swagger.annotations.Api;

//@Api("SPV订单操作相关接口")
@RestController
@RequestMapping(value = "/mimosa/spv/order", produces = "application/json")
public class SPVOrderController extends BaseController {

	@Autowired
	private SPVOrderService spvOrderService;

	/**
	 * 新加订单
	 * 
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<SPVOrderResp> saveInvestorOrder(@Valid SaveSPVOrderForm form) throws ParseException {
		String operator = super.getLoginUser();
		SPVOrder investorOrder = this.spvOrderService.saveSpvOrder(form, operator);
		return new ResponseEntity<SPVOrderResp>(new SPVOrderResp(investorOrder), HttpStatus.OK);
	}

	/**
	 * 作废
	 * 
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/delete", method = { RequestMethod.POST, RequestMethod.DELETE })
	@ResponseBody
	public ResponseEntity<SPVOrderResp> delete(@RequestParam String oid) {
		String operator = super.getLoginUser();
		SPVOrder investorOrder = this.spvOrderService.delete(oid, operator);
		return new ResponseEntity<SPVOrderResp>(new SPVOrderResp(investorOrder), HttpStatus.OK);
	}

	/**
	 * 审核确定
	 * 
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/confirm", method = { RequestMethod.POST, RequestMethod.DELETE })
	@ResponseBody
	public ResponseEntity<SPVOrderResp> confirm(@Valid AuditSPVOrderForm form) throws Exception {
		String operator = super.getLoginUser();
		SPVOrder investorOrder = this.spvOrderService.confirm(form, operator);
		return new ResponseEntity<SPVOrderResp>(new SPVOrderResp(investorOrder), HttpStatus.OK);
	}

	/**
	 * 根据资产池获取可以赎回金额
	 * 
	 * @param assetPoolOid
	 * @return
	 */
	@RequestMapping(value = "/reemAmount", method = { RequestMethod.POST, RequestMethod.DELETE })
	@ResponseBody
	public BigDecimal reemAmount(@RequestParam String assetPoolOid) {
		return BigDecimal.ZERO;
	}

	/**
	 * 根据资产池获取关联的产品名称
	 * 
	 * @param assetPoolOid
	 * @return
	 */
	@RequestMapping(value = "/product", method = { RequestMethod.POST, RequestMethod.DELETE })
	@ResponseBody
	public ResponseEntity<ProductDetailResp> product(@RequestParam String assetPoolOid) {
		ProductDetailResp pr = this.spvOrderService.getProduct(assetPoolOid);
		return new ResponseEntity<ProductDetailResp>(pr, HttpStatus.OK);
	}

	/**
	 * spv订单列表
	 * 
	 * @param request
	 * @param productName
	 * @param orderType
	 *            交易类型
	 * @param orderCate
	 *            订单类型
	 * @param orderStatus
	 *            订单状态
	 * @param entryStatus
	 *            订单入账状态
	 * @param page
	 *            第几页
	 * @param rows
	 *            每页显示多少记录数
	 * @param sort
	 *            排序字段 update
	 * @param order
	 *            排序规则：升序还是降序 desc
	 * @return {@link ResponseEntity<PagesRep<InvestorOrderResp>>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageResp<SPVOrderResp>> indetList(HttpServletRequest request, @RequestParam final String assetPoolName, @RequestParam final String productName, @RequestParam final String orderType,
			@RequestParam final String orderCate, @RequestParam final String orderStatus, @RequestParam final String entryStatus, @RequestParam int page, @RequestParam int rows,
			@RequestParam(required = false, defaultValue = "updateTime") String sort, @RequestParam(required = false, defaultValue = "desc") String order) {

		if (page < 1) {
			page = 1;
		}
		if (rows < 1) {
			rows = 1;
		}

		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		Specification<SPVOrder> spec = null;
		if (StringUtil.isEmpty(orderType)) {
			spec = new Specification<SPVOrder>() {
				@Override
				public Predicate toPredicate(Root<SPVOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.or(cb.equal(root.get("orderType").as(String.class), SPVOrder.ORDER_TYPE_Invest), cb.equal(root.get("orderType").as(String.class), SPVOrder.ORDER_TYPE_Redeem));
				}
			};
		} else {
			spec = new Specification<SPVOrder>() {
				@Override
				public Predicate toPredicate(Root<SPVOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.equal(root.get("orderType").as(String.class), orderType);
				}
			};
		}

		if (StringUtil.isEmpty(orderCate)) {
			Specification<SPVOrder> orderCateSpec = new Specification<SPVOrder>() {
				@Override
				public Predicate toPredicate(Root<SPVOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.or(cb.equal(root.get("orderCate").as(String.class), SPVOrder.ORDER_CATE_Trade), cb.equal(root.get("orderCate").as(String.class), SPVOrder.ORDER_CATE_Strike));
				}
			};
			spec = Specifications.where(spec).and(orderCateSpec);
		} else {
			Specification<SPVOrder> orderCateSpec = new Specification<SPVOrder>() {
				@Override
				public Predicate toPredicate(Root<SPVOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.equal(root.get("orderCate").as(String.class), orderCate);
				}
			};
			spec = Specifications.where(spec).and(orderCateSpec);
		}

		Specification<SPVOrder> orderStatusSpec = null;
		if (!StringUtil.isEmpty(orderStatus)) {
			orderStatusSpec = new Specification<SPVOrder>() {
				@Override
				public Predicate toPredicate(Root<SPVOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.equal(root.get("orderStatus").as(String.class), orderStatus);
				}
			};
			spec = Specifications.where(spec).and(orderStatusSpec);
		}

		if (!StringUtil.isEmpty(entryStatus)) {
			Specification<SPVOrder> entryStatusSpec = new Specification<SPVOrder>() {
				@Override
				public Predicate toPredicate(Root<SPVOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.equal(root.get("entryStatus").as(String.class), entryStatus);
				}
			};
			spec = Specifications.where(spec).and(entryStatusSpec);
		}

		if (!StringUtil.isEmpty(productName)) {
			Specification<SPVOrder> productNameSpec = new Specification<SPVOrder>() {
				@Override
				public Predicate toPredicate(Root<SPVOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.or(cb.like(root.get("hold").get("product").get("name").as(String.class), "%" + productName + "%"),
							cb.like(root.get("hold").get("product").get("fullName").as(String.class), "%" + productName + "%"));
				}
			};
			spec = Specifications.where(spec).and(productNameSpec);
		}

		if (!StringUtil.isEmpty(assetPoolName)) {
			Specification<SPVOrder> assetPoolNameSpec = new Specification<SPVOrder>() {
				@Override
				public Predicate toPredicate(Root<SPVOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.like(root.get("assetPool").get("name").as(String.class), "%" + assetPoolName + "%");
				}
			};
			spec = Specifications.where(spec).and(assetPoolNameSpec);
		}

		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PageResp<SPVOrderResp> rep = this.spvOrderService.list(spec, pageable);
		return new ResponseEntity<PageResp<SPVOrderResp>>(rep, HttpStatus.OK);
	}

	@RequestMapping(value = "/detail", method = { RequestMethod.POST, RequestMethod.DELETE })
	@ResponseBody
	public ResponseEntity<SPVOrderDetailResp> detail(@RequestParam String oid) {
		SPVOrderDetailResp spvOrderResp = this.spvOrderService.detail(oid);
		return new ResponseEntity<SPVOrderDetailResp>(spvOrderResp, HttpStatus.OK);
	}

}
