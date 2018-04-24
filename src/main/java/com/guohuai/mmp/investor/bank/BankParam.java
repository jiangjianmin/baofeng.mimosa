package com.guohuai.mmp.investor.bank;

public class BankParam {

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
