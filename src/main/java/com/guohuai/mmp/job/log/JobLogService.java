package com.guohuai.mmp.job.log;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.fastjson.JSONObject;
import com.guohuai.bfsms.BfSMSTypeEnum;
import com.guohuai.bfsms.BfSMSUtils;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.PageResp;
import com.guohuai.mmp.job.JobEnum;



@Service
@Transactional
//@Slf4j
public class JobLogService {
	
	
	@Autowired
	private BfSMSUtils bfSMSUtils;
	
	@Autowired
	private JobLogDao jobLogDao;
	
	@Value("${bfsms.momitor:#{null}}")
	private String monitor;

	@Transactional(value = TxType.REQUIRES_NEW)
	public JobLogEntity saveEntity(JobLogEntity jobLog) {
		if (JobEnum.isNeedSendMessage(jobLog.getJobId())) {
			if (!StringUtil.isEmpty(monitor) && JobLogEntity.JOB_jobStatus_failed.equals(jobLog.getJobStatus())) {
				bfSMSUtils.sendSMS(monitor, BfSMSTypeEnum.smstypeEnum.jobAlerm.toString(),JSONObject.toJSONString(jobLog));
			}
		}
		return this.jobLogDao.save(jobLog);
	}

	public int queryRunedTimes(String jobId, String begin, String end) {
		return this.jobLogDao.queryRunedTimes(jobId, begin, end);
	}
	
	public PageResp<JobLogEntityResp> getJobLogByJobId(int page,int rows,String jobId,Date createTime,String jobStatus){
		if (page < 1) {
			page = 1;
		}
		if (rows < 1) {
			rows = 1;
		}
		Sort sort = new Sort(Sort.Direction.fromString("desc"), "batchStartTime");
		Pageable pageable = new PageRequest(page - 1, rows,sort);
		PageResp<JobLogEntityResp> pagesRep = new PageResp<JobLogEntityResp>();
		
		Specification<JobLogEntity> spec = new Specification<JobLogEntity>() {
			@Override
			public Predicate toPredicate(Root<JobLogEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get("jobId").as(String.class), jobId);
			}
		};
		
		if (null != createTime) {
			spec = Specifications.where(spec).and(new Specification<JobLogEntity>() {
				@Override
				public Predicate toPredicate(Root<JobLogEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.equal(root.get("createTime").as(Date.class), createTime);
				}
			});
		}
		
		if (!StringUtil.isEmpty(jobStatus)) {
			spec = Specifications.where(spec).and(new Specification<JobLogEntity>() {
				@Override
				public Predicate toPredicate(Root<JobLogEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					return cb.equal(root.get("jobStatus").as(String.class), jobStatus);
				}
			});
		}
		
		Page<JobLogEntity> cas = jobLogDao.findAll(spec, pageable);
		if (cas != null && cas.getContent() != null && cas.getTotalElements() > 0) {
			List<JobLogEntityResp> rowss = new ArrayList<JobLogEntityResp>();
			for (JobLogEntity j : cas) {
				JobLogEntityResp queryRep = new JobLogEntityResp(j);
				rowss.add(queryRep);
			}
			pagesRep.setRows(rowss);
		}
		pagesRep.setTotal(cas.getTotalElements());
		return pagesRep;
	}
	
	

	

}
