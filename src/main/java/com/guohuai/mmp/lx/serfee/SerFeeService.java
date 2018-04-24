package com.guohuai.mmp.lx.serfee;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.guohuai.ams.product.ProductService;
import com.guohuai.ams.product.productChannel.ProductChannelService;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderService;
import com.guohuai.mmp.platform.accountingnotify.AccountingNotifyEntity;
import com.guohuai.mmp.platform.accountingnotify.AccountingNotifyService;
import com.guohuai.mmp.platform.publisher.offset.PublisherOffsetService;

@Service
@Transactional
public class SerFeeService {
	
	Logger logger = LoggerFactory.getLogger(SerFeeService.class);
	
	@Autowired
	private SerFeeDao serFeeDao;
	@Autowired
	private AccountingNotifyService accountingNotifyService;
	
	

	
	
	public SerFeeQueryRep channelSumAccruedFee(String channelOid) {
		BigDecimal total = this.serFeeDao.channelSumAccruedFee(channelOid);
		SerFeeQueryRep serRep = new SerFeeQueryRep();
		if (total == null) {
			total = new BigDecimal(0);
		}
		serRep.setAccruedSumFee(total);
		serRep.setChannelOid(channelOid);
		
		serRep.setPayPlatformSumFee(new BigDecimal(0));
		serRep.setPayPlatformCouSumFee(new BigDecimal(0));
		
		List<Object[]> aRep = accountingNotifyService.totalActualOfPayment(channelOid);
		for (Object[] tmp : aRep) {
			
			if(AccountingNotifyEntity.NOTIFY_notifyType_payPlatform.equals(tmp[0])){
				if (tmp[1] == null){
					serRep.setPayPlatformSumFee(new BigDecimal(0));
				} else {
					serRep.setPayPlatformSumFee(new BigDecimal(String.valueOf(tmp[1])));
				}
			}
			//手续费
			if(AccountingNotifyEntity.NOTIFY_notifyType_payPlatformFee.equals(tmp[0])){
				if (tmp[1] == null){
					serRep.setPayPlatformCouSumFee(new BigDecimal(0));
				} else {
					serRep.setPayPlatformCouSumFee(new BigDecimal(String.valueOf(tmp[1])));
				}
			}
		}
		return serRep;
	}
	
	public SerFeeQueryRep findFeeByDate(String productOid,Date queryDate) {
		BigDecimal fee = this.serFeeDao.findFeeByDate(productOid,queryDate);
		SerFeeQueryRep serRep = new SerFeeQueryRep();
		if (fee == null) {
			fee = new BigDecimal(0);
		}
		serRep.setFee(fee);
		serRep.setProductOid(productOid);
		serRep.setTday(queryDate);
		return serRep;
	}

	public PageResp<SerFeeQueryRep> getAccruedFeeListByOid(String productOid, Pageable pageable) {
		List<SerFeeQueryRep> formList = Lists.newArrayList();
		Page<SerFeeEntity> serList =  serFeeDao.findAccruedFeeByPOid(productOid,pageable);
		List<SerFeeEntity> contentList = serList.getContent();
		if (null != contentList && !contentList.isEmpty()) {
			for (SerFeeEntity tmp : contentList) {
				SerFeeQueryRep sRep = new SerFeeQueryRep();
				sRep.setTday(tmp.getTDay());
				sRep.setFee(tmp.getFee());
				sRep.setFeePercent(tmp.getFeePercent());
				sRep.setProductOid(tmp.getProduct().getOid());
				formList.add(sRep);
			}
		}
		PageResp<SerFeeQueryRep> repPage = new PageResp<SerFeeQueryRep>();
		repPage.setRows(formList);
		repPage.setTotal(serList.getTotalElements());
		return repPage;
	}

	public SerFeeEntity saveEntity(SerFeeEntity fee) {
		return this.serFeeDao.save(fee);
		
	}
}
   