package com.guohuai.mmp.platform.finance.result;

import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;

@Service
@Transactional
public class PlatformFinanceCompareDataResultNewService {
	@Autowired
	private PlatformFinanceCompareDataResultDao platformFinanceCompareDataResultDao;
	

	public int deleteByCheckOid(String checkOid){
		return platformFinanceCompareDataResultDao.deleteByCheckOid(checkOid);
	}
	
	public void save(List<PlatformFinanceCompareDataResultEntity> compareDataResultList) {
		this.platformFinanceCompareDataResultDao.save(compareDataResultList);
	}
	@Transactional(value=TxType.REQUIRES_NEW)
	public void updateDealStatusByOid(String oid,String dealStatus) {
		this.platformFinanceCompareDataResultDao.updateDealStatusByOid(oid,dealStatus);
	}
	@Transactional(value=TxType.REQUIRES_NEW)
	public int updateDealStatusDealtByOrderCode(String orderCode) {
		int i = this.platformFinanceCompareDataResultDao.updateDealStatusDealtByOrderCode(orderCode);
		if (i < 1) {
			throw new AMPException("对账状态非处理中");
		}
		return i;
	}
	@Transactional(value=TxType.REQUIRES_NEW)
	public int updateDealStatusDealingByOrderCode(String orderCode) {
		int i = this.platformFinanceCompareDataResultDao.updateDealStatusDealingByOrderCode(orderCode);
		if (i < 1) {
			throw new AMPException("对账状态非待处理");
		}
		return i;
	}
	
	public Long countByCheckOid(String checkOid) {
		return this.platformFinanceCompareDataResultDao.countByCheckOid(checkOid);
	}
}
