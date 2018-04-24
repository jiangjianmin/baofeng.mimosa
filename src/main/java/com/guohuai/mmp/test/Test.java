package com.guohuai.mmp.test;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Test {
	public static void main(String[] args) {
		//new Test().day();
//		System.out.println("===============");
////		new Test().month();
//		new Test().year();
//		BigDecimal num1 = new BigDecimal("0");
//		BigDecimal num2 = new BigDecimal("0.0000");
//		System.out.println(num1.multiply(new BigDecimal(10000)).compareTo(num2));
		
		System.out.println(Integer.parseInt("000009"));
	}
	
	
	private void year() {
		/**
		 * 本金	10000	
年化利率	8%	
总年数	5	
开始时间	2017/1/1	
结束时间	2019/10/29	
期限	2	
本&利	¥10,322.56	¥10,322.56
利息	¥322.56	¥322.56
		 */
		BigDecimal toll = BigDecimal.ZERO;
		BigDecimal bj = new BigDecimal(10000);
		for (int i = 0; i <  2; i++) {
			BigDecimal ll = bj.multiply(new BigDecimal(0.08)).setScale(2, RoundingMode.DOWN);
			System.out.println("第"+ (i + 1)+ "年本金:" + bj.toString() + "第" + (i + 1) + "一年收益:" + ll);
			bj = bj.add(ll);
			toll = toll.add(ll);
		}
		System.out.println("两年利率：" + toll.toString());
	}


	private void month() {
		/**
		 * 按月算复利		
本金	10000	
年化利率	8%	
总月份	12	
开始时间	2017/1/1	
结束时间	2017/5/31	
期限	5	
本&利	¥10,337.81	¥10,337.81
利息	¥337.81	¥337.81

		 */
		BigDecimal toll = BigDecimal.ZERO;
		BigDecimal bj = new BigDecimal(10000);
		for (int i = 0; i <  5; i++) {
			BigDecimal ll = bj.multiply(new BigDecimal(0.08).divide(new BigDecimal(12), 10, RoundingMode.DOWN)).setScale(2, RoundingMode.DOWN);
			System.out.println("第"+ (i + 1)+ "个月本金:" + bj.toString() + "第" + (i + 1) + "一个月收益:" + ll);
			bj = bj.add(ll);
			toll = toll.add(ll);
		}
		System.out.println("5个月利率：" + toll.toString());
	}


	public void day() {
		/**
		 * 本金	10000	
年化利率	8%	
年天数	360	
开始时间	2017/1/1	
结束时间	2017/1/31	
期限	30	
本&利	¥10,066.88	¥10,066.88
利息	¥66.88	¥66.88

		 */
		BigDecimal toll = BigDecimal.ZERO;
		BigDecimal bj = new BigDecimal(10000);
		for (int i = 0; i <  30; i++) {
			BigDecimal ll = bj.multiply(new BigDecimal(0.08).divide(new BigDecimal(360), 10, RoundingMode.DOWN)).setScale(2, RoundingMode.DOWN);
			System.out.println("第"+ (i + 1)+ "天本金:" + bj.toString() + "第" + (i + 1) + "天收益:" + ll);
			bj = bj.add(ll);
			toll = toll.add(ll);
		}
		System.out.println("30天利率：" + toll.toString());
		
	}
}
