package com.guohuai.ams.cashtool.log;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guohuai.ams.cashtool.CashTool;
import com.guohuai.ams.cashtool.CashToolDao;
import com.guohuai.ams.enums.CashToolEventType;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;

@Service
@Transactional
public class CashToolLogService {

	@Autowired
	private CashToolLogDao cashToolLogDao;
	@Autowired
	private CashToolDao cashToolDao;


	/**
	 * 添加现金管理工具操作日志
	 * @Title: saveCashToolLog 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param cashtoolOid
	 * @param operator
	 * @param eventType
	 * @return TargetLog    返回类型
	 */
	public CashToolLog saveCashToolLog(String cashtoolOid, CashToolEventType eventType, String operator) {
		if (StringUtils.isBlank(cashtoolOid))
			throw AMPException.getException("现金管理工具id不能为空");
		CashTool cashTool = this.cashToolDao.findOne(cashtoolOid);
		if (null == cashTool)
			throw AMPException.getException("找不到id为[" + cashtoolOid + "]的现金管理工具");

		return this.saveCashToolLog(cashTool, eventType, operator);
	}
	
	/**
	 * 添加现金管理工具操作日志
	 * @Title: saveCashToolLog 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param cashTool
	 * @param eventType
	 * @param operator
	 * @return CashToolLog    返回类型
	 */
	public CashToolLog saveCashToolLog(CashTool cashTool, CashToolEventType eventType, String operator) {
		if(null == eventType)
			throw AMPException.getException("操作类型不能为空!");
		CashToolLog entity = CashToolLog.builder().cashTool(cashTool).eventTime(DateUtil.getSqlCurrentDate())
				.eventType(eventType.name()) // 用name吧 直观一点
//				.eventType(eventType.ordinal() + "")
//				.eventType(eventType.getCode())
				.operator(operator)
				.build();
		return cashToolLogDao.save(entity);
	}
	
	
}
