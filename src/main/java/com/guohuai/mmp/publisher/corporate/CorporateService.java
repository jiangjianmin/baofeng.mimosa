package com.guohuai.mmp.publisher.corporate;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guohuai.basic.common.SeqGenerator;
import com.guohuai.basic.common.StringUtil;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.DateUtil;
import com.guohuai.component.web.view.PagesRep;
import com.guohuai.mmp.platform.baseaccount.statistics.PlatformStatisticsService;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountService;
import com.guohuai.mmp.publisher.baseaccount.loginacc.PublisherLoginAccEntity;
import com.guohuai.mmp.publisher.baseaccount.loginacc.PublisherLoginAccService;
import com.guohuai.mmp.sys.CodeConstants;

@Service
public class CorporateService {

	@Autowired
	private CorporateDao corporateDao;
	@Autowired
	private SeqGenerator seqGenerator;
	@Autowired
	private PublisherBaseAccountService publisherBaseAccountService;
	@Autowired
	private PublisherLoginAccService publisherLoginAccService;
	@Autowired
	private PlatformStatisticsService platformStatisticsService;

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Transactional
	public Corporate create(CorporateReq corporateReq, String operator) {

		
		if (this.accountExists(corporateReq.getAccount())) {
			// error.define[30043]=账号已存在(CODE:30043)
			throw AMPException.getException(30043);
		}
		
		String[] userOids = corporateReq.getUserOids();
		if (null != userOids && 0 != userOids.length) {
			for (String userOid : userOids) {
				
				boolean isExsits = this.publisherLoginAccService.isExistsLoginAcc(userOid);
				if (isExsits) {
					// error.define[30059]=用户已有所属发行人(CODE:30059)
					throw new AMPException(30059);
				}
			}
		}
		
		Corporate corporate = new Corporate();
		try {
			BeanUtils.copyProperties(corporate, corporateReq);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		corporate.setAuditOrderNo(this.seqGenerator.next(CodeConstants.corAuditOrderNo));
		corporate.setIdentityId(StringUtil.uuid());
		corporate.setIdentityType("UID");
		corporate.setMemberType("2");
		corporate.setStatus(Corporate.STATUS_Using);
		corporate.setAuditStatus(Corporate.AUDIT_STATUS_Success);
		corporate.setOperator(operator);
		corporate.setUpdateTime(DateUtil.getSqlCurrentDate());
		corporate.setCreateTime(DateUtil.getSqlCurrentDate());
		corporate.setIsOpen(Corporate.YES);
		
		Corporate saved = this.corporateDao.save(corporate);
		
		
		PublisherBaseAccountEntity baseAccount = publisherBaseAccountService.openDo(saved);
		for (String userOid : userOids) {
			PublisherLoginAccEntity loginAcc = new PublisherLoginAccEntity();
			loginAcc.setPublisherBaseAccount(baseAccount);
			loginAcc.setLoginAcc(userOid);
			this.publisherLoginAccService.save(loginAcc);
		}
		
		platformStatisticsService.increatePublisherAmount();
		
		return saved;

	}

	@Transactional
	private boolean accountExists(String account) {
		Corporate c = this.corporateDao.findByAccount(account);
		return null != c;
	}
	
	public CorporateDetailRep detail(String oid) {
		CorporateDetailRep rep = new CorporateDetailRep();
		try {
			BeanUtils.copyProperties(rep, this.read(oid));
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return rep;
	}

	@Transactional
	public Corporate read(String oid) {
		
		if (StringUtil.isEmpty(oid)) {
			// error.define[30044]=错误的ID(CODE:30044)
			throw AMPException.getException(30044);
		}
		Corporate corporate = this.corporateDao.findOne(oid);
		if (null == corporate) {
			// error.define[30045]=法人不存在(CODE:30045)
			throw AMPException.getException(30045);
		}
		
		return corporate;
	}

	@Transactional
	public PagesRep<CorporateQueryRep> query(String account, String beginCreateTime, String endCreateTime, int status) {
		Timestamp begin, end;
		if (StringUtil.isEmpty(beginCreateTime)) {
			begin = new Timestamp(0);
		} else {
			try {
				begin = new Timestamp(this.dateFormat.parse(beginCreateTime).getTime());
			} catch (ParseException e) {
				throw AMPException.getException(e);
			}
		}
		if (StringUtil.isEmpty(endCreateTime)) {
			end = DateUtil.getSqlCurrentDate();
		} else {
			try {
				end = new Timestamp(this.dateFormat.parse(endCreateTime).getTime());
			} catch (ParseException e) {
				throw AMPException.getException(e);
			}
		}
		List<Corporate> list;
		if (status == 0) {
			list = this.corporateDao.findByAccountAndCreateTime(account, begin, end);
		} else {
			list = this.corporateDao.findByAccountAndCreateTimeAndStatus(account, begin, end, status);
		}
		PagesRep<CorporateQueryRep> pagesRep = new PagesRep<CorporateQueryRep>();
		for (Corporate cor : list) {
			CorporateQueryRep rep = new CorporateQueryRep();
			try {
				BeanUtils.copyProperties(rep, cor);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			pagesRep.add(rep);
		}
		pagesRep.setTotal(list.size());
		return pagesRep;
	}

	@Transactional
	public Corporate lockin(String oid, String operator) {
		Corporate corporate = this.read(oid);
		corporate.setStatus(Corporate.STATUS_Lockin);
		corporate.setOperator(operator);
		corporate.setUpdateTime(DateUtil.getSqlCurrentDate());
		corporate = this.corporateDao.save(corporate);
		return corporate;
	}

	public Corporate saveEntity(Corporate corporate) {
		return this.corporateDao.save(corporate);
		
	}
	
	/**
	 * 满足条件的发行人列表
	 * @author star.zhu
	 * @return
	 */
	@Transactional
	public List<Corporate> getCorportateList() {
		Specification<Corporate> spec = new Specification<Corporate>() {
			
			@Override
			public Predicate toPredicate(Root<Corporate> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("status").as(int.class), Corporate.STATUS_Using),
						cb.equal(root.get("auditStatus").as(int.class), Corporate.AUDIT_STATUS_Success),
						cb.equal(root.get("isOpen").as(String.class), Corporate.YES));
			}
		};
		
		return this.corporateDao.findAll(spec);
	}

}
