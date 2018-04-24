package com.guohuai.ams.cashtool.pool;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.ams.cashtool.CashTool;
import com.guohuai.ams.cashtool.CashToolDao;
import com.guohuai.ams.cashtool.log.CashToolLogService;
import com.guohuai.ams.enums.CashToolEventType;
import com.guohuai.component.exception.AMPException;

/**
 * 
 * <p>
 * Title: CashtoolRevenueService.java
 * </p>
 * <p>
 * 本息兑付Service
 * </p>
 * 
 * @author vania
 * @version 1.0
 * @created 2016年5月18日 下午3:31:20
 */
@Service
@Transactional
public class CashtoolRevenueService {
	@Autowired
	private CashtoolRevenueDao cashtoolRevenueDao;

	@Autowired
	private CashToolDao cashtoolDao;

	@Autowired
	private CashToolLogService cashtoolLogservice;

	/**
	 * 现金管理工具收益采集
	 * 
	 * @Title: save
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param form
	 * @return CashToolRevenue 返回类型
	 */
	public CashToolRevenue save(CashToolRevenueForm form) {
		String cashtoolOid = form.getCashtoolOid();
		if (StringUtils.isBlank(cashtoolOid))
			throw AMPException.getException("现金管理工具id不能为空");

		CashToolRevenue cashToolRevenue = new CashToolRevenue();
		BeanUtils.copyProperties(form, cashToolRevenue);

		CashTool cashTool = this.cashtoolDao.findOne(cashtoolOid);
		if (null == cashTool)
			throw AMPException.getException("找不到id为[" + cashtoolOid + "]的现金管理工具");
		cashToolRevenue.setCashTool(cashTool);

		cashToolRevenue.setCreateTime(new Timestamp(System.currentTimeMillis()));
		cashToolRevenue.setUpdateTime(new Timestamp(System.currentTimeMillis()));

		this.cashtoolLogservice.saveCashToolLog(cashTool, CashToolEventType.revenue, form.getOperator()); // 现金管理工具收益采集

		this.cashtoolRevenueDao.delete(CashToolRevenue.builder().dailyProfitDate(form.getDailyProfitDate()).build());
		return cashtoolRevenueDao.save(cashToolRevenue);
	}

	/**
	 * 分页查询现金收益
	 * 
	 * @Title: CashToolRevenue
	 * @author vania
	 * @version 1.0
	 * @see: TODO
	 * @param spec
	 * @param pageable
	 * @return
	 * @return Page<CashToolRevenue> 返回类型
	 */
	public Page<CashToolRevenue> getCashToolRevenueList(Specification<CashToolRevenue> spec, Pageable pageable) {
		return cashtoolRevenueDao.findAll(spec, pageable);
	}

	public CashToolRevenue findByCashToolAndDate(CashTool cashTool, Date date) {
		List<CashToolRevenue> list = this.cashtoolRevenueDao.findByCashToolAndDailyProfitDate(cashTool, date);
		if (null != list && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
}
