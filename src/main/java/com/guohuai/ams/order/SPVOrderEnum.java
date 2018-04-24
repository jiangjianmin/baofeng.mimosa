package com.guohuai.ams.order;

import java.util.HashMap;
import java.util.Map;

public class SPVOrderEnum {
	public static Map<String, String> enums = new HashMap<String, String>();
	static {
		/**
		 * 交易类型orderType：
		 */
		enums.put("INVEST", "申购");
		enums.put("REDEEM", "赎回");

		/**
		 * 订单类型orderCate：
		 */
		enums.put("TRADE", "交易订单");
		enums.put("STRIKE", "冲账订单");

		/**
		 * 订单状态orderStatus：
		 */
		enums.put("SUBMIT", "未确认");
		enums.put("CONFIRM", "确认");
		enums.put("DISABLE", "失效");
		enums.put("CALCING", "清算中");

		/**
		 * 订单入账状态entryStatus：
		 */
		enums.put("YES", "已入账");
		enums.put("NO", "未入账");


	}
}
