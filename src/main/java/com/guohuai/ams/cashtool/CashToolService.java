/*   
 * Copyright © 2015 guohuaigroup All Rights Reserved.   
 *   
 * This software is the confidential and proprietary information of   
 * Founder. You shall not disclose such Confidential Information   
 * and shall use it only in accordance with the terms of the agreements   
 * you entered into with Founder.   
 *   
 */
package com.guohuai.ams.cashtool;

import java.math.BigDecimal;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.cashtool.log.CashToolLogService;
import com.guohuai.ams.enums.CashToolEventType;
import com.guohuai.component.util.DateUtil;

/**
 * <p>
 * Title: CashToolService.java
 * </p>
 * <p>
 * Description: 描述
 * </p>
 * 
 * @author vania
 * @version 1.0
 * @created 2016年5月17日 下午2:34:52
 */
@Service
@Transactional
public class CashToolService {

	@Autowired
	private CashToolDao cashToolDao;

	@Autowired
	private CashToolLogService cashToolLogService;

	public CashTool save(CashTool cashTool) {
		return this.cashToolDao.save(cashTool);
	}

	/**
	 * 根据oid查询现金管理工具
	 * 
	 * @Title: findByOid
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param oid
	 * @return CashTool 返回类型
	 */
	public CashTool findByOid(String oid) {
		return this.cashToolDao.findOne(oid);
	}

	public List<CashTool> getCashToolList(Specification<CashTool> spec) {
		return cashToolDao.findAll(spec);
	}

	/**
	 * 根据名称模糊查询现金管理工具
	 * 
	 * @Title: getCashToolListByName
	 * @author vania
	 * @version 1.0
	 * @see: TODO
	 * @param name
	 * @return List<Object> 返回类型
	 */
	public List<Object> getCashToolListByName(String name) {
		return cashToolDao.getCashToolByName(name);
	}

	/**
	 * 分页查询现金管理工具
	 * 
	 * @Title: getCashToolList
	 * @author vania
	 * @version 1.0
	 * @see: TODO
	 * @param spec
	 * @param pageable
	 * @return
	 * @return Page<CashTool> 返回类型
	 */
	public Page<CashTool> getCashToolList(Specification<CashTool> spec, Pageable pageable) {
		return cashToolDao.findAll(spec, pageable);
	}

	/**
	 * form转entity
	 * 
	 * @param form
	 * @return
	 */
	public CashTool createInvestment(CashToolManageForm form) {
		CashTool entity = new CashTool();
		try {
			BeanUtils.copyProperties(form, entity);
			// 保本比例 百分比转小数
			if (form.getGuarRatio() != null) {
				BigDecimal decimal = form.getGuarRatio().divide(new BigDecimal(100));
				entity.setGuarRatio(decimal);
			}
			return entity;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	/**
	 * 移除出库
	 * 
	 * @Title: remove
	 * @author vania
	 * @version 1.0
	 * @see: TODO
	 * @param oid
	 *            void 返回类型
	 */
	public void remove(String oid, String operator) {
		CashTool ct = this.cashToolDao.findOne(oid);
		ct.setState(CashTool.CASHTOOL_STATE_invalid);
		ct.setOperator(operator);
		ct.setUpdateTime(DateUtil.getSqlCurrentDate());
		cashToolLogService.saveCashToolLog(oid, CashToolEventType.invalid, operator);
		this.cashToolDao.save(ct);
	}

	/**
	 * 现金管理工具审核
	 * 
	 * @param oid
	 * @param state
	 * @param operator
	 */
	public void check(String oid, String state, String operator) {
		CashTool cashTool = this.findByOid(oid);
		if (null == cashTool || !CashTool.CASHTOOL_STATE_pretrial.equals(cashTool.getState())) {
			throw new RuntimeException();
		}
		cashTool.setState(state);
		cashTool.setUpdateTime(DateUtil.getSqlCurrentDate());
		cashTool.setOperator(operator);
		this.save(cashTool);
	}

}
