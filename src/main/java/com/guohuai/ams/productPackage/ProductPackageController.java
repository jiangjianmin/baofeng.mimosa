package com.guohuai.ams.productPackage;

import java.text.ParseException;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.guohuai.ams.productPackage.loan.CompanyLoanProductReq;
import com.guohuai.component.web.view.BaseRep;
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
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.util.CheckUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.component.web.view.Response;

/**
 * 产品包操作相关接口
 * @author wangyan
 *
 */
@RestController
@RequestMapping(value = "/mimosa/productPackage", produces = "application/json")
public class ProductPackageController extends BaseController {

	@Autowired
	private ProductPackageService productPackageService;
	
	/**
	 * 获取productOid对应产品所有可以选择的资产池的名称列表
	 * @return
	 */
	@RequestMapping(value = "/getOptionalAssetPoolNameList", name="新加编辑产品可以选择的资产池名称列表", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getAllNameList(@RequestParam(required = false) String productOid, @RequestParam(required = false) String type) {
		List<JSONObject> jsonList = productPackageService.getOptionalAssetPoolNameList(productOid, type);
		Response r = new Response();
		r.with("rows", jsonList);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 新加定期产品包
	 */
	@RequestMapping(value = "/save/productPackage", name = "新加定期产品包", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> saveProductPackage(@Valid SavePeriodicProductPackageForm form) throws ParseException, Exception {
		String operator = super.getLoginUser();
		CheckUtil.checkProductPackageParams(form);
		BaseResp repponse = this.productPackageService.periodicPackage(form, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	@RequestMapping(value = "/delete", name="作废产品包", method = { RequestMethod.POST, RequestMethod.DELETE })
	@ResponseBody
	public ResponseEntity<ProductPackageResp> delete(@RequestParam(required = true) String oid) {
		String operator = super.getLoginUser();
		ProductPackage productPackage = this.productPackageService.delete(oid, operator);
		return new ResponseEntity<ProductPackageResp>(new ProductPackageResp(productPackage), HttpStatus.OK);
	}

	/**
	 * 更新定期产品包
	 */
	@RequestMapping(value = "/update/productPackage", name="编辑定期产品包", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> updateProductPackage(@Valid SavePeriodicProductPackageForm form) throws ParseException,Exception {
		String operator = super.getLoginUser();
		CheckUtil.checkProductPackageParams(form);
		BaseResp repponse = this.productPackageService.updateProductPackage(form, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品包明细
	 * 
	 * @param oid
	 *            产品类型的oid
	 * @return {@link ResponseEntity<ProductPackageDetailResp>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	@RequestMapping(value = "/detail", name="产品包明细", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<ProductPackageDetailResp> detail(@RequestParam(required = true) String oid) {
		ProductPackageDetailResp pr = this.productPackageService.read(oid);
		return new ResponseEntity<ProductPackageDetailResp>(pr, HttpStatus.OK);
	}

	/**
	 * 产品申请列表查询
	 * 
	 * @param request
	 * @param spec
	 * @param page
	 *            第几页
	 * @param rows
	 *            每页显示多少记录数
	 * @param sort
	 *            排序字段 update
	 * @param order
	 *            排序规则：升序还是降序 desc
	 * @return {@link ResponseEntity<PagesRep<ProductQueryRep>>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	@RequestMapping(value = "/apply/list",name="产品包申请列表查询", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageResp<ProductPackageResp>> applyList(HttpServletRequest request, @RequestParam final String name, @RequestParam int page, @RequestParam int rows,
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
		Specification<ProductPackage> spec = new Specification<ProductPackage>() {
			@Override
			public Predicate toPredicate(Root<ProductPackage> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("isDeleted").as(String.class), ProductPackage.NO), cb.equal(root.get("auditState").as(String.class), ProductPackage.AUDIT_STATE_Nocommit));
			}
		};
		spec = Specifications.where(spec);
		
		Specification<ProductPackage> nameSpec = null;
		if (!StringUtil.isEmpty(name)) {
			nameSpec = new Specification<ProductPackage>() {
				@Override
				public Predicate toPredicate(Root<ProductPackage> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.or(cb.like(root.get("name").as(String.class), "%" + name + "%"), cb.like(root.get("fullName").as(String.class), "%" + name + "%"));
				}
			};
			spec = Specifications.where(spec).and(nameSpec);
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PageResp<ProductPackageResp> rep = this.productPackageService.listPackage(spec, pageable);
		return new ResponseEntity<PageResp<ProductPackageResp>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 产品包提交审核
	 * 
	 * @param oids
	 *            产品oids
	 * @return {@link ResponseEntity<BaseResp>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	@RequestMapping(value = "/aduit/apply", name="产品包提交审核", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> aduitApply(@RequestParam String oids) throws ParseException {
		String operator = super.getLoginUser();
		List<String> oidlist = JSON.parseArray(oids, String.class);
		BaseResp repponse = this.productPackageService.aduitApply(oidlist, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品包审核列表查询
	 * 
	 * @param request
	 * @param spec
	 * @param page
	 *            第几页
	 * @param rows
	 *            每页显示多少记录数
	 * @param sort
	 *            排序字段 update
	 * @param order
	 *            排序规则：升序还是降序 desc
	 * @return {@link ResponseEntity<PagesRep<ProductQueryRep>>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	@RequestMapping(value = "/audit/list", name="产品包审核列表查询", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageResp<ProductPackageLogListResp>> auditList(HttpServletRequest request, @RequestParam final String name, @RequestParam int page, @RequestParam int rows,
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
		Specification<ProductPackage> spec = new Specification<ProductPackage>() {
			@Override
			public Predicate toPredicate(Root<ProductPackage> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("isDeleted").as(String.class), ProductPackage.NO), cb.equal(root.get("auditState").as(String.class), ProductPackage.AUDIT_STATE_Auditing));
			}
		};
		spec = Specifications.where(spec);

		Specification<ProductPackage> nameSpec = null;
		if (!StringUtil.isEmpty(name)) {
			nameSpec = new Specification<ProductPackage>() {
				@Override
				public Predicate toPredicate(Root<ProductPackage> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.or(cb.like(root.get("name").as(String.class), "%" + name + "%"), cb.like(root.get("fullName").as(String.class), "%" + name + "%"));
				}
			};
			spec = Specifications.where(spec).and(nameSpec);
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PageResp<ProductPackageLogListResp> rep = this.productPackageService.auditList(spec, pageable);
		return new ResponseEntity<PageResp<ProductPackageLogListResp>>(rep, HttpStatus.OK);
	}

	/**
	 * 产品包查询复核列表
	 * 
	 * @param request
	 * @param spec
	 * @param page
	 *            第几页
	 * @param rows
	 *            每页显示多少记录数
	 * @param sort
	 *            排序字段 update
	 * @param order
	 *            排序规则：升序还是降序 desc
	 * @return {@link ResponseEntity<PagesRep<ProductQueryRep>>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	@RequestMapping(value = "/check/list", name="产品包查询复核列表", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageResp<ProductPackageLogListResp>> checkList(HttpServletRequest request, @RequestParam final String name, @RequestParam int page, @RequestParam int rows,
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
		Specification<ProductPackage> spec = new Specification<ProductPackage>() {
			@Override
			public Predicate toPredicate(Root<ProductPackage> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("isDeleted").as(String.class), ProductPackage.NO), cb.or(
						cb.equal(root.get("auditState").as(String.class), ProductPackage.AUDIT_STATE_Reviewing),
						cb.equal(root.get("auditState").as(String.class), ProductPackage.AUDIT_STATE_Reviewed)));
			}
		};
		spec = Specifications.where(spec);

		Specification<ProductPackage> nameSpec = null;
		if (!StringUtil.isEmpty(name)) {
			nameSpec = new Specification<ProductPackage>() {
				@Override
				public Predicate toPredicate(Root<ProductPackage> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.or(cb.like(root.get("name").as(String.class), "%" + name + "%"), cb.like(root.get("fullName").as(String.class), "%" + name + "%"));
				}
			};
			spec = Specifications.where(spec).and(nameSpec);
		}

		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PageResp<ProductPackageLogListResp> rep = this.productPackageService.checkList(spec, pageable);
		return new ResponseEntity<PageResp<ProductPackageLogListResp>>(rep, HttpStatus.OK);
	}


	/**
	 * 产品审核通过
	 * 
	 * @param oids
	 *            产品oids
	 * @return {@link ResponseEntity<BaseResp>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	@RequestMapping(value = "/aduit/approve", name="产品审核通过", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> aduitApprove(@RequestParam(required = true) String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productPackageService.aduitApprove(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品包审核不通过
	 * 
	 * @param oid
	 *            产品oid
	 * @param auditComment
	 *            审核不通过备注
	 * @return {@link ResponseEntity<BaseResp>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 * @throws ParseException
	 */
	@RequestMapping(value = "/aduit/reject", name="产品包审核不通过", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> aduitReject(@RequestParam(required = true) String oid, @RequestParam(required = true) String auditComment) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productPackageService.aduitReject(oid, auditComment, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品包复核通过
	 * 
	 * @param oids
	 *            产品oid
	 * @return {@link ResponseEntity<BaseResp>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	@RequestMapping(value = "/review/approve", name="产品包复核通过", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> reviewApprove(@RequestParam(required = true) String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productPackageService.reviewApprove(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品包复核不通过
	 * 
	 * @param oid
	 *            产品oid
	 * @param auditComment
	 *            审核不通过备注
	 * @return {@link ResponseEntity<BaseResp>} ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 * @throws ParseException
	 */
	@RequestMapping(value = "/review/reject", name="产品包复核不通过", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> reviewReject(@RequestParam(required = true) String oid, @RequestParam(required = true) String auditComment) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productPackageService.reviewReject(oid, auditComment, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 验证名称是否唯一
	 * 
	 * @param name
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/validateName", name="验证名称是否唯一", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<BaseResp> validateName(@RequestParam String name, @RequestParam(required = false) String id) {
		BaseResp pr = new BaseResp();
		long single = this.productPackageService.validateSingle("name", name, id);
		return new ResponseEntity<BaseResp>(pr, single > 0 ? HttpStatus.CONFLICT : HttpStatus.OK);
	}

	/**
	 * 验证全称是否唯一
	 * 
	 * @param name
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/validateFullName", name="验证全称是否唯一", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<BaseResp> validateFullName(@RequestParam String fullName, @RequestParam(required = false) String id) {
		BaseResp pr = new BaseResp();
		long single = this.productPackageService.validateSingle("fullName", fullName, id);
		return new ResponseEntity<BaseResp>(pr, single > 0 ? HttpStatus.CONFLICT : HttpStatus.OK);
	}

	/**
	 * 验证编码是否唯一
	 * 
	 * @param name
	 * @param oid
	 * @return
	 */
	@RequestMapping(value = "/validateCode", name="验证编码是否唯一", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<BaseResp> validateCode(@RequestParam String code, @RequestParam(required = false) String id) {
		BaseResp pr = new BaseResp();
		long single = this.productPackageService.validateSingle("code", code, id);
		return new ResponseEntity<BaseResp>(pr, single > 0 ? HttpStatus.CONFLICT : HttpStatus.OK);
	}

	@RequestMapping(value = "/loan/apply", name = "企业借款产品推送接口", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> loanProductApply(@RequestBody CompanyLoanProductReq req) {
		return new ResponseEntity<>(this.productPackageService.loanProductPackageSave(req), HttpStatus.OK);
	}
}
