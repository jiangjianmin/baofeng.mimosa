package com.guohuai.mmp.platform.payment;

import com.guohuai.mmp.investor.tradeorder.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.annotations.JsonAdapter;
import com.guohuai.basic.common.StringUtil;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.util.StrRedisUtil;
import com.guohuai.component.version.VersionUtils;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.tradeorder.check.InvestorRefundTradeOrderService;
import com.guohuai.mmp.platform.accment.AccParam;
import com.guohuai.mmp.platform.payment.log.PayInterface;
import com.guohuai.mmp.platform.payment.log.PayLogEntity;
import com.guohuai.mmp.platform.payment.log.PayLogReq;
import com.guohuai.mmp.platform.payment.log.PayLogService;
import com.guohuai.settlement.api.SettlementCallBackApi;
import com.guohuai.settlement.api.SettlementSdk;
import com.guohuai.settlement.api.request.OrderRequest;
import com.guohuai.settlement.api.request.WriterOffOrderRequest;
import com.guohuai.settlement.api.response.BaseResponse;
import com.guohuai.settlement.api.response.OrderResponse;
import com.guohuai.settlement.api.response.WriteOffResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * the third party payment notification implemented service
 * @author Jeffrey.Wong
 * 2015年7月22日下午1:45:02
 */
@Service
@Slf4j
public class PaymentServiceImpl implements SettlementCallBackApi {
	
	
	@Autowired
	private PayLogService payLogService;
	@Autowired
	private SettlementSdk settlementSdk;
	@Autowired
	private InvestorInvestTradeOrderExtService investorInvestTradeOrderExtService;
	@Autowired
	private InvestorRedeemTradeOrderService investorRedeemTradeOrderService;
	@Autowired
	private InvestorTradeOrderService investorTradeOrderService;
	@Autowired
	private InvestorRefundTradeOrderService investorRefundTradeOrderService;
	@Autowired
	private RedisTemplate<String, String> redis;
	
	public static final String PAY_orderCode = "p:o:c:";
	
	public BaseRep queryPay(QueryPayRequest ireq) {
		BaseRep irep = new BaseRep();
		OrderRequest oreq = new OrderRequest();
		oreq.setOrderNo(ireq.getOrderNo());
		oreq.setUserOid(ireq.getUserOid());

		OrderResponse orep = new OrderResponse();
		try {
			orep = this.settlementSdk.queryPay(oreq);
		} catch (Exception e) {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(AMPException.getStacktrace(e));

		}

		this.setIrep(irep, orep);

		writeLog(ireq, irep, PayInterface.queryPay.getInterfaceName());
		return irep;

	}
	
	public BaseRep investPay(PayRequest payRequest) {
		
		RemarkReq remarkReq = new RemarkReq();
		remarkReq.setVersion(VersionUtils.getVersion());
		
		log.info("JSON.toJSONString(remarkReq):{}", JSON.toJSONString(remarkReq));
		payRequest.setDescribe("invest describe");
		payRequest.setRemark(JSON.toJSONString(remarkReq));
		payRequest.setType(PayParam.PayType.INVEST.toString());
		return this.pay(payRequest, true);
	}

	public TradeOrderGatewayRep investPayThroughMode(PayRequest payRequest) {

		RemarkReq remarkReq = new RemarkReq();
		remarkReq.setVersion(VersionUtils.getVersion());

		log.info("JSON.toJSONString(remarkReq):{}", JSON.toJSONString(remarkReq));
		payRequest.setDescribe("invest describe");
		payRequest.setRemark(JSON.toJSONString(remarkReq));
		payRequest.setType(PayParam.PayType.INVEST.toString());
		return this.payThroughMode(payRequest, true);
	}
	
	
	public BaseRep redeemPay(RedeemPayRequest payRequest) {
		
		payRequest.setDescribe("redeem describe");
		payRequest.setRemark("");
		payRequest.setType(PayParam.PayType.REDEEM.toString());
		
		return this.payee(payRequest, true);
	}
	
	public BaseRep specialRedeemPay(RedeemPayRequest payRequest) {
		
		payRequest.setDescribe("redeem describe");
		payRequest.setRemark("");
		payRequest.setType(PayParam.PayType.SPECIALREDEEM.toString());
		
		return this.payee(payRequest, true);
	}
	
	public BaseRep incrementRedeemPay(RedeemPayRequest payRequest) {
		
		payRequest.setDescribe("redeem describe");
		payRequest.setRemark("");
		payRequest.setType(PayParam.PayType.INCREMENTREDEEM.toString());
		
		return this.payee(payRequest, true);
	}
	
	public BaseRep refundPay(RedeemPayRequest payRequest) {
		
		payRequest.setDescribe("refund describe");
		payRequest.setRemark("");
		payRequest.setType(PayParam.PayType.REDEEM.toString());
		
		return this.payee(payRequest, true);
	}
	
	/**
	 * 赎回
	 */
	public BaseRep payee(RedeemPayRequest ireq, boolean isLog) {
		log.info("======正常赎回(normalRedeem)开始======ireq:" + JSONObject.toJSONString(ireq));
		BaseRep irep = new BaseRep();
		
		OrderRequest oreq = new OrderRequest();
		oreq.setUserOid(ireq.getUserOid());
		oreq.setOrderNo(ireq.getOrderNo());
		oreq.setAmount(DecimalUtil.setScaleDown(ireq.getAmount()));
		oreq.setFee(ireq.getFee());
		oreq.setDescribe(ireq.getDescribe());
		oreq.setRemark(ireq.getRemark());
		oreq.setType(ireq.getType());
		oreq.setInAcctProvinceCode(ireq.getProvince());
		oreq.setInAcctCityName(ireq.getCity());
		oreq.setSystemSource(PayParam.SystemSource.MIMOSA.toString());
		oreq.setRequestNo(StringUtil.uuid());
		oreq.setOrderCreateTime(ireq.getOrderTime());
		oreq.setPayDate(ireq.getPayDate());
		
		OrderResponse orep = new OrderResponse();
	
		try {
			log.info("========正常赎回(normalRedeem)调用结算payee方法请求参数========oreq:" + JSONObject.toJSONString(oreq));
			orep = settlementSdk.payee(oreq);
			log.info("========正常赎回(normalRedeem)调用结算payee方法同步返回结果========orep:" + JSONObject.toJSONString(orep));
		} catch (Exception e) {
			log.info("payee,error:" , e);
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(AMPException.getStacktrace(e));
		}
		
		this.setIrep(irep, orep);
		if (isLog) {
			writeLog(ireq, irep, PayInterface.payee.getInterfaceName());
		}
		
		log.info("======正常赎回(normalRedeem)结束======ireq:" + JSONObject.toJSONString(ireq));
		return irep;
	}

	/**
	 * 投资
	 */
	public BaseRep pay(PayRequest ireq, boolean isLog) {
		BaseRep irep = new BaseRep();
		
		OrderRequest oreq = new OrderRequest();
		oreq.setUserOid(ireq.getUserOid());
		oreq.setOrderNo(ireq.getOrderNo());
		oreq.setAmount(DecimalUtil.setScaleDown(ireq.getAmount()));
		oreq.setFee(ireq.getFee());
		oreq.setDescribe(ireq.getDescribe());
		oreq.setRemark(ireq.getRemark());
		oreq.setType(ireq.getType());
		oreq.setSystemSource(PayParam.SystemSource.MIMOSA.toString());
		oreq.setRequestNo(StringUtil.uuid());
		oreq.setOrderCreateTime(ireq.getOrderTime());
		
		OrderResponse orep = new OrderResponse();
		try {
			log.info(JSONObject.toJSONString(oreq));
			if (PayParam.PayType.INVEST.toString().equals(ireq.getType())) {
				log.info("orderCode={}, startTime={}, remark={}", ireq.getOrderNo(), DateUtil.getSqlCurrentDate(), ireq.getRemark());
				orep = settlementSdk.pay(oreq);
				log.info("orderCode={}, endTime={}, remark={}", ireq.getOrderNo(), DateUtil.getSqlCurrentDate(), ireq.getRemark());
			} 
			
		} catch (Exception e) {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(AMPException.getStacktrace(e));
			
		}
		
		this.setIrep(irep, orep);
		if (isLog) {
			writeLog(ireq, irep, PayInterface.pay.getInterfaceName());
		}
		
		return irep;
	}

	/**
	 * 可选支付方式投资
	 */
	public TradeOrderGatewayRep payThroughMode(PayRequest ireq, boolean isLog) {
		TradeOrderGatewayRep rep = new TradeOrderGatewayRep();

		OrderRequest oreq = new OrderRequest();
		oreq.setUserOid(ireq.getUserOid());
		oreq.setOrderNo(ireq.getOrderNo());
		oreq.setAmount(DecimalUtil.setScaleDown(ireq.getAmount()));
		oreq.setFee(ireq.getFee());
		oreq.setDescribe(ireq.getDescribe());
		oreq.setRemark(ireq.getRemark());
		oreq.setType(ireq.getType());
		oreq.setSystemSource(PayParam.SystemSource.MIMOSA.toString());
		oreq.setRequestNo(StringUtil.uuid());
		oreq.setOrderCreateTime(ireq.getOrderTime());
		oreq.setPaymentMode(ireq.getPaymentMode());
		oreq.setBankCode(ireq.getBankCode());

		OrderResponse orep = new OrderResponse();
		try {
			log.info(JSONObject.toJSONString(oreq));
			if (PayParam.PayType.INVEST.toString().equals(ireq.getType())) {
				log.info("orderCode={}, startTime={}, remark={}", ireq.getOrderNo(), DateUtil.getSqlCurrentDate(), ireq.getRemark());
				orep = settlementSdk.pay(oreq);
				log.info("orderCode={}, endTime={}, remark={}", ireq.getOrderNo(), DateUtil.getSqlCurrentDate(), ireq.getRemark());
			}

		} catch (Exception e) {
			rep.setErrorCode(BaseRep.ERROR_CODE);
			rep.setErrorMessage(AMPException.getStacktrace(e));

		}

		this.setIrepForGateway(rep, orep);
		if (isLog) {
			writeLog(ireq, rep, PayInterface.pay.getInterfaceName());
		}

		return rep;
	}
	
	private void writeLog(String content, String orderCode, String handleType, BaseRep irep, String interfaceName) {
		PayLogReq logReq = new PayLogReq();
		logReq.setErrorCode(irep.getErrorCode());
		logReq.setErrorMessage(irep.getErrorMessage());
		logReq.setInterfaceName(interfaceName);
		logReq.setSendedTimes(1);
		logReq.setContent(content);
		logReq.setOrderCode(orderCode);
		logReq.setHandleType(handleType);
		this.payLogService.createEntity(logReq);
	}
	
	private void writeLog(QueryPayRequest ireq, BaseRep irep, String interfaceName) {
		this.writeLog(JSONObject.toJSONString(ireq), ireq.getOrderNo(), PayLogEntity.PAY_handleType_query, irep, interfaceName);
	}

	private void writeLog(PayRequest ireq, BaseRep irep, String interfaceName) {
		this.writeLog(JSONObject.toJSONString(ireq), ireq.getOrderNo(), PayLogEntity.PAY_handleType_applyCall, irep, interfaceName);
	}
	
	private void writeLog(RedeemPayRequest ireq, BaseRep irep, String interfaceName) {
		this.writeLog(JSONObject.toJSONString(ireq), ireq.getOrderNo(), PayLogEntity.PAY_handleType_applyCall, irep, interfaceName);
	}
	
	private void setIrep(BaseRep irep, BaseResponse orep) {
		
		/** 调用接口异常 */
		if (BaseRep.ERROR_CODE == irep.getErrorCode()) {
			return;
		}

		/** orep == null, 当接口返回为NULL时 */
		if (null == orep) {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage("返回为空");
			return;
		}

		if (AccParam.ReturnCode.RC0000.toString().equals(orep.getReturnCode())) {
			irep.setErrorMessage(orep.getErrorMessage());
		} else {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(orep.getErrorMessage() + "(" + orep.getReturnCode() + ")");
		}
	}

	private void setIrepForGateway(TradeOrderGatewayRep irep, OrderResponse orep) {

		/** 调用接口异常 */
		if (BaseRep.ERROR_CODE == irep.getErrorCode()) {
			return;
		}

		/** orep == null, 当接口返回为NULL时 */
		if (null == orep) {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage("返回为空");
			return;
		}

		if (AccParam.ReturnCode.RC0000.toString().equals(orep.getReturnCode())) {
			irep.setErrorMessage(orep.getErrorMessage());
			irep.setGatewayForm(orep.getRespHtml());
		} else {
			irep.setErrorCode(BaseRep.ERROR_CODE);
			irep.setErrorMessage(orep.getErrorMessage() + "(" + orep.getReturnCode() + ")");
			irep.setGatewayForm(orep.getRespHtml());
		}
	}
	
	
	@Override
	public boolean tradeCallback(OrderResponse orderResponse) {
		log.info("<------------结算回调:{}----------------->",JSONObject.toJSONString(orderResponse));
		Assert.notNull(orderResponse, "orderResponse is null");
		if (StringUtil.isEmpty(orderResponse.getOrderNo()) || StringUtil.isEmpty(orderResponse.getReturnCode())) {
			throw new IllegalArgumentException("orderNo is empty or returnCode is empty");
		}
		
		if (orderResponse.getRemark() != null && !"".equals(orderResponse.getRemark())) {// app不为空，非app为空
			RemarkReq remark = JSON.parseObject(orderResponse.getRemark(), RemarkReq.class);
			if (remark != null) {
				String version = remark.getVersion();
				log.info("<------------结算-回调-version:{}", version);
				StrRedisUtil.set(redis, StrRedisUtil.ORDERCODE_VERSION + orderResponse.getOrderNo(), version);
			}
		}
		
		OrderNotifyReq ireq = new OrderNotifyReq();
		ireq.setOrderCode(orderResponse.getOrderNo());
		ireq.setReturnCode(orderResponse.getReturnCode());
		ireq.setErrorMessage(orderResponse.getErrorMessage());
		ireq.setPayChannel(orderResponse.getChannelNo());
		return this.tradeCallback(ireq);
	}

	
	public boolean tradeCallback(OrderNotifyReq ireq) {
		if (!StrRedisUtil.setnx(redis, PAY_orderCode + ireq.getOrderCode(), ireq.getOrderCode())) {
			log.info("orderCode={} is dealing ....", ireq.getOrderCode());
			return false;
		}

		boolean flag = true;
		/** 判断申请是否成功 */
		PayLogEntity isHasApplyCallSuccLog = this.payLogService.getSuccessPayAplly(ireq.getOrderCode());

		if (null == isHasApplyCallSuccLog) {
			StrRedisUtil.del(redis, PAY_orderCode + ireq.getOrderCode());
			log.info("orderCode={} have not success apply ", ireq.getOrderCode());
			return false;
		}

		/** 判断是否已成功回调 */
		PayLogEntity isHasCallBackSuccLog = this.payLogService.getPayNotify(ireq.getOrderCode());
		if (null != isHasCallBackSuccLog) {
			StrRedisUtil.del(redis, PAY_orderCode + ireq.getOrderCode());
			log.info("orderCode={} already callback ", ireq.getOrderCode());
			return true;
		}

		InvestorTradeOrderEntity orderEntity = this.investorTradeOrderService.findByOrderCode(ireq.getOrderCode());
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_invest.equals(orderEntity.getOrderType())) {
			try {
				if (!InvestorTradeOrderEntity.TRADEORDER_orderStatus_toPay.equals(orderEntity.getOrderStatus())) {
					StrRedisUtil.del(redis, PAY_orderCode + ireq.getOrderCode());
					log.info("orderCode={} have not toPay", ireq.getOrderCode());
					return false;
				} else {
					log.info("<--------------投资回调订单号:{}--------------------->",ireq.getOrderCode());
					flag = investorInvestTradeOrderExtService.investCallBack(ireq);
				}
			} catch (Exception e) {
				log.error("投资回调异常", e);
				flag = false;
			}
		}

		if (InvestorTradeOrderEntity.TRADEORDER_orderType_refund.equals(orderEntity.getOrderType())) {
			try {
				log.info("<--------------退款回调订单号:{}--------------------->",ireq.getOrderCode());
				flag = investorRefundTradeOrderService.refundCallback(ireq);
			} catch (Exception e) {
				log.error("退款回调异常", e);
				flag = false;
			}
		}

		if (InvestorTradeOrderEntity.TRADEORDER_orderType_normalRedeem.equals(orderEntity.getOrderType())
			|| InvestorTradeOrderEntity.TRADEORDER_orderType_incrementRedeem.equals(orderEntity.getOrderType())) {
			try {
				if (!InvestorTradeOrderEntity.TRADEORDER_orderStatus_confirmed.equals(orderEntity.getOrderStatus())) {
					StrRedisUtil.del(redis, PAY_orderCode + ireq.getOrderCode());
					log.info("orderCode={} have not confirmed", ireq.getOrderCode());
					return false;
				} else {
					log.info("<--------------普赎回调订单号:{}--------------------->",ireq.getOrderCode());
					flag = this.investorRedeemTradeOrderService.redeemCallback(ireq);
				}

			} catch (Exception e) {
				log.error("赎回回调异常", e);
				flag = false;
			}
		}
		if (InvestorTradeOrderEntity.TRADEORDER_orderType_specialRedeem.equals(orderEntity.getOrderType())) {
			try {
				if (!InvestorTradeOrderEntity.TRADEORDER_orderStatus_confirmed.equals(orderEntity.getOrderStatus())) {
					StrRedisUtil.del(redis, PAY_orderCode + ireq.getOrderCode());
					log.info("specialRedeem orderCode={} have not confirmed", ireq.getOrderCode());
					return false;
				} else {
					log.info("<--------------特殊赎回回调订单号:{}--------------------->",ireq.getOrderCode());
					flag = this.investorRedeemTradeOrderService.redeemCallback(ireq);
				}

			} catch (Exception e) {
				log.error("特殊赎回回调异常", e);
				flag = false;
			}
		}

		PayLogReq logReq = new PayLogReq();
		logReq.setErrorCode(
				PayParam.ReturnCode.RC0000.toString().equals(ireq.getReturnCode()) ? 0 : BaseRep.ERROR_CODE);
		logReq.setErrorMessage(ireq.getErrorMessage());
		logReq.setInterfaceName(PayInterface.tradeCallback.getInterfaceName());
		logReq.setOrderCode(ireq.getOrderCode());
		logReq.setSendedTimes(1);
		logReq.setContent(JSONObject.toJSONString(ireq));
		logReq.setHandleType(PayLogEntity.PAY_handleType_notify);
		this.payLogService.createEntity(logReq);

		StrRedisUtil.del(redis, PAY_orderCode + ireq.getOrderCode());
		log.info("<------------------订单号:{},结算回调处理结果:{}--------------------->",ireq.getOrderCode(),flag);
		return flag;
	}




	@Override
	public BaseResponse writerOffOrder(WriterOffOrderRequest req) {
		log.info(JSONObject.toJSONString(req));
		WriteOffResponse rep = new WriteOffResponse();
		Assert.notNull(req);
		Assert.notNull(req.getOriginalRedeemOrderCode());
		try {
			InvestorTradeOrderEntity orderEntity = this.investorTradeOrderService.findByOrderCode(req.getOriginalRedeemOrderCode());
			if (InvestorTradeOrderEntity.TRADEORDER_orderStatus_submitted.equals(orderEntity.getOrderStatus())) {
				rep.setReturnCode("0001");
				rep.setErrorMessage("订单已提交，暂不能撤消");
				return rep;
			}
			if (InvestorTradeOrderEntity.TRADEORDER_orderStatus_accepted.equals(orderEntity.getOrderStatus())) {
				rep.setReturnCode("0002");
				rep.setErrorMessage("订单已受理，暂不能撤消");
				return rep;
			}
			if (InvestorTradeOrderEntity.TRADEORDER_orderStatus_done.equals(orderEntity.getOrderStatus())) {
				rep.setReturnCode("0003");
				rep.setErrorMessage("订单已完成，不能撤消");
				return rep;
			}
			if (InvestorTradeOrderEntity.TRADEORDER_orderStatus_refused.equals(orderEntity.getOrderStatus())) {
				rep.setReturnCode("0004");
				rep.setErrorMessage("订单已拒绝，不能撤消");
				return rep;
			}
			if (!InvestorTradeOrderEntity.TRADEORDER_orderStatus_confirmed.equals(orderEntity.getOrderStatus())) {
				rep.setReturnCode("0005");
				rep.setErrorMessage("订单尚未确认，不能撤消");
			}
			//todo：线上数据怎么
//			orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_payFailed);
			//---------修改冲销单并发，幂等性问题------2017.03.31----
			//update T_MONEY_INVESTOR_TRADEORDER set orderStatus = 'payFailed' where orderCode = ?1 and orderStatus != 'payFailed'
			boolean is= investorTradeOrderService.fetchLock(req.getOriginalRedeemOrderCode());
			if(!is){//更新失败，获取锁失败，直接返回成功
				rep.setReturnCode(PayParam.ReturnCode.RC0000.toString());
				rep.setErrorMessage("撤消成功,用户仓位已恢复");
				return rep;
			}
			//---------修改冲销单并发，幂等性问题------2017.03.31----
			
//			orderEntity.setOrderStatus(InvestorTradeOrderEntity.TRADEORDER_orderStatus_abandoned);
			
			TradeOrderReq tradeOrderReq = new TradeOrderReq();
			tradeOrderReq.setProductOid(orderEntity.getProduct().getOid());
			tradeOrderReq.setMoneyVolume(orderEntity.getOrderAmount());
			tradeOrderReq.setUid(orderEntity.getInvestorBaseAccount().getUserOid());
			tradeOrderReq.setOrderOid(orderEntity.getOid());
			TradeOrderRep iRep = investorInvestTradeOrderExtService.writerOffOrder(tradeOrderReq);
			if (0 == iRep.getErrorCode()) {
				rep.setReturnCode(PayParam.ReturnCode.RC0000.toString());
				rep.setErrorMessage("撤消成功,用户仓位已恢复");
				
			} else {
				rep.setReturnCode("0001");
				rep.setErrorMessage(iRep.getErrorMessage());
			}
			
		} catch (Exception e) {
			rep.setReturnCode("0001");
			rep.setErrorMessage(e.getMessage());
		}
		
		return rep;
	}

	
	

}
