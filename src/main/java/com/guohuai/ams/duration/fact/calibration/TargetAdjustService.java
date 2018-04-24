package com.guohuai.ams.duration.fact.calibration;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.guohuai.component.util.BigDecimalUtil;

/**
 * 估值校准记录
 * @author star.zhu
 * 2016年6月29日
 */
@Service
public class TargetAdjustService {

	@Autowired
	private TargetAdjustDao adjustDao;
	
	@Transactional
	public void save(TargetAdjustEntity entity) {
		adjustDao.save(entity);
	}
	
	/**
	 * 计算当前资产池当日累计的调整金额
	 * @param pid
	 * @param date
	 * @return
	 */
	@Transactional
	public BigDecimal navData(final String pid, final Date date) {
		BigDecimal navData = BigDecimalUtil.init0;
		Specification<TargetAdjustEntity> spec = new Specification<TargetAdjustEntity>() {
			
			@Override
			public Predicate toPredicate(Root<TargetAdjustEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				// TODO Auto-generated method stub
				return cb.and(cb.equal(root.get("assetPoolOid").as(String.class), pid),
						cb.equal(root.get("operaterDate").as(Date.class), date));
			}
		};
		List<TargetAdjustEntity> list = adjustDao.findAll(spec);
		if (null != list && !list.isEmpty()) {
			for (TargetAdjustEntity entity : list) {
				navData = navData.add(entity.getChangeAmount()).setScale(4, BigDecimal.ROUND_HALF_UP);
			}
		}
		
		return navData;
	}
}
