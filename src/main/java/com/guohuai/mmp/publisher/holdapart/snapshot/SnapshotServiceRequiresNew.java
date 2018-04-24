package com.guohuai.mmp.publisher.holdapart.snapshot;

import java.math.BigDecimal;
import java.sql.Date;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class SnapshotServiceRequiresNew {
	@Autowired
	private SnapshotDao snapshotDao;
	
	/**
	 * 重新同步在派发收益日期之后已经拍过快照的数据
	 * 
	 * @param productOid
	 * @param incomeDate
	 * @param afterIncomeDate
	 * @param netUnitShare
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int reupdateAfterIncomeDateSnapshot(String productOid, Date incomeDate, Date afterIncomeDate,
			BigDecimal netUnitShare){
		int result=this.snapshotDao.reupdateAfterIncomeDateSnapshot(productOid, incomeDate, afterIncomeDate, netUnitShare);
		log.info("重新同步在派发收益日期之后已经拍过快照的数据，productOid{},incomeDate{},afterIncomeDate{},结果为{}",productOid,incomeDate,afterIncomeDate,result>0);
		return result;
	}
	
	/**
	 * 收益更新到投资统计信息
	 * @param productOid
	 * @param incomeDate
	 * @return
	 */
	@Transactional(value=Transactional.TxType.REQUIRES_NEW)
	public int distributeInterestToInvestorStatistic(String productOid,Date incomeDate,String ptype){
		int result= this.snapshotDao.distributeInterestToInvestorStatistic(productOid, incomeDate,ptype);
		log.info("结束收益更新到投资统计信息，结果为{}",result>0);
		return result;
	}
}
