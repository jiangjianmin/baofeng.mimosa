package com.guohuai.ams.acct.account;

import java.io.Serializable;


import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@DynamicInsert
@DynamicUpdate
public class FourElementVo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5886236698469120680L;
	private String name;
	private String idNumb;
	private String cardNumb;
	private String phoneNo;
}
