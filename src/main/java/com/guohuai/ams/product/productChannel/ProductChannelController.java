package com.guohuai.ams.product.productChannel;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
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

import com.guohuai.ams.product.Product;
import com.guohuai.basic.cardvo.rep.PageRespTulip;
import com.guohuai.basic.common.StringUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseResp;
import com.guohuai.component.web.view.PageResp;

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

/**
 * 产品关联渠道操作相关接口
 * @author wangyan
 *
 */
@RestController
@RequestMapping(value = "/mimosa/product/channel", produces = "application/json")
public class ProductChannelController extends BaseController {

	@Autowired
	private ProductChannelService productChannelService;

	/**
	 * 该渠道的产品列表
	 * 
	 * @param request
	 * @param productOid
	 * @param channelOids
	 * @return
	 */
	@RequestMapping(value = "/list", name="该渠道的产品列表", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<PageResp<ProductChannelResp>> list(HttpServletRequest request,
			@RequestParam(required = true) final String channelOid, @RequestParam int page, @RequestParam int rows)
			throws ParseException {
		if (page < 1) {
			page = 1;
		}
		if (rows < 1) {
			rows = 1;
		}

		Direction sortDirection = Direction.DESC;
		Specification<ProductChannel> spec = new Specification<ProductChannel>() {
			@Override
			public Predicate toPredicate(Root<ProductChannel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("channel").get("oid").as(String.class), channelOid);
			}
		};
		spec = Specifications.where(spec);

		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, "createTime")));
		PageResp<ProductChannelResp> rep = this.productChannelService.list(spec, pageable);
		return new ResponseEntity<PageResp<ProductChannelResp>>(rep, HttpStatus.OK);
	}

	/**
	 * 上架
	 * 
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/upshelf", name="上架产品", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseResp> upshelf(@RequestParam(required = true) String oid) throws ParseException {
		String operator = super.getLoginUser();
		BaseResp r = this.productChannelService.upshelf(oid, operator);
		return new ResponseEntity<BaseResp>(r, HttpStatus.OK);
	}

	/**
	 * 下架
	 * 
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/donwshelf", name="下架产品", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<BaseResp> donwshelf(@RequestParam String oid) {
		String operator = super.getLoginUser();
		BaseResp r = this.productChannelService.donwshelf(oid, operator);
		return new ResponseEntity<BaseResp>(r, HttpStatus.OK);
	}

	@RequestMapping(value = "/channelQuery", name="渠道详情中的产品列表查询", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<ProductChannelViewPage> channelQuery(
			@RequestParam(defaultValue = "") final String productState, @RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int rows,
			@And({ @Spec(params = "channelOid", path = "channel.oid", spec = Equal.class),
					@Spec(params = "marketState", path = "marketState", spec = Equal.class),
					@Spec(params = "productType", path = "product.type.oid", spec = Equal.class),
					@Spec(params = "productCode", path = "product.code", spec = Like.class) }) Specification<ProductChannel> spec,
			@Or({ @Spec(params = "productName", path = "product.name", spec = Like.class),
					@Spec(params = "productName", path = "product.fullName", spec = Like.class) }) Specification<ProductChannel> productNameSpec) {
				
		if (!StringUtil.isEmpty(productState)) {
			final String[] productStates = productState.split(";");
			if(productStates!=null && productStates.length>0) {
				Specification<ProductChannel> productStateSpc = new Specification<ProductChannel>() {
					@Override
					public Predicate toPredicate(Root<ProductChannel> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
						if(productStates.length==1) {
							return cb.equal(root.get("product").get("state").as(String.class),productStates[0]);
						} else {
							Expression<String> exp = root.get("product").get("state").as(String.class);
							In<String> in = cb.in(exp);
							for(String productState : productStates) {
								in.value(productState);
							}
							in.value(Product.STATE_Reviewpass);
							return in;
						}
					}
				};
				spec = Specifications.where(spec).and(productNameSpec).and(productStateSpc);
			} else {
				spec = Specifications.where(spec).and(productNameSpec);
			}
		} else {
			spec = Specifications.where(spec).and(productNameSpec);
		}

		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(Direction.DESC, "rackTime")));
		ProductChannelViewPage p = this.productChannelService.channelQuery(spec, pageable);
		return new ResponseEntity<ProductChannelViewPage>(p, HttpStatus.OK);
	}
	
	/**
 	 * 活动模板根据渠道查询产品列表
 	 */
 	@RequestMapping(value = "/activityModelProducts", name="活动模板根据渠道查询产品列表", method = {RequestMethod.POST,RequestMethod.GET})
 	@ResponseBody
 	public ResponseEntity<PageRespTulip<Object>> getActivityModelProducts(@RequestParam(required = true) String channelId) {
 		PageRespTulip<Object> resp = this.productChannelService.getActivityModelProducts(channelId);
 		return new ResponseEntity<PageRespTulip<Object>>(resp, HttpStatus.OK);
 	}

}
