package com.guohuai.mmp.platform.inform;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.web.view.PagesRep;

@Service
@Transactional
public class InformService {

	@Autowired
	InformDao informDao;

	public PagesRep<InformQueryRep> query(Specification<InformEntity> spec, Pageable pageable) {
		Page<InformEntity> cas = this.informDao.findAll(spec, pageable);
		PagesRep<InformQueryRep> pagesRep = new PagesRep<InformQueryRep>();
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			for (InformEntity entity : cas) {
				InformQueryRep queryRep = InformQueryRep.builder()
						.informOid(entity.getOid())
						.informCode(entity.getInformCode())
						.informType(entity.getInformType())
						.informTypeDisp(informTypeEn2Ch(entity.getInformType()))
						.informContent(entity.getInformContent())
						.updateTime(entity.getUpdateTime())
						.createTime(entity.getCreateTime())
						.build();
				
				pagesRep.add(queryRep);
			}
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}
	
	/**
	 * /** 通知类型--产品通过 *
	public static final String INFORM_informType_productPass = "产品通过";
	/** 通知类型--产品通过 *
	public static final String INFORM_informType_productRefused = "产品拒绝";
	/** 通知类型--产品通过 *
	public static final String INFORM_informType_publisherCloseCollect = "日结催收";
	/** 通知类型--产品通过 *
	public static final String INFORM_informType_publisherCloseExpired = "日结逾期";
	 * @param informType
	 * @return
	 */
	private String informTypeEn2Ch(String informType) {
		if (InformEntity.INFORM_informType_productPass.equals(informType)) {
			return "产品通过";
		}
		if (InformEntity.INFORM_informType_productRefused.equals(informType)) {
			return "产品拒绝";
		}
		if (InformEntity.INFORM_informType_publisherCloseCollect.equals(informType)) {
			return "日结催收";
		}
		if (InformEntity.INFORM_informType_publisherCloseExpired.equals(informType)) {
			return "日结逾期";
		}
		return informType;
	}

	public InformDetailRep detail(String informOid) {
		InformEntity entity = this.findByOid(informOid);
		InformDetailRep detailRep = InformDetailRep.builder()
				.informCode(entity.getInformCode())
				.informType(entity.getInformType())
				.informTypeDisp(informTypeEn2Ch(entity.getInformType()))
				.informContent(entity.getInformContent())
				.updateTime(entity.getUpdateTime())
				.createTime(entity.getCreateTime())
				.build();
		return detailRep;
	}

	private InformEntity findByOid(String informOid) {
		InformEntity entity = this.informDao.findOne(informOid);
		if (null == entity) {
			throw new AMPException("通知不存在");
		}
		return entity;
	}
	
}
