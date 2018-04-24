package com.guohuai.mmp.platform.payment.log;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.component.web.view.PagesRep;

/**
 * 推广平台-请求日志信息
 * 
 * @author wanglei
 *
 */
@Service
public class PayLogService {

	@Autowired
	private PayLogDao payLogDao;
	
	

	/** 创建日志对象 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public PayLogEntity createEntity(PayLogReq req) {
		PayLogEntity payLogEntity = new PayLogEntity();
		payLogEntity.setInterfaceName(req.getInterfaceName());// 接口名称
		payLogEntity.setErrorCode(req.getErrorCode());// 错误码
		payLogEntity.setErrorMessage(req.getErrorMessage());// 错误消息
		payLogEntity.setSendedTimes(req.getSendedTimes());// 已发送次数
		payLogEntity.setContent(req.getContent()); // 发送内容
		payLogEntity.setOrderCode(req.getOrderCode());
		payLogEntity.setLimitSendTimes(PayInterface.getTimes(req.getInterfaceName()));
		payLogEntity.setNextNotifyTime(getNextNotifyTime(payLogEntity));
		payLogEntity.setHandleType(req.getHandleType());
		return this.save(payLogEntity);
	}
	
	public Timestamp getNextNotifyTime(PayLogEntity payLogEntity) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, payLogEntity.getSendedTimes() * payLogEntity.getSendedTimes());
		return new Timestamp(cal.getTimeInMillis());
	}


	protected PayLogEntity save(PayLogEntity entity) {
		return this.payLogDao.save(entity);
	}

	public List<PayLogEntity> getResendEntities() {
		return this.payLogDao.getResendEntities();
	}

	public void batchUpdate(List<PayLogEntity> entities) {
		this.payLogDao.save(entities);
		
	}

	public PagesRep<PayLogQueryRep> mng(Specification<PayLogEntity> spec, Pageable pageable) {
		Page<PayLogEntity> cas = this.payLogDao.findAll(spec, pageable);
		PagesRep<PayLogQueryRep> pagesRep = new PagesRep<PayLogQueryRep>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			for (PayLogEntity entity : cas) {
				PayLogQueryRep queryRep = new PayLogQueryRep();
				queryRep.setInterfaceName(entity.getInterfaceName());
				queryRep.setErrorCode(entity.getErrorCode());
				queryRep.setErrorMessage(entity.getErrorMessage());
				
				queryRep.setSendedTimes(entity.getSendedTimes());
				queryRep.setLimitSendTimes(entity.getLimitSendTimes());
				queryRep.setNextNotifyTime(entity.getNextNotifyTime());
				queryRep.setContent(entity.getContent());
				queryRep.setCreateTime(entity.getCreateTime());
				queryRep.setUpdateTime(entity.getUpdateTime());

				pagesRep.add(queryRep);
			}
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}

	public PayLogEntity getSuccessPayAplly(String orderCode) {
		return this.payLogDao.getSuccessPayAplly(orderCode);
	}

	public PayLogEntity getPayNotify(String orderCode) {
		return this.payLogDao.getPayNotify(orderCode);
	}

	public PayLogEntity findByOrderCodeAndHandleType(String orderCode, String handleType) {
		return this.payLogDao.findByOrderCodeAndHandleType(orderCode, handleType);
	}

	public PayLogEntity saveEntity(PayLogEntity entity) {
		return this.payLogDao.save(entity);
		
	}
	

	
}
