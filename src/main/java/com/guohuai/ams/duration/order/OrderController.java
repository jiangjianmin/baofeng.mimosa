package com.guohuai.ams.duration.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.guohuai.ams.duration.capital.CapitalEntity;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.web.view.Response;

/**
 * 存续期--订单视图入口
 * @author star.zhu
 * 2016年5月17日
 */
@RestController
@RequestMapping(value = "/mimosa/duration/order", produces = "application/json;charset=utf-8")
public class OrderController extends BaseController {
	
	@Autowired
	private OrderService orderService;

	/**
	 * 货币基金（现金管理工具）申购
	 * @param form
	 * @param type
	 * 			申购方式：assetPool（资产池）；order（订单）
	 * @return
	 */
	@RequestMapping(value = "/purchaseForFund", name = "资产池 - 货币基金 - 申购", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> purchaseForFund(FundForm form, String type) {
		orderService.purchaseForFund(form, super.getLoginUser(), type);
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 货币基金（现金管理工具）申购审核
	 * @param form
	 */
	@RequestMapping(value = "/auditForPurchase", name = "资产池 - 货币基金 - 申购审核", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> auditForPurchase(FundForm form) {
		orderService.auditForPurchase(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 货币基金（现金管理工具）申购预约
	 * @param form
	 */
	@RequestMapping(value = "/appointmentForPurchase", name = "资产池 - 货币基金 - 申购预约", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> appointmentForPurchase(FundForm form) {
		orderService.appointmentForPurchase(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 货币基金（现金管理工具）申购确认
	 * @param form
	 */
	@RequestMapping(value = "/orderConfirmForPurchase", name = "资产池 - 货币基金 - 申购确认", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> orderConfirmForPurchase(FundForm form) {
		orderService.orderConfirmForPurchase(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 货币基金（现金管理工具）赎回
	 * @param from
	 */
	@RequestMapping(value = "/redeem", name = "资产池 - 货币基金 - 赎回", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> redeem(FundForm form) {
		orderService.redeem(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 货币基金（现金管理工具）赎回审核
	 * @param form
	 */
	@RequestMapping(value = "/auditForRedeem", name = "资产池 - 货币基金 - 赎回审核", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> auditForRedeem(FundForm form) {
		orderService.auditForRedeem(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 货币基金（现金管理工具）赎回预约
	 * @param form
	 */
	@RequestMapping(value = "/appointmentForRedeem", name = "资产池 - 货币基金 - 赎回预约", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> appointmentForRedeem(FundForm form) {
		orderService.appointmentForRedeem(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 货币基金（现金管理工具）赎回确认
	 * @param form
	 */
	@RequestMapping(value = "/orderConfirmForRedeem", name = "资产池 - 货币基金 - 赎回确认", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> orderConfirmForRedeem(FundForm form) {
		orderService.orderConfirmForRedeem(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 信托（计划）申购
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/purchaseForTrust", name = "资产池 - 信托计划 - 申购", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> purchaseForTrust(TrustForm form) {
		orderService.purchaseForTrust(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 信托（计划）申购 审核
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/auditForTrust", name = "资产池 - 信托计划 - 申购审核", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> auditForTrust(TrustForm form) {
		orderService.auditForTrust(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 信托（计划）申购 预约
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/appointmentForTrust", name = "资产池 - 信托计划 - 申购预约", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> appointmentForTrust(TrustForm form) {
		orderService.appointmentForTrust(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 信托（计划）申购 确认
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/orderConfirmForTrust", name = "资产池 - 信托计划 - 申购确认", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> orderConfirmForTrust(TrustForm form) {
		orderService.orderConfirmForTrust(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 信托（计划）转入申购
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/purchaseForTrans", name = "资产池 - 信托计划 - 转入申购", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> purchaseForTrans(TransForm form) {
		orderService.purchaseForTrans(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 信托（计划）转入 审核
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/auditForTrans", name = "资产池 - 信托计划 - 转入审核", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> auditForTrans(TrustForm form) {
		orderService.auditForTrans(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 信托（计划）转入 预约
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/appointmentForTrans", name = "资产池 - 信托计划 - 转入预约", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> appointmentForTrans(TrustForm form) {
		orderService.appointmentForTrans(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	/**
	 * 信托（计划）转入 确认
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/orderConfirmForTrans", name = "资产池 - 信托计划 - 转入确认", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> orderConfirmForTrans(TrustForm form) {
		orderService.orderConfirmForTrans(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 信托（计划）本息兑付
	 * @param from
	 */
	@RequestMapping(value = "/applyForIncome", name = "资产池 - 信托计划 - 本息兑付", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> applyForIncome(TrustForm form) {
		orderService.applyForIncome(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 信托（计划）本息兑付 审核
	 * @param oid
	 * 			标的oid
	 */
	@RequestMapping(value = "/auditForIncome", name = "资产池 - 信托计划 - 本息兑付审核", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> auditForIncome(TrustForm form) {
		orderService.auditForIncome(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 信托（计划）本息兑付 确认
	 * @param oid
	 * 			标的oid
	 */
	@RequestMapping(value = "/orderConfirmForIncome", name = "资产池 - 信托计划 - 本息兑付确认", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> orderConfirmForIncome(TrustForm form) {
		orderService.orderConfirmForIncome(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 信托（计划）退款
	 * @param from
	 */
	@RequestMapping(value = "/applyForBack", name = "资产池 - 信托计划 - 退款申请", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> applyForBack(TrustForm form) {
		orderService.applyForBack(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 信托（计划）退款 审核
	 * @param oid
	 * 			标的oid
	 */
	@RequestMapping(value = "/auditForBack", name = "资产池 - 信托计划 - 退款审核", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> auditForBack(TrustForm form) {
		orderService.auditForBack(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 信托（计划）退款 确认
	 * @param oid
	 * 			标的oid
	 */
	@RequestMapping(value = "/orderConfirmForBack", name = "资产池 - 信托计划 - 退款确认", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> orderConfirmForBack(TrustForm form) {
		orderService.orderConfirmForBack(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 信托（计划）转让
	 * @param from
	 */
	@RequestMapping(value = "/applyForTransfer", name = "资产池 - 信托计划 - 转入申请", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> applyForTransfer(TrustForm form) {
		orderService.applyForTransfer(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 信托（计划）转让 审核
	 * @param oid
	 * 			标的oid
	 */
	@RequestMapping(value = "/auditForTransfer", name = "资产池 - 信托计划 - 转入审核", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> auditTrust(TrustForm form) {
		orderService.auditForTransfer(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 信托（计划）转让 确认
	 * @param oid
	 * 			标的oid
	 */
	@RequestMapping(value = "/orderConfirmForTransfer", name = "资产池 - 信托计划 - 转入确认", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> orderConfirmForTransfer(TrustForm form) {
		orderService.orderConfirmForTransfer(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 信托（计划）逾期转让
	 * @param from
	 */
	@RequestMapping(value = "/applyForOverdueTransfer", name = "资产池 - 信托计划 - 逾期转让申请", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> applyForOverdueTransfer(TrustForm form) {
		orderService.applyForOverdueTransfer(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 信托（计划）逾期转让 审核
	 * @param oid
	 * 			标的oid
	 */
	@RequestMapping(value = "/auditForOverdueTransfer", name = "资产池 - 信托计划 - 逾期转让审核", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> auditForOverdueTransfer(TrustForm form) {
		orderService.auditForOverdueTransfer(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 信托（计划）逾期转让 确认
	 * @param oid
	 * 			标的oid
	 */
	@RequestMapping(value = "/orderConfirmForOverdueTransfer", name = "资产池 - 信托计划 - 逾期转让确认", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> orderConfirmForOverdueTransfer(TrustForm form) {
		orderService.orderConfirmForOverdueTransfer(form, super.getLoginUser());
		Response r = new Response();
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 货币基金（现金管理工具）的持仓信息
	 * @param oid
	 * 			标的oid
	 */
	@RequestMapping(value = "/getFundByOid", name = "资产池 - 货币基金的持仓信息", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getFundByOid(String oid) {
		FundForm form = orderService.getFundByOid(oid);
		Response r = new Response();
		r.with("result", form);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 货币基金（现金管理工具）的订单信息
	 * @param oid
	 * 			标的oid
	 */
	@RequestMapping(value = "/getFundOrderByOid", name = "资产池 - 货币基金的订单信息", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getFundOrderByOid(String oid) {
		FundForm form = orderService.getFundOrderByOid(oid);
		Response r = new Response();
		r.with("result", form);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 信托（计划） 的持仓信息
	 * @param oid
	 * 			标的oid
	 */
	@RequestMapping(value = "/getTrustByOid", name = "资产池 - 信托计划的持仓信息", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getTrustByOid(String oid, String type) {
		TrustForm form = orderService.getTrustByOid(oid, type);
		Response r = new Response();
		r.with("result", form);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 信托（计划） 的订单信息
	 * @param oid
	 * 			标的oid
	 */
	@RequestMapping(value = "/getTrustOrderByOid", name = "资产池 - 信托计划订单信息", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getTrustOrderByOid(String oid, String type) {
		TrustForm form = orderService.getTrustOrderByOid(oid, type);
		Response r = new Response();
		r.with("result", form);
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 订单信息（资金明细）
	 * @param oid
	 * 			标的oid
	 */
	@RequestMapping(value = "/getTargetOrderByOidForCapital", name = "资产池 - 资金明细", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> getTargetOrderByOidForCapital(String oid, String operation) {
		Response r = new Response();
		if (CapitalEntity.PURCHASE.equals(operation)
				|| CapitalEntity.REDEEM.equals(operation)) {
			FundForm form = orderService.getFundOrderByOid(oid);
			r.with("result", form);
		} else {
			TrustForm form = orderService.getTrustOrderByOid(oid, operation);
			r.with("result", form);
		}
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 逻辑删除订单
	 * @param oid
	 * 		订单oid
	 */
	@RequestMapping(value = "/updateOrder", name = "资产池 - 逻辑删除订单", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> updateOrder(String oid, String operation) {
		Response r = new Response();
		orderService.updateOrder(oid, operation, super.getLoginUser());
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 逻辑作废订单--坏账核销
	 * @param oid
	 * 		订单oid
	 */
	@RequestMapping(value = "/cancelOrder", name = "资产池 - 坏账核销", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> cancelOrder(String oid) {
		Response r = new Response();
		orderService.cancelOrder(oid);
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 纠偏现金管理工具的持有额度
	 * @param form
	 */
	@RequestMapping(value = "/updateFund", name = "资产池 - 纠偏货币基金的持有额度", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> updateFund(FundForm form) {
		Response r = new Response();
		orderService.updateFund(form, super.getLoginUser());
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
	
	/**
	 * 纠偏投资标的的持有额度
	 * @param form
	 */
	@RequestMapping(value = "/updateTrust", name = "资产池 - 纠偏信托计划的持有额度", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> updateTrust(TrustForm form) {
		Response r = new Response();
		orderService.updateTrust(form, super.getLoginUser());
		r.with("result", "SUCCESSED!");
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}
}
