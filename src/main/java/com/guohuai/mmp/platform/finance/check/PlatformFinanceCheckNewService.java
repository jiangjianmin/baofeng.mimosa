package com.guohuai.mmp.platform.finance.check;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Map;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.net.ntp.TimeStamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.platform.publisher.offset.OffsetCodePojo;

@Service
@Transactional
public class PlatformFinanceCheckNewService {
	@Autowired
	private PlatformFinanceCheckDao platformFinanceCheckDao;

	@Transactional(value = TxType.REQUIRES_NEW)
	public void save(PlatformFinanceCheckEntity entity) {
		this.platformFinanceCheckDao.save(entity);
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public void checkDataConfirm(String oid, String operator) {
		this.platformFinanceCheckDao.checkDataConfirm(oid, operator);
	}

	public PlatformFinanceCheckEntity findByCheckDate(Date checkDate) {
		return this.platformFinanceCheckDao.findByCheckDate(checkDate);
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public int checking(String pdcOid,String operator) {
		int i = this.platformFinanceCheckDao.checking(pdcOid,operator);
		if (i < 1) {
			throw new AMPException("对账数据正在对账中或已对账");
		}
		return i;
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public int syncing(String pdcOid) {
		int i = this.platformFinanceCheckDao.syncing(pdcOid);
		if (i < 1) {
			throw new AMPException("对账数据正在同步或已同步");
		}
		return i;
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public int syncFailed(String pdcOid) {
		int i = this.platformFinanceCheckDao.syncFailed(pdcOid);
		if (i < 1) {
			throw new AMPException("更新对账同步数据状态失败时异常");
		}
		return i;
	}

	public int syncOK(String pdcOid, int totalCount) {
		int i = this.platformFinanceCheckDao.syncOK(pdcOid, totalCount);
		if (i < 1) {
			throw new AMPException("更新对账同步数据状态成功时异常");
		}
		return i;
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void createEntity(Map<String, OffsetCodePojo> map,String checkCode) {
		
		try {
			PlatformFinanceCheckEntity entity = new PlatformFinanceCheckEntity();
			entity.setCheckCode(PlatformFinanceCheckEntity.PREFIX+checkCode);
			entity.setCheckDate(new java.sql.Date(DateUtil.parse(checkCode, "yyyyMMdd").getTime()));
			entity.setCheckStatus(PlatformFinanceCheckEntity.CHECKSTATUS_TOCHECK);
			entity.setCheckDataSyncStatus(PlatformFinanceCheckEntity.CHECKDATASYNCSTATUS_toSync);
			entity.setConfirmStatus(PlatformFinanceCheckEntity.CONFIRMSTATUS_NO);
			entity.setBeginTime(DateUtil.fetchTimestamp(map.get(checkCode).getStartTime()));
			entity.setEndTime(DateUtil.fetchTimestamp(map.get(checkCode).getEndTime()));
			entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
			entity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			entity.setEndTime(DateUtil.fetchTimestamp(map.get(checkCode).getEndTime()));
			this.platformFinanceCheckDao.saveAndFlush(entity);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
