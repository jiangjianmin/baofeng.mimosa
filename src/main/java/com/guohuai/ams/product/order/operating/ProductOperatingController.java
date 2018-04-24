package com.guohuai.ams.product.order.operating;

import java.text.ParseException;
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
import com.guohuai.component.util.StringUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;

/**
 * 产品申购和赎回开关申请操作相关接口
 * @author wangyan
 *
 */
@RestController
@RequestMapping(value = "/mimosa/product/duration/operate", produces = "application/json")
public class ProductOperatingController extends BaseController {

	@Autowired
	private ProductOperatingService productOperatingService;

	/**
	 * 查询产品申购开关,赎回开关申请列表
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
	 * @return {@link ResponseEntity<PagesRep<ProductOperatingOrderResp>>}
	 *         ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	@RequestMapping(value = "/purchaseRemeedApplyList", name="查询产品申购开关,赎回开关申请列表", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageResp<ProductOperatingOrderResp>> purchaseApplyList(HttpServletRequest request,
			@RequestParam final String productOid, @RequestParam int page, @RequestParam int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {

		PageResp<ProductOperatingOrderResp> rep = new PageResp<ProductOperatingOrderResp>();
		if(!StringUtil.isEmpty(productOid)) {
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
			
			Specification<ProductOperatingOrder> spec = new Specification<ProductOperatingOrder>() {
				@Override
				public Predicate toPredicate(Root<ProductOperatingOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.and(cb.equal(root.get("product").get("oid").as(String.class), productOid),
							cb.notEqual(root.get("status").as(String.class), ProductOperatingOrder.STATUS_CANCEL),
							cb.notEqual(root.get("status").as(String.class), ProductOperatingOrder.STATUS_DELETE));
				}
			};
			
			spec = Specifications.where(spec);
			Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
			rep = this.productOperatingService.list(spec, pageable);
		}
		
		return new ResponseEntity<PageResp<ProductOperatingOrderResp>>(rep, HttpStatus.OK);
	}

	/**
	 * 产品打开申购开关申请
	 * 
	 * @param oid
	 *            产品oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/openPurchase", name="产品打开申购开关申请", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> openPurchaseApply(@RequestParam String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productOperatingService.openPurchaseApply(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品关闭申购开关申请
	 * 
	 * @param oid
	 *            产品oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/closePurchase", name="产品关闭申购开关申请", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> closePurchaseApply(@RequestParam String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productOperatingService.closePurchaseApply(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品打开赎回开关申请
	 * 
	 * @param oid
	 *            产品oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/openRedeem", name="产品打开赎回开关申请", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> openRedeemApply(@RequestParam String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productOperatingService.openRedeemApply(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品关闭赎回开关申请
	 * 
	 * @param oid
	 *            产品oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/closeRedeem", name="产品关闭赎回开关申请", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> closeRedeemApply(@RequestParam String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productOperatingService.closeRedeemApply(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 审核通过-(申请打开申购,申请关闭申购,申请打开赎回,申请关闭赎回)
	 * 
	 * @param oid
	 *            ProductOperatingOrder的oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/passPurchaseRemeed", name="审核通过-(申请打开申购,申请关闭申购,申请打开赎回,申请关闭赎回)", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> passPurchaseRemeedApply(@RequestParam String oid) throws ParseException,Exception {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productOperatingService.passPurchaseRemeedApply(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 审核驳回-(申请打开申购,申请关闭申购,申请打开赎回,申请关闭赎回)
	 * 
	 * @param oid
	 *            ProductOperatingOrder的oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/failPurchaseRemeed", name="审核驳回-(申请打开申购,申请关闭申购,申请打开赎回,申请关闭赎回)", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> failPurchaseRemeedApply(@RequestParam String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productOperatingService.failPurchaseRemeedApply(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 撤销-(申请打开申购,申请关闭申购,申请打开赎回,申请关闭赎回)
	 * 
	 * @param oid
	 *            ProductOperatingOrder的oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/rollbackPurchaseRemeed", name="撤销-(申请打开申购,申请关闭申购,申请打开赎回,申请关闭赎回)", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> rollbackPurchaseRemeedApply(@RequestParam String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productOperatingService.rollbackPurchaseRemeedApply(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 删除-(申请打开申购,申请关闭申购,申请打开赎回,申请关闭赎回)
	 * 
	 * @param oid
	 *            ProductOperatingOrder的oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/deletePurchaseRemeed", name="删除-(申请打开申购,申请关闭申购,申请打开赎回,申请关闭赎回)", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> deletePurchaseRemeedApply(@RequestParam String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productOperatingService.deletePurchaseRemeedApply(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

}
