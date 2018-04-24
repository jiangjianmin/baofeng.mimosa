/*   
 * Copyright © 2015 guohuaigroup All Rights Reserved.   
 *   
 * This software is the confidential and proprietary information of   
 * Founder. You shall not disclose such Confidential Information   
 * and shall use it only in accordance with the terms of the agreements   
 * you entered into with Founder.   
 *   
 */
package com.guohuai.ams.investment.pool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TargetOverdueService {
	@Autowired
	TargetOverdueDao targetOverdueDao;

	/**
	 * 根据投资标的id查询逾期信息
	 * @Title: findByTargetOid 
	 * @author vania
	 * @version 1.0
	 * @see: 
	 * @param targetOid
	 * @return TargetOverdue    返回类型
	 */
	public TargetOverdue findByTargetOid(String targetOid) {
		return targetOverdueDao.findByTargetOid(targetOid);
	}
}
