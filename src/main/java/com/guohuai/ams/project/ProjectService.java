package com.guohuai.ams.project;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guohuai.ams.investment.Investment;
import com.guohuai.ams.investment.InvestmentDao;
import com.guohuai.ams.investment.InvestmentService;
import com.guohuai.ams.system.config.project.warrantyExpire.CCPWarrantyExpire;
import com.guohuai.ams.system.config.project.warrantyExpire.CCPWarrantyExpireDao;
import com.guohuai.ams.system.config.project.warrantyMode.CCPWarrantyMode;
import com.guohuai.ams.system.config.project.warrantyMode.CCPWarrantyModeDao;
import com.guohuai.component.exception.AMPException;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ProjectService {

	@Autowired
	private ProjectDao projectDao;
	@Autowired
	private InvestmentDao investmentDao;
	@Autowired
	private InvestmentService investmentService;
	@Autowired
	private CCPWarrantyModeDao cCPWarrantyModeDao;
	@Autowired
	private CCPWarrantyExpireDao cCPWarrantyExpireDao;
	
	@PersistenceContext
	private EntityManager em;//注入entitymanager
	
	public Page<Project> list(Specification<Project> spec, int page, int size, String sortDirection, String sortField) {
		Order order = new Order(Direction.valueOf(sortDirection.toUpperCase()), sortField);
		Pageable pageable = new PageRequest(page, size, new Sort(order));
		Page<Project> pagedata = this.projectDao.findAll(spec, pageable);
		return pagedata;
	}

	public Project get(String oid) {
		Project approv = this.projectDao.findOne(oid);
		if (null == approv) {
			throw AMPException.getException("未知的项目ID");
		}
		return approv;
	}

	/**
	 * 保存底层项目
	 * 
	 * @Title: save
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param projectForm
	 * @return
	 * @return Project 返回类型
	 */	
	public Project save(ProjectForm projectForm) {
		if (null == projectForm)
			throw AMPException.getException("底层项目不能为空");
		String targetOid = projectForm.getTargetOid();
		if (StringUtils.isBlank(targetOid))
			throw AMPException.getException("投资标的id不能为空");
		String oid = projectForm.getOid(); // 项目id
		if (StringUtils.isBlank(oid))
			log.debug("投资标的id=[" + targetOid + "]新增底层项目");
		else
			log.debug("投资标的id=[" + targetOid + "]修改底层项目");

		Project prj = new Project();
		BeanUtils.copyProperties(projectForm, prj);	

		Investment investment = this.investmentService.getInvestment(targetOid);
		prj.setInvestment(investment);
		
		/* 计算项目风险系数开始 */
		// 计算公式 : Max(保证方式担保方式权数 * 保证方式担保期数权数, 抵押方式担保方式权数 * 抵押方式担保期数权数, 质押方式担保方式权数 * 质押方式担保期数权数) * 标的信用等级系数
		List<CCPWarrantyMode> modeList = cCPWarrantyModeDao.findAll(); // 查询担保方式权数配置		
		List<CCPWarrantyExpire> expireList = cCPWarrantyExpireDao.findAll(); // 查询担保期限权数配置

		prj = this.calculateProjectRiskFactor(modeList, expireList, investment, prj);
		/* 计算项目风险系数结束 */
		
		prj.setCreateTime(new Timestamp(System.currentTimeMillis()));
		prj.setUpdateTime(new Timestamp(System.currentTimeMillis()));

		Project p = this.projectDao.save(prj);
		
		/* 重新计算标的的风险系数 */
		BigDecimal riskRate = this.getMaxRiskFactor(targetOid);
		log.debug("标的下底层项目最大的[风险系数]为: " + riskRate);
		investment.setRiskRate(riskRate);
		investmentDao.save(investment);
		
		/* 重新计算标的的风险系数 */
		return p;
	}

	

	/**
	 * 计算标的下面所有项目的项目系数
	 * @Title: calculateInvestmentRisk 
	 * @author vania
	 * @version 1.0
	 * @see: TODO
	 * @param targetOid
	 * @return BigDecimal    标的下面所有项目的最大的风险系数
	 */
	public BigDecimal calculateInvestmentRisk(String targetOid) {
		return calculateInvestmentRisk(this.investmentService.getInvestment(targetOid));
	}
	
	/**
	 * 计算标的下面所有项目的项目系数
	 * @Title: calculateInvestmentRisk 
	 * @author vania
	 * @version 1.0
	 * @see: TODO
	 * @param investment
	 * @return BigDecimal    标的下面所有项目的最大的风险系数
	 */
	public BigDecimal calculateInvestmentRisk(Investment investment) {
		BigDecimal max = null;
		String targetOid = investment.getOid();
		if (StringUtils.isBlank(targetOid))
			throw AMPException.getException("投资标的id不能为空");
		List<Project> list = this.findByTargetId(targetOid);
		int size = null == list ? 0 : list.size();
		if (0 == size) {
			log.debug("标的id=" + targetOid + "下暂无底层项目");
			return max;
		}
		List<CCPWarrantyMode> modeList = cCPWarrantyModeDao.findAll(); // 查询担保方式权数配置
		List<CCPWarrantyExpire> expireList = cCPWarrantyExpireDao.findAll(); // 查询担保期限权数配置
		double[] dbs = new double[size]; // 项目风险系数数组
		for (int i = 0; i < size; i++) {
			Project prj = list.get(i);
			this.calculateProjectRiskFactor(modeList, expireList, investment, prj); // 计算项目风险系数
			prj.setInvestment(investment);
			prj = this.projectDao.save(prj); // 重新保存项目
			dbs[i] = list.get(i).getRiskFactor().doubleValue(); // 获取每一个项目的项目风险系数
		}
		// max = getMaxRiskFactor(targetOid); // 通过数据库查询最大的项目风险系数
		max = new BigDecimal(NumberUtils.max(dbs));// 取最大的项目风险系数
		return max;
	}
	
	/**
	 * 计算项目风险系数
	 * @Title: calculateProjectRiskFactor 
	 * @author vania
	 * @version 1.0
	 * @see: TODO
	 * @param modeList
	 * @param expireList
	 * @param investment
	 * @param prj
	 * @return Project    返回类型
	 */
	public Project calculateProjectRiskFactor(List<CCPWarrantyMode> modeList, List<CCPWarrantyExpire> expireList, Investment investment, Project prj) {
		if (null == modeList || null == expireList) {
			log.warn("担保方式权数配置或者担保期限权数配置为空");
			return prj;
		}
		/* 计算项目风险系数开始 */
		// 计算公式 : Max(保证方式担保方式权数 * 保证方式担保期数权数, 抵押方式担保方式权数 * 抵押方式担保期数权数, 质押方式担保方式权数 * 质押方式担保期数权数) * 标的信用等级系数

		BigDecimal guaranteeModeWeight = new BigDecimal(0), mortgageModeWeight = new BigDecimal(0), hypothecationModeWeight = new BigDecimal(0);
		String guaranteeModeOid = prj.getGuaranteeModeOid(), mortgageModeOid = prj.getMortgageModeOid(), hypothecationModeOid = prj.getHypothecationModeOid();
		for (CCPWarrantyMode mode : modeList) {
			String mOid = mode.getOid();
			if (StringUtils.equals(guaranteeModeOid, mOid)) {
				guaranteeModeWeight = mode.getWeight();
				prj.setGuaranteeModeWeight(guaranteeModeWeight);
				prj.setGuaranteeModeTitle(mode.getTitle());
			}
			if (StringUtils.equals(mortgageModeOid, mOid)) {
				mortgageModeWeight = mode.getWeight();
				prj.setMortgageModeWeight(mortgageModeWeight);
				prj.setMortgageModeTitle(mode.getTitle());
			}
			if (StringUtils.equals(hypothecationModeOid, mOid)) {
				hypothecationModeWeight = mode.getWeight();
				prj.setHypothecationModeWeight(mortgageModeWeight);
				prj.setHypothecationModeTitle(mode.getTitle());
			}
		}

		BigDecimal guaranteeModeExpireWeight = new BigDecimal(0), mortgageModeExpireWeight = new BigDecimal(0), hypothecationModeExpireWeight = new BigDecimal(0);
		String guaranteeModeExpireOid = prj.getGuaranteeModeExpireOid(), mortgageModeExpireOid = prj.getMortgageModeExpireOid(), hypothecationModeExpireOid = prj.getHypothecationModeExpireOid();
		for (CCPWarrantyExpire expire : expireList) {
			String eOid = expire.getOid();
			if (StringUtils.equals(guaranteeModeExpireOid, eOid)) {
				guaranteeModeExpireWeight = expire.getWeight();
				prj.setGuaranteeModeExpireWeight(guaranteeModeExpireWeight);
				prj.setGuaranteeModeExpireTitle(expire.getTitle());
			}
			if (StringUtils.equals(mortgageModeExpireOid, eOid)) {
				mortgageModeExpireWeight = expire.getWeight();
				prj.setMortgageModeExpireWeight(mortgageModeExpireWeight);
				prj.setMortgageModeExpireTitle(expire.getTitle());
			}
			if (StringUtils.equals(hypothecationModeExpireOid, eOid)) {
				hypothecationModeExpireWeight = expire.getWeight();
				prj.setHypothecationModeExpireWeight(hypothecationModeExpireWeight);
				prj.setHypothecationModeExpireTitle(expire.getTitle());
			}
		}
		
		BigDecimal guaranteeRato = guaranteeModeWeight.multiply(guaranteeModeExpireWeight); // 担保系数
		BigDecimal mortgageRato = mortgageModeWeight.multiply(mortgageModeExpireWeight); // 抵押系数
		BigDecimal hypothecation = hypothecationModeWeight.multiply(hypothecationModeExpireWeight); // 质押系数

		log.debug("项目的[担保系数]为: " + guaranteeRato);
		log.debug("项目的[抵押系数]为: " + mortgageRato);
		log.debug("项目的[质押系数]为: " + hypothecation);
		double maxRato = NumberUtils.max(new double[] { guaranteeRato.doubleValue(), mortgageRato.doubleValue(), hypothecation.doubleValue() });// 取最大值
		log.debug("项目的[最大系数]为: " + maxRato);

		BigDecimal collectScoreWeight = investment.getCollectScoreWeight(); // 标的[信用等级系数]
		log.debug("标的的[信用等级系数]为: " + collectScoreWeight);
		BigDecimal riskFactor = null;
		if (null != collectScoreWeight) {
			riskFactor = collectScoreWeight.multiply(new BigDecimal(maxRato)); // 计算出项目的风险系数
			prj.setRiskFactor(riskFactor);
		}
		log.debug("计算出项目: [" + prj.getProjectName() + "]的最终的风险系数为: " + riskFactor);
		return prj;
	}

	/**
	 * 求标的下面所有项目的最大的风险系数
	 * 
	 * @Title: getMaxRiskFactor
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param targetOid
	 * @return BigDecimal 返回类型
	 */
	public BigDecimal getMaxRiskFactor(String targetOid) {
		if (StringUtils.isBlank(targetOid))
			throw AMPException.getException("投资标的id不能为空");
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
		Root<Project> root = query.from(Project.class);

		// Expression<BigDecimal> maxExp = root.get("").as(BigDecimal.class);
		// query.select(cb.max(maxExp));
		query.select(cb.max((Expression)root.get("riskFactor")));

		query.where(cb.equal(root.get("investment").get("oid").as(String.class), targetOid));
		return em.createQuery(query).getSingleResult();
	}

	/**
	 * 删除底层项目
	 * @Title: deleteByTargetOidAndOid 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param oid void    返回类型
	 */
	public void deleteByTargetOidAndOid(String targetOid, String oid) {
		this.projectDao.deleteByTargetOidAndOid(targetOid, oid);
	}
	
	/**
	 * 删除底层项目
	 * @Title: deleteByTargetOid 
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param oid void    返回类型
	 */
	public void deleteByTargetOid(String targetOid) {
		this.projectDao.deleteByTargetOid(targetOid);
	}

	/**
	 * 通过标的id查询底层项目
	 * 
	 * @Title: findByTargetId
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param targetOid
	 * @return
	 * @return List<Project> 返回类型
	 */
	public List<Project> findByTargetId(String targetOid) {
		return this.projectDao.findByTargetOid(targetOid);
	}
	
	/**
	 * 通过id查询底层项目
	 * 
	 * @Title: findByOid
	 * @author vania
	 * @version 1.0
	 * @see:
	 * @param oid
	 * @return
	 * @return Project 返回类型
	 */
	public Project findByOid(String oid) {
		return this.projectDao.findOne(oid);
	}

}
