package com.guohuai.ams.acct.books;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.guohuai.ams.acct.account.Account;

public interface AccountBookDao extends JpaRepository<AccountBook, String> {

	public List<AccountBook> findByRelative(String relative);

	public AccountBook findByRelativeAndAccount(String relative, Account account);

	@Query("from AccountBook b order by b.account.oid asc")
	public List<AccountBook> search();

	@Query("from AccountBook b where b.relative = ?1 order by b.account.oid asc")
	public List<AccountBook> search(String relative);

	@Query("from AccountBook b where b.relative = ?1 and b.account.oid in ?2 order by b.account.oid asc")
	public List<AccountBook> search(String relative, String[] accounts);

}
