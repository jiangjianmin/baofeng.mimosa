package com.guohuai.mmp.investor.tradeorder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FirstInvestService {

	@Autowired
	FirstInvestDao firstInvestDao;
	
	public void firstInvest(FirstInvestReq req,FirstInvestRes res) {
		String userId = req.getUserOid();
		Long is = 0L;
		if(req.getOrderCode() == null) {
			is = firstInvestDao.investTimes(userId);
			log.info("=========用户{}投资次数为{}==============", userId, is);
		} else {
			is = firstInvestDao.investTimesByOrderCode(req.getUserOid(),  req.getOrderCode());
			log.info("=========用户{}在order code{}之前的投资次数为{}==============", req.getUserOid(), req.getOrderCode(), is);
		}
		if(is==1){
			res.setErrorCode("0");
			res.setErrorMessage("成功");
		}else{
			res.setErrorCode("-1");
			res.setErrorMessage("需完成首次投资后，进行体验金提现");
		}
	}
	
	public long investTimes(FirstInvestReq req) {
		String userId = req.getUserOid();
		Long is = firstInvestDao.investTimes(userId);
		return is;
	}
}
