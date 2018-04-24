package com.guohuai.mmp.platform.finance.result;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountDao;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;

@Service
public class PlatformFinanceCompareDataResultService {
	
	@Autowired
	PlatformFinanceCompareDataResultDao platformFinanceCompareDataResultDao;
	@Autowired
	InvestorBaseAccountDao investorBaseAccountDao;
	
	public PagesRep<PlatformFinanceCompareDataResultRep> checkResultList(Specification<PlatformFinanceCompareDataResultEntity> spec,
			Pageable pageable) {
		Page<PlatformFinanceCompareDataResultEntity> list = this.platformFinanceCompareDataResultDao.findAll(spec, pageable);
		PagesRep<PlatformFinanceCompareDataResultRep> pagesRep = new PagesRep<PlatformFinanceCompareDataResultRep>();
		for (PlatformFinanceCompareDataResultEntity entity : list) {
			PlatformFinanceCompareDataResultRep rep = new PlatformFinanceCompareDataResultRep();
			if(null != entity.getCheckInvestorOid() && !"".equals(entity.getCheckInvestorOid())){
				InvestorBaseAccountEntity checkInvestor=investorBaseAccountDao.findByMemberId(entity.getCheckInvestorOid());
				rep.setCheckInvestorOid(checkInvestor.getMemberId());
				rep.setCheckInvestorName(checkInvestor.getRealName());
			}
			rep.setCheckOrderAmount(entity.getCheckOrderAmount());
			rep.setCheckOrderCode(entity.getCheckOrderCode());
			rep.setCheckOrderStatus(entity.getCheckOrderStatus());
			rep.setCheckOrderType(entity.getCheckOrderType());
			
			if(null !=entity.getInvestorOid() && !"".equals(entity.getInvestorOid())){
				InvestorBaseAccountEntity investor=investorBaseAccountDao.findByMemberId(entity.getInvestorOid());
				rep.setInvestorOid(investor.getMemberId());
				rep.setInvestorName(investor.getRealName());
			}
			rep.setOrderAmount(entity.getOrderAmount());
			rep.setOrderCode(entity.getOrderCode());
			rep.setOrderStatus(entity.getOrderStatus());
			rep.setOrderType(entity.getOrderType());
			
			rep.setCheckOid(entity.getCheckOid());
			rep.setBuzzDate(entity.getBuzzDate());
			rep.setCheckStatus(entity.getCheckStatus());
			rep.setCreateTime(entity.getCreateTime());
			rep.setUpdateTime(entity.getUpdateTime());
			rep.setDealStatus(entity.getDealStatus());
			rep.setOid(entity.getOid());
			pagesRep.add(rep);
		}
		pagesRep.setTotal(list.getTotalElements());
		return pagesRep;
	}
	
	public List<List<String>> data(String orderCode,String orderAmount,String orderStatus,String orderType,String checkStatus,String checkDate,String orderAmountMax,String checkOrderStatus){
		orderCode = StringStr(orderCode);
		orderAmount = StringStr(orderAmount);
		orderStatus = StringStr(orderStatus);
		orderType = StringStr(orderType);
		checkStatus = StringStr(checkStatus);
		checkDate = StringStr(checkDate);
		orderAmountMax = StringStr(orderAmountMax);
		checkOrderStatus = StringStr(checkOrderStatus);
		List<Object[]> list = this.platformFinanceCompareDataResultDao.findCompareDataResultDown(orderCode, orderAmount, orderStatus, orderType, checkStatus, checkDate, orderAmountMax, checkOrderStatus);
		List<List<String>> result = new ArrayList<List<String>>();
		
		for(Object[] obj : list){
			List<String> line = new ArrayList<String>();
			for(Object o : obj){
				line.add(StringStr(o));
			}
			result.add(line);
		}
		return result;
	}
	
	public String StringStr(Object obj){
		if(obj == null || obj.equals("null")){
			return "";
		}else{
			return String.valueOf(obj);
		}
	}
	
	public List<String> header() {
		List<String> header = new ArrayList<String>();
		header.add("订单号");
		header.add("订单类型");
		header.add("交易金额");
		header.add("订单状态");
		header.add("业务日期");
		header.add("投资人");
		header.add("对账订单号");
		header.add("对账订单类型");
		header.add("对账交易金额");
		header.add("对账订单状态");
		header.add("对账投资人");
		header.add("对账状态");
		header.add("处理结果");
		return header;
	}

}
