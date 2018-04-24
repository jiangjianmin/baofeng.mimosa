package com.guohuai.ams.system.config.risk.warning;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RiskWarningOption {
	private String oid;
	private String title;

	private List<Indicate> options = new ArrayList<Indicate>();

	public Indicate get(String oid) {
		for (Indicate option : options) {
			if (option.getOid().equals(oid))
				return option;
		}
		return null;
	}

	public boolean hasExists(String oid) {
		return null != get(oid);
	}

	@Data
	public static class Indicate {
		private String oid;
		private String title;
		private String dataType;
		private String dataUnit;

		private List<Indicate.Option> options = new ArrayList<Indicate.Option>();

		public Indicate.Option get(String oid) {
			for (Option option : options) {
				if (option.getOid().equals(oid))
					return option;
			}
			return null;
		}

		public boolean hasExists(String oid) {
			return null != get(oid);
		}

		@Data
		public static class Option {
			private String oid;
			private String title;
		}
	}
}
