package com.guohuai.ams.system.config.risk.options;

import lombok.Data;

@Data
public class RiskOptionsResp {

	public RiskOptionsResp(RiskOptions options) {
		this.cateOid = options.getIndicate().getCate().getOid();
		this.cateTitle = options.getIndicate().getCate().getTitle();

		this.indicateOid = options.getIndicate().getOid();
		this.indicateTitle = options.getIndicate().getTitle();
		this.indicateDataType = options.getIndicate().getDataType();
		this.indicateDataUnit = options.getIndicate().getDataUnit();

		this.oid = options.getOid();
		this.dft = options.getDft();
		this.score = options.getScore();
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

	private String oid;
	private String dft;
	private int score;
	private String param0;
	private String param1;
	private String param2;
	private String param3;

	private int seq;

	private String title;

}
