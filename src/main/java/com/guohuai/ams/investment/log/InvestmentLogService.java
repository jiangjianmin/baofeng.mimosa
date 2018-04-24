package com.guohuai.ams.investment.log;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guohuai.ams.enums.TargetEventType;
import com.guohuai.ams.investment.Investment;
import com.guohuai.ams.investment.InvestmentDao;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;

@Service
@Transactional
public class InvestmentLogService {

	@Autowired
	private InvestmentLogDao investmentLogDao;
	@Autowired
	private InvestmentDao investmentDao;

	/**
	 * 添加标的操作日志
	 * 
	 * @Title: saveInvestmentLog
	 * @author vania
	 * @version 1.0
	 * @see: TODO
	 * @param targetOid
	 * @param operator
	 * @param eventType
	 * @return TargetLog 返回类型
	 */
	public InvestmentLog saveInvestmentLog(String targetOid, TargetEventType eventType, String operator) {
		if (StringUtils.isBlank(targetOid))
			throw AMPException.getException("投资标的id不能为空");
		Investment investment = this.investmentDao.findOne(targetOid);
		if (null == investment)
			throw AMPException.getException("找不到id为[" + targetOid + "]的投资标的");

		return this.saveInvestmentLog(investment, eventType, operator);
	}

	/**
	 * 添加标的操作日志
	 * 
	 * @Title: saveInvestmentLog
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param investment
	 * @param eventType
	 * @param operator
	 * @return InvestmentLog 返回类型
	 */
	public InvestmentLog saveInvestmentLog(Investment investment, TargetEventType eventType, String operator) {
		if (null == eventType)
			throw AMPException.getException("操作类型不能为空!");
		InvestmentLog entity = InvestmentLog.builder().investment(investment).eventTime(DateUtil.getSqlCurrentDate())
				.eventType(eventType.name()) // 用name吧 直观一点
				// .eventType(eventType.ordinal() + "")
				// .eventType(eventType.getCode())
				.operator(operator).build();
		return investmentLogDao.save(entity);
	}

}
