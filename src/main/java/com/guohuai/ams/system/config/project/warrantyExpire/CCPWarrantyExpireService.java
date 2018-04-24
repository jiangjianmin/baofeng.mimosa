package com.guohuai.ams.system.config.project.warrantyExpire;

import java.math.BigDecimal;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.StringUtil;

@Service
public class CCPWarrantyExpireService {

	@Autowired
	private CCPWarrantyExpireDao ccpWarrantyExpireDao;

	@Transactional
	public CCPWarrantyExpire create(CCPWarrantyExpireForm form) {

		CCPWarrantyExpire.CCPWarrantyExpireBuilder builder = CCPWarrantyExpire.builder().oid(StringUtil.uuid());
		builder.title(form.getTitle()).weight(form.getWeight100().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));

		CCPWarrantyExpire warrantyExpire = this.ccpWarrantyExpireDao.save(builder.build());

		return warrantyExpire;

	}

	@Transactional
	public CCPWarrantyExpire update(CCPWarrantyExpireForm form) {

		CCPWarrantyExpire warrantyExpire = this.get(form.getOid());

		warrantyExpire.setTitle(form.getTitle());
		warrantyExpire.setWeight(form.getWeight100().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));

		warrantyExpire = this.ccpWarrantyExpireDao.save(warrantyExpire);

		return warrantyExpire;

	}

	@Transactional
	public void delete(String oid) {

		this.ccpWarrantyExpireDao.delete(oid);

	}

	public CCPWarrantyExpire get(String oid) {

		CCPWarrantyExpire warrantyExpire = this.ccpWarrantyExpireDao.findOne(oid);

		if (null == warrantyExpire) {
			throw new AMPException(String.format("No data found for oid '%s'", oid));
		}

		return warrantyExpire;

	}

	public List<CCPWarrantyExpire> search() {
		List<CCPWarrantyExpire> list = this.ccpWarrantyExpireDao.search();
		return list;
	}

}
