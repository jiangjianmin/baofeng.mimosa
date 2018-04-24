package com.guohuai.ams.product;

public enum ProductTypeDetailEnum {
	/**
	 * 产品要素
	 */
	productElement("0", "产品要素"), 
	/**
	 * 产品说明
	 */
	productIntro("01", "产品说明"), 
	/**
	 * 活动产品详情
	 */
	activityDetail("02", "活动产品详情");
	
	private String code;
	private String desc;

	private ProductTypeDetailEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString() {
		return this.code;
	}

	/**
	 * 通过code取得类型
	 * 
	 * @param code
	 * @return
	 */
	public static ProductTypeDetailEnum getType(String code) {
		for (ProductTypeDetailEnum type : ProductTypeDetailEnum.values()) {
			if (type.getCode().equals(code)) {
				return type;
			}
		}
		return null;
	}
}
