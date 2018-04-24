package com.guohuai.mmp.platform.publisher.order;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetEntity;
import com.guohuai.mmp.sys.CodeConstants;

@Service
@Transactional
public class PublisherOrderService {

	Logger logger = LoggerFactory.getLogger(PublisherOrderService.class);

	@Autowired
	private PublisherOrderDao publisherOrderDao;
	
	@Autowired
	private SeqGenerator seqGenerator;

	
	/**
	 * 代收单
	 * @param offset
	 * @return
	 */
	public PublisherOrderEntity createPublisherOrderCollect(PublisherOffsetEntity offset) {
		PublisherOrderEntity order = new PublisherOrderEntity();
		order.setPublisher(offset.getPublisherBaseAccount());
		order.setOffset(offset);
		order.setOrderCode(this.seqGenerator.next(CodeConstants.Publisher_create_hosting_collect_trade)); //订单号
		order.setOrderType( PublisherOrderEntity.ORDER_orderType_return); //交易类型
		order.setOrderAmount(offset.getNetPosition().abs()); //订单金额
		order.setOrderStatus(PublisherOrderEntity.ORDER_orderStatus_toPay); //订单状态
		return this.saveEntity(order);
	}
	
	/**
	 * 代付单
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public PublisherOrderEntity createPublisherOrderPay(PublisherOffsetEntity offset) {
		PublisherOrderEntity order = new PublisherOrderEntity();
		order.setPublisher(offset.getPublisherBaseAccount());
		order.setOffset(offset);
		order.setOrderCode(this.seqGenerator.next(CodeConstants.Publisher_create_single_hosting_pay_trade)); //订单号
		order.setOrderType( PublisherOrderEntity.ORDER_orderType_borrow); //交易类型
		order.setOrderAmount(offset.getNetPosition().abs()); //订单金额
		order.setOrderStatus(PublisherOrderEntity.ORDER_orderStatus_toPay); //订单状态
		return this.saveEntity(order);
	}
	

	public PublisherOrderEntity saveEntity(PublisherOrderEntity order) {
		order.setCreateTime(DateUtil.getSqlCurrentDate());
		return updateEntity(order);
	}

	public PublisherOrderEntity updateEntity(PublisherOrderEntity order) {
		order.setUpdateTime(DateUtil.getSqlCurrentDate());
		return this.publisherOrderDao.save(order);
	}

	public PublisherOrderEntity findByOrderCode(String orderCode) {
		PublisherOrderEntity order = this.publisherOrderDao.findByOrderCode(orderCode);
		if (null == order) {
			throw new AMPException("订单不存在");
		}
		return order;
	}
	
	public PublisherOrderEntity findByOid(String orderCode) {
		PublisherOrderEntity order = this.publisherOrderDao.findOne(orderCode);
		if (null == order) {
			throw new AMPException("订单不存在");
		}
		return order;
	} 

	

}
