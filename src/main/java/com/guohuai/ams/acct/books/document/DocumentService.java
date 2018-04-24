package com.guohuai.ams.acct.books.document;

import java.sql.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import com.guohuai.basic.common.StringUtil;
import com.guohuai.component.exception.AMPException;

@Service
public class DocumentService {

	@Autowired
	private DocumentDao documentDao;

	@Transactional
	public Page<Document> search(final String relative, final Date startDate, final Date endDate, int page, int size) {

		Pageable pageable = new PageRequest(page, size, new Sort(new Order(Direction.DESC, "updateTime"), new Order(Direction.DESC, "acctSn")));

		Specification<Document> spec = null;

		if (!StringUtil.isEmpty(relative)) {
			Specification<Document> q = new Specification<Document>() {

				@Override
				public Predicate toPredicate(Root<Document> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

					return cb.equal(root.get("relative").as(String.class), relative);
				}
			};
			spec = q;
		}

		if (null != startDate) {
			Specification<Document> q = new Specification<Document>() {

				@Override
				public Predicate toPredicate(Root<Document> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.greaterThanOrEqualTo(root.get("acctDate").as(Date.class), startDate);
				}
			};
			if (null != spec) {
				spec = Specifications.where(spec).and(q);
			} else {
				spec = q;
			}
		}

		if (null != endDate) {
			Specification<Document> q = new Specification<Document>() {

				@Override
				public Predicate toPredicate(Root<Document> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.lessThanOrEqualTo(root.get("acctDate").as(Date.class), endDate);
				}
			};

			if (null != spec) {
				spec = Specifications.where(spec).and(q);
			} else {
				spec = q;
			}
		}

		if (null != spec) {
			return this.documentDao.findAll(spec, pageable);
		} else {
			return this.documentDao.findAll(pageable);
		}
	}

	@Transactional
	public Document get(String oid) {
		Document d = this.documentDao.findOne(oid);
		if (null == d) {
			throw new AMPException(String.format("No data found for oid '%s'.", oid));
		}
		return d;
	}

}
