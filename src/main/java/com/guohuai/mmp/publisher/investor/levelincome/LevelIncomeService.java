package com.guohuai.mmp.publisher.investor.levelincome;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.duration.fact.income.IncomeDistributionService;
import com.guohuai.component.util.BigDecimalUtil;




@Service
@Transactional
public class LevelIncomeService {

	
	@Autowired
	LevelIncomeDao levelIncomeDao;
	@Autowired
	IncomeDistributionService incomeDistributionService;
	
	@PersistenceContext
	private EntityManager em;//注入entitymanager
	
	/**
	 * 批量持久化<<阶段收益>>
	 * @param values
	 */
	public void saveBatch(Collection<LevelIncomeEntity> values) {
		this.levelIncomeDao.save(values);
	}

//	public LevelHoldResponse queryBalanceGroupByLevel(String investorOid, String productOid, String confirmDate) {
//		
//		List<LevelHold> levelHolds = new ArrayList<LevelHold>();
//		List<LevelIncomeEntity> list = null;
//		if (null != confirmDate) {
//			list = levelIncomeDao.queryBalanceGroupByLevel(investorOid, productOid, confirmDate);
//			if (list.size() == 0) {
//				list=this.queryBalanceGroupByLevelInHist(investorOid, productOid, confirmDate);
//			}
//		} else {
//			list = this.queryBalanceGroupByLevel(investorOid, productOid);
//		}
//		
//		for (LevelIncomeEntity entity : list) {
//			LevelHold levelHold = new LevelHold();
//			levelHold.setProductOid(productOid);
//			levelHold.setValue(ProductDecimalFormat.format2Cent(entity.getValue()));
//			levelHold.setHoldVolume(ProductDecimalFormat.format2Cent(entity.getAccureVolume()));
//			levelHold.setHoldYesterdayIncome(ProductDecimalFormat.format2Cent(entity.getIncomeAmount()));
//			levelHold.setConfirmDate(entity.getConfirmDate());
//			levelHold.setUpdateTime(entity.getUpdateTime());
//			levelHolds.add(levelHold);
//		}
//		return LevelHoldResponse.builder().levelHolds(levelHolds).build();
//	}

	private List<LevelIncomeEntity> queryBalanceGroupByLevel(String investorOid, String productOid) {
		java.sql.Date incomeDate = incomeDistributionService.getLatestIncomeDate(productOid);
		List<LevelIncomeEntity> list = levelIncomeDao.queryBalanceGroupByLevel(investorOid, productOid, incomeDate.toString());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	private List<LevelIncomeEntity> queryBalanceGroupByLevelInHist(String investorOid, String productOid, String confirmDate) {
		try {
			String tn="T_MONEY_PUBLISHER_INVESTOR_LEVELINCOME"+confirmDate.replaceAll("-", "");
			String sql="select * from "+tn+" where investorOid = '"+investorOid+"' and productOid = '"+productOid+"' and confirmDate = "+confirmDate;
			return this.em.createNativeQuery(sql, LevelIncomeEntity.class).getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<LevelIncomeEntity>();
		}
	}
	
	/**
	 * <pre>
	 *  查询投资人某产品的分档明细（奖励收益率，起始日期，结束日期，最新市值，收益金额，阶段）
	 * 
	 *  以产品奖励设置为主显示，关联我的阶段收益。如果产品奖励的对应阶段无我的阶段收益，则我的阶段收益显示为0
	 * </pre>
	 *  */
	public List<LevelIncomeRep> queryLevelDetialsForInvestor(String userOid,String productOid){
		List<LevelIncomeRep> repList = new ArrayList<LevelIncomeRep>();
		List<Object[]> levelList = this.levelIncomeDao.queryLevelIncome(userOid,productOid);
		if(levelList!=null){
				for (Object[] levelInfo : levelList) {
					LevelIncomeRep lrep = new LevelIncomeRep();
					lrep.setRatio(BigDecimalUtil.parseFromObject(levelInfo[0]).multiply(new BigDecimal("100.00")));//奖励收益率
					lrep.setStartDate(parseIntegerFromObject(levelInfo[1]));//起始日
					lrep.setEndDate(parseIntegerFromObject(levelInfo[2]));//endDate
					lrep.setValue(BigDecimalUtil.parseFromObject(levelInfo[3]));//value
					lrep.setIncomeAmount(BigDecimalUtil.parseFromObject(levelInfo[4]));//奖励金额
					lrep.setLevel(String.valueOf(levelInfo[5]));//阶段
					repList.add(lrep);
				}
			}
		return repList;
	}

   private Integer parseIntegerFromObject(Object obj) {
	  return obj == null ? null : Integer.valueOf(obj.toString());
   }
}
