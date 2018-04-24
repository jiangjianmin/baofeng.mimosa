package com.guohuai.mmp.publisher.product.statistics;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.guohuai.ams.product.Product;
import com.guohuai.ams.product.ProductService;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.BigDecimalUtil;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.util.StringUtil;
import com.guohuai.mmp.investor.tradeorder.InvestorTradeOrderDao;
import com.guohuai.mmp.job.lock.JobLockEntity;
import com.guohuai.mmp.job.lock.JobLockService;
import com.guohuai.mmp.job.log.JobLogEntity;
import com.guohuai.mmp.job.log.JobLogFactory;
import com.guohuai.mmp.job.log.JobLogService;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountDao;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;

@Service
public class PublisherProductStatisticsService {
	Logger logger = LoggerFactory.getLogger(PublisherProductStatisticsService.class);

	@Autowired
	private PublisherProductStatisticsDao publisherProductStatisticsDao;
	@Autowired
	private InvestorTradeOrderDao investorTradeOrderDao;
	@Autowired
	private PublisherBaseAccountDao publisherBaseAccountDao;
	@Autowired
	private ProductService productService;
	@Autowired
	private JobLockService jobLockService;
	@Autowired
	private JobLogService jobLogService;
	
	
	public void statPublishersProductInvestInfoByDate() {
		if (this.jobLockService.getRunPrivilege(JobLockEntity.JOB_jobId_publishersProductInvestorTop5)) {
			statPublishersProductInvestInfoByDateLog();
		}
	}
	
	public void statPublishersProductInvestInfoByDateLog() {
		JobLogEntity jobLog = JobLogFactory.getInstance(JobLockEntity.JOB_jobId_publishersProductInvestorTop5);

		try {
			String yesterday = DateUtil.format(DateUtil.addDay(new Date(), -1));
			Date investDate = DateUtil.parseDate(yesterday, "yyyy-MM-dd");
			this.statPublishersProductInvestInfoByDateDo(investDate);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			jobLog.setJobMessage(AMPException.getStacktrace(e));
			jobLog.setJobStatus(JobLogEntity.JOB_jobStatus_failed);
		}
		jobLog.setBatchEndTime(DateUtil.getSqlCurrentDate());
		this.jobLogService.saveEntity(jobLog);
		this.jobLockService.resetJob(JobLockEntity.JOB_jobId_publishersProductInvestorTop5);

	}
	
	/** 统计发行人新增投资额和累计投资额 */
	@Transactional
	public void statPublishersProductInvestInfoByDateDo(Date date) {

		List<PublisherProductStatisticsEntity> productInvestList = queryPublisherInvestAmount(date);

		if (productInvestList != null && productInvestList.size() > 0) {
			// 删除旧数据
			this.publisherProductStatisticsDao.deleteByPubliserOidAndDate(date);

			// 按照新增投资金额倒序排列
			Collections.sort(productInvestList, new Comparator<PublisherProductStatisticsEntity>() {
				@Override
				public int compare(PublisherProductStatisticsEntity o1, PublisherProductStatisticsEntity o2) {
					return -o1.getInvestAmount().compareTo(o2.getInvestAmount());
				}
			});

			// 计算新增投资排名
			for (int i = 0; i < productInvestList.size(); i++) {
				productInvestList.get(i).setInvestRank(i + 1);
			}

			// 按照累计投资金额倒序排列
			Collections.sort(productInvestList, new Comparator<PublisherProductStatisticsEntity>() {
				@Override
				public int compare(PublisherProductStatisticsEntity o1, PublisherProductStatisticsEntity o2) {
					return -o1.getTotalInvestAmount().compareTo(o2.getTotalInvestAmount());
				}
			});

			// 计算累计投资排名
			for (int i = 0; i < productInvestList.size(); i++) {
				productInvestList.get(i).setTotalInvestRank(i + 1);
			}

			// 保存到数据库
			this.publisherProductStatisticsDao.save(productInvestList);
		}
	}

	/** 查询发行人各产品昨日新增投资额和截止昨日累计投资额 */
	private List<PublisherProductStatisticsEntity> queryPublisherInvestAmount(Date date) {

		Timestamp startTime = DateUtil.getTimestampZeroOfDate(date);
		Timestamp endTime = DateUtil.getTimestampLastOfDate(date);
		// 昨日新增投资额
		List<Object[]> yesterdayInvestList = this.investorTradeOrderDao.countPublishersYesterdayInvestAmount(startTime,
				endTime);

		Map<String, Map<String, PublisherProductStatisticsEntity>> map = new HashMap<String, Map<String, PublisherProductStatisticsEntity>>();
		if (yesterdayInvestList != null && yesterdayInvestList.size() > 0) {
			for (Object[] objects : yesterdayInvestList) {
				String publisherOid = objects[0].toString();// 发行人ID
				String productOid = objects[1].toString();// 产品ID
				BigDecimal investAmount = BigDecimalUtil.parseFromObject(objects[2]);// 昨日新增投资金额
				if (!map.containsKey(objects[0])) {
					map.put(publisherOid, new HashMap<String, PublisherProductStatisticsEntity>());
				}
				if (!map.get(publisherOid).containsKey(productOid)) {
					PublisherProductStatisticsEntity entity = createEntity(publisherOid, productOid);
					// 投资金额
					entity.setInvestAmount(investAmount);
					entity.setInvestDate(date);
					map.get(publisherOid).put(productOid, entity);
				}
			}
		}

		// 截止昨日累计投资额
		List<Object[]> totalInvestList = this.investorTradeOrderDao.countPublishersTotalInvestAmount(endTime);
		if (totalInvestList != null && totalInvestList.size() > 0) {
			for (Object[] objects : totalInvestList) {
				String publisherOid = objects[0].toString();// 发行人ID
				String productOid = objects[1].toString();// 产品ID
				BigDecimal investAmount = BigDecimalUtil.parseFromObject(objects[2]);// 昨日新增投资金额
				if (!map.containsKey(objects[0])) {
					map.put(publisherOid, new HashMap<String, PublisherProductStatisticsEntity>());
				}
				if (!map.get(publisherOid).containsKey(productOid)) {
					PublisherProductStatisticsEntity entity = createEntity(publisherOid, productOid);
					// 累计投资金额
					entity.setTotalInvestAmount(investAmount);
					entity.setInvestDate(date);
					map.get(publisherOid).put(productOid, entity);
				}
			}
		}

		List<PublisherProductStatisticsEntity> list = new ArrayList<PublisherProductStatisticsEntity>();
		Iterator<String> iter = map.keySet().iterator();
		while (iter.hasNext()) {
			Map<String, PublisherProductStatisticsEntity> tmpMap = map.get(iter.next());
			Iterator<String> tmpIter = tmpMap.keySet().iterator();
			while (tmpIter.hasNext()) {
				list.add(tmpMap.get(tmpIter.next()));
			}
		}

		return list;
	}

	private PublisherProductStatisticsEntity createEntity(String publisherOid, String productOid) {
		PublisherProductStatisticsEntity entity = new PublisherProductStatisticsEntity();
		// oid
		entity.setOid(StringUtil.uuid());
		// 所属发行人
		if (!StringUtils.isEmpty(publisherOid)) {
			PublisherBaseAccountEntity publisherBaseAccount = this.publisherBaseAccountDao.findOne(publisherOid);
			if (publisherBaseAccount != null) {
				entity.setPublisherBaseAccount(publisherBaseAccount);
			}
		}
		// 所属产品
		if (!StringUtils.isEmpty(productOid)) {
			Product product = this.productService.findOne(productOid);
			if (product != null) {
				entity.setProduct(product);
			}
		}
		return entity;
	}

	/** 查询发行人产品投资额前五排名信息 */
	public List<PublisherProductStatisticsEntity> findTop5ByPublisherOid(final String publisherOid) {

		Specification<PublisherProductStatisticsEntity> spec = new Specification<PublisherProductStatisticsEntity>() {
			@Override
			public Predicate toPredicate(Root<PublisherProductStatisticsEntity> root, CriteriaQuery<?> query,
					CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("publisherBaseAccount").get("oid").as(String.class), publisherOid),
						cb.le(root.get("investRank").as(Integer.class), 5));
			}
		};

		Sort sort = new Sort(new Order(Direction.ASC, "investRank"));

		return this.publisherProductStatisticsDao.findAll(spec, sort);
	}

	/** 查询发行人产品投资额前五排名信息 */
	public List<Object[]> findTop5ByPublisherOidAndDate(String publisherOid, Date date) {
		return this.publisherProductStatisticsDao.findTop5ByPublisherOid(publisherOid, date, 5);
	}

	/** 查询平台昨日新增投资额TOP5产品 */
	public List<Object[]> findTop5InvestorOfPlatform(Date date) {
		return this.publisherProductStatisticsDao.findTop5InvestorOfPlatform(date);
	}
}
