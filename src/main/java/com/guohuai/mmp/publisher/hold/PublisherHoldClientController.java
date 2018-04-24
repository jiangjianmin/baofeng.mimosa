package com.guohuai.mmp.publisher.hold;

import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.component.web.view.RowsRep;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(value = "/mimosa/client/holdconfirm", produces = "application/json")
public class PublisherHoldClientController extends BaseController {

	@Autowired
	private PublisherHoldService  publisherHoldService;
	@Autowired
	private HoldQueryService holdService;
	

	
	
	@RequestMapping(value = "pmng", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<HoldQueryRep>> mng(HttpServletRequest request,
			@And({@Spec(params = "holdStatus", path = "holdStatus", spec = In.class),
				@Spec(params = "productType", path = "product.type", spec = In.class)}) Specification<PublisherHoldEntity> spec,
			@RequestParam int page, 
			@RequestParam int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		final String uid = "";
		Specification<PublisherHoldEntity> uidSpec = new Specification<PublisherHoldEntity>() {
			@Override
			public Predicate toPredicate(Root<PublisherHoldEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

				return cb.equal(root.get("investorBaseAccount").get("userOid").as(String.class), uid);
			}

		};
		spec = Specifications.where(spec).and(uidSpec);
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PagesRep<HoldQueryRep> rep = this.publisherHoldService.holdMng(spec, pageable);
		return new ResponseEntity<PagesRep<HoldQueryRep>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 获取持有人列表
	 */
	@RequestMapping(value = "getDataByParams", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PageResp<HoldQueryRep>> getDataByParams(
			@And({
				@Spec(params = "productName", path = "product.name", spec = Like.class),
				@Spec(params = "investorType", path = "accountType", spec = Equal.class)
			}) Specification<PublisherHoldEntity> spec,
			@RequestParam int page, 
			@RequestParam int rows,
			@RequestParam(required = false, defaultValue = "createTime") String sort,
			@RequestParam(required = false, defaultValue = "desc") String order) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}
		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PageResp<HoldQueryRep> rep = holdService.getDataByParams(spec, pageable);
		return new ResponseEntity<PageResp<HoldQueryRep>>(rep, HttpStatus.OK);
	}
	
	
	/**
	 * APP可用于赎回来买定期的活期产品
	 */
	@RequestMapping(value = "t0redeemable", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<RowsRep<T0RedeemableRep>> getT0RedeemableProduct() {

		String userOid = this.getLoginUser();
		RowsRep<T0RedeemableRep> rep = this.publisherHoldService.getT0RedeemableProduct(userOid);
		return new ResponseEntity<RowsRep<T0RedeemableRep>>(rep, HttpStatus.OK);
	}
	
	/**
	 * 我的活期  
	 */
	@RequestMapping(value = "t0hold", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<MyHoldT0QueryRep> queryMyT0HoldProList() {

		String userOid = this.getLoginUser();
		MyHoldT0QueryRep rep = this.publisherHoldService.queryMyT0HoldProList(userOid);
		return new ResponseEntity<MyHoldT0QueryRep>(rep, HttpStatus.OK);
	}
	/**
	 * 我的活期 --包含详情的产品列表信息
	 */
	@RequestMapping(value = "t0holdDetailList", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<List<MyHoldQueryRep>> queryMyT0HoldProListDetail() {

		String userOid = this.getLoginUser();
		List<MyHoldQueryRep> rep = this.publisherHoldService.queryMyT0HoldProListDetail(userOid);
		return new ResponseEntity<List<MyHoldQueryRep>>(rep, HttpStatus.OK);
	}
	
	/** 
	 * 我的持有中活期详情 
	 */
	@RequestMapping(value = "mycurrdetail", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> queryMyCurrProInfo(@RequestParam String productOid) {
		String userOid = this.getLoginUser();
		MyHoldQueryRep rep = this.publisherHoldService.queryMyCurrProInfo(userOid, productOid);
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	@RequestMapping(value = "mholdvol", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> getMaxHoldVol(@RequestParam String productOid) {
		String userOid = this.getLoginUser();
		BaseRep rep = this.publisherHoldService.getMaxHoldVol(userOid, productOid);
		
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 我的定期
	 */
	@RequestMapping(value = "tnhold", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> queryMyTnHoldProList() {

		String userOid = this.getLoginUser();
		BaseRep rep = this.publisherHoldService.queryMyTnHoldProList(userOid);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}
	
	/**
	 * 查询 我的定期  持有中  产品详情
	 */
	@RequestMapping(value = "tningdetail", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<TnHoldingDetail> queryTnHoldingDetail(@RequestParam String productOid) {

		String userOid = this.getLoginUser();
		TnHoldingDetail rep = this.publisherHoldService.queryTnHoldingDetail(userOid, productOid);
		return new ResponseEntity<TnHoldingDetail>(rep, HttpStatus.OK);
	}
	/**
	 * 
	 * @author yihonglei
	 * @Title: tnHoldList
	 * @Description: 我的定期--1.6.0 版本新增加方法
	 * 1. 旧版本方法中我的定期是按持有手册维度显示
	 * 2. 新版本方法中修改为按照定期订单单条展示
	 * @param req
	 * @return ResponseEntity<MyTnClientRep<Map<String,Object>>>
	 * @date 2017年9月7日 下午3:48:33
	 * @since  1.0.0
	 */
	@RequestMapping(value = "tnHoldList", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<MyTnClientRep<Map<String,Object>>> tnHoldList(@Valid @RequestBody MyTnClientReq req) {
		String uid = this.getLoginUser();
		MyTnClientRep<Map<String,Object>> res = this.publisherHoldService.tnHoldList(req, uid);
		return new ResponseEntity<MyTnClientRep<Map<String,Object>>>(res, HttpStatus.OK);
	}
	/**
	 * jiangjianmin
	 * 我的企业散标订单列表
	 */
	@RequestMapping(value = "scatterHoldList", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<MyTnClientRep<Map<String,Object>>> scatterHoldList(@Valid @RequestBody MyTnClientReq req) {
		String uid = this.getLoginUser();
//		String uid = "ef474154efa74c029a3775dc03195424";
		MyTnClientRep<Map<String,Object>> res = this.publisherHoldService.scatterHoldList(req, uid);
		return new ResponseEntity<MyTnClientRep<Map<String,Object>>>(res, HttpStatus.OK);
	}
	/**
	 * 我的节节高产品
	 * @return
	 */
	@RequestMapping(value = "jjghold", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JjgHoldDetailRep> queryMyJjgHoldList() {

		String userOid = this.getLoginUser();
		JjgHoldDetailRep rep = this.publisherHoldService.queryMyJjgHoldProList(userOid);
		return new ResponseEntity<JjgHoldDetailRep>(rep, HttpStatus.OK);
	}

	/**
	 * @Desc: 快定宝订单列表
	 * @author huyong
	 * @date 2018/3/21 下午4:15
	 */
	@RequestMapping(value = "kdbHoldList", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<MyTnClientRep<Map<String,Object>>> kdbHoldList(@Valid @RequestBody MyKdbClientReq req) {
		String userOid = this.getLoginUser();
		MyTnClientRep<Map<String,Object>> res = this.publisherHoldService.kdbHoldList(req, userOid);
		return new ResponseEntity<MyTnClientRep<Map<String,Object>>>(res, HttpStatus.OK);
	}

	/**
	 * @Desc: 快定宝详情页
	 * @author huyong
	 * @date 2018/3/21 下午4:15
	 */
	@RequestMapping(value = "kdbHoldDetail", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseEntity<Map<String,Object>> kdbHolDetail(@RequestParam String orderCode,@RequestParam String orderType) {
		String userOid = this.getLoginUser();
		Map<String,Object> res = this.publisherHoldService.kdbHoldDetail(orderCode, orderType, userOid);
		return new ResponseEntity<Map<String,Object>>(res, HttpStatus.OK);
	}
}
