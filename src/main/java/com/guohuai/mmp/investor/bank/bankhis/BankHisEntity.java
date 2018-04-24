//package com.guohuai.mmp.investor.bank.bankhis;
//
//import java.io.Serializable;
//import java.sql.Timestamp;
//
//import javax.persistence.Entity;
//import javax.persistence.Table;
//
//import org.hibernate.annotations.DynamicInsert;
//import org.hibernate.annotations.DynamicUpdate;
//
//import com.guohuai.component.persist.UUID;
//
//import lombok.EqualsAndHashCode;
//import lombok.NoArgsConstructor;
//@Entity
//@Table(name = "T_MONEY_INVESTOR_BANK_HIS")
//@lombok.Data
//@EqualsAndHashCode(callSuper = false)
//@NoArgsConstructor
//@DynamicInsert
//@DynamicUpdate
//public class BankHisEntity extends UUID implements Serializable {
//
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
//	
//	/** 用户oid */
//	String investorOid;
//	
//	/** 姓名 */
//	String name;
//	
//	/** 身份证号 */
//	String idNumb;
//	
//	/** 银行名称 */
//	String bankName;
//	
//	/** 银行卡号 */
//	String cardNumb;
//	
//	/** 银行预留手机号 */
//	String phoneNo;
//	
//	/** 操作者 */
//	String operator;
//	
//	Timestamp updateTime;
//	
//	Timestamp createTime;
//
//}
