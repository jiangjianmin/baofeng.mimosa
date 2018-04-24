package com.guohuai.mmp.platform.accment.log;

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
import com.guohuai.mmp.platform.accment.AccInterface;

/**
 * 推广平台-请求日志信息
 * 
 * @author wanglei
 *
 */
@Service
public class AccLogService {

	@Autowired
	private AccLogDao accLogDao;
	
	

	/** 创建日志对象 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public AccLogEntity createEntity(AccLogReq req) {
		AccLogEntity accLogEntity = new AccLogEntity();
		accLogEntity.setInterfaceName(req.getInterfaceName());// 接口名称
		accLogEntity.setErrorCode(req.getErrorCode());// 错误码
		accLogEntity.setErrorMessage(req.getErrorMessage());// 错误消息
		accLogEntity.setSendedTimes(req.getSendedTimes());// 已发送次数
		accLogEntity.setSendObj(req.getSendObj()); // 发送内容
		accLogEntity.setLimitSendTimes(AccInterface.getTimes(req.getInterfaceName()));
		accLogEntity.setNextNotifyTime(getNextNotifyTime(accLogEntity));
		return this.save(accLogEntity);
	}
	
	public Timestamp getNextNotifyTime(AccLogEntity accLogEntity) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, accLogEntity.getSendedTimes() * 2);
		return new Timestamp(cal.getTimeInMillis());
	}


	protected AccLogEntity save(AccLogEntity entity) {
		return this.accLogDao.save(entity);
	}

	public List<AccLogEntity> getResendEntities(String lastOid) {
		return this.accLogDao.getResendEntities(lastOid);
	}

	public void batchUpdate(List<AccLogEntity> entities) {
		this.accLogDao.save(entities);
		
	}

	public PagesRep<AccLogQueryRep> mng(Specification<AccLogEntity> spec, Pageable pageable) {
		Page<AccLogEntity> cas = this.accLogDao.findAll(spec, pageable);
		PagesRep<AccLogQueryRep> pagesRep = new PagesRep<AccLogQueryRep>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			for (AccLogEntity entity : cas) {
				AccLogQueryRep queryRep = new AccLogQueryRep();
				queryRep.setInterfaceName(entity.getInterfaceName());
				queryRep.setErrorCode(entity.getErrorCode());
				queryRep.setErrorMessage(entity.getErrorMessage());
				
				queryRep.setSendedTimes(entity.getSendedTimes());
				queryRep.setLimitSendTimes(entity.getLimitSendTimes());
				queryRep.setNextNotifyTime(entity.getNextNotifyTime());
				queryRep.setSendObj(entity.getSendObj());
				queryRep.setCreateTime(entity.getCreateTime());
				queryRep.setUpdateTime(entity.getUpdateTime());

				pagesRep.add(queryRep);
			}
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}
	

	
}
