package com.guohuai.mmp.publisher.hold;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.mmp.publisher.corporate.Corporate;
import com.guohuai.mmp.publisher.corporate.CorporateDao;

/**
 * 运营管理 - 持有人名册
 * @author star.zhu
 * 2016年6月25日
 */
@Service
public class HoldQueryService {

	@Autowired
	private PublisherHoldDao holdDao;
	@Autowired
	private CorporateDao corporateDao;
	
	public PageResp<HoldQueryRep> getDataByParams(Specification<PublisherHoldEntity> spec, Pageable pageable) {
		PageResp<HoldQueryRep> rep = new PageResp<HoldQueryRep>();
		Page<PublisherHoldEntity> list = holdDao.findAll(spec, pageable);
		if (null != list && !list.getContent().isEmpty()) {
			List<HoldQueryRep> repList = Lists.newArrayList();
			HoldQueryRep hold = null;
			for (PublisherHoldEntity entity : list.getContent()) {
				hold = new HoldQueryRep();
				hold.setHoldOid(entity.getOid());
				if (null != entity.getProduct()) {
					hold.setProductOid(entity.getProduct().getOid());
					hold.setProductName(entity.getProduct().getName());
					hold.setProductCode(entity.getProduct().getCode());
				}
				if (null != entity.getPublisherBaseAccount()) {
					hold.setSpvOid(entity.getPublisherBaseAccount().getOid());
					Corporate corporate = this.corporateDao.findOne(entity.getPublisherBaseAccount().getCorperateOid());
					if(corporate!=null) {
						hold.setSpvName(corporate.getName());
					}
				}
				if (PublisherHoldEntity.PUBLISHER_accountType_SPV.equals(entity.getAccountType()) ) {
					if (null != entity.getPublisherBaseAccount()) {
						hold.setInvestorOid(hold.getSpvName());
					}
					hold.setPhoneNum(hold.getInvestorOid());
				} else {
					hold.setInvestorOid(entity.getInvestorBaseAccount().getOid());
					
					if(!StringUtil.isEmpty(entity.getInvestorBaseAccount().getPhoneNum())) {
						hold.setPhoneNum(entity.getInvestorBaseAccount().getPhoneNum().replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2"));
					}
				}
				hold.setAccountType(entity.getAccountType());
				hold.setTotalVolume(entity.getTotalVolume());
				hold.setTotalBaseIncome(entity.getTotalBaseIncome());
				hold.setTotalRewardIncome(entity.getTotalRewardIncome());
				hold.setAccruableHoldVolume(entity.getAccruableHoldVolume());
				
				repList.add(hold);
			}
			
			rep.setTotal(list.getTotalElements());
			rep.setRows(repList);
		}
		
		return rep;
	}
}
