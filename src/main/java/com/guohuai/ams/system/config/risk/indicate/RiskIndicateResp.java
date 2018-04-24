package com.guohuai.ams.system.config.risk.indicate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskIndicateResp {

	public RiskIndicateResp(RiskIndicate indicate) {
		this.cateOid = indicate.getCate().getOid();
		this.cateType = indicate.getCate().getType();
		this.cateTitle = indicate.getCate().getTitle();
		this.indicateOid = indicate.getOid();
		this.indicateTitle = indicate.getTitle();
		this.indicateState = indicate.getState();
		this.indicateDataType = indicate.getDataType();
		this.indicateDataUnit = indicate.getDataUnit();
	}

	private String cateOid;
	private String cateType;
	private String cateTitle;

	private String indicateOid;
	private String indicateTitle;
	private String indicateState;
	private String indicateDataType;
	private String indicateDataUnit;

}
