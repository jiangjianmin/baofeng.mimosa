package com.guohuai.mmp.investor.baseaccount.changephone;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChangePhoneService {
	
	@Autowired
	private ChangePhoneDao changePhoneDao;
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public ChangePhoneEntity save(ChangePhoneEntity changePhoneEntity){
		ChangePhoneEntity result = changePhoneDao.save(changePhoneEntity);
		return result;
	}
	
	public int updateStatus(String oid, int status){
		return changePhoneDao.updateStatusByOid(oid, status);
	}

}
