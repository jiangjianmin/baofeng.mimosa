package com.guohuai.mmp.investor.tradeorder;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.guohuai.mmp.investor.tradeorder.cycleProduct.OrderContinueStatusReq;
import com.guohuai.mmp.investor.tradeorder.p2p.P2PCreditorDetailsRep;
import com.guohuai.mmp.investor.tradeorder.p2p.P2PCreditorDetailsReq;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.basic.common.StringUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.basic.messageBody.annotations.SerializedField;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PagesRep;

import lombok.extern.slf4j.Slf4j;
import net.kaczmarzyk.spring.data.jpa.domain.DateAfterInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.DateBeforeInclusive;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

@RestController
@RequestMapping(value = "/mimosa/client/tradeorder", produces = "application/json")
@Slf4j
public class InvestorTradeOrderClientController extends BaseController {

	@Autowired
	private InvestorInvestTradeOrderExtService investorInvestTradeOrderExtService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private FirstInvestService firstInvestService;

	/**
	 * 申购校验
	 *
	 * @param tradeOrderReq
	 * @return
	 */
	@RequestMapping(value = "investValidate", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<TradeOrderRep> investValidate(@RequestBody @Valid TradeOrderReq tradeOrderReq) {
		log.info("<------------申购校验:{}--------------->", tradeOrderReq);
		String uid = this.getLoginUser();
		tradeOrderReq.setUid(uid);
		log.info("uid={}, startTime={}", uid, DateUtil.getSqlCurrentDate());
		TradeOrderRep rep = this.investorInvestTradeOrderExtService.investValidate(tradeOrderReq);
		log.info("uid={}, endTime={}", uid, DateUtil.getSqlCurrentDate());
		log.info("<------------申购返回:{}--------------->", rep);
		return new ResponseEntity<>(rep, HttpStatus.OK);
	}

	/**
	 * 正常申购
	 */
	@RequestMapping(value = "invest", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<TradeOrderRep> invest(@RequestBody @Valid TradeOrderReq tradeOrderReq) {
		log.info("<------------申购请求:{}--------------->", tradeOrderReq);
		String uid = this.getLoginUser();
		tradeOrderReq.setUid(uid);
		log.info("uid={}, startTime={}", uid, DateUtil.getSqlCurrentDate());
		TradeOrderRep rep = this.investorInvestTradeOrderExtService.normalInvest(tradeOrderReq);
		log.info("uid={}, endTime={}", uid, DateUtil.getSqlCurrentDate());
		log.info("<------------申购返回:{}--------------->", rep);
		return new ResponseEntity<TradeOrderRep>(rep, HttpStatus.OK);
	}

	/**
	 * 网关申购
	 */
	@RequestMapping(value = "investThroughGateway", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<TradeOrderGatewayRep> investThroughGateway(@RequestBody @Valid TradeOrderGatewayReq tradeOrderGatewayReq) {
		log.info("<------------申购请求:{}--------------->", tradeOrderGatewayReq);
		String uid = this.getLoginUser();
		tradeOrderGatewayReq.setUid(uid);
		log.info("uid={}, startTime={}", uid, DateUtil.getSqlCurrentDate());
		tradeOrderGatewayReq.setPaymentMode(InvestorTradeOrderEntity.TRADEORDER_paymentMode_gateway);
		TradeOrderGatewayRep rep = this.investorInvestTradeOrderExtService.normalInvestThroughGateway(tradeOrderGatewayReq);
		log.info("uid={}, endTime={}", uid, DateUtil.getSqlCurrentDate());
		log.info("<------------申购返回:{}--------------->", rep);
		return new ResponseEntity<TradeOrderGatewayRep>(rep, HttpStatus.OK);
	}

	/**
	 * 活转定
	 */
	@RequestMapping(value = "redeemInvest", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<TradeOrderRep> redeemInvest(@RequestBody @Valid RedeemInvestTradeOrderReq tradeOrderReq) {
		log.info("<------------活转定请求:{}--------------->", tradeOrderReq);
		String uid = this.getLoginUser();
		log.info("uid={}, startTime={}", uid, DateUtil.getSqlCurrentDate());
		TradeOrderRep rep = this.investorInvestTradeOrderExtService.redeemInvest(tradeOrderReq, uid);
		log.info("uid={}, endTime={}", uid, DateUtil.getSqlCurrentDate());
		return new ResponseEntity<TradeOrderRep>(rep, HttpStatus.OK);
	}

	/**
	 * 转入活期有奖励产品
	 */
	@RequestMapping(value = "transferInvest", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<TradeOrderRep> transferInvest(@RequestBody @Valid RedeemInvestTradeOrderReq tradeOrderReq) {
		String uid = this.getLoginUser();

		log.info("transferInvest uid={}, startTime={}", uid, DateUtil.getSqlCurrentDate());
		TradeOrderRep rep = this.investorInvestTradeOrderExtService.transferInvest(tradeOrderReq, uid);
		log.info("transferInvest uid={}, endTime={}", uid, DateUtil.getSqlCurrentDate());
		return new ResponseEntity<TradeOrderRep>(rep, HttpStatus.OK);
	}

	/**
	 * 快定宝赎回页面
	 *
	 * @param tradeOrderReq
	 * @return
	 */
	@RequestMapping(value = "bfPlusToRedeem", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<TradeOrderBFPlusRep> bfPlusToRedeem(@RequestBody @Valid TradeOrderBFPlusReq bfPlusReq) {
		String uid = this.getLoginUser();
		TradeOrderBFPlusRep rep = this.investorInvestTradeOrderExtService.bfPlusToRedeem(uid, bfPlusReq);
		return new ResponseEntity<>(rep, HttpStatus.OK);
	}

	/**
	 * 快定宝赎回
	 *
	 * @param redeemTradeOrderReq
	 * @return
	 */
	@RequestMapping(value = "bfPlusRedeem", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<TradeOrderRep> bfPlusRedeem(@RequestBody @Valid PlusRedeemTradeOrderReq redeemTradeOrderReq) {
		redeemTradeOrderReq.setOrderType(InvestorTradeOrderEntity.TRADEORDER_orderType_bfPlusRedeem);
		log.info("<------------赎回请求:{}--------------->", redeemTradeOrderReq);
		String uid = this.getLoginUser();
		redeemTradeOrderReq.setUid(uid);
		FirstInvestRes firstRes = new FirstInvestRes();
		FirstInvestReq firstReq = new FirstInvestReq();
		firstReq.setUserOid(redeemTradeOrderReq.getUid());
		firstInvestService.firstInvest(firstReq, firstRes);
		TradeOrderRep rep = new TradeOrderRep();
		long investTimes = firstInvestService.investTimes(firstReq);
		if (investTimes == 0L) {
			//没有进行过投资,不能提现
			rep.setErrorCode(-1);
			rep.setErrorMessage(firstRes.getErrorMessage());
		} else {
			rep = this.investorInvestTradeOrderExtService.plusRedeem(redeemTradeOrderReq);
		}
		log.info("<------------赎回返回:{}--------------->", rep);
		return new ResponseEntity<TradeOrderRep>(rep, HttpStatus.OK);
	}

	/**
	 * 快活宝赎回
	 *
	 * @param redeemTradeOrderReq
	 * @return
	 */
	@RequestMapping(value = "redeem", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<TradeOrderRep> redeem(@RequestBody @Valid RedeemTradeOrderReq redeemTradeOrderReq) {
		log.info("<------------赎回请求:{}--------------->", redeemTradeOrderReq);
		String uid = this.getLoginUser();
		redeemTradeOrderReq.setUid(uid);
		FirstInvestRes firstRes = new FirstInvestRes();
		FirstInvestReq firstReq = new FirstInvestReq();
		firstReq.setUserOid(redeemTradeOrderReq.getUid());
		firstInvestService.firstInvest(firstReq, firstRes);
		TradeOrderRep rep = new TradeOrderRep();
		long investTimes = firstInvestService.investTimes(firstReq);
		if (investTimes == 0L) {
			//没有进行过投资,不能提现
			rep.setErrorCode(-1);
			rep.setErrorMessage(firstRes.getErrorMessage());
		} else {
			rep = this.investorInvestTradeOrderExtService.redeem(redeemTradeOrderReq);
		}
		log.info("<------------赎回返回:{}--------------->", rep);
		return new ResponseEntity<TradeOrderRep>(rep, HttpStatus.OK);
	}

	//节节高赎回
	@RequestMapping(value = "incrementRedeem", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<TradeOrderRep> incrementRedeem(@RequestBody @Valid RedeemTradeOrderReq redeemTradeOrderReq) {
		log.info("<------------有奖励活期产品赎回请求:{}--------------->", redeemTradeOrderReq);
		String uid = this.getLoginUser();
		redeemTradeOrderReq.setUid(uid);
		FirstInvestRes firstRes = new FirstInvestRes();
		FirstInvestReq firstReq = new FirstInvestReq();
		firstReq.setUserOid(redeemTradeOrderReq.getUid());
		firstInvestService.firstInvest(firstReq, firstRes);
		TradeOrderRep rep = new TradeOrderRep();
		long investTimes = firstInvestService.investTimes(firstReq);
		if (investTimes == 0L) {
			//没有进行过投资,不能提现
			rep.setErrorCode(-1);
			rep.setErrorMessage(firstRes.getErrorMessage());
		} else {
			rep = this.investorInvestTradeOrderExtService.incrementRedeem(redeemTradeOrderReq);
		}
		log.info("<------------有奖励活期产品赎回返回:{}--------------->", rep);
		return new ResponseEntity<TradeOrderRep>(rep, HttpStatus.OK);
	}

	@RequestMapping(value = "isdone", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> isDone(@RequestBody @Valid TradeOrderIsDoneReq isDone) {
		this.getLoginUser();

		BaseRep rep = this.investorTradeOrderService.isDone(isDone);
		return new ResponseEntity<BaseRep>(rep, HttpStatus.OK);
	}

	/**
	 * 我的快活宝-持有中
	 */
	@RequestMapping(value = "mng", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<PagesRep<TradeOrderQueryRep>> mng(HttpServletRequest request,
															@And({@Spec(params = "orderType", path = "orderType", spec = In.class),
																	@Spec(params = "orderTimeBegin", path = "orderTime", spec = DateAfterInclusive.class, config = DateUtil.defaultDatePattern),
																	@Spec(params = "orderTimeEnd", path = "orderTime", spec = DateBeforeInclusive.class, config = DateUtil.defaultDatePattern),
																	@Spec(params = "productOid", path = "product.oid", spec = Equal.class),
																	@Spec(params = "productType", path = "product.type.oid", spec = Equal.class)}) Specification<InvestorTradeOrderEntity> spec,
															@RequestParam int page,
															@RequestParam int rows,
															@RequestParam(required = false, defaultValue = "createTime") String sort,
															@RequestParam(required = false, defaultValue = "desc") String order) {
		Direction sortDirection = Direction.DESC;
		if (!"desc".equals(order)) {
			sortDirection = Direction.ASC;
		}

		final String uid = this.getLoginUser();
		Specification<InvestorTradeOrderEntity> uidspec = new Specification<InvestorTradeOrderEntity>() {
			@Override
			public Predicate toPredicate(Root<InvestorTradeOrderEntity> root, CriteriaQuery<?> query,
										 CriteriaBuilder cb) {
				return cb.equal(root.get("investorBaseAccount").get("oid").as(String.class), uid);
			}
		};
		spec = Specifications.where(uidspec).and(spec);

		Pageable pageable = new PageRequest(page - 1, rows, new Sort(new Order(sortDirection, sort)));
		PagesRep<TradeOrderQueryRep> rep = this.investorTradeOrderService.investorTradeOrderMng(spec, pageable);
		return new ResponseEntity<PagesRep<TradeOrderQueryRep>>(rep, HttpStatus.OK);
	}

	/**
	 * @param req
	 * @return ResponseEntity<TradeDetailRep   <   Map   <   String   ,   Object>>>
	 * @author yihonglei
	 * @Title: tradeDetail
	 * @Description: app和pc交易明细查询
	 * @date 2017年9月4日 下午5:11:50
	 * @since 1.0.0
	 */
	@RequestMapping(value = "tradeDetail", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<TradeDetailRep<Map<String, Object>>> tradeDetail(@Valid @RequestBody TradeDetailReq req) {
		log.info("交易明细查询请求参数:{}", JSONObject.toJSONString(req));
		String uid = this.getLoginUser();
		TradeDetailRep<Map<String, Object>> res = this.investorTradeOrderService.tradeOrderMng(req, uid);
		return new ResponseEntity<TradeDetailRep<Map<String, Object>>>(res, HttpStatus.OK);
	}

	/**
	 * @param req
	 * @return ResponseEntity<Map   <   String   ,   Object>>
	 * @author yihonglei
	 * @Title: orderDetail
	 * @Description:app和pc根据订单号查询订单详情
	 * @date 2017年9月4日 下午9:15:34
	 * @since 1.0.0
	 */
	@RequestMapping(value = "orderDetail", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<Map<String, Object>> orderDetail(@Valid @RequestBody TradeDetailReq req) {
		log.info("订单详情查询请求参数:{}", JSONObject.toJSONString(req));
		Map<String, Object> res = this.investorTradeOrderService.orderDetail(req);
		return new ResponseEntity<Map<String, Object>>(res, HttpStatus.OK);
	}


	@RequestMapping(value = "/firstinvest")
	public ResponseEntity<FirstInvestRes> firstinvest(@Valid @RequestBody FirstInvestReq req) {
		FirstInvestRes res = new FirstInvestRes();
		firstInvestService.firstInvest(req, res);
		return new ResponseEntity<FirstInvestRes>(res, HttpStatus.OK);
	}

	/**
	 * 创建特殊赎回订单
	 */
	@RequestMapping(value = "specialRedeem", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<TradeOrderRep> specialRedeem(@RequestBody @Valid SpecialRedeemOrderReq specialRedeemOrderReq) {
//		String loginUser = this.getLoginUserName();
		String operator = super.getLoginUser();
		specialRedeemOrderReq.setLoginUser(operator);
		TradeOrderRep rep = this.investorInvestTradeOrderExtService.specialRedeem(specialRedeemOrderReq);
		return new ResponseEntity<TradeOrderRep>(rep, HttpStatus.OK);
	}

	@RequestMapping(value = "isorderdone", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<MyOrderStatusRes> isOrderAllDone(String uid) {
		MyOrderStatusRes res = null;
		if (uid == null) {
			log.error("查询用户订单是否完结时用户传入为空！");
			res = new MyOrderStatusRes();
			res.setIsOrderAllDone(false);
		} else {
			res = investorTradeOrderService.isOrderAllDone(uid);
		}
		return new ResponseEntity<MyOrderStatusRes>(res, HttpStatus.OK);
	}

	@RequestMapping(value = "isFirstTn", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<FirstTnOrderRes> isFirstTn(String uid) {
		FirstTnOrderRes res = new FirstTnOrderRes();
		res.setIsFirstTn(investorTradeOrderService.isFirstTn(uid));
		return new ResponseEntity<FirstTnOrderRes>(res, HttpStatus.OK);
	}

	@RequestMapping(value = "kdbOrderSetContinue", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<BaseRep> kdbOrderSetContinue(@RequestBody OrderContinueStatusReq req) {
		String uid = this.getLoginUser();
		return new ResponseEntity<>(investorTradeOrderService.changeOrderContinueStatus(uid, req.getOrderCode(), req.getIsContinue()), HttpStatus.OK);
	}

	/**
	 * 根据订单oid获取债权匹配信息
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "p2pAssetPackageDetail", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<P2PCreditorDetailsRep> p2pAssetPackageDetail(@RequestBody P2PCreditorDetailsReq req) {
		String uid = this.getLoginUser();
		P2PCreditorDetailsRep rep = investorTradeOrderService.findCreditorDetailByUidAndOrderOid(uid, req);
		return new ResponseEntity<>(rep, HttpStatus.OK);
	}
}
