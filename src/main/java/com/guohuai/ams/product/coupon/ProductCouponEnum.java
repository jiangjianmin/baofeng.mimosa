package com.guohuai.ams.product.coupon;


public enum ProductCouponEnum {
	
	redCoupon(1,"红包"),
	raiseRateCoupon(2,"加息券"),
	
	useRedCoupon(1,"可以使用红包"),
	useRaiseRateCoupon(1,"可以使用加息券"),
	
	notUseRedCoupon(2,"不可以使用红包"),
	notUseRaiseRateCoupon(2,"不可以使用加息券"),
	
	useAllRedCoupon(3,"可以使用全部红包"),
	useAllRaiseRateCoupon(3,"可以使用全部加息券");
	
	private ProductCouponEnum(Integer code, String name) {
		this.code = code;
		this.name = name;
	}
	/**
	 * 通过code取得类型
	 * 
	 * @param code
	 * @return
	 */
	public static String getName(Integer code) {
		for (ProductCouponEnum type : ProductCouponEnum.values()) {
			if (type.getCode().equals(code)) {
				return type.getName();
			}
		}
		return null;
	}
	
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	private Integer code;
	private String name;
}
