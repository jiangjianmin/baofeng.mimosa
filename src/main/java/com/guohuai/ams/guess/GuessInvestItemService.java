package com.guohuai.ams.guess;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.web.view.BaseRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderEntity;
import com.guohuai.mmp.investor.tradeorder.RedeemInvestTradeOrderReq;
import com.guohuai.mmp.investor.tradeorder.TradeOrderReq;

@Service
public class GuessInvestItemService {
	
	@Autowired
	private GuessInvestItemDao guessInvestItemDao;
	
	@Autowired
	private GuessItemService guessItemService;
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public BaseRep choose(TradeOrderReq tradeOrderReq, InvestorTradeOrderEntity orderEntity) {
		BaseRep rep = new BaseRep();
		String oid = tradeOrderReq.getGuessItemOid();
		GuessInvestItemEntity item = new GuessInvestItemEntity();
		GuessItemEntity guessItem = guessItemService.getByOid(oid);
		InvestorBaseAccountEntity account = orderEntity.getInvestorBaseAccount();
		item.setInvestor(account);
		item.setItem(guessItem);
		item.setOrder(orderEntity);
		guessInvestItemDao.save(item);
		return rep;
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public BaseRep choose(RedeemInvestTradeOrderReq tradeOrderReq, InvestorTradeOrderEntity orderEntity) {
		
		BaseRep rep = new BaseRep();
		String oid = tradeOrderReq.getGuessItemOid();
		GuessInvestItemEntity item = new GuessInvestItemEntity();
		GuessItemEntity guessItem = guessItemService.getByOid(oid);
		InvestorBaseAccountEntity account = orderEntity.getInvestorBaseAccount();
		item.setInvestor(account);
		item.setItem(guessItem);
		item.setOrder(orderEntity);
		guessInvestItemDao.save(item);
		return rep;
	}

}
