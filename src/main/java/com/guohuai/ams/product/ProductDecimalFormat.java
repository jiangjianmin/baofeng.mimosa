package com.guohuai.ams.product;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class ProductDecimalFormat {
	
	public static String format(BigDecimal decimal) {
		if(decimal==null) {
			return "";
		}
		return new DecimalFormat("0.00").format(decimal); 
	}
	
	public static String format(BigDecimal decimal, String pattern) {
		if(decimal==null) {
			return "";
		}
		return new DecimalFormat(pattern).format(decimal); 
	}
	
	public static BigDecimal multiply(BigDecimal multiplier, BigDecimal multiplicand) {
		if(multiplier==null || multiplicand==null) {
			return null;
		}
		return multiplier.multiply(multiplicand);
	}
	
	public static BigDecimal multiply(BigDecimal multiplier, int multiplicand) {
		if(multiplier==null) {
			return null;
		}
		return multiplier.multiply(new BigDecimal(multiplicand));
	}
	
	public static BigDecimal multiply(BigDecimal multiplier) {
		if(multiplier==null) {
			return null;
		}
		return multiplier.multiply(new BigDecimal("100"));
	}
	
	public static long format2Cent(BigDecimal amount) {
		if (amount == null) {
			return 0;
		}
		return amount.multiply(new BigDecimal("100")).longValue();
	}
	
	public static BigDecimal format2Yuan(long amount) {
		
		return new BigDecimal(amount).divide(new BigDecimal(100));
	}
	
	public static BigDecimal divide(BigDecimal divisor,BigDecimal dividend) {
		if(divisor==null || dividend==null) {
			return null;
		}
		return divisor.divide(dividend);
	}
	
	public static BigDecimal divide(BigDecimal divisor,int dividend) {
		if(divisor==null) {
			return null;
		}
		return divisor.divide(new BigDecimal(dividend));
	}
	
	public static BigDecimal divide(BigDecimal divisor) {
		if(divisor==null) {
			return null;
		}
		return divisor.divide(new BigDecimal("100"));
	}
			
}
