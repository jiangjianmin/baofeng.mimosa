package com.guohuai.mmp.publisher.investor;

import java.math.BigDecimal;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.duration.fact.income.IncomeAllocate;
import com.guohuai.ams.duration.fact.income.IncomeAllocateDao;
import com.guohuai.ams.duration.fact.income.IncomeEvent;
import com.guohuai.ams.duration.fact.income.IncomeEventDao;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.DecimalUtil;
import com.guohuai.component.util.StringUtil;

@Service
@Transactional
public class InterestRequireNew {
	
	@Autowired
	private IncomeEventDao incomeEventDao;
	@Autowired
	private IncomeAllocateDao incomeAllocateDao;
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public IncomeAllocate newAllocate(InterestReq ireq) {
		IncomeEvent incomeEvent = new IncomeEvent();
		incomeEvent.setOid(StringUtil.uuid());
		incomeEvent.setAssetPool(ireq.getProduct().getAssetPool());
		incomeEvent.setBaseDate(ireq.getIncomeDate());
		incomeEvent.setAllocateIncome(ireq.getIncomeAmount());// 总分配收益
		incomeEvent.setCreator("system");
		incomeEvent.setCreateTime(DateUtil.getSqlCurrentDate());
		incomeEvent.setDays(1);
		incomeEvent.setStatus(IncomeEvent.STATUS_Create);
		incomeEventDao.save(incomeEvent);
		
		
		IncomeAllocate incomeAllocate = new IncomeAllocate();
		incomeAllocate.setOid(StringUtil.uuid());
		incomeAllocate.setIncomeEvent(incomeEvent);
		incomeAllocate.setProduct(ireq.getProduct());
		incomeAllocate.setAllocateIncomeType(ireq.getIncomeType());
		incomeAllocate.setBaseDate(incomeEvent.getBaseDate());
		incomeAllocate.setCapital(ireq.getTotalInterestedVolume());// 产品可计息规模
		incomeAllocate.setAllocateIncome(incomeEvent.getAllocateIncome());
		incomeAllocate.setLeftAllocateBaseIncome(ireq.getIncomeAmount());
		incomeAllocate.setLeftAllocateRewardIncome(BigDecimal.ZERO);
		incomeAllocate.setRewardIncome(BigDecimal.ZERO);// 奖励收益
		incomeAllocate.setRatio(ireq.getRatio());// 年化收益
		incomeAllocate.setWincome(ireq.getProduct().getRecPeriodExpAnYield().multiply(new BigDecimal(10000))
				.divide(new BigDecimal(ireq.getProduct().getIncomeCalcBasis()), DecimalUtil.scale, DecimalUtil.roundMode)); //万份收益

		incomeAllocate.setDays(1);// 收益分配天数
		incomeAllocate.setSuccessAllocateIncome(BigDecimal.ZERO);// 成功分配基础收益
		incomeAllocate.setSuccessAllocateRewardIncome(BigDecimal.ZERO);
		incomeAllocate.setLeftAllocateIncome(incomeAllocate.getAllocateIncome().add(incomeAllocate.getRewardIncome()));// 剩余收益
		incomeAllocate.setSuccessAllocateInvestors(0);// 成功分配投资者数
		incomeAllocate.setFailAllocateInvestors(0);// 失败分配投资者数
		incomeAllocateDao.save(incomeAllocate);
		
		
		incomeEvent.setStatus(IncomeEvent.STATUS_Allocating);
		incomeEvent.setAuditor("system");
		incomeEvent.setAuditTime(DateUtil.getSqlCurrentDate());
		incomeEventDao.save(incomeEvent);
		
		return incomeAllocate;
	}
}
