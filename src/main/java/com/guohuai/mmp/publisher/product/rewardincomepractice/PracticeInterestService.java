package com.guohuai.mmp.publisher.product.rewardincomepractice;

import java.math.BigDecimal;
import java.sql.Date;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.product.reward.ProductIncomeRewardCacheService;
import com.guohuai.ams.product.reward.ProductIncomeRewardSnapshotService;
import com.guohuai.mmp.publisher.holdapart.snapshot.SnapshotService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PracticeInterestService {
	@Autowired
	private SnapshotService snapshotService;
	@Autowired
	private ProductIncomeRewardCacheService productIncomeRewardCacheService;
	@Autowired
	private PracticeServiceRequireNew practiceServiceRequireNew;
	@Autowired
	private ProductIncomeRewardSnapshotService productIncomeRewardSnapshotService;
	
	/**
	 * 基于快照表试算出收益
	 * 
	 * @param product
	 * @param incomeDate
	 * @param incomeAllocate
	 * @param baseIncomeRatio
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public void practiceInterest(String productOid, Date incomeDate) {
		// 复利模式
		if (this.productIncomeRewardCacheService.hasRewardIncome(productOid)) {// 有定义奖励收益
			this.snapshotService.distributeInterestCompoundWithRewardIncome(productOid, BigDecimal.ZERO,
					incomeDate);
			// 更新到试算表
			this.practiceServiceRequireNew.delByProductAndTDate(productOid, incomeDate);
			this.snapshotService.practiceDistributeInterestWithRewardIncome(productOid, incomeDate);
			productIncomeRewardSnapshotService.snapShotProductIncomeReward(productOid, incomeDate);
		} else {
			// 复利无奖励收益
			this.snapshotService.distributeInterestCompoundWithoutRewardIncome(productOid, BigDecimal.ZERO,
					incomeDate);
			// 更新到试算表
			this.practiceServiceRequireNew.delByProductAndTDate(productOid, incomeDate);
			this.snapshotService.practiceDistributeInterestWithoutRewardIncome(productOid, incomeDate);
		}
		log.info("handle practiceInterest,productOid={},incomeDate={}",productOid,incomeDate);
	}
}
