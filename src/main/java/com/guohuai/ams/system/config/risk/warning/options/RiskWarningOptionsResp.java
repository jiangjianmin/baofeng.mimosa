package com.guohuai.ams.system.config.risk.warning.options;

import com.guohuai.ams.system.config.risk.cate.RiskCate;
import com.guohuai.ams.system.config.risk.indicate.RiskIndicate;
import com.guohuai.ams.system.config.risk.warning.RiskWarning;

import lombok.Data;

@Data
public class RiskWarningOptionsResp {

	public RiskWarningOptionsResp(RiskWarningOptions options) {		
		RiskWarning warning = options.getWarning();
		RiskIndicate indicate = warning.getIndicate();
		RiskCate cate = indicate.getCate();
		
		this.cateOid = cate.getOid();
		this.cateTitle = cate.getTitle();

		this.indicateOid = warning.getOid();
		this.indicateTitle = warning.getTitle();
		this.indicateDataType = indicate.getDataType();
		this.indicateDataUnit = indicate.getDataUnit();

		this.oid = options.getOid();
		this.wlevel = options.getWlevel();
		this.param0 = options.getParam0();
		this.param1 = options.getParam1();
		this.param2 = options.getParam2();
		this.param3 = options.getParam3();
		this.seq = options.getSeq();

		this.title = options.showTitle();

	}

	private String cateOid;
	private String cateTitle;

	private String indicateOid;
	private String indicateTitle;
	private String indicateDataType;
	private String indicateDataUnit;

	private String warningOid;
	private String warningTitle;
	
	
	private String oid;
	private String wlevel;
	private String param0;
	private String param1;
	private String param2;
	private String param3;

	private int seq;

	private String title;

}
