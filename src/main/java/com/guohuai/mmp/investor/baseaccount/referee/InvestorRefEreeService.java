package com.guohuai.mmp.investor.baseaccount.referee;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.util.DateUtil;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;

@Service
@Transactional
public class InvestorRefEreeService {

	@Autowired
	InvestorRefEreeDao investorRefEreeDao;
	
	/**
	 * 更新注册人数
	 * @param refEreeOid
	 * @return
	 */
	public int updateRegister(String refEreeOid) {
		return this.investorRefEreeDao.updateRegister(refEreeOid);
	}
	
	/**
	 * 根据资金账号获取  资金用户-推荐人
	 * @param investorBaseAccount
	 * @return
	 */
	public InvestorRefEreeEntity getInvestorRefEreeByAccount(InvestorBaseAccountEntity investorBaseAccount) {
		return this.investorRefEreeDao.findByInvestorBaseAccount(investorBaseAccount);
	}
	
	/**
	 * 新增
	 * @param entity
	 * @return
	 */
	public InvestorRefEreeEntity saveEntity(InvestorRefEreeEntity entity){
		entity.setCreateTime(DateUtil.getSqlCurrentDate());
		return this.updateEntity(entity);
	}
	
	/**
	 * 修改
	 * @param entity
	 * @return
	 */
	private InvestorRefEreeEntity updateEntity(InvestorRefEreeEntity entity) {
		entity.setUpdateTime(DateUtil.getSqlCurrentDate());
		return this.investorRefEreeDao.save(entity);
	}
}
