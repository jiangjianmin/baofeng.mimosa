package com.guohuai.ams.acct.account;

import java.util.Comparator;

public class AccountComparator implements Comparator<Account> {

	@Override
	public int compare(Account o1, Account o2) {
		return o1.getOid().compareTo(o2.getOid());
	}

}
