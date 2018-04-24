package com.guohuai.cms.mail;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guohuai.basic.common.Clock;
import com.guohuai.basic.component.ext.web.BaseResp;
import com.guohuai.basic.component.ext.web.PageResp;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class MailService {
	
	@Autowired
	private MailDao mailDao;
	@Autowired
	private RedisTemplate<String, String> redis;
	
	/**
	 * 前端获取站内信列表
	 * @param pageable
	 * @param userOid
	 * @return
	 */
	public PageResp<MailCTResp> queryCTPage(Pageable pageable, final String userOid) {
		log.debug("前端获取站内信列表 userOid:{}", userOid);
		Specification<MailEntity> sa = new Specification<MailEntity>() {
			@Override
			public Predicate toPredicate(Root<MailEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate b = cb.equal(root.get("status").as(String.class), "pass");
				
				Predicate c = cb.equal(root.get("userOid").as(String.class), userOid);
				Predicate d = cb.equal(root.get("mailType").as(String.class), MailEntity.MAIL_mailType_all);
				
				Predicate e = cb.and(b,c);
				Predicate f = cb.and(b,d);
				
				return cb.or(e,f);
			}
		};
		
		Page<MailEntity> enchs = this.mailDao.findAll(sa, pageable);
		PageResp<MailCTResp> pageResp = new PageResp<>();		
		List<MailCTResp> list = new ArrayList<MailCTResp>();
		for (MailEntity en : enchs) {
			if (en.getMailType().equals(MailEntity.MAIL_mailType_all)){
				String isRead = MailEntity.MAIL_isRead_is;
				String hadReadStr = RedisUtil.hget(redis, StrRedisUtil.VERI_NOREAD_REDIS_KEY, userOid);
				if (hadReadStr == null || hadReadStr.isEmpty()){
					isRead = MailEntity.MAIL_isRead_no;
				}else{
					@SuppressWarnings("unchecked")
					Set<String> oidSet = JSON.parseObject(hadReadStr, Set.class);
					if (!oidSet.contains(en.getOid())){
						isRead = MailEntity.MAIL_isRead_no;
					}
				}
//				MailCTResp rep = new MailCTRespBuilder().oid(en.getOid())
//						.mailType(en.getMailType()).mesType(en.getMesType()).mesTitle(en.getMesTitle()).mesContent(en.getMesContent()).isRead(isRead)
//						.updateTime(en.getCreateTime()).build();
				MailCTResp rep = new MailCTResp(en.getOid(), en.getMailType(), 
						en.getMesType(), en.getMesTitle(), en.getMesContent(), isRead, en.getCreateTime());
				list.add(rep);
			}else{
//				MailCTResp rep = new MailCTRespBuilder().oid(en.getOid())
//						.mailType(en.getMailType()).mesType(en.getMesType()).mesTitle(en.getMesTitle()).mesContent(en.getMesContent()).isRead(en.getIsRead())
//						.updateTime(en.getCreateTime()).build();
				MailCTResp rep = new MailCTResp(en.getOid(), en.getMailType(), 
						en.getMesType(), en.getMesTitle(), en.getMesContent(), en.getIsRead(), en.getCreateTime());
				list.add(rep);
			}
		}
		
		pageResp.setTotal(enchs.getTotalElements());
		pageResp.setRows(list);
		return pageResp;
	}

}
