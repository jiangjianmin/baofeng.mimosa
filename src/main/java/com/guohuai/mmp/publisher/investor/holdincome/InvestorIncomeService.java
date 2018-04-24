package com.guohuai.mmp.publisher.investor.holdincome;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.component.web.view.RowsRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountService;
import com.guohuai.mmp.investor.baseaccount.statistics.InvestorStatisticsEntity;
import com.guohuai.mmp.investor.baseaccount.statistics.InvestorStatisticsService;
import com.guohuai.mmp.publisher.investor.holdincome.InvestorIncomeQueryRep.InvestorIncomeQueryRepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * 发行人-投资人-合仓收益明细
 * @author xjj
 *
 */
@Service
@Transactional
public class InvestorIncomeService {

	@Autowired
	private InvestorIncomeDao investorIncomeDao;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private InvestorBaseAccountService  investorBaseAccountService;
	@Autowired
	private InvestorStatisticsService investorStatisticsService;
	@Autowired
	private ProductIncomeRewardCacheService incomeRewardService;
	
	@Transactional
	public PagesRep<InvestorIncomeQueryRep> investorIncomeQuery(Specification<InvestorIncomeEntity> spec, Pageable pageable) {		
		Page<InvestorIncomeEntity> incomes = this.investorIncomeDao.findAll(spec, pageable);
		PagesRep<InvestorIncomeQueryRep> pagesRep = new PagesRep<InvestorIncomeQueryRep>();

		for (InvestorIncomeEntity income : incomes) {
			InvestorIncomeQueryRep rep = new InvestorIncomeQueryRepBuilder()
					.oid(income.getOid())
					.productCode(income.getProduct().getCode())
					.productName(income.getProduct().getName())
					.incomeAmount(income.getIncomeAmount())
					.baseAmount(income.getBaseAmount())
					.rewardAmount(income.getRewardAmount())
					.accureVolume(income.getAccureVolume())
					.confirmDate(income.getConfirmDate())
					.createTime(income.getCreateTime())
					.build();
			pagesRep.add(rep);
		}
		pagesRep.setTotal(incomes.getTotalElements());	
		return pagesRep;
	}
	
	public InvestorIncomeEntity saveEntity(InvestorIncomeEntity holdIncome) {
		holdIncome.setCreateTime(DateUtil.getSqlCurrentDate());
		return this.updateEntity(holdIncome);
	}

	public InvestorIncomeEntity updateEntity(InvestorIncomeEntity holdIncome) {
		holdIncome.setUpdateTime(DateUtil.getSqlCurrentDate());
		return this.investorIncomeDao.save(holdIncome);
	}

	public List<InvestorIncomeEntity> findByInvestorOidAndConfirmDate(String investorOid, String incomeDate) {
		
		return this.investorIncomeDao.findByInvestorOidAndConfirmDate(investorOid, incomeDate);
	}

	public List<InvestorIncomeEntity> findByInvestorOidAndConfirmDateInHis(String investorOid, String incomeDate) {
		String tName = "select * from T_MONEY_PUBLISHER_INVESTOR_HOLDINCOME_" + incomeDate.replace("-", "");
		String where = " where investorOid = '" + investorOid + "' and confirmDate = '" + incomeDate +"'";
		@SuppressWarnings("unchecked")
		List<InvestorIncomeEntity> list = em.createNativeQuery(tName + where, InvestorIncomeEntity.class).getResultList();
		return list;
	}
	
	/** 
	 * 针对 产品的收益明细、累计收益 
	 */
	public MyInvestorIncomeRep queryAloneHoldIncome(Specification<InvestorIncomeEntity> spec, Pageable pageable) {
		MyInvestorIncomeRep rep = new MyInvestorIncomeRep();

		Page<InvestorIncomeEntity> page = this.investorIncomeDao.findAll(spec, pageable);
		PagesRep<InvestorIncomeRep> pagesRep = new PagesRep<InvestorIncomeRep>();
		if (page != null && page.getContent() != null && page.getTotalElements() > 0) {
			List<InvestorIncomeRep> rows = new ArrayList<InvestorIncomeRep>();
			for (InvestorIncomeEntity p : page) {
				InvestorIncomeRep queryRep = new InvestorIncomeRep();
				queryRep.setAmount(p.getIncomeAmount());
				queryRep.setTime(p.getConfirmDate());
				queryRep.setProductName(p.getProduct().getName());
				rows.add(queryRep);
			}
			pagesRep.setRows(rows);
			pagesRep.setTotal(page.getTotalElements());

			rep.setDetails(pagesRep);
			if (page.hasContent()) {
				rep.setTotalIncome(page.getContent().get(0).getPublisherHold().getHoldTotalIncome());
			}
			
		}

		return rep;
	}
	
	/**
	 * 查询 用户收益 (可根据产品OID查询)
	 */
	public PagesRep<InvestorIncomeRep> queryInvestorIncome(Specification<InvestorIncomeEntity> spec,
			Pageable pageable) {

		Page<InvestorIncomeEntity> page = this.investorIncomeDao.findAll(spec, pageable);
		PagesRep<InvestorIncomeRep> pagesRep = new PagesRep<InvestorIncomeRep>();
		if (page != null && page.getContent() != null && page.getTotalElements() > 0) {
			List<InvestorIncomeRep> rows = new ArrayList<InvestorIncomeRep>();
			for (InvestorIncomeEntity p : page) {
				InvestorIncomeRep queryRep = new InvestorIncomeRep();
				queryRep.setAmount(p.getIncomeAmount());
				queryRep.setTime(p.getConfirmDate());
				queryRep.setProductName(p.getProduct().getName());
				rows.add(queryRep);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(page.getTotalElements());

		return pagesRep;
	}
	
	/**
	 * 我的收益明细
	 */
	public PagesRep<MyInvestorIncomeDetailsRep> findMyIncomeByPages(Specification<InvestorIncomeEntity> spec ,Pageable pageable) {

		Page<InvestorIncomeEntity> page = this.investorIncomeDao.findAll(spec,pageable);
		PagesRep<MyInvestorIncomeDetailsRep> pagesRep = new PagesRep<MyInvestorIncomeDetailsRep>();
		if (page != null && page.getContent() != null && page.getTotalElements() > 0) {
			List<MyInvestorIncomeDetailsRep> rows = new ArrayList<MyInvestorIncomeDetailsRep>();
			for (InvestorIncomeEntity p : page) {
				MyInvestorIncomeDetailsRep queryRep = new MyInvestorIncomeDetailsRep(p);
				rows.add(queryRep);
			}
			pagesRep.setRows(rows);
		}
		pagesRep.setTotal(page.getTotalElements());
		
		return pagesRep;
	}
	
	/** 
	 * 累计 收益 页面 
	 */
	public IncomeRep queryMyTotalIncomeByDate(String userOid, String yearMonth) {
		
		
		IncomeRep incomeRep = new IncomeRep();
		RowsRep<MyInvestorIncomeOfDateRep> details = new RowsRep<MyInvestorIncomeOfDateRep>();
		incomeRep.setDetails(details);
		
		InvestorBaseAccountEntity baseAccount = this.investorBaseAccountService.findByUid(userOid);
		InvestorStatisticsEntity st = this.investorStatisticsService.findByInvestorBaseAccount(baseAccount);
		incomeRep.setConfirmDate(st.getIncomeConfirmDate());
		incomeRep.setTotalIncome(st.getTotalIncomeAmount());
		List<InvestorIncomeEntity> incomes = this.investorIncomeDao.queryIncomeByYearMonth(baseAccount.getOid(), yearMonth);
		Date confirmDate = new Date(0L);
		MyInvestorIncomeOfDateRep dateRep = null;
		for (InvestorIncomeEntity income : incomes) {
			
			if (confirmDate.compareTo(income.getConfirmDate()) != 0) {
				dateRep = new MyInvestorIncomeOfDateRep();
				details.add(dateRep);
				confirmDate = income.getConfirmDate();
				dateRep.setDate(confirmDate);
			}
			if (Product.TYPE_Producttype_02.equals(income.getProduct().getType().getOid())) {
				if(incomeRewardService.hasRewardIncome(income.getProduct().getOid())) {
					dateRep.setJjgIncome(dateRep.getJjgIncome().add(income.getIncomeAmount()));
				} else {
					dateRep.setT0Income(dateRep.getT0Income().add(income.getIncomeAmount()));
				}
			}
			if (Product.TYPE_Producttype_01.equals(income.getProduct().getType().getOid())) {
				dateRep.setTnIncome(dateRep.getTnIncome().add(income.getIncomeAmount()));
			}
			if (Product.TYPE_Producttype_04.equals(income.getProduct().getType().getOid())) {
				dateRep.setKdbIncome(dateRep.getTnIncome().add(income.getIncomeAmount()));
			}
		}
		return incomeRep;
	}

	/** 
	 * 累计 收益 分页页面 
	 */
	public IncomeRep queryMyJjgIncomeByDate(String userOid, int pageNo, int pageSize) {
		
		
		IncomeRep incomeRep = new IncomeRep();
		RowsRep<MyInvestorIncomeOfDateRep> details = new RowsRep<MyInvestorIncomeOfDateRep>();
		incomeRep.setDetails(details);
		
		InvestorBaseAccountEntity baseAccount = this.investorBaseAccountService.findByUid(userOid);
		InvestorStatisticsEntity st = this.investorStatisticsService.findByInvestorBaseAccount(baseAccount);
		incomeRep.setConfirmDate(st.getIncomeConfirmDate());
		incomeRep.setTotalIncome(st.getTotalIncomeAmount());
		
		List<String> productOidList = incomeRewardService.getAwardProductOid();
		if(!productOidList.isEmpty()) {
			incomeRep.setTotal(this.investorIncomeDao.queryCountIncome(baseAccount.getOid(), productOidList));
			List<InvestorIncomeEntity> incomes = this.investorIncomeDao.queryIncomeByPage(baseAccount.getOid(), incomeRewardService.getAwardProductOid(),(pageNo-1) * pageSize, pageSize);
			MyInvestorIncomeOfDateRep dateRep = null;
			for (InvestorIncomeEntity income : incomes) {
				dateRep = new MyInvestorIncomeOfDateRep();
				details.add(dateRep);
				dateRep.setDate(income.getConfirmDate());
				dateRep.setJjgIncome(dateRep.getJjgIncome().add(income.getIncomeAmount()));
			}
		}
		return incomeRep;
	}
}

