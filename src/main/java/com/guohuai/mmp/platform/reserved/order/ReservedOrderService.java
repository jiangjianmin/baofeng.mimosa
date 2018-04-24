package com.guohuai.mmp.platform.reserved.order;

import java.math.BigDecimal;

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
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.platform.reserved.account.ReservedAccountEntity;
import com.guohuai.mmp.platform.reserved.account.ReservedAccountReq;
import com.guohuai.mmp.platform.reserved.account.ReservedAccountService;
import com.guohuai.mmp.platform.reserved.account.ReservedAccountTradeReq;
import com.guohuai.mmp.sys.CodeConstants;

@Service
@Transactional
public class ReservedOrderService {

	Logger logger = LoggerFactory.getLogger(ReservedOrderService.class);

	@Autowired
	private ReservedOrderDao reservedOrderDao;
	
	@Autowired
	private SeqGenerator seqGenerator;
	@Autowired
	private ReservedAccountService reservedAccountService;
	
	/**
	 * 创建代收订单
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public ReservedOrderEntity createReservedOrderCollect(
			ReservedAccountEntity reservedAccount, ReservedAccountReq reservedAccountReq) {
		ReservedOrderEntity order = new ReservedOrderEntity();
		order.setReservedAccount(reservedAccount);
		order.setOrderCode(this.seqGenerator.next(CodeConstants.Reserved_create_hosting_collect_trade)); //订单号
		order.setOrderType(ReservedOrderEntity.ORDER_orderType_borrow); //交易类型
		order.setOrderAmount(reservedAccountReq.getOrderAmount()); //订单金额
		order.setOrderStatus(ReservedOrderEntity.ORDER_orderStatus_toPay); //订单状态
		order.setRelatedAcc(reservedAccountReq.getRelatedAcc());
		return this.saveEntity(order);
	}
	/**
	 * 创建代付订单
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public ReservedOrderEntity createReservedOrderPay(
			ReservedAccountEntity reservedAccount, ReservedAccountReq reservedAccountReq) {
		ReservedOrderEntity order = new ReservedOrderEntity();
		order.setReservedAccount(reservedAccount);
		order.setOrderCode(this.seqGenerator.next(CodeConstants.Reserved_create_single_hosting_pay_trade)); //订单号
		order.setOrderType(ReservedOrderEntity.ORDER_orderType_return); //交易类型
		
		order.setOrderAmount(reservedAccountReq.getOrderAmount()); //订单金额
		order.setOrderStatus(ReservedOrderEntity.ORDER_orderStatus_toPay); //订单状态
		order.setRelatedAcc(reservedAccountReq.getRelatedAcc());
		return this.saveEntity(order);
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public ReservedOrderEntity createReservedTradeOrder(ReservedAccountEntity reservedAccount,
			ReservedAccountTradeReq tReq) {
		ReservedOrderEntity order = new ReservedOrderEntity();
		order.setReservedAccount(reservedAccount);
		order.setOrderCode(this.seqGenerator.next(CodeConstants.Reserved_create_single_hosting_pay_trade)); //订单号
		order.setOrderType(tReq.getOrderType()); //交易类型
		
		order.setOrderAmount(tReq.getOrderAmount()); //订单金额
		order.setOrderStatus(ReservedOrderEntity.ORDER_orderStatus_toPay); //订单状态
		order.setRelatedAcc(null);
		return this.saveEntity(order);
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public ReservedOrderEntity createCancellation(BigDecimal orderAmount) {
		ReservedOrderEntity en = new ReservedOrderEntity();
		en.setReservedAccount(this.reservedAccountService.getReservedAccount());
		en.setOrderCode(this.seqGenerator.next(CodeConstants.Reserved_create_hosting_collect_trade));
		en.setOrderType(ReservedOrderEntity.ORDER_orderType_borrow);
		en.setOrderAmount(orderAmount);
		en.setOrderStatus(ReservedOrderEntity.ORDER_orderStatus_toPay);
		en.setRelatedAcc(ReservedOrderEntity.ORDER_relatedAcc_operationAcc);
		return this.saveEntity(en);
	}

	
	
	
	public ReservedOrderEntity saveEntity(ReservedOrderEntity order) {
		return this.reservedOrderDao.save(order);
	}
	
	public ReservedOrderEntity findByOid(String orderOid) {
		ReservedOrderEntity order = this.reservedOrderDao.findOne(orderOid);
		if (null == order) {
			throw new AMPException("备付金订单不存在");
		}
		return order;
	}
	
	
	public PagesRep<ReservedOrderQueryRep> mng(Specification<ReservedOrderEntity> spec, Pageable pageable) {
		Page<ReservedOrderEntity> cas = this.reservedOrderDao.findAll(spec, pageable);
		PagesRep<ReservedOrderQueryRep> pagesRep = new PagesRep<ReservedOrderQueryRep>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			for (ReservedOrderEntity order : cas) {
				ReservedOrderQueryRep queryRep = new ReservedOrderQueryRep();
				queryRep.setOrderCode(order.getOrderCode()); //订单号
				queryRep.setOrderAmount(order.getOrderAmount()); //订单金额
				queryRep.setOrderType(order.getOrderType()); //交易类型
				queryRep.setOrderTypeDisp(orderTypeEn2Ch(order.getOrderType()));
				queryRep.setOrderStatus(order.getOrderStatus()); //订单状态
				queryRep.setOrderStatusDisp(orderStatusEn2Ch(order.getOrderStatus()));
				queryRep.setCreateTime(order.getCreateTime()); //订单创建时间
				queryRep.setCompleteTime(order.getCompleteTime()); //订单完成时间
				queryRep.setRelatedAcc(order.getRelatedAcc());//关联账户
				queryRep.setRelatedAccDisp(relatedAccEn2Ch(order.getRelatedAcc()));
				pagesRep.add(queryRep);
			}
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}
	
	
	/**
	 * /** 订单状态--待支付 *
	public static final String ORDER_orderStatus_toPay = "toPay";
	/** 订单状态--支付失败 *
	public static final String ORDER_orderStatus_payFailed = "payFailed";
	/** 订单状态--支付成功 *
	public static final String ORDER_orderStatus_paySuccess = "paySuccess";
	/** 订单状态--申请失败 *
	public static final String ORDER_orderStatus_submitFailed = "submitFailed";
	 * @param orderStatus
	 * @return
	 */
	private String orderStatusEn2Ch(String orderStatus) {
		
		if (ReservedOrderEntity.ORDER_orderStatus_toPay.equals(orderStatus)) {
			return "待支付";
		}
		if (ReservedOrderEntity.ORDER_orderStatus_payFailed.equals(orderStatus)) {
			return "支付失败";
		}
		if (ReservedOrderEntity.ORDER_orderStatus_paySuccess.equals(orderStatus)) {
			return "支付成功";
		}
		if (ReservedOrderEntity.ORDER_orderStatus_submitFailed.equals(orderStatus)) {
			return "申请失败";
		}
		return orderStatus;
	}
	/**
	 * /** 交易类型--借款 *
	public static final String ORDER_orderType_borrow = "borrow";
	/** 交易类型--还款 *
	public static final String ORDER_orderType_return = "return";
	 * @param orderType
	 * @return
	 */
	private String orderTypeEn2Ch(String orderType) {
		if (ReservedOrderEntity.ORDER_orderType_borrow.equals(orderType)) {
			return "借款";
		}
		if (ReservedOrderEntity.ORDER_orderType_return.equals(orderType)) {
			return "还款";
		}
		
		if (ReservedOrderEntity.ORDER_orderType_deposit.equals(orderType)) {
			return "充值";
		}
		
		if (ReservedOrderEntity.ORDER_orderType_withdraw.equals(orderType)) {
			return "提现";
		}
		return orderType;
	}
	
	/**
	 * 超级户：superAcc，基本户：basicAcc
	 * public static final String ORDER_relatedAcc_superAcc = "superAcc";
	public static final String ORDER_relatedAcc_basicAcc = "basicAcc";
	 * @param relatedAcc
	 * @return
	 */
	private String relatedAccEn2Ch(String relatedAcc) {
		if (ReservedOrderEntity.ORDER_relatedAcc_basicAcc.equals(relatedAcc)) {
			return "基本户";
		}
		if (ReservedOrderEntity.ORDER_relatedAcc_superAcc.equals(relatedAcc)) {
			return "超级户";
		}
		return relatedAcc;
	}
	
	
	
	
	
	
	
}
