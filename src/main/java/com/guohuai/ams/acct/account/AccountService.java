package com.guohuai.ams.acct.account;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;

@Service
public class AccountService {

	@Autowired
	private AccountDao accountDao;

	@Transactional
	public Account get(String oid) {
		Account a = this.accountDao.findOne(oid);
		if (null == a) {
			throw new AMPException(String.format("No data found for oid '%s'.", oid));
		}
		return a;
	}

	@Transactional
	public List<Account> search() {
		List<Account> list = this.accountDao.search();
		return list;
	}

	@Transactional
	public Account update(AccountForm form) {
		Account a = this.get(form.getOid());
		a.setName(form.getName());
		a = this.accountDao.save(a);
		return a;
	}

	@Transactional
	public List<Account> list() {
		List<Account> list = this.accountDao.findAll();
		return list;
	}

	@Transactional
	public Object[] selectFourElement(String uid) {
		List<Object[]> list = this.accountDao.selectFourElement(uid);
		if(list.size()!=0) {
			return list.get(0);
		}
		return null;
	}

}
