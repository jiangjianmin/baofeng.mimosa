package com.guohuai.mmp.platform.publisher.offset;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.investor.orderlog.OrderLogEntity;
import com.guohuai.mmp.investor.orderlog.OrderLogService;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.platform.baseaccount.PlatformBaseAccountService;
import com.guohuai.mmp.platform.publisher.product.offset.ProductOffsetService;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;

@Service
public class PublisherOffsetServiceRequiresNew {

	Logger logger = LoggerFactory.getLogger(PublisherOffsetServiceRequiresNew.class);

	@Autowired
	private PublisherOffsetDao publisherOffsetDao;
	@Autowired
	private OrderLogService orderLogService;
	
	@Autowired
	private ProductOffsetService productOffsetService;
	
	@Autowired
	private OffsetService offsetService;
	@Autowired
	private PlatformBaseAccountService platformBaseAccountService;
	
	public VolumeConfirmRep processOneItem(InvestorTradeOrderEntity orderEntity) {
		VolumeConfirmRep iRep = new VolumeConfirmRep();
		OrderLogEntity orderLog = new OrderLogEntity();
		orderLog.setOrderType(orderEntity.getOrderType());
		orderLog.setTradeOrderOid(orderEntity.getOrderCode());
		orderLog.setOrderStatus(OrderLogEntity.ORDERLOG_orderStatus_confirmed);
		try {
			offsetService.processItem(orderEntity.getOid(), iRep);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			orderLog.setOrderStatus(OrderLogEntity.ORDERLOG_orderStatus_confirmFailed);
			orderLog.setErrorCode(-1);
			orderLog.setErrorMessage(e.getMessage());
			iRep.setSuccess(false);
		}
		
		//创建日志
		this.orderLogService.create(orderLog);
		return iRep;
	}

	public VolumeConfirmRep plusProcessOneItem(InvestorTradeOrderEntity orderEntity) {
		VolumeConfirmRep iRep = new VolumeConfirmRep();
		OrderLogEntity orderLog = new OrderLogEntity();
		orderLog.setOrderType(orderEntity.getOrderType());
		orderLog.setTradeOrderOid(orderEntity.getOrderCode());
		orderLog.setOrderStatus(OrderLogEntity.ORDERLOG_orderStatus_confirmed);
		try {
			offsetService.plusProcessItem(orderEntity, iRep);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			orderLog.setOrderStatus(OrderLogEntity.ORDERLOG_orderStatus_confirmFailed);
			orderLog.setErrorCode(-1);
			orderLog.setErrorMessage(e.getMessage());
			iRep.setSuccess(false);
		}

		//创建日志
		this.orderLogService.create(orderLog);
		return iRep;
	}

	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void processStatus(PublisherOffsetEntity offsetEntity, String spvOffsetStatus,
			String proOffsetStatus) {
		//更新发行人轧差状态
		int i = publisherOffsetDao.updateConfirmStatus(offsetEntity.getOid(), spvOffsetStatus);
		//更新产品轧差状态
		int j = productOffsetService.updateConfirmStatus(offsetEntity.getOid(), proOffsetStatus);
		if (i < 1 || j < 1) {
			// error.define[30067]=轧差状态异常非确认中(CODE:30067)
			throw new AMPException(30067);
		}
	}
	
	
	public PublisherOffsetEntity createEntity(PublisherBaseAccountEntity publisher, String offsetCode) {
		PublisherOffsetEntity offset = new PublisherOffsetEntity();
		offset.setPlatformBaseAccount(platformBaseAccountService.getPlatfromBaseAccount());
		offset.setPublisherBaseAccount(publisher);
		offset.setOffsetCode(offsetCode);
		offset.setOffsetDate(new java.sql.Date(DateUtil.parse(offsetCode, "yyyyMMdd").getTime()));
		offset.setClearStatus(PublisherOffsetEntity.OFFSET_clearStatus_toClear);
		offset.setConfirmStatus(PublisherOffsetEntity.OFFSET_confirmStatus_toConfirm);
		offset.setCloseStatus(PublisherOffsetEntity.OFFSET_closeStatus_toClose);
		
		return this.publisherOffsetDao.save(offset);
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void updateSpvConfirmStatus4Lock(String offsetOid, String spvConfirmStatus, String proConfirmStatus){
		int i = this.publisherOffsetDao.updateConfirmStatus4Lock(offsetOid, spvConfirmStatus);
		int j = productOffsetService.updateConfirmStatus4Lock(offsetOid, proConfirmStatus);
		
		if (i < 1 || j < 1) {
			// error.define[30026]=份额确认中或已确认(CODE:30026)
			throw new AMPException(30026);
		}
	
	}
	
}
