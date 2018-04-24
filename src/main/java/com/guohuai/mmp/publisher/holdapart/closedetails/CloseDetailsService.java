package com.guohuai.mmp.publisher.holdapart.closedetails;

import java.math.BigDecimal;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;




@Service
@Transactional
public class CloseDetailsService {

	
	@Autowired
	CloseDetailsDao closeDetailsDao;

	public CloseDetailsEntity createCloseDetails(InvestorTradeOrderEntity investOrder, BigDecimal volume,InvestorTradeOrderEntity redeemOrder) {
		CloseDetailsEntity entity = new CloseDetailsEntity();
		entity.setChangeVolume(volume);
		entity.setRedeemOrder(redeemOrder);
		entity.setInvestOrder(investOrder);
		entity.setChangeDirection(CloseDetailsEntity.DETAIL_changeDirection_out);
		return this.saveEntity(entity);
	}

	private CloseDetailsEntity saveEntity(CloseDetailsEntity entity) {
		return this.closeDetailsDao.save(entity);
	}
	
	
	
}
