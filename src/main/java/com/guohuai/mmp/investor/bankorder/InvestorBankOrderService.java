package com.guohuai.mmp.investor.bankorder;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.sys.CodeConstants;

@Service
@Transactional
public class InvestorBankOrderService {

	Logger logger = LoggerFactory.getLogger(InvestorBankOrderService.class);

	@Autowired
	private InvestorBaseAccountService investorBaseAccountService;
	@Autowired
	private InvestorBankOrderDao investorBankOrderDao;
	@Autowired
	private SeqGenerator seqGenerator;
	
	/**
	 * 第三方支付充值成功回调
	 */
	public static final String PAYMENT_success = "SUCCESS";


	


	public InvestorBankOrderEntity updateEntity(InvestorBankOrderEntity bankOrder) {
		bankOrder.setUpdateTime(DateUtil.getSqlCurrentDate());
		return this.investorBankOrderDao.save(bankOrder);
	}

	public InvestorBankOrderEntity saveEntity(InvestorBankOrderEntity bankOrder) {
		bankOrder.setCreateTime(DateUtil.getSqlCurrentDate());
		return this.updateEntity(bankOrder);
	}
	
	public InvestorBankOrderEntity findByOrderCodeAndOrderStatusAndOrderType(String orderCode, String orderStatus, String orderType) {
		InvestorBankOrderEntity bankOrder = this.investorBankOrderDao.findByOrderCodeAndOrderStatusAndOrderType(orderCode, orderStatus, orderType);
		if (null == bankOrder) {
			//error.define[80001]=投资人-银行委托单的订单号不存在!(CODE:80001)
			throw new AMPException(80001);
		}
		return bankOrder;
	}
	


	public BaseRep isDone(BankOrderIsDoneReq isDone) {
		BankOrderIsDoneRep rep = new BankOrderIsDoneRep();
		
		InvestorBankOrderEntity bankOrder = this.investorBankOrderDao.findOne(isDone.getBankOrderOid());
		if (null == bankOrder) {
			//error.define[80001]=投资人-银行委托单的订单号不存在!(CODE:80001)
			throw new AMPException(80001);
		}
		if (!InvestorBankOrderEntity.BANKORDER_orderStatus_done.equals(bankOrder.getOrderStatus())) {
			rep.setErrorCode(BaseRep.ERROR_CODE);
		} else {
			rep.setCompleteTime(bankOrder.getCompleteTime());
		}
		return rep;
	}
	
	/**
	 * 创建投资者充值订单
	  * @param {@link BankOrderReq bankOrderReq}
	 * @return {@link InvestorBankOrderEntity bankOrder}
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorBankOrderEntity createDepostBankOrder(BankOrderReq bankOrderReq) {
		InvestorBankOrderEntity bankOrder = new InvestorBankOrderEntity();
		bankOrder.setOrderCode(this.seqGenerator.next(CodeConstants.PAYMENT_deposit));
		bankOrder.setOrderType(InvestorBankOrderEntity.BANKORDER_orderType_deposit);
		//PaymentChannelFee fee = this.paymentChannelService.getInputFee(bankOrderReq.getMoneyVolume());
		return createBankOrder(bankOrder, bankOrderReq);
	}
	/**
	 * 创建投资者提现订单
	 * @param {@link BankOrderReq bankOrderReq}
	 * @return {@link InvestorBankOrderEntity bankOrder}
	 */
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public InvestorBankOrderEntity createWithdrawBankOrder(BankOrderReq bankOrderReq) {
		InvestorBankOrderEntity bankOrder = new InvestorBankOrderEntity();
		bankOrder.setOrderCode(this.seqGenerator.next(CodeConstants.PAYMENT_withdraw));
		bankOrder.setOrderType(InvestorBankOrderEntity.BANKORDER_orderType_withdraw);
		//PaymentChannelFee fee = this.paymentChannelService.getOutputFee(bankOrderReq.getMoneyVolume());
		
		return createBankOrder(bankOrder, bankOrderReq);
	}
	
	/**
	 * 创建投资者充值、体现订单
	 * @param {@link InvestorBankOrderEntity bankOrder}
	 * @param {@link PaymentChannelFee fee}
	 * @param {@link BankOrderReq bankOrderReq}
	 * @return {@link InvestorBankOrderEntity bankOrder}
	 */
	
	private InvestorBankOrderEntity createBankOrder(InvestorBankOrderEntity bankOrder, BankOrderReq bankOrderReq) {
		bankOrder.setInvestorBaseAccount(investorBaseAccountService.findByUid(bankOrderReq.getUid()));
//		bankOrder.setFee(fee.getFee());
//		bankOrder.setFeePayer(fee.getPayer());
		bankOrder.setOrderAmount(bankOrderReq.getMoneyVolume());
		bankOrder.setOrderStatus(InvestorBankOrderEntity.BANKORDER_orderStatus_submitted);
		return  this.saveEntity(bankOrder);
	}
	
	/**
	 * /** 手续费支付方--平台 *
	public static final String BANKORDER_feePayer_platform = "platform";
	/** 手续费支付方--用户 *
	public static final String BANKORDER_feePayer_user = "user";
	 */
	public String feePayerEn2Ch(String feePayer) {
		if (InvestorBankOrderEntity.BANKORDER_feePayer_user.equals(feePayer)) {
			return "用户";
		}
		if (InvestorBankOrderEntity.BANKORDER_feePayer_platform.equals(feePayer)) {
			return "平台";
		}
		
		return feePayer;
	}
	
	/**
	 * /** 交易类型--充值 *
	public static final String BANKORDER_orderType_deposit = "deposit";
	/** 交易类型--提现 *
	public static final String BANKORDER_orderType_withdraw = "withdraw";
	 */
	public String orderTypeEn2Ch(String orderType) {
		if (InvestorBankOrderEntity.BANKORDER_orderType_deposit.equals(orderType)) {
			return "充值";
		}
		if (InvestorBankOrderEntity.BANKORDER_orderType_withdraw.equals(orderType)) {
			return "提现";
		}
		
		return orderType;
	}
	

	public String orderStatusEn2Ch(String orderStatus) {
		if (InvestorBankOrderEntity.BANKORDER_orderStatus_submitted.equals(orderStatus)) {
			return "已提交";
		}
		if (InvestorBankOrderEntity.BANKORDER_orderStatus_toPay.equals(orderStatus)) {
			return "待支付";
		}
		if (InvestorBankOrderEntity.BANKORDER_orderStatus_submitFailed.equals(orderStatus)) {
			return "申请失败";
		}
		if (InvestorBankOrderEntity.BANKORDER_orderStatus_payFailed.equals(orderStatus)) {
			return "支付失败";
		}
		if (InvestorBankOrderEntity.BANKORDER_orderStatus_done.equals(orderStatus)) {
			return "支付成功";
		}
		
		return orderStatus;
	}
	
	/**我的充值提现记录*/
	public PagesRep<MyBankOrderRep> myquery(Specification<InvestorBankOrderEntity> spec,Pageable pageable) {
		
		PagesRep<MyBankOrderRep> pageRep = new PagesRep<MyBankOrderRep>();
		
		Page<InvestorBankOrderEntity> page = this.investorBankOrderDao.findAll(spec, pageable);
		if(page!=null && page.getSize()>0 && page.getTotalElements()>0){
			List<MyBankOrderRep> rows = new ArrayList<MyBankOrderRep>();
			for (InvestorBankOrderEntity entity : page) {
				rows.add(new MyBankOrderRep(entity.getOrderCode(),//订单流水
						orderTypeEn2Ch(entity.getOrderType()),//订单类型
						entity.getOrderAmount(),//订单金额
						entity.getCreateTime(),//订单创建时间
						getOrderStatusName(entity.getOrderType(),entity.getOrderStatus())//订单状态
								));
			}
			pageRep.setRows(rows);
			pageRep.setTotal(page.getTotalElements());
		}
		
		return pageRep;
	}
	
	/**根据充提记录翻译对应的订单状态*/
	private String getOrderStatusName(String orderType, String orderStatus) {
		switch (orderType) {
		//充值记录
		case "deposit":
			switch (orderStatus) {
			case InvestorBankOrderEntity.BANKORDER_orderStatus_submitted:
				return "已提交";
			case InvestorBankOrderEntity.BANKORDER_orderStatus_toPay:
				return "待支付";
			case InvestorBankOrderEntity.BANKORDER_orderStatus_submitFailed:
				return "申请失败";
			case InvestorBankOrderEntity.BANKORDER_orderStatus_payFailed:
				return "支付失败";
			case InvestorBankOrderEntity.BANKORDER_orderStatus_done:
				return "支付成功";
			default:
				return orderStatus;
			}
			//提现记录
		case "withdraw":
			switch (orderStatus) {
			case InvestorBankOrderEntity.BANKORDER_orderStatus_submitted:
				return "已提交";
			case InvestorBankOrderEntity.BANKORDER_orderStatus_toPay:
				return "申请中";
			case InvestorBankOrderEntity.BANKORDER_orderStatus_submitFailed:
				return "申请失败";
			case InvestorBankOrderEntity.BANKORDER_orderStatus_payFailed:
				return "提现失败";
			case InvestorBankOrderEntity.BANKORDER_orderStatus_done:
				return "提现成功";
			default:
				return orderStatus;
			}
		default:
			return orderStatus;
		}
//		// 充值记录
//		if ("deposit".equals(orderType)) {
//			if (InvestorBankOrderEntity.BANKORDER_orderStatus_submitted.equals(orderStatus)) {
//				return "已提交";
//			}
//			if (InvestorBankOrderEntity.BANKORDER_orderStatus_toPay.equals(orderStatus)) {
//				return "待支付";
//			}
//			if (InvestorBankOrderEntity.BANKORDER_orderStatus_submitFailed.equals(orderStatus)) {
//				return "申请失败";
//			}
//			if (InvestorBankOrderEntity.BANKORDER_orderStatus_payFailed.equals(orderStatus)) {
//				return "支付失败";
//			}
//			if (InvestorBankOrderEntity.BANKORDER_orderStatus_done.equals(orderStatus)) {
//				return "支付成功";
//			}
//			// 提现记录
//		} else if ("withdraw".equals(orderType)) {
//			if (InvestorBankOrderEntity.BANKORDER_orderStatus_submitted.equals(orderStatus)) {
//				return "已提交";
//			}
//			if (InvestorBankOrderEntity.BANKORDER_orderStatus_toPay.equals(orderStatus)) {
//				return "申请中";
//			}
//			if (InvestorBankOrderEntity.BANKORDER_orderStatus_submitFailed.equals(orderStatus)) {
//				return "申请失败";
//			}
//			if (InvestorBankOrderEntity.BANKORDER_orderStatus_payFailed.equals(orderStatus)) {
//				return "提现失败";
//			}
//			if (InvestorBankOrderEntity.BANKORDER_orderStatus_done.equals(orderStatus)) {
//				return "提现成功";
//			}
//		}

//		return orderStatus;
	}
	
}
