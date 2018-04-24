package com.guohuai;

import java.math.BigDecimal;

import com.guohuai.component.util.DecimalUtil;

public class GeneralTest {
	public static void main(String[] args) {
		BigDecimal corpus = new BigDecimal(10000);
		BigDecimal rate = new BigDecimal(0.06);
		double incomeCalcBasis = 365;
		
		BigDecimal income = DecimalUtil.setScaleDown(corpus.multiply(new BigDecimal(Math.pow(1 + rate.doubleValue(),
				1 / incomeCalcBasis)).subtract(BigDecimal.ONE).setScale(7,
						BigDecimal.ROUND_HALF_UP)));
		
		System.out.println("income:" + income);
	}
}
