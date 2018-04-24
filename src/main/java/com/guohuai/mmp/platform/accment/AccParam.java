package com.guohuai.mmp.platform.accment;

public class AccParam {
	
	public enum UserType {
		INVESTOR("T1"), SPV("T2"), PLATFORM("T3");
		private String value;
		private UserType(String value) {
			this.value = value;
		}
		@Override
		public String toString() {
			
			return this.value;
		}
	}
	
	/**
	 * CLOSED("06") 结算
	 * @author yuechao
	 *
	 */
	/**
	 * 申购:01、赎回:02、派息:03、赠送体验金:04、体验金到期:05、充值:50、提现:51、活转定:52、定转活:53
	 */
	public enum OrderType {
		INVEST("01"), REDEEM("02"), INTEREST("03"), DONATEEXP("04"), EXPEXPIRED("05"), 
		CLOSED("06"),
		PLATFORM2SPV("11"), SPV2PlATFORM("12"), RESERVED2SUPER("13"), SUPER2RESERVED("14"), RESERVED2PLATFORM("15"), PLATFORM2RESERVED("16"),
		PLUSPLUS("20"), MINUSMINUS("30"),
		DEPOSIT("50"), WITHDRAW("51"), HZD("52"), DZH("53");
		private String value;
		private OrderType(String value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return this.value;
		}
	}
	
	/**
	 *  活期  01、定期 06
	 * @author yuechao
	 */
	public enum ProductType {
		HQ("01"), DQ("06");
		private String value;
		private ProductType(String value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return this.value;
		}
		
	}
	
	public enum SystemSource {
		MIMOSA("mimosa");
		private String value;
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
	
	/**
	 * 01为活期，02为活期利息，03为体验金，04为在途，05为冻结户（冻结户，非冻结状态），06定期户，07产品户，08备付金户，09 超级户，10基本户，11 运营户
	 * version 1
	 */
	/**
	 * 01为活期，02为活期利息，03为体验金，04为在途，05为冻结户（冻结户，非冻结状态），06定期户，16定期利息户，08备付金户，09 超级户，10基本户，11 运营户
	 * version 2
	 */
	public static enum AccountType {
		HQ("01"), HQLX("02"), EXP("03"), ONWAY("04"), DJH("05"), DQ("06"), DQLX("16"),
		RESERVEDACCOUNT("08"), SUPERACCOUNT("09"),  PLATFORM("10"), OPERATION("11");
		String value;
		private AccountType(String value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return this.value;
		}
	}
}
