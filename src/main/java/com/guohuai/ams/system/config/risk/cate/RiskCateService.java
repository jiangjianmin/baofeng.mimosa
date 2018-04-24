package com.guohuai.ams.system.config.risk.cate;

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

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.StringUtil;

@Service
public class RiskCateService {

	@Autowired
	private RiskCateDao riskCateDao;

	@Transactional
	public RiskCate save(String oid, String type, String title) {

		RiskCate c;
		if (StringUtil.isEmpty(oid)) {
			c = new RiskCate();
			c.setOid(StringUtil.uuid());
		} else {
			c = this.get(oid);
		}

		c.setType(type);
		c.setTitle(title);

		c = this.riskCateDao.save(c);

		return c;
	}

	@Transactional
	public RiskCate save(String type, String title) {

		return this.save(null, type, title);

	}

	@Transactional
	public RiskCate get(String oid) {
		RiskCate c = this.riskCateDao.findOne(oid);

		if (null == c) {
			throw new AMPException(String.format("No data found for oid '%s'", oid));
		}

		return c;
	}

	@Transactional
	public List<RiskCate> options(String type) {
		List<RiskCate> list = this.riskCateDao.search(type);
		return list;

	}

	@Transactional
	public Map<String, List<RiskCate>> options() {
		Map<String, List<RiskCate>> map = new HashMap<String, List<RiskCate>>();
		map.put("WARNING", new ArrayList<RiskCate>());
		map.put("SCORE", new ArrayList<RiskCate>());

		List<RiskCate> list = this.riskCateDao.search();
		if (null != list && list.size() > 0) {
			for (RiskCate c : list) {
				if (!map.containsKey(c.getType())) {
					map.put(c.getType(), new ArrayList<RiskCate>());
				}
				map.get(c.getType()).add(c);
			}
		}

		return map;
	}



	@Transactional
	public long validateSingle(final String attrName, final String value,final String oid) {

		Specification<RiskCate> spec = new Specification<RiskCate>() {
			@Override
			public Predicate toPredicate(Root<RiskCate> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				if (StringUtil.isEmpty(oid)) {
					return cb.equal(root.get(attrName).as(String.class), value);
				} else {
					return cb.and(cb.equal(root.get(attrName).as(String.class), value), cb.notEqual(root.get("oid").as(String.class), oid));
				}
			}
		};
		spec = Specifications.where(spec);

		return this.riskCateDao.count(spec);
	}
}
