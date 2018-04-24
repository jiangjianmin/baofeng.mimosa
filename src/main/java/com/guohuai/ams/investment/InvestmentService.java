package com.guohuai.ams.investment;

import java.math.BigDecimal;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.guohuai.ams.investment.log.InvestmentLogService;
import com.guohuai.ams.investment.manage.InvestmentManageForm;
import com.guohuai.ams.project.ProjectService;
import com.guohuai.ams.system.config.project.warrantor.CCPWarrantor;
import com.guohuai.ams.system.config.project.warrantor.CCPWarrantorService;
import com.guohuai.ams.system.config.risk.indicate.collect.RiskIndicateCollectForm;
import com.guohuai.ams.system.config.risk.indicate.collect.RiskIndicateCollectResp;
import com.guohuai.ams.system.config.risk.indicate.collect.RiskIndicateCollectService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class InvestmentService {

	@Autowired
	private InvestmentDao investmentDao;

	@Autowired
	InvestmentLogService investmentLogService;
	@Autowired
	RiskIndicateCollectService riskIndicateCollectService;
	@Autowired
	CCPWarrantorService cCPWarrantorService;
	@Autowired
	ProjectService projectService;

	// @Autowired
	// WorkflowAssetService workflowAssetService;

	/**
	 * 获得投资标的列表
	 * 
	 * @param spec
	 * @param pageable
	 * @return
	 */
	public Page<Investment> getInvestmentList(Specification<Investment> spec, Pageable pageable) {
		return investmentDao.findAll(spec, pageable);
	}

	/**
	 * 获得投资标的详情
	 * 
	 * @param oid
	 * @return
	 */
	public Investment getInvestmentDet(String oid) {
		return investmentDao.findOne(oid);
	}

	/**
	 * 新建投资标的
	 * 
	 * @param entity
	 * @param operator
	 * @return
	 */
	@Deprecated
	public Investment saveInvestment(Investment entity, String operator) {
		entity.setCreateTime(DateUtil.getSqlCurrentDate());
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		entity.setOperator(operator);
		return this.investmentDao.save(entity);
	}

	/**
	 * 新建投资标的
	 * 
	 * @Title: saveInvestment
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param form
	 * @return Investment 返回类型
	 */
	public Investment saveInvestment(InvestmentManageForm form) {
		Investment entity = createInvestment(form);
		entity.setState(Investment.INVESTMENT_STATUS_waitPretrial);

		entity.setCreateTime(DateUtil.getSqlCurrentDate());
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());

		entity = this.investmentDao.save(entity);
		/* 计算标的风险开始 */
		this.calculateRiskRate(entity);
		/* 计算标的风险结束 */
		// 创建工作流
		// Map<String, Object> vars = new HashMap<String, Object>();
		// vars.put("invesment", entity);
		// workflowAssetService.startWorkflow(form.getOperator(),
		// "targetExamination", entity.getOid(), vars);
		return entity;
	}

	/**
	 * 计算标的风险开始
	 * 
	 * @Title: calculateRiskRate
	 * @author vania
	 * @version 1.0
	 * @see: TODO
	 * @param entity
	 * @return Investment 返回类型
	 */
	public Investment calculateRiskRate(Investment entity) {
		/* 计算标的风险开始 */
		String riskOption = entity.getRiskOption(); // 风险配置对象
		if (StringUtils.isNotBlank(riskOption)) {
			RiskIndicateCollectForm collForm = JSON.parseObject(riskOption, RiskIndicateCollectForm.class);
			collForm.setRelative(entity.getOid()); // 把投资标的的oid存入relative
			List<RiskIndicateCollectResp> list = riskIndicateCollectService.save(collForm);
			// TODO
			// 计算公式: sum(CollectScore)
			int sum = 0; // 标的信用等级评分总和
			BigDecimal weight = null; // 标的信用等级系数
			if (null != list) {
				for (RiskIndicateCollectResp rc : list) {
					sum += rc.getCollectScore();
				}
				log.debug("投资标的的风险总分: " + sum);
				entity.setCollectScore(sum);
				CCPWarrantor cCPWarrantor = cCPWarrantorService.getByScoreBetween(sum); // 根据标的风险总分获取[标的信用等级系数]
				if (null != cCPWarrantor) {
					weight = cCPWarrantor.getWeight();
					log.debug("投资标的id=" + entity.getOid() + "的信用等级对象为: " + JSON.toJSONString(cCPWarrantor));
				} else {
					log.warn("找不到风险总分:" + sum + "对应的信用等级系数区间");
				}
			}
			log.debug("投资标的id=" + entity.getOid() + "的信用等级系数为: " + weight);
			entity.setCollectScoreWeight(weight);

			// 根据[标的信用等级系数]计算本标的下所有项目的[项目系数]
			BigDecimal riskRate = projectService.calculateInvestmentRisk(entity); // 返回标的下面所有项目的最大的风险系数

			// 取max(各个项目的[项目系数])作为标的的[风险系数]
			// riskRate = projectService.getMaxRiskFactor(entity.getOid());
			entity.setRiskRate(riskRate);
			log.debug("投资标的id=" + entity.getOid() + "的风险系数为: " + riskRate);
		}
		/* 计算标的风险结束 */
		return entity;
	}

	/**
	 * 修改投资标的
	 * 
	 * @param entity
	 * @param operator
	 * @return
	 */
	public Investment updateInvestment(Investment entity, String operator) {
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		entity.setOperator(operator);
		entity = this.investmentDao.save(entity);
		/* 计算标的风险开始 */
		this.calculateRiskRate(entity);
		/* 计算标的风险结束 */
		return entity;
	}

	/**
	 * 修改投资标的
	 * 
	 * @param entity
	 * @param operator
	 * @return
	 */
	public Investment updateInvestment(InvestmentManageForm form) {
		Investment investment = getInvestment(form.getOid());
		if (!Investment.INVESTMENT_STATUS_waitPretrial.equals(investment.getState())
				&& !Investment.INVESTMENT_STATUS_reject.equals(investment.getState())) {
			throw new RuntimeException();
		}
		Investment temp = createInvestment(form);
		temp.setState(investment.getState());
		temp.setCreateTime(investment.getCreateTime());
		temp.setCreator(investment.getCreator());

		temp.setUpdateTime(DateUtil.getSqlCurrentDate());
		Investment entity = this.investmentDao.save(temp);
		/* 计算标的风险开始 */
		entity.setRiskOption(form.getRiskOption()); // TODO
													// 因为数据库里面没有RiskOption这个字段
													// 所以要重新设置一下
		entity = this.calculateRiskRate(entity);
		/* 计算标的风险结束 */
		return entity;
	}

	/**
	 * form转entity
	 * 
	 * @param form
	 * @return
	 */
	public Investment createInvestment(InvestmentManageForm form) {
		Investment entity = new Investment();
		try {
			BeanUtils.copyProperties(form, entity);
			// 资产规模 万转元
			if (form.getRaiseScope() != null) {
				BigDecimal yuan = form.getRaiseScope().multiply(new BigDecimal(10000));
				entity.setRaiseScope(yuan);
			}
			// 起购金额 万转元
			if (form.getFloorVolume() != null) {
				BigDecimal yuan = form.getFloorVolume().multiply(new BigDecimal(10000));
				entity.setFloorVolume(yuan);
			}
			// 预计年化收益 百分比转小数
			if (form.getExpAror() != null) {
				BigDecimal decimal = form.getExpAror().divide(new BigDecimal(100));
				entity.setExpAror(decimal);
			}
			// 逾期收益 百分比转小数
			if (form.getOverdueRate() != null) {
				BigDecimal decimal = form.getOverdueRate().divide(new BigDecimal(100));
				entity.setOverdueRate(decimal);
			}

			//募集期收益 百分比转小数
			if (form.getCollectIncomeRate() != null) {
				BigDecimal decimal = form.getCollectIncomeRate().divide(new BigDecimal(100));
				entity.setCollectIncomeRate(decimal);
			}

			String lifeUnit = form.getLifeUnit();// day month year
			int life = form.getLife();
			int lifed = 0;

			if (null == lifeUnit)
				new RuntimeException();
			else if ("month".equals(lifeUnit))
				lifed = life * 30; // 一个月以30天算
			else if ("year".equals(lifeUnit))
				lifed = life * 360; // 一年以360天算
			else
				lifed = life;

			entity.setLifed(lifed);
			return entity;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public Investment getInvestment(String oid) {
		if (null == oid)
			throw AMPException.getException("投资标的ID不能为空");
		Investment old = this.investmentDao.findOne(oid);
		if (null == old)
			throw AMPException.getException("未知的投资标的ID");
		return old;
	}

	/**
	 * 标的预审
	 * 
	 * @param oid
	 * @param state
	 * @param operator
	 * @param trustAmount
	 * @param suggest
	 */
	public Investment precheck(String oid, String state, String operator,BigDecimal trustAmount, String suggest) {
		Investment investment = this.getInvestmentDet(oid);
//		if (investment == null || !Investment.INVESTMENT_STATUS_pretrial.equals(investment.getState())) {
//			throw new RuntimeException();
//		}
		investment.setState(state);
//		if (Investment.INVESTMENT_STATUS_waitMeeting.equals(state)) {
		if (Investment.INVESTMENT_STATUS_metted.equals(state)) {
			investment.setLifeState(Investment.INVESTMENT_LIFESTATUS_PREPARE);  // 设置生命周期状态
			
			if (null == trustAmount || trustAmount.doubleValue() <= 0)
				throw AMPException.getException("授信额度必须大于0");
			trustAmount = trustAmount.multiply(BigDecimal.valueOf(10000)); // 把万转成元
			investment.setTrustAmount(trustAmount);
			investment.setRestTrustAmount(trustAmount); // 初始化剩余授信额度和授信额度一样
		}
		if (!StringUtils.isEmpty(suggest)) {
			investment.setRejectDesc(suggest);
		}
		this.updateInvestment(investment, operator);
		// 工作流
		// String flowState = "reject";
		// if (Investment.INVESTMENT_STATUS_waitMeeting.equals(state))
		// flowState = "pass";
		// workflowAssetService.complete(operator, investment.getOid(),
		// WorkflowConstant.NODEID_DOEXAMINATION, flowState);
		return investment;
	}

	/**
	 * 根据名称模糊查询投资标的
	 * 
	 * @Title: getInvestmentByName
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param name
	 * @return List<Object> 返回类型
	 */
	public List<Object> getInvestmentByName(String name) {
		return this.investmentDao.getInvestmentByName(name);
	}

	/**
	 * 提交预审
	 * 
	 * @param oid
	 * @param operator
	 * @return
	 */
	public Investment comitCheck(String oid, String operator) {
		Investment investment = this.getInvestmentDet(oid);
		if (!Investment.INVESTMENT_STATUS_waitPretrial.equals(investment.getState())
				&& !Investment.INVESTMENT_STATUS_reject.equals(investment.getState())) {
			// 标的状态不是待预审或驳回不能提交预审
			throw new RuntimeException();
		}
		investment.setState(Investment.INVESTMENT_STATUS_pretrial);
		this.updateInvestment(investment, operator);
		// 工作流
		// workflowAssetService.complete(operator, investment.getOid(),
		// WorkflowConstant.NODEID_COMITEXAMINATION, "pass");
		return investment;
	}

	/**
	 * 标的作废
	 * 
	 * @param oid
	 * @param operator
	 * @return
	 */
	public Investment invalid(String oid, String operator) {
		Investment investment = this.getInvestmentDet(oid);
		String statte  = investment.getState();
		if (!Investment.INVESTMENT_STATUS_waitPretrial.equals(statte)
				&& !Investment.INVESTMENT_STATUS_reject.equals(statte)) {
			throw AMPException.getException("投资标的状态不合法");
		}
		investment.setState(Investment.INVESTMENT_STATUS_invalid);
		this.updateInvestment(investment, operator);
		// 工作流

		return investment;
	}

	/**
	 * 标的确认
	 * 
	 * @param oid
	 * @param operator
	 * @return
	 */
	public Investment enter(String oid, String operator) {
		Investment investment = this.getInvestmentDet(oid);
		if (!Investment.INVESTMENT_STATUS_metted.equals(investment.getState()))
			throw new RuntimeException();
		investment.setState(Investment.INVESTMENT_STATUS_collecting);
		this.updateInvestment(investment, operator);
		return investment;
	}

}
