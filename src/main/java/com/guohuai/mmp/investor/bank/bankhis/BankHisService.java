//package com.guohuai.mmp.investor.bank.bankhis;
//
//import java.util.List;
//
//import javax.transaction.Transactional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.guohuai.component.util.DateUtil;
//
//@Service
//@Transactional
//public class BankHisService {
//
//	@Autowired
//	private BankHisDao bankHisDao;
//	
//	/**
//	 * 新增
//	 * @param entity
//	 * @return
//	 */
//	public BankHisEntity saveEntity(BankHisEntity entity){
//		entity.setCreateTime(DateUtil.getSqlCurrentDate());
//		return this.updateEntity(entity);
//	}
//	
//	/**
//	 * 修改
//	 * @param entity
//	 * @return
//	 */
//	private BankHisEntity updateEntity(BankHisEntity entity) {
//		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
//		return this.bankHisDao.save(entity);
//	}
//	
//	/**
//	 * 根据用户OID，获取解绑卡记录
//	 * @param investorOid
//	 * @return
//	 */
//	public List<BankHisEntity> findByInvestorOid(String investorOid) {
//		List<BankHisEntity> bankHis = this.bankHisDao.findByInvestorOid(investorOid);
//		return bankHis;
//	}
//	
//}
