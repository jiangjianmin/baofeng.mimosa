package com.guohuai.ams.acct.books;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.acct.account.Account;
import com.guohuai.ams.acct.account.AccountComparator;
import com.guohuai.ams.acct.account.AccountService;
import com.guohuai.component.util.StringUtil;

@Service
public class AccountBookService {

	@Autowired
	private AccountBookDao accountBookDao;
	@Autowired
	private AccountService accountService;

	@Transactional
	public List<AccountBook> init(String relative) {

		List<Account> accounts = this.accountService.search();

		List<AccountBook> persisted = this.accountBookDao.findByRelative(relative);
		Map<String, AccountBook> exists = new HashMap<String, AccountBook>();
		if (null != persisted && persisted.size() > 0) {
			for (AccountBook book : persisted) {
				exists.put(book.getAccount().getOid(), book);
			}
		}

		List<AccountBook> books = new ArrayList<AccountBook>();

		for (Account account : accounts) {
			if (exists.containsKey(account.getOid())) {
				books.add(exists.get(account.getOid()));
			} else {
				AccountBook book = new AccountBook();
				book.setOid(StringUtil.uuid());
				book.setRelative(relative);
				book.setAccount(account);
				book.setBalance(BigDecimal.ZERO);
				book.setOpeningBalance(BigDecimal.ZERO);
				book = this.accountBookDao.save(book);
				books.add(book);
			}
		}

		return books;
	}

	@Transactional
	public AccountBook safeGet(String relative, Account account) {
		AccountBook book = this.accountBookDao.findByRelativeAndAccount(relative, account);
		if (null == book) {
			book = new AccountBook();
			book.setOid(StringUtil.uuid());
			book.setRelative(relative);
			book.setAccount(account);
			book.setBalance(BigDecimal.ZERO);
			book.setOpeningBalance(BigDecimal.ZERO);
			book = this.accountBookDao.save(book);
		}
		return book;
	}

	@Transactional
	public AccountBook incrCredit(AccountBook book, BigDecimal cash) {
		book.setBalance(book.getBalance().add(cash));
		book = this.accountBookDao.save(book);

		if (!StringUtil.isEmpty(book.getAccount().getParent())) {
			AccountBook parent = this.safeGet(book.getRelative(), this.accountService.get(book.getAccount().getParent()));
			this.incrCredit(parent, cash);
		}

		return book;
	}

	@Transactional
	public AccountBook decrCredit(AccountBook book, BigDecimal cash) {
		book.setBalance(book.getBalance().subtract(cash));
		book = this.accountBookDao.save(book);

		if (!StringUtil.isEmpty(book.getAccount().getParent())) {
			AccountBook parent = this.safeGet(book.getRelative(), this.accountService.get(book.getAccount().getParent()));
			this.decrCredit(parent, cash);
		}

		return book;
	}

	@Transactional
	public List<AccountBookBalance> balance() {
		List<AccountBook> bs = this.accountBookDao.search();
		return this.balance(bs);
	}

	@Transactional
	public List<AccountBookBalance> balance(String relative) {
		if (StringUtil.isEmpty(relative)) {
			return this.balance();
		}
		List<AccountBook> bs = this.accountBookDao.search(relative);
		return this.balance(bs);
	}

	@Transactional
	public List<AccountBookBalance> balance(List<AccountBook> bs) {
		LinkedList<AccountBookBalance> result = new LinkedList<AccountBookBalance>();

		LinkedHashMap<String, Account> laccount = new LinkedHashMap<String, Account>();
		Map<String, BigDecimal> lbalance = new HashMap<String, BigDecimal>();
		BigDecimal lsum = BigDecimal.ZERO;
		int lsumIndex = 0;

		LinkedHashMap<String, Account> raccount = new LinkedHashMap<String, Account>();
		Map<String, BigDecimal> rbalance = new HashMap<String, BigDecimal>();
		BigDecimal rsum = BigDecimal.ZERO;
		int rsumIndex = 0;

		BigDecimal equitySum = BigDecimal.ZERO;
		int equitySumIndex = 0;

		for (AccountBook b : bs) {
			String account = b.getAccount().getOid();
			// 资产
			if (b.getAccount().getType().equals(Account.TYPE_Assets)) {
				if (!laccount.containsKey(account)) {
					laccount.put(account, b.getAccount());
				}
				if (!lbalance.containsKey(account)) {
					lbalance.put(account, BigDecimal.ZERO);
				}
				lbalance.put(account, lbalance.get(account).add(b.getBalance()));
				if (StringUtil.isEmpty(b.getAccount().getParent())) {
					lsum = lsum.add(b.getBalance());
				}
			}
			// 负债+权益
			if (b.getAccount().getType().equals(Account.TYPE_Equity) || b.getAccount().getType().equals(Account.TYPE_Liability)) {
				if (!raccount.containsKey(account)) {
					raccount.put(account, b.getAccount());
				}
				if (!rbalance.containsKey(account)) {
					rbalance.put(account, BigDecimal.ZERO);
				}
				rbalance.put(account, rbalance.get(account).add(b.getBalance()));
				if (StringUtil.isEmpty(b.getAccount().getParent())) {
					rsum = rsum.add(b.getBalance());
					if (b.getAccount().getType().equals(Account.TYPE_Equity)) {
						equitySum = equitySum.add(b.getBalance());
					}
				}
			}
		}

		// 补充默认账户
		List<Account> accounts = this.accountService.search();
		if (null != accounts && accounts.size() > 0) {
			for (Account a : accounts) {
				if (a.getType().equals(Account.TYPE_Assets)) {
					if (!laccount.containsKey(a.getOid())) {
						laccount.put(a.getOid(), a);
					}
					if (!lbalance.containsKey(a.getOid())) {
						lbalance.put(a.getOid(), BigDecimal.ZERO);
					}
				}
				if (a.getType().equals(Account.TYPE_Equity) || a.getType().equals(Account.TYPE_Liability)) {
					if (!raccount.containsKey(a.getOid())) {
						raccount.put(a.getOid(), a);
					}
					if (!rbalance.containsKey(a.getOid())) {
						rbalance.put(a.getOid(), BigDecimal.ZERO);
					}
				}

			}
		}

		// 初始化 result 表
		for (int i = 0; i < Math.max(laccount.size(), raccount.size()); i++) {
			result.add(new AccountBookBalance());
		}

		AtomicInteger index = new AtomicInteger(1);
		AtomicInteger lindex = new AtomicInteger(0);
		AtomicInteger rindex = new AtomicInteger(0);

		final List<Account> llist = new ArrayList<Account>();

		for (String t : laccount.keySet()) {
			Account u = laccount.get(t);
			llist.add(u);
		}

		/*
		 * laccount.forEach(new BiConsumer<String, Account>() {
		 * 
		 * @Override
		 * public void accept(String t, Account u) {
		 * llist.add(u);
		 * }
		 * });
		 */

		Collections.sort(llist, new AccountComparator());

		for (Account a : llist) {
			AccountBookBalance bb = result.get(lindex.getAndIncrement());
			bb.setLcode(a.getCode());
			bb.setLname(a.getName());
			bb.setLsn(String.valueOf(index.getAndIncrement()));
			bb.setLbalance(lbalance.get(a.getOid()));
		}

		lsumIndex = index.getAndIncrement();

		final List<Account> rlist = new ArrayList<Account>();

		for (String t : raccount.keySet()) {
			Account u = raccount.get(t);
			rlist.add(u);
		}

		/*
		raccount.forEach(new BiConsumer<String, Account>() {

			@Override
			public void accept(String t, Account u) {
				rlist.add(u);
			}
		});
		*/

		Collections.sort(rlist, new AccountComparator());

		for (Account a : rlist) {
			AccountBookBalance bb = result.get(rindex.getAndIncrement());
			bb.setRcode(a.getCode());
			bb.setRname(a.getName());
			bb.setRsn(String.valueOf(index.getAndIncrement()));
			bb.setRbalance(rbalance.get(a.getOid()));
		}

		equitySumIndex = index.getAndIncrement();
		rsumIndex = index.getAndIncrement();

		// AccountBookBalance title = new AccountBookBalance();
		// title.setLname("流动资产：");
		// title.setRname("流动负债：");
		// result.add(0, title);

		if (rindex.get() == result.size()) {
			AccountBookBalance b = new AccountBookBalance();
			b.setRname("所有者权益（或股东权益）合计");
			b.setRsn(String.valueOf(equitySumIndex));
			b.setRbalance(equitySum);
			result.add(b);
		} else {
			AccountBookBalance b = result.getLast();
			b.setRname("所有者权益（或股东权益）合计");
			b.setRsn(String.valueOf(equitySumIndex));
			b.setRbalance(equitySum);
		}

		AccountBookBalance sum = new AccountBookBalance();
		sum.setLname("资产总计");
		sum.setLsn(String.valueOf(lsumIndex));
		sum.setLbalance(lsum);
		sum.setRname("负债和所有者权益（或股东权益）总计");
		sum.setRsn(String.valueOf(rsumIndex));
		sum.setRbalance(rsum);

		result.add(sum);

		return result;
	}

	@Transactional
	public Map<String, AccountBook> find(String relative, String... accounts) {
		Map<String, AccountBook> books = new HashMap<String, AccountBook>();
		List<AccountBook> abs = null;
		if (null == accounts || accounts.length == 0) {
			abs = this.accountBookDao.search(relative);
		} else {
			abs = this.accountBookDao.search(relative, accounts);
		}

		if (null != abs && abs.size() > 0) {
			for (AccountBook ab : abs) {
				books.put(ab.getAccount().getOid(), ab);
			}
		}

		return books;
	}

}
