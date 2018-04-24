package com.guohuai.ams.duration.fact.calibration;

import java.math.BigDecimal;
import java.sql.Date;

import com.guohuai.component.util.DateUtil;

import lombok.Data;

@Data
public class MarketAdjustResp {

	public MarketAdjustResp(MarketAdjustEntity e, Date baseDate) {
		this(e);
		this.overtime = DateUtil.same(baseDate, e.getBaseDate()) ? "NO" : "YES";
	}

	public MarketAdjustResp(MarketAdjustEntity e) {
		this.oid = e.getOid();
		this.assetpoolOid = e.getAssetPool().getOid();
		this.lastBaseDate = e.getLastBaseDate();
		this.baseDate = e.getBaseDate();
		this.shares = e.getShares();
		this.nav = e.getNav();
		this.purchase = e.getPurchase();
		this.redemption = e.getRedemption();
		this.lastOrders = e.getLastOrders();
		this.lastShares = e.getLastShares();
		this.lastNav = e.getLastNav();
		this.profit = e.getProfit();
		this.ratio = e.getRatio();
		this.status = e.getStatus();
		this.aversion = e.getAversion();
	}

	private String oid;
	private String assetpoolOid;
	// 上一个校准日
	private Date lastBaseDate;
	// 校准日
	private Date baseDate;
	// 份额
	private BigDecimal shares;
	// 单位净值
	private BigDecimal nav;
	// 净申购
	private BigDecimal purchase;
	// 净赎回
	private BigDecimal redemption;
	// 昨日净申赎
	private BigDecimal lastOrders;
	// 昨日份额
	private BigDecimal lastShares;
	// 昨日单位净值
	private BigDecimal lastNav;
	// 净收益
	private BigDecimal profit;
	// 收益率
	private BigDecimal ratio;
	// 状态 (待审核: CREATE;通过: PASS;驳回: FAIL;已删除: DELETE)
	private String status;

	private String aversion;

	private String overtime;

}
