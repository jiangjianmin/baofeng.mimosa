package com.guohuai.ams.duration.fact.income;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

/**
 * 收益分配
 * 
 * @author wagnlei
 *
 */
@Service
public class IncomeAllocateService {

	@Autowired
	private IncomeAllocateDao incomeAllocateDao;

	/**
	 * 获取某个产品的最新收益分配
	 * @param assetPoolOid
	 * @return
	 */
	public Page<IncomeAllocate> getProductIncomeAllocate(final String assetPoolOid,int rows) {
		//收益分配
		Specification<IncomeAllocate> pspec = new Specification<IncomeAllocate>() {
			@Override
			public Predicate toPredicate(Root<IncomeAllocate> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("incomeEvent").get("status").as(String.class), IncomeEvent.STATUS_Allocated),
						cb.equal(root.get("incomeEvent").get("assetPool").get("oid").as(String.class), assetPoolOid));
				
			}
		};
		pspec = Specifications.where(pspec);
		Page<IncomeAllocate> pcas = this.incomeAllocateDao.findAll(pspec, new PageRequest(0, rows, new Sort(new Order(Direction.DESC, "baseDate"))));
		return pcas;
	}

}
