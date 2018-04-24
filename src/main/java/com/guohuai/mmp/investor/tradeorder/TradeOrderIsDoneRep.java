package com.guohuai.mmp.investor.tradeorder;

import java.sql.Date;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class TradeOrderIsDoneRep extends BaseRep {
	
	/**
	 * 预期收益起始日
	 */
	private Date beginInterestDate;
	/**
	 * 预期收益到账日
	 */
	private Date interestArrivedDate;
	/**
	 * 预计打款日
	 */
	private Date payDate;
}
