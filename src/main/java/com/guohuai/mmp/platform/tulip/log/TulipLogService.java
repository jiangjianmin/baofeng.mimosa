package com.guohuai.mmp.platform.tulip.log;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * 推广平台-请求日志信息
 */
@Service
public class TulipLogService {

	@Autowired
	private TulipLogDao tulipLogDao;


	/** 创建日志对象 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public TulipLogEntity createTulipLogEntity(TuipLogReq req) {
		TulipLogEntity tulipLogEntity = new TulipLogEntity();
		tulipLogEntity.setInterfaceCode(req.getInterfaceCode());
		tulipLogEntity.setInterfaceName(req.getInterfaceName());// 接口名称
		tulipLogEntity.setErrorCode(req.getErrorCode());// 错误码
		tulipLogEntity.setErrorMessage(req.getErrorMessage());// 错误消息
		tulipLogEntity.setSendedTimes(req.getSendedTimes());// 已发送次数
		tulipLogEntity.setSendObj(req.getSendObj()); // 发送内容
		tulipLogEntity.setLimitSendTimes(TulipLogEntity.getTimes(req.getInterfaceCode()));
		tulipLogEntity.setNextNotifyTime(getNextNotifyTime(tulipLogEntity));
		return this.save(tulipLogEntity);
	}



	public Timestamp getNextNotifyTime(TulipLogEntity tulipLogEntity) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, tulipLogEntity.getSendedTimes() * tulipLogEntity.getSendedTimes());
		return new Timestamp(cal.getTimeInMillis());
	}


	protected TulipLogEntity save(TulipLogEntity entity) {
		return this.tulipLogDao.save(entity);
	}

	public List<TulipLogEntity> getResendEntities(String lastOid) {
		return this.tulipLogDao.getResendEntities(lastOid);
	}

	public void batchUpdate(List<TulipLogEntity> entities) {
		this.tulipLogDao.save(entities);
		
	}

	
}
