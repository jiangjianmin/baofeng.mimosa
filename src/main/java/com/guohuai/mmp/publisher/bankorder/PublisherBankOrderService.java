package com.guohuai.mmp.publisher.bankorder;

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
import com.guohuai.mmp.investor.bankorder.InvestorBankOrderEntity;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountService;
import com.guohuai.mmp.publisher.baseaccount.statistics.PublisherStatisticsService;
import com.guohuai.mmp.publisher.cashflow.PublisherCashFlowService;
import com.guohuai.mmp.sys.CodeConstants;


@Service
@Transactional
public class PublisherBankOrderService {

	Logger logger = LoggerFactory.getLogger(PublisherBankOrderService.class);

	@Autowired
	private PublisherBaseAccountService publisherBaseAccountService;
	@Autowired
	private PublisherBankOrderDao publisherBankOrderDao;
	@Autowired
	private SeqGenerator seqGenerator;
	@Autowired
	private PublisherCashFlowService publisherCashFlowService;
	@Autowired
	private PublisherStatisticsService publisherStatisticsService;
	@Autowired
	private PlatformStatisticsService platformStatisticsService;
	
	

	/**
	 * 第三方支付充值成功回调
	 */
	public static final String PAYMENT_success = "SUCCESS";


	



	public PublisherBankOrderEntity saveEntity(PublisherBankOrderEntity bankOrder) {
		
		return this.publisherBankOrderDao.save(bankOrder);
	}
	
	public PublisherBankOrderEntity findByOrderCodeAndOrderStatusAndOrderType(String orderCode, String orderStatus, String orderType) {
		PublisherBankOrderEntity bankOrder = this.publisherBankOrderDao.findByOrderCodeAndOrderStatusAndOrderType(orderCode, orderStatus, orderType);
		if (null == bankOrder) {
			//error.define[80001]=投资人-银行委托单的订单号不存在!(CODE:80001)
			throw new AMPException(80001);
		}
		return bankOrder;
	}
	
	public PublisherBankOrderEntity findByOidAndOrderStatusAndOrderType(String oid, String orderStatus, String orderType) {
		PublisherBankOrderEntity bankOrder = this.publisherBankOrderDao.findByOidAndOrderStatusAndOrderType(oid, orderStatus, orderType);
		if (null == bankOrder) {
			//error.define[80001]=投资人-银行委托单的订单号不存在!(CODE:80001)
			throw new AMPException(80001);
		}
		return bankOrder;
	}
	
	/**
	 * 判断SPV银行委托是否回调完成
	 * @param {@link BankOrderIsDoneReq isDone}
	 * @return {@link BaseRep rep}
	 */
	public BaseRep isDone(BankOrderIsDoneReq isDone) {
		BankOrderIsDoneRep rep = new BankOrderIsDoneRep();
		
		PublisherBankOrderEntity bankOrder = this.publisherBankOrderDao.findOne(isDone.getBankOrderOid());
		if (null == bankOrder) {
			//error.define[80001]=投资人-银行委托单的订单号不存在!(CODE:80001)
			throw new AMPException(80001);
		}
		if (!PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_paySuccess.equals(bankOrder.getOrderStatus())) {
			rep.setErrorCode(BaseRep.ERROR_CODE);
		} else {
			rep.setCompleteTime(bankOrder.getCompleteTime());
		}
		return rep;
	}
	
	/**
	 * 创建SPV充值订单
	 * @param {@link BankOrderReq bankOrderReq}
	 * @return {@link InvestorBankOrderEntity bankOrder}
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public PublisherBankOrderEntity createDepostBankOrder(BankOrderReq bankOrderReq) {
		PublisherBankOrderEntity bankOrder = new PublisherBankOrderEntity();
		bankOrder.setOrderCode(this.seqGenerator.next(CodeConstants.PAYMENT_debitDeposit));
		bankOrder.setOrderType(PublisherBankOrderEntity.BANK_ORDER_ORDER_TYPE_deposit);
//		PaymentChannelFee fee = this.paymentChannelService.getInputFee(bankOrderReq.getOrderAmount());
//		bankOrder.setFee(fee.getFee());
//		bankOrder.setFeePayer(fee.getPayer());
		return createBankOrder(bankOrder, bankOrderReq);
	}
	
	/**
	 * 创建SPV提现订单
	 * @param {@link BankOrderReq bankOrderReq}
	 * @return {@link InvestorBankOrderEntity bankOrder}
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public PublisherBankOrderEntity createWithdrawBankOrder(BankOrderReq bankOrderReq) {
		PublisherBankOrderEntity bankOrder = new PublisherBankOrderEntity();
		bankOrder.setOrderCode(this.seqGenerator.next(CodeConstants.PAYMENT_debitWithdraw));
		bankOrder.setOrderType(PublisherBankOrderEntity.BANK_ORDER_ORDER_TYPE_withdraw);
//		PaymentChannelFee fee = this.paymentChannelService.getOutputFee(bankOrderReq.getOrderAmount());
//		bankOrder.setFee(fee.getFee());
//		bankOrder.setFeePayer(fee.getPayer());
		return createBankOrder(bankOrder, bankOrderReq);
	}
	/**
	 * 创建SPV充值、提现订单
	 * @param {@link BankOrderReq bankOrderReq}
	 * @return {@link InvestorBankOrderEntity bankOrder}
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	private PublisherBankOrderEntity createBankOrder(PublisherBankOrderEntity bankOrder, BankOrderReq bankOrderReq) {
		bankOrder.setPublisherBaseAccount(publisherBaseAccountService.findByLoginAcc(bankOrderReq.getUid()));
		bankOrder.setOrderAmount(bankOrderReq.getOrderAmount());
		bankOrder.setOrderStatus(PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_toPay);
		return  this.saveEntity(bankOrder);
	}
	
	/**
	 * SPV银行委托查询
	 * @param {@link Specification<PublisherBankOrderEntity> spec}
	 * @param {@link Pageable pageable}
	 * @return {@link PagesRep<PublisherBankOrderQueryRep> cas}
	 */
	public PagesRep<PublisherBankOrderQueryRep> publisherBankOrderMng(Specification<PublisherBankOrderEntity> spec, Pageable pageable) {
		Page<PublisherBankOrderEntity> cas = this.publisherBankOrderDao.findAll(spec, pageable);
		PagesRep<PublisherBankOrderQueryRep> pagesRep = new PagesRep<PublisherBankOrderQueryRep>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			for (PublisherBankOrderEntity entity : cas) {
				PublisherBankOrderQueryRep queryRep = new PublisherBankOrderQueryRep();
				queryRep.setOrderCode(entity.getOrderCode()); //订单号
				queryRep.setOrderType(entity.getOrderType()); //交易类型
				queryRep.setOrderTypeDisp(orderTypeEn2Ch(entity.getOrderType())); //交易类型disp
				queryRep.setFeePayer(entity.getFeePayer()); //手续费支付方
				queryRep.setFeePayerDisp(feePayerEn2Ch(entity.getFeePayer())); //手续费支付方disp
				queryRep.setFee(entity.getFee()); //手续费
				queryRep.setOrderAmount(entity.getOrderAmount()); //订单金额
				queryRep.setOrderStatus(entity.getOrderStatus()); //订单状态
				queryRep.setOrderStatusDisp(orderStatusEn2Ch(entity.getOrderStatus())); //订单状态disp
				queryRep.setCompleteTime(entity.getCompleteTime()); //订单创建时间
				queryRep.setCreateTime(entity.getCreateTime()); //订单完成时间
				
				pagesRep.add(queryRep);
			}
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}
	
	private String orderStatusEn2Ch(String orderStatus) {
		String orderStatusDisp = null;
		switch (orderStatus) {
		case PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_submitFailed:
			orderStatusDisp = "申请失败";
			break;
		case PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_refused:
			orderStatusDisp = "已拒绝";
			break;
		case PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_toPay:
			orderStatusDisp = "待支付";
			break;
		case PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_payFailed:
			orderStatusDisp = "支付失败";
			break;
		case PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_paySuccess:
			orderStatusDisp = "成交";
			break;

		default:
			orderStatusDisp = "orderStatus";
			break;
		}
		return orderStatusDisp;
	}

	private String feePayerEn2Ch(String feePayer) {
		if (PublisherBankOrderEntity.BANK_ORDER_FEE_PAYER_platform.equals(feePayer)) {
			return "平台";
		} else if (PublisherBankOrderEntity.BANK_ORDER_FEE_PAYER_user.equals(feePayer)) {
			return "用户";
		}
		return feePayer;
	}

	private String orderTypeEn2Ch(String orderType) {
		if (PublisherBankOrderEntity.BANK_ORDER_ORDER_TYPE_deposit.equals(orderType)) {
			return "充值";
		} else if (PublisherBankOrderEntity.BANK_ORDER_ORDER_TYPE_withdraw.equals(orderType)) {
			return "提现";
		}
		return orderType;
	}
	
	public BaseRep withdrawCallback(String orderCode, BaseRep baseRep) {
		PublisherBankOrderEntity bankOrder = this.findByOrderCodeAndOrderStatusAndOrderType(orderCode, 
				PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_toPay, PublisherBankOrderEntity.BANK_ORDER_ORDER_TYPE_withdraw);
		String orderStatus = PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_paySuccess;
		if (0 == baseRep.getErrorCode()) {
			/** 创建<<发行人-资金变动明细>> */
			this.publisherCashFlowService.createCashFlow(bankOrder);
			/** 更新<<发行人-基本账户>>.<<余额>> */
			this.publisherBaseAccountService.updateBalanceMinusMinus(bankOrder.getPublisherBaseAccount(), bankOrder.getOrderAmount());
			/** 更新<<发行人-统计>>.<<累计提现总额>> */
			this.publisherStatisticsService.updateStatistics4Withdraw(bankOrder);
			/** 更新<<平台-统计>>.<<累计交易总额>><<发行人提现总额>> */
			this.platformStatisticsService.updateStatistics4PublisherWithdraw(bankOrder.getOrderAmount());
			
		} else {
			orderStatus = PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_payFailed;
			
		}
		
		bankOrder.setOrderStatus(orderStatus);
		bankOrder.setCompleteTime(DateUtil.getSqlCurrentDate());
		this.saveEntity(bankOrder);
		
		return new BaseRep();
	}
	
	
	public BaseRep depositCallback(String orderOid, BaseRep baseRep) {
		PublisherBankOrderEntity bankOrder = this.findByOidAndOrderStatusAndOrderType(orderOid,
				PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_toPay, PublisherBankOrderEntity.BANK_ORDER_ORDER_TYPE_deposit);
		String orderStatus = PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_paySuccess;
		if (0 == baseRep.getErrorCode()) {
			/** 创建<<发行人-资金变动明细>> */
			publisherCashFlowService.createCashFlow(bankOrder);
			/** 更新<<发行人-基本账户>>.<<账户余额>> */
			this.publisherBaseAccountService.updateBalancePlusPlus(bankOrder.getPublisherBaseAccount(), bankOrder.getOrderAmount());
			/** 更新<<发行人-统计>>.<<累计充值总额>> */
			publisherStatisticsService.updateStatistics4Deposit(bankOrder);
			/** 更新<<平台-统计>>.<<累计交易总额>><<发行人充值总额>> */
			this.platformStatisticsService.updateStatistics4PublisherDeposit(bankOrder.getOrderAmount());
		} else {
			orderStatus = PublisherBankOrderEntity.BANK_ORDER_ORDER_STATUS_payFailed;
		}
		
		bankOrder.setOrderStatus(orderStatus);
		bankOrder.setCompleteTime(DateUtil.getSqlCurrentDate());
		this.saveEntity(bankOrder);
		
		return new BaseRep();
	}

}
