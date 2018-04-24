package com.guohuai.mmp.platform.superacc.order;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.component.web.view.PagesRep;

@Service
@Transactional
public class PlatformSuperaccOrderService {

	Logger logger = LoggerFactory.getLogger(PlatformSuperaccOrderService.class);

	@Autowired
	private PlatformSuperaccOrderDao platformSuperaccOrderDao;

	public PagesRep<PlatformSuperaccOrderQueryRep> mng(Specification<PlatformSuperaccOrderEntity> spec,
			Pageable pageable) {
		Page<PlatformSuperaccOrderEntity> accounts = this.platformSuperaccOrderDao.findAll(spec, pageable);
		PagesRep<PlatformSuperaccOrderQueryRep> pagesRep = new PagesRep<PlatformSuperaccOrderQueryRep>();		

		for (PlatformSuperaccOrderEntity en : accounts) {
				
			PlatformSuperaccOrderQueryRep rep = new PlatformSuperaccOrderQueryRep();
			rep.setOrderCode(en.getOrderCode());
			rep.setOrderType(en.getOrderType());
			rep.setOrderTypeDisp(this.orderTypeEn2Ch(en.getOrderType()));
			rep.setOrderAmount(en.getOrderAmount());
			rep.setOrderStatus(en.getOrderStatus());
			rep.setOrderStatusDisp(this.orderStatusEn2Ch(en.getOrderStatus()));
			rep.setRelatedAcc(this.relatedAccEn2Ch(en.getRelatedAcc()));
			rep.setCompleteTime(en.getCompleteTime());
			rep.setUpdateTime(en.getUpdateTime());
			rep.setCreateTime(en.getCreateTime());
			pagesRep.add(rep);
		}
		pagesRep.setTotal(accounts.getTotalElements());	
		return pagesRep;
	}
	
	
	private String orderStatusEn2Ch(String orderStatus) {
		if (PlatformSuperaccOrderEntity.ORDER_orderStatus_paySuccess.equals(orderStatus)) {
			return "支付成功";
		}
		return orderStatus;
	}
	
	private String relatedAccEn2Ch(String relatedAcc) {
		if (PlatformSuperaccOrderEntity.ORDER_relatedAcc_superAcc.equals(relatedAcc)) {
			return "超级户";
		}
		return relatedAcc;
	}
	
	private String orderTypeEn2Ch(String orderType) {
		if (PlatformSuperaccOrderEntity.ORDER_orderType_borrow.equals(orderType)) {
			return "借款";
		}
		if (PlatformSuperaccOrderEntity.ORDER_orderType_return.equals(orderType)) {
			return "还款";
		}
		return orderType;
		
	}
	
	public PlatformSuperaccOrderEntity saveEntity(PlatformSuperaccOrderEntity entity) {
		return this.platformSuperaccOrderDao.save(entity);
	}


}
