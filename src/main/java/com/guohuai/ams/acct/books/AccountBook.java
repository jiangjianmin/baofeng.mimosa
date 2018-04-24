package com.guohuai.ams.acct.books;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.acct.account.Account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_ACCT_BOOKS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class AccountBook implements Serializable {

	private static final long serialVersionUID = 3470477856323261664L;

	@Id
	private String oid;
	private String relative;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "accountOid", referencedColumnName = "oid")
	private Account account;
	private BigDecimal balance;
	private BigDecimal openingBalance;

}
