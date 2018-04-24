package com.guohuai.mmp.platform.finance.modifyorder;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;

@Service
@Transactional
public class ModifyOrderNewService {
	@Autowired
	ModifyOrderDao modifyOrderDao;

	@Transactional(value = TxType.REQUIRES_NEW)
	public void deleteByCheckOid(String checkOid) {
		modifyOrderDao.deleteByCheckOid(checkOid);
	}
	@Transactional(value = TxType.REQUIRES_NEW)
	public int updateDealStatusDealtByOrderCode(String orderCode) {
		int i = this.modifyOrderDao.updateDealStatusDealtByOrderCode(orderCode);
		if (i < 1) {
			throw new AMPException("对账状态非处理中");
		}
		return i;
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public int updateDealStatusDealingByOrderCode(String orderCode) {
		int i = this.modifyOrderDao.updateDealStatusDealingByOrderCode(orderCode);
		if (i < 1) {
			throw new AMPException("对账状态非待处理");
		}
		return i;
	}

	// @Transactional(value=TxType.REQUIRES_NEW)
	// public void updateDealStatus(String oid,String dealStatus) {
	// this.modifyOrderDao.updateDealStatus(oid,dealStatus);
	// }
	@Transactional(value = TxType.REQUIRES_NEW)
	public int modifyOrderApprove(String oid, String approveStatus, String operator) {
		int i = this.modifyOrderDao.modifyOrderApprove(oid,approveStatus,operator);
		if (i < 1) {
			throw new AMPException("对账状态非处理中");
		}
		return i;
		
	}

}
