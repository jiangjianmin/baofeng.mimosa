package com.guohuai.ams.enums;

public enum RiskWarningCollectLevel {

	high("HIGH", 3), mid("MID", 2), low("LOW", 1), none("NONE", 0);

	private int code;
	private String level;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	private RiskWarningCollectLevel(String level, int code) {
		this.code = code;
		this.level = level;
	}

	public static Integer getLeveCode(String level) {
		for (RiskWarningCollectLevel temp : RiskWarningCollectLevel.values()) {
			if (temp.getLevel().equals(level)) {
				return temp.getCode();
			}
		}
		return null;
	}
	
	public static String getLevel(int code) {
		for (RiskWarningCollectLevel temp : RiskWarningCollectLevel.values()) {
			if (temp.getCode() == code) {
				return temp.getLevel();
			}
		}
		return null;
	}
}
