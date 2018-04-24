package com.guohuai.mmp.platform.accment;

import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.platform.accment.log.AccLogEntity;
import com.guohuai.mmp.platform.accment.log.AccLogService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class AccResendServiceRequireNew {

	@Autowired
	private AccLogService accLogService;
	@Autowired
	private AccmentService accmentService;
	

	@Transactional(value = TxType.REQUIRES_NEW)
	public String requireNew(String lastOid, List<AccLogEntity> entities) {
		for (AccLogEntity entity : entities) {
			
			try {
				
				BaseRep irep = (BaseRep)AccmentService.class
						.getMethod(entity.getInterfaceName(),
								Class.forName(AccInterface.getIReq(entity.getInterfaceName())), boolean.class)
						.invoke(this.accmentService,
								JSONObject.parseObject(entity.getSendObj(), Class.forName(AccInterface.getIReq(entity.getInterfaceName()))), false);
				entity.setErrorCode(irep.getErrorCode());
				entity.setErrorMessage(irep.getErrorMessage());
				entity.setSendedTimes(entity.getSendedTimes() + 1);
				entity.setNextNotifyTime(this.accLogService.getNextNotifyTime(entity));
				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			lastOid = entity.getOid();
		}
		this.accLogService.batchUpdate(entities);
		return lastOid;
	}
}
