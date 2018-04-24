package com.guohuai.ams.supplement.order;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.supplement.mechanism.Mechanism;
import com.guohuai.ams.supplement.mechanism.MechanismController;
import com.guohuai.ams.supplement.mechanism.MechanismForm;
import com.guohuai.ams.supplement.mechanism.MechanismRep;
import com.guohuai.ams.supplement.mechanism.MechanismService;
import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.mmp.sys.CodeConstants;
import com.guohuai.operate.api.AdminSdk;

import lombok.extern.slf4j.Slf4j;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

/**补单机构管理
 * @author qiuliang
 *
 */
@RestController
@RequestMapping(value = "/mimosa/boot/mechanismOrder", produces = "application/json;charset=UTF-8")
@Slf4j
public class MechanismOrderController extends BaseController{
	
	@Autowired
	private MechanismOrderService mechanismOrderService;
	@Autowired
	private MechanismService mechanismService;
	@Autowired
	private ProductService productService;
	@Autowired
	private SeqGenerator seqGenerator;
	
	@RequestMapping(name = "保存补单机构", value = "create", method = RequestMethod.POST)
	public ResponseEntity<BaseResp> save(@Valid MechanismOrderForm mechanismOrderForm) {
		String loginId = super.getLoginUser();
		String productOid = mechanismOrderForm.getProductOid();
		String mechanismOid = mechanismOrderForm.getMechanismOid();
		Product p = this.productService.findByOid(productOid);
		Mechanism m = this.mechanismService.detail(mechanismOid);
		MechanismOrder mo = MechanismOrder.builder().mechanism(m).product(p)
				.orderAmount(mechanismOrderForm.getOrderAmount())
				.productName(mechanismOrderForm.getProductName())
				.fictitiousTime(new Date(mechanismOrderForm.getFictitiousTime().getTime()))
				.orderCode(this.seqGenerator.next(CodeConstants.PAYMENT_supplement))
				.orderStatus(MechanismOrder.Order_Status_Created)
				.orderType(MechanismOrder.Order_Type_Supplement)
				.operator(loginId)
				.operateTime(new Date())
				.createTime(new Date())
				.build();
		BaseResp response = mechanismOrderService.create(mo);
		return new ResponseEntity<BaseResp>(response, HttpStatus.OK);
	}
//	@RequestMapping(name = "新建补单机构订单", value = "create", method = RequestMethod.POST)
//	public ResponseEntity<BaseResp> save(@RequestBody List<MechanismOrderForm> mechanismOrderForms) {
//		List<MechanismOrder> mechanismOrders = new ArrayList<MechanismOrder>();
//		String productOid = mechanismOrderForms.get(0).getProductOid();
//		String mechanismOid = mechanismOrderForms.get(0).getMechanismOid();
//		Product p = this.productService.findByOid(productOid);
//		Mechanism m = this.mechanismService.detail(mechanismOid);
//		String loginId = super.getLoginUser();
//		for(MechanismOrderForm mof : mechanismOrderForms){
//			MechanismOrder mo = MechanismOrder.builder().mechanism(m).product(p)
//					.orderAmount(mof.getOrderAmount())
//					.productName(mof.getProductName())
//					.fictitiousTime(new Date(mof.getFictitiousTime().getTime()))
//					.orderCode(this.seqGenerator.next(CodeConstants.PAYMENT_supplement))
//					.orderStatus(MechanismOrder.Order_Status_Created)
//					.orderType(MechanismOrder.Order_Type_Supplement)
//					.operator(loginId)
//					.operateTime(new Date())
//					.createTime(new Date())
//					.build();
//			mechanismOrders.add(mo);		
//		}
//		BaseResp response = mechanismOrderService.create(mechanismOrders);
//		return new ResponseEntity<BaseResp>(response, HttpStatus.OK);
//	}
	
	@RequestMapping(name = "查询补单列表", value = "orderlist", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<PageResp<MechanismOrderRep>> orderlist(
			@RequestParam String productOid, 
			@RequestParam(defaultValue = "1") int page, 
			@RequestParam(defaultValue = "50") int rows, 
			@RequestParam(defaultValue = "desc") String sort,
			@RequestParam(defaultValue = "createTime") String sortField) {
		if (page < 1) {
			page = 1;
		}
		if (rows <= 0) {
			rows = 50;
		}
		Direction sortDirection = Direction.DESC;
		if("asc".equals(sort)){
			sortDirection = Direction.ASC;
		}
		Specification<MechanismOrder> spec = Specifications.where(new Specification<MechanismOrder>() {
			@Override
			public Predicate toPredicate(Root<MechanismOrder> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("product").get("oid").as(String.class), productOid);
			}
		});
		PageResp<MechanismOrderRep> pageResp = mechanismOrderService.list(spec,page, rows, sortDirection, sortField);
		return new ResponseEntity<PageResp<MechanismOrderRep>>(pageResp, HttpStatus.OK);
	}
	
	@RequestMapping(name = "非新手标的定期产品列表", value = "tnproductlist", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<TnProductRep> tnproductlist() {
		TnProductRep tns = this.productService.findTnProductAndNotFreashman();
		return new ResponseEntity<TnProductRep>(tns, HttpStatus.OK);
	}
	
	@RequestMapping(name = "剩余订单金额", value = "restOrderAmount", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ResponseEntity<RestOrderAmountRep> restOrderAmount(@RequestParam String productOid) {
		RestOrderAmountRep rep = this.productService.findMaxScaleAmountByProduct(productOid);
		return new ResponseEntity<RestOrderAmountRep>(rep, HttpStatus.OK);
	}
	

}
