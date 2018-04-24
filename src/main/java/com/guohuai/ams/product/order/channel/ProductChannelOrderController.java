package com.guohuai.ams.product.order.channel;

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
import com.guohuai.ams.product.productChannel.ChooseChannelResp;
import com.guohuai.component.util.StringUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;

/**
 * 产品渠道申请操作相关接口
 * @author wangyan
 *
 */
@RestController
@RequestMapping(value = "/mimosa/product/channel/order", produces = "application/json")
public class ProductChannelOrderController extends BaseController {
	
	@Autowired
	private ProductChannelOrderService productChannelOrderService;
	
	/**
	 * 查询产品渠道申请列表
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
	 * @return {@link ResponseEntity<PagesRep<ProductChannelOrderResp>>}
	 *         ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	@RequestMapping(value = "/channelApplyList", name="查询产品渠道申请列表", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageResp<ProductChannelOrderResp>> saleVolumeApplyList(HttpServletRequest request,
			@RequestParam final String productOid, @RequestParam int page, @RequestParam int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {

		PageResp<ProductChannelOrderResp> rep = new PageResp<ProductChannelOrderResp>();
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
			
			Specification<ProductChannelOrder> spec = new Specification<ProductChannelOrder>() {
				@Override
				public Predicate toPredicate(Root<ProductChannelOrder> root, CriteriaQuery<?> query,
						CriteriaBuilder cb) {
					return cb.and(cb.equal(root.get("product").get("oid").as(String.class), productOid),
							cb.notEqual(root.get("status").as(String.class), ProductChannelOrder.STATUS_CANCEL),
							cb.notEqual(root.get("status").as(String.class), ProductChannelOrder.STATUS_DELETE));
				}
			};
			spec = Specifications.where(spec);
			Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
			rep = this.productChannelOrderService.list(spec, pageable);
		}

		return new ResponseEntity<PageResp<ProductChannelOrderResp>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 查询产品已经申请的渠道列表
	 * 
	 * @param request
	 * @param productOid
	 * @return {@link ResponseEntity<PagesRep<ProductChannelOrderResp>>}
	 *         ,如果返回的errCode属性等于0表示成功，否则表示失败，失败原因在errMessage里面体现
	 */
	@RequestMapping(value = "/productChannels", name="查询产品已经申请的渠道列表", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<PageResp<ProductChannelOrderResp>> saleVolumeApplyList(HttpServletRequest request, @RequestParam final String productOid) {

		PageResp<ProductChannelOrderResp> rep = null;
		if (!StringUtil.isEmpty(productOid)) {

			Specification<ProductChannelOrder> spec = new Specification<ProductChannelOrder>() {
				@Override
				public Predicate toPredicate(Root<ProductChannelOrder> root, CriteriaQuery<?> query,
						CriteriaBuilder cb) {
					return cb.and(cb.equal(root.get("product").get("oid").as(String.class), productOid),
							cb.notEqual(root.get("status").as(String.class), ProductChannelOrder.STATUS_FAIL),
							cb.notEqual(root.get("status").as(String.class), ProductChannelOrder.STATUS_CANCEL),
							cb.notEqual(root.get("status").as(String.class), ProductChannelOrder.STATUS_DELETE));
				}
			};
			spec = Specifications.where(spec);
			
			rep = this.productChannelOrderService.productChannels(spec, new Sort(new Order(Direction.DESC, "createTime")));
		}

		return new ResponseEntity<PageResp<ProductChannelOrderResp>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 产品选择渠道列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/channels", name="产品选择渠道列表", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<PageResp<ChooseChannelResp>> chooseList(HttpServletRequest request, @RequestParam String productOid) {

		

		PageResp<ChooseChannelResp> rep = this.productChannelOrderService.queryChannels(productOid);
		return new ResponseEntity<PageResp<ChooseChannelResp>>(rep, HttpStatus.OK);
	}

	/**
	 * 新加产品渠道申请
	 * 
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/saveChannel", name="新加产品渠道申请", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseResp> saveChannel(@Valid SaveProductChannelForm form) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productChannelOrderService.save(form, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品渠道申请审核通过
	 * 
	 * @param oid
	 *            ProductSalePositionOrder的oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/auditPassChannel", name="产品渠道申请审核通过", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> auditPass(@RequestParam String oid) throws ParseException,Exception {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productChannelOrderService.auditPass(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品渠道申请审核驳回
	 * 
	 * @param oid
	 *            ProductSalePositionOrder的oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/auditFailChannel", name="产品渠道申请审核驳回", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> auditFail(@RequestParam String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productChannelOrderService.auditFail(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品渠道申请撤销
	 * 
	 * @param oid
	 *            ProductSalePositionOrder的oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/rollbackChannel", name="产品渠道申请撤销", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> rollbackApply(@RequestParam String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productChannelOrderService.rollbackApply(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

	/**
	 * 产品渠道申请删除
	 * 
	 * @param oid
	 *            ProductSalePositionOrder的oid
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/deleteChannel", name="产品渠道申请删除", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<BaseResp> delete(@RequestParam String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp repponse = this.productChannelOrderService.delete(oid, operator);
		return new ResponseEntity<BaseResp>(repponse, HttpStatus.OK);
	}

}
