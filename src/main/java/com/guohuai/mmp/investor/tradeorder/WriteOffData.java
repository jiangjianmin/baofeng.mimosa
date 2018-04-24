package com.guohuai.mmp.investor.tradeorder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.guohuai.ams.product.Product;
import com.guohuai.mmp.investor.baseaccount.InvestorBaseAccountEntity;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class WriteOffData implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -945316993532804977L;

	List<WriteOffProduct> products = new ArrayList<WriteOffProduct>();
	
	List<WriteOffUser> users = new ArrayList<WriteOffUser>();
}
