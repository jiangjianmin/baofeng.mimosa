package com.guohuai.ams.system.config.risk.warning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import com.guohuai.ams.system.config.risk.cate.RiskCate;
import com.guohuai.ams.system.config.risk.indicate.RiskIndicate;
import com.guohuai.ams.system.config.risk.indicate.RiskIndicateService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.StringUtil;

@Service
public class RiskWarningService {
	@Autowired
	private RiskWarningDao riskWarningDao;
	@Autowired
	private RiskIndicateService riskIndicateService;

	@Transactional
	public RiskWarning save(RiskWarningForm form) {
		RiskWarning warning = new RiskWarning();
		warning.setOid(form.getOid());
		warning.setIndicate(riskIndicateService.get(form.getIndicateOid()));
		warning.setTitle(form.getTitle());
		return riskWarningDao.save(warning);
	}

	@Transactional
	public RiskWarning save(RiskWarning entity) {
		return riskWarningDao.save(entity);
	}

	@Transactional
	public void delete(String oid) {
		this.riskWarningDao.delete(oid);
	}

	@Transactional
	public RiskWarning get(String oid) {
		RiskWarning indicate = this.riskWarningDao.findOne(oid);

		if (null == indicate) {
			throw new AMPException(String.format("No data found for oid '%s'", oid));
		}

		return indicate;
	}

	@Transactional
	public List<RiskWarningResp> search(String type, String keyword) {

		List<RiskWarning> list = this.riskWarningDao.search(String.format("%%%s%%", type), String.format("%%%s%%", keyword), new String[] { RiskIndicate.STATE_Enable, RiskIndicate.STATE_Disable });

		List<RiskWarningResp> result = new ArrayList<RiskWarningResp>();
		for (RiskWarning i : list) {
			result.add(new RiskWarningResp(i));
		}

		return result;
	}

	@Transactional
	public List<RiskWarningOption> options(String type) {
		List<RiskWarningOption> options = new ArrayList<RiskWarningOption>();
		int wsize = 0;

		List<RiskWarning> list = this.riskWarningDao.search(type, new String[] { RiskIndicate.STATE_Enable });
		wsize = null == list ? 0 : list.size();
		if (wsize > 0) {
			Map<String, RiskWarningOption> cmap = new HashMap<String, RiskWarningOption>();
			for (RiskWarning i : list) {
				String woid = i.getOid();
				RiskIndicate indicate = i.getIndicate();
				String ioid = indicate.getOid();
				RiskCate cate = indicate.getCate();
				String coid = cate.getOid();

				RiskWarningOption wo = cmap.get(coid);
				if (null == wo) {
					wo = new RiskWarningOption();
					wo.setOid(coid);
					wo.setTitle(cate.getTitle());
					cmap.put(coid, wo);
				}

				List<RiskWarningOption.Indicate> ilist = wo.getOptions();
				if (null == ilist)
					ilist = new ArrayList<>();
				RiskWarningOption.Indicate ind = wo.get(ioid);
				if (null == ind) { // 不存在
					ind = new RiskWarningOption.Indicate();
					ilist.add(ind);

					ind.setOid(ioid);
					ind.setTitle(indicate.getTitle());
					ind.setDataType(indicate.getDataType());
					ind.setDataUnit(indicate.getDataUnit());
				}

				List<RiskWarningOption.Indicate.Option> wlist = ind.getOptions();
				if (wlist == null) {
					wlist = new ArrayList<>();
				}
				RiskWarningOption.Indicate.Option op = ind.get(woid);
				if (null == op) { // 不存在
					op = new RiskWarningOption.Indicate.Option();
					wlist.add(op);
				}
				op.setOid(woid);
				op.setTitle(i.getTitle());
			}
			for (RiskWarningOption rw : cmap.values()) {
				options.add(rw);
			}

		}
		return options;
	}

	@Transactional
	public long validateSingle(final String attrName, final String value, final String oid) {

		Specification<RiskWarning> spec = new Specification<RiskWarning>() {
			@Override
			public Predicate toPredicate(Root<RiskWarning> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				if (StringUtil.isEmpty(oid)) {
					return cb.equal(root.get(attrName).as(String.class), value);
				} else {
					return cb.and(cb.equal(root.get(attrName).as(String.class), value), cb.notEqual(root.get("oid").as(String.class), oid));
				}
			}
		};
		spec = Specifications.where(spec);

		return this.riskWarningDao.count(spec);
	}
}
