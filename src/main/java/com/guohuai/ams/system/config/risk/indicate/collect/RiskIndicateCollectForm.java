package com.guohuai.ams.system.config.risk.indicate.collect;

import java.util.List;

import lombok.Data;

@Data
public class RiskIndicateCollectForm {

	private String type;
	private String relative;

	private List<OptionData> datas;

	@Data
	public static class OptionData {

		private String indicateOid;
		private String collectData;
		private String options;

	}

}
