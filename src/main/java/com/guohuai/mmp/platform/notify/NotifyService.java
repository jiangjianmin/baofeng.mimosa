package com.guohuai.mmp.platform.notify;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.mmp.sys.CodeConstants;

@Service
@Transactional
public class NotifyService {

	@Autowired
	private NotifyDao notifyDao;
	@Autowired
	private SeqGenerator seqGenerator;

	public NotifyEntity save(NotifyEntity orderLog) {
		return this.notifyDao.save(orderLog);
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public NotifyEntity create(String notifyType, String notifyContent) {
		NotifyEntity orderLog = new NotifyEntity();
		orderLog.setNotifyId(this.seqGenerator.next(CodeConstants.OrderLog_notifyId));
		orderLog.setNotifyType(notifyType);
		orderLog.setNotifyStatus(NotifyEntity.ORDERLOG_notifyStatus_toConfirm);
		orderLog.setNotifyContent(notifyContent);
		return save(orderLog);
	}
}
