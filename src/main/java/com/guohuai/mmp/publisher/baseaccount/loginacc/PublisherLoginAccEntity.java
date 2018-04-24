package com.guohuai.mmp.publisher.baseaccount.loginacc;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.persist.UUID;
import com.guohuai.mmp.publisher.baseaccount.PublisherBaseAccountEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 发行人-基本账户-登录账户关系
 * 
 * @author yuechao
 *
 */
@Entity
@Table(name = "T_MONEY_PUBLISHER_BASEACCOUNT_LOGINACC")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class PublisherLoginAccEntity extends UUID {

	private static final long serialVersionUID = -6301241499172524045L;


	/**
	 * 所属发行人
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "publisherOid", referencedColumnName = "oid")
	private PublisherBaseAccountEntity publisherBaseAccount;
	
	/**
	 * 登录账号
	 */
	private String loginAcc;
	
	
	private Timestamp updateTime;

	private Timestamp createTime;
}
