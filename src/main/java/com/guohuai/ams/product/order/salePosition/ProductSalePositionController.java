package com.guohuai.ams.product.order.salePosition;

import java.sql.Date;
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

import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;

/**
 * 产品可售份额申请操作相关接口
 * @author wangyan
 *
 */
@RestController
@RequestMapping(value = "/mimosa/product/duration/salePosition", produces = "application/json")
public class ProductSalePositionController extends BaseController {

	@Autowired
	private ProductSalePositionService salePositionService;

	/**
	 * 查询产品可售份额申请列表
	 * 
	 * @param request
	 * @param productOid
	 * @param page
	 *            第几页
	 * @param rows
	 *            每页显示多少记录数
	 * @param sort
	 *            排序字段 createTime
	 * @param order
	 *            排序规则：升序还是降序 desc
	 * @return {@link ResponseEntity<PagesRep<ProductSalePositionResp>>}
	 *         ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	@RequestMapping(value = "/saleVolumeApplyList", name="查询产品可售份额申请列表", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageResp<ProductSalePositionResp>> saleVolumeApplyList(HttpServletRequest request,
			@RequestParam final String productOid, @RequestParam int page, @RequestParam int rows,
			@RequestParam(required = false, defaultValue = "basicDate") String sort,
			@RequestParam(required = false, defaultValue = "asc") String order) {

		PageResp<ProductSalePositionResp> rep = new PageResp<ProductSalePositionResp>();
		if (!StringUtil.isEmpty(productOid)) {
			if (page < 1) {
				page = 1;
			}
			if (rows < 1) {
				rows = 1;
			}

			Direction sortDirection = Direction.ASC;
			if (!"asc".equals(order)) {
				sortDirection = Direction.DESC;
			}
			
			final Date today = DateUtil.formatUtilToSql(DateUtil.getCurrDate());

			Specification<ProductSalePositionOrder> spec = new Specification<ProductSalePositionOrder>() {
				@Override
				public Predicate toPredicate(Root<ProductSalePositionOrder> root, CriteriaQuery<?> query,
						CriteriaBuilder cb) {
					return cb.and(cb.equal(root.get("product").get("oid").as(String.class), productOid),
							cb.greaterThanOrEqualTo(root.get("basicDate").as(Date.class), today),
							cb.notEqual(root.get("status").as(String.class), ProductSalePositionOrder.STATUS_CANCEL),
							cb.notEqual(root.get("status").as(String.class), ProductSalePositionOrder.STATUS_DELETE));
				}
			};
			spec = Specifications.where(spec);
			Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)).and(new Sort(new Order(Direction.DESC, "createTime"))));
			rep = this.salePositionService.list(spec, pageable);
		}

		return new ResponseEntity<PageResp<ProductSalePositionResp>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 查询产品可售份额排期历史列表
	 * 
	 * @param request
	 * @param productOid
	 * @param page
	 *            第几页
	 * @param rows
	 *            每页显示多少记录数
	 * @param sort
	 *            排序字段 createTime
	 * @param order
	 *            排序规则：升序还是降序 desc
	 * @return {@link ResponseEntity<PagesRep<ProductSalePositionResp>>}
	 *         ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	@RequestMapping(value = "/saleVolumeScheduleList", name="查询产品可售份额排期历史列表", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageResp<ProductSalePositionResp>> saleVolumeScheduleList(HttpServletRequest request,
			@RequestParam final String productOid, @RequestParam int page, @RequestParam int rows,
			@RequestParam(required = false, defaultValue = "basicDate") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {

		PageResp<ProductSalePositionResp> rep = new PageResp<ProductSalePositionResp>();
		if (!StringUtil.isEmpty(productOid)) {
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
			
			final Date today = DateUtil.formatUtilToSql(DateUtil.getCurrDate());

			Specification<ProductSalePositionOrder> spec = new Specification<ProductSalePositionOrder>() {
				@Override
				public Predicate toPredicate(Root<ProductSalePositionOrder> root, CriteriaQuery<?> query,
						CriteriaBuilder cb) {
					return cb.and(cb.equal(root.get("product").get("oid").as(String.class), productOid),
							cb.lessThan(root.get("basicDate").as(Date.class), today));
					
				}
			};
			
			spec = Specifications.where(spec);
			Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)).and(new Sort(new Order(Direction.DESC, "createTime"))));
			rep = this.salePositionService.list(spec, pageable);
		}

		return new ResponseEntity<PageResp<ProductSalePositionResp>>(rep, HttpStatus.OK);
	}

	/**
	 * 是否已经有申请过的
	 * 
	 * @param productOid
	 * @return
	 */
	@RequestMapping(value = "/findSalePositionApply", name="是否已经有申请过的", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<ProductSalePositionApplyResp> findSalePositionApply(
			@RequestParam(required = true) String productOid) {
		ProductSalePositionApplyResp psrr = this.salePositionService.findSalePositionApply(productOid);
		return new ResponseEntity<ProductSalePositionApplyResp>(psrr, HttpStatus.OK);
	}

	/**
	 * 新加产品可售份额申请
	 * 
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/save", name="新加产品可售份额申请", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> save(@Valid SaveProductSalePositionForm form) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.salePositionService.save(form, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品可售份额申请审核通过
	 * 
	 * @param oid
	 *            ProductSalePositionOrder的oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/auditPass", name="产品可售份额申请审核通过", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> auditPass(@RequestParam String oid) throws ParseException,Exception {
		String operator = super.getLoginUser();
		BaseResp repponse = this.salePositionService.auditPass(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品可售份额申请审核驳回
	 * 
	 * @param oid
	 *            ProductSalePositionOrder的oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/auditFail", name="产品可售份额申请审核驳回", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> auditFail(@RequestParam String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.salePositionService.auditFail(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品可售份额申请撤销
	 * 
	 * @param oid
	 *            ProductSalePositionOrder的oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/rollback", name="产品可售份额申请撤销", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> rollbackApply(@RequestParam String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.salePositionService.rollbackApply(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品可售份额申请删除
	 * 
	 * @param oid
	 *            ProductSalePositionOrder的oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/delete", name="产品可售份额申请删除", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> delete(@RequestParam String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.salePositionService.delete(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

}
