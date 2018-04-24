package com.guohuai.mmp.platform.payment;

public class PayParam {
	
	
	public static enum PayType {
		
		INVEST("01"), REDEEM("02"),SPECIALREDEEM("03"),INCREMENTREDEEM("04");
		String value;
		private PayType(String value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return this.value;
		}
	}
	
	
	public static enum SystemSource {
		MIMOSA("mimosa");
		String value;
		private SystemSource(String value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return this.value;
		}
	}
	
	public static enum ReturnCode {
		RC0000("0000");
		String value;
		private ReturnCode(String value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return this.value;
		}
	}
	

}
