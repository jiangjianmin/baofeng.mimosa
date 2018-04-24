package com.guohuai.ams.acct.doc.template;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import com.guohuai.ams.acct.doc.type.DocType;
import com.guohuai.ams.acct.doc.type.DocTypeService;
import com.guohuai.component.util.StringUtil;

@Service
public class DocTemplateService {

	@Autowired
	private DocTemplateDao docTemplateDao;
	@Autowired
	private DocTypeService docTypeService;

	@Transactional
	public Page<DocTemplate> search(String type, final String name, int page, int size) {

		Specification<DocTemplate> spec = new Specification<DocTemplate>() {

			@Override
			public Predicate toPredicate(Root<DocTemplate> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate p = cb.like(root.get("name").as(String.class), String.format("%%%s%%", name));
				return p;
			}
		};

		if (!StringUtil.isEmpty(type)) {
			final DocType docType = this.docTypeService.get(type);
			spec = Specifications.where(spec).and(new Specification<DocTemplate>() {

				@Override
				public Predicate toPredicate(Root<DocTemplate> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.equal(root.get("type").as(DocType.class), docType);
				}
			});

		}

		spec = Specifications.where(spec).and(new Specification<DocTemplate>() {

			@Override
			public Predicate toPredicate(Root<DocTemplate> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("state").as(String.class), DocTemplate.STATE_Enable);
			}
		});

		Pageable pageable = new PageRequest(page, size, Direction.ASC, "oid");

		return this.docTemplateDao.findAll(spec, pageable);

	}

	@Transactional
	public DocTemplate safeGet(String oid) {
		return this.docTemplateDao.findOne(oid);
	}

}
