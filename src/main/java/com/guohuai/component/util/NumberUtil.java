package com.guohuai.component.util;

import java.text.NumberFormat;
import java.util.Random;

public final class NumberUtil {

	private static NumberFormat formater = NumberFormat.getInstance();

	static {
		formater.setGroupingUsed(false);
		formater.setMaximumFractionDigits(32);
		formater.setMaximumIntegerDigits(32);
	}

	public static String valueOf(double d) {
		return formater.format(d);
	}

	public static String valueOf(int i) {
		return formater.format(i);
	}

	public static String valueOf(long l) {
		return formater.format(l);
	}
	
	public static String randomNumb(){
		Random r = new Random();
		String result = "";
		for (int i = 0; i < 6; i++) {
			int num = r.nextInt(10);
			while (0 == num) {
				num = r.nextInt(10);
			}
			result += num;
		}
		return result;
	}
	
	public static void main(String[] args) {
		System.out.println(NumberUtil.randomNumb());
	}

}
