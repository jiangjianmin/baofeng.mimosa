package com.guohuai.women.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.women.dao.WomenDao;
import com.guohuai.women.to.WomenReq;
import com.guohuai.women.to.WomenRes;


@Service
//@Component
public class WomenService {
	
	@Autowired
	WomenDao womenDao;
	
//	@Value("${laborday.start.time}")
	String startTime;
//	@Value("${laborday.end.time}")
	String endTime;

	public void checkInvest(WomenReq req, WomenRes res) {
		String phone = req.getPhone();
		Long is = womenDao.checkInvest(phone);
//		Long is = womenDao.checkInvest(phone,startTime,endTime);
		if(is>0){
			res.setErrorCode("0");
			res.setErrorMessage("成功");
		}else{
			res.setErrorCode("-2");
			res.setErrorMessage("没有在活动期间投资");
		}
	}

}
