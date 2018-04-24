package com.guohuai.ams.system.config.risk.indicate.collect;

import lombok.Data;

@Data
public class RiskIndicateCollectResp {

	public RiskIndicateCollectResp(RiskIndicateCollect collect) {
		this.oid = collect.getOid();
		this.indicateOid = collect.getIndicate().getOid();
		this.relative = collect.getRelative();
		this.collectOption = collect.getCollectOption();
		this.collectData = collect.getCollectData();
		this.collectScore = collect.getCollectScore();
	}
	
	private String oid;
	private String indicateOid;
	private String relative;
	private String collectOption;
	private String collectData;
	private int collectScore;

}
