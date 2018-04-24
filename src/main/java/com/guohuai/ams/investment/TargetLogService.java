package com.guohuai.ams.investment;

import java.sql.Timestamp;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;

/**
 * 
 * <p>
 * Title: TargetLogService.java
 * </p>
 * <p>
 * 投资标的操作日志Service
 * </p>
 * 
 * @author vania
 * @version 1.0
 * @created 2016年5月18日 下午3:31:20
 */
@Service
@Transactional
public class TargetLogService {
	@Autowired
	TargetLogDao targetLogDao;
	@Autowired
	private InvestmentDao investmentDao;

	/**
	 * 添加标的操作日志
	 * 
	 * @Title: saveLog
	 * @author vania
	 * @version 1.0
	 * @see: TODO
	 * @param targetOid
	 * @param operator
	 * @param eventType
	 * @return TargetLog 返回类型
	 */
	public TargetLog saveLog(String targetOid, String operator, String eventType) {
		if (StringUtils.isBlank(targetOid))
			throw AMPException.getException("投资标的id不能为空");
		Investment investment = this.investmentDao.findOne(targetOid);
		if (null == investment)
			throw AMPException.getException("找不到id为[" + targetOid + "]的投资标的");

		return this.saveLog(investment, operator, eventType);
	}

	/**
	 * 添加标的操作日志
	 * 
	 * @Title: saveLog
	 * @author vania
	 * @version 1.0
	 * @see: TODO
	 * @param targetOid
	 * @param operator
	 * @param eventType
	 * @return TargetLog 返回类型
	 */
	public TargetLog saveLog(Investment investment, String operator, String eventType) {
		if (StringUtils.isBlank(eventType))
			throw AMPException.getException("事件类型不能为空");

		if (null == investment)
			throw AMPException.getException("投资标的不能为空");

		TargetLog log = TargetLog.builder().investment(investment).eventTime(new Timestamp(System.currentTimeMillis())).operator(operator).eventType(eventType).build();
		return targetLogDao.save(log);
	}
}
