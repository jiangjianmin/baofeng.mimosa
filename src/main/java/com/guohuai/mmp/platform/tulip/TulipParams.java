package com.guohuai.mmp.platform.tulip;

public class TulipParams {
	
	public static enum Status {
		SUCCESS("success"), FAIL("fail");
		private String value;

		private Status(String value) {
			this.value = value;
		}

		public String toString() {
			return this.value;
		};
	}
}
