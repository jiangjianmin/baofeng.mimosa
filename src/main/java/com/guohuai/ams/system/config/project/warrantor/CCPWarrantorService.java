package com.guohuai.ams.system.config.project.warrantor;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.StringUtil;

@Service
public class CCPWarrantorService {

	@Autowired
	private CCPWarrantorDao ccpWarrantorDao;

	@Transactional
	public CCPWarrantor create(CCPWarrantorForm form) {

		CCPWarrantor.CCPWarrantorBuilder builder = CCPWarrantor.builder().oid(StringUtil.uuid());
		builder.title(form.getTitle()).lowScore(form.getLowScore()).highScore(form.getHighScore()).weight(form.getWeight100().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));

		CCPWarrantor warrantor = this.ccpWarrantorDao.save(builder.build());

		return warrantor;

	}

	@Transactional
	public CCPWarrantor update(CCPWarrantorForm form) {

		CCPWarrantor warrantor = this.get(form.getOid());

		warrantor.setTitle(form.getTitle());
		warrantor.setLowScore(form.getLowScore());
		warrantor.setHighScore(form.getHighScore());
		warrantor.setWeight(form.getWeight100().divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_UP));

		warrantor = this.ccpWarrantorDao.save(warrantor);

		return warrantor;

	}

	@Transactional
	public void delete(String oid) {

		this.ccpWarrantorDao.delete(oid);

	}

	public CCPWarrantor get(String oid) {

		CCPWarrantor warrantor = this.ccpWarrantorDao.findOne(oid);

		if (null == warrantor) {
			throw new AMPException(String.format("No data found for oid '%s'", oid));
		}

		return warrantor;

	}

	public List<CCPWarrantor> search() {
		List<CCPWarrantor> list = this.ccpWarrantorDao.search();
		return list;
	}

	/**
	 * 根据分数区间查询担保对象权数
	 * @Title: getByScoreBetween
	 * @author vania
	 * @version 1.0
	 * @see: TODO
	 * @param score
	 * @return CCPWarrantor 返回类型
	 */
	public CCPWarrantor getByScoreBetween(final Integer score) {
		Specification<CCPWarrantor> spec = new Specification<CCPWarrantor>() {

			@Override
			public Predicate toPredicate(Root<CCPWarrantor> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Expression<Integer> expV = cb.literal(score);
				Expression<Integer> expLow = root.get("lowScore").as(Integer.class);
				Expression<Integer> expHigh = root.get("highScore").as(Integer.class);
				return cb.between(expV, expLow, expHigh);
			}
		};
		return this.ccpWarrantorDao.findOne(spec);
	}
}
