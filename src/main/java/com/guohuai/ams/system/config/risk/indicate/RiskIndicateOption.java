package com.guohuai.ams.system.config.risk.indicate;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RiskIndicateOption {

	private String oid;
	private String title;
	private List<Option> options = new ArrayList<Option>();

	@Data
	public static class Option {
		private String oid;
		private String title;
		private String dataType;
		private String dataUnit;
	}
}
