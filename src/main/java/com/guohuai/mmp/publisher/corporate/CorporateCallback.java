//package com.guohuai.mmp.publisher.corporate;
//
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.ghg.pay.api.AuditStatusCallback;
//import com.ghg.pay.gateway.SinaAccClient;
//import com.ghg.pay.sina.model.notify.AuditStatusSync;
//import com.ghg.pay.sina.model.query.QueryBankCard;
//import com.ghg.pay.sina.model.query.QueryBankCardBuilder;
//import com.ghg.pay.sina.model.query.QueryBankCardResp;
//import com.guohuai.component.exception.AMPException;
//import com.guohuai.component.util.DateUtil;
//import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
//
//@Service
//public class CorporateCallback implements AuditStatusCallback {
//	Logger logger = LoggerFactory.getLogger(CorporateCallback.class);
//	@Autowired
//	private CorporateDao corporateDao;
//	@Autowired
//	private SinaAccClient sinaAccClient;
//	@Autowired
//	private PlatformStatisticsService platformStatisticsService;
//
//	@Override
//	@Transactional
//	public void onAuditStatus(AuditStatusSync auditStatus) {
//		logger.info("sinacallback:" + auditStatus.toString());
//		String orderNo = auditStatus.getAudit_order_no();
//		int status = 0;
//		if (auditStatus.getAudit_status().equalsIgnoreCase("PROCESSING")) {
//			status = 1;
//		} else if (auditStatus.getAudit_status().equalsIgnoreCase("SUCCESS")) {
//			status = 2;
//		} else if (auditStatus.getAudit_status().equalsIgnoreCase("FAILED")) {
//			status = 3;
//		} else {
//			status = 0;
//		}
//		String message = auditStatus.getAudit_message();
//
//		Corporate c = this.corporateDao.findByAuditOrderNo(orderNo);
//		if (null == c) {
//			// error.define[30042]=审核订单不存在(CODE:30042)
//			throw AMPException.getException(30042);
//		}
//		
//		c.setStatus(status);
//		c.setAuditMessage(message);
//		c.setOperator("system");
//		c.setUpdateTime(DateUtil.getSqlCurrentDate());
//	
//
//		QueryBankCard query = QueryBankCardBuilder.n().identity_id(c.getIdentityId()).build();
//		QueryBankCardResp card = this.sinaAccClient.queryBankCard(query);
//		String cardId = card.getCard_list().substring(0, card.getCard_list().indexOf("^"));
//		c.setCardId(cardId);
//		this.corporateDao.save(c);
//		if (status == 2) {
//			platformStatisticsService.increatePublisherAmount();
//		}
//	}
//
//}
