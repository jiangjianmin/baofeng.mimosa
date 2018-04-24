package com.guohuai.mmp.investor.tradeorder;

import java.io.Serializable;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class WriteOffRep extends BaseRep implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2400157446721571902L;
	WriteOffData writeOffData=new WriteOffData();
}
