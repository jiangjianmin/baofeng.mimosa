package com.guohuai.mmp.publisher.investor;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.guohuai.component.util.DecimalUtil;

@Service
public class InterestFormula {
	
//	快活宝复利算法
//	预期年化收益10%，设每日复利率为a，用户本金为A
//	日期	用户份额
//	申购日T日	A
//	起息日T+1日	A+A*a=A(1+a)
//	T+2日	A(1+a)(1+a)=A(1+a)^2
//	T+3日	A(1+a)^3
//	...	
//	T+n日	A(1+a)^n
//	则当n=365时，预期年化收益率为；
//	[A(1+a)^365-A]/A=10%
//	即，(1+a)^365-1=10%
//	[[  a=(1+10%)^1/365-1 ]]
//	这种算法下，a=0.0261%，即每日每万份收益2.61元
//	而我们平台目前每万份收益是按照单利模式进行推算的，10000本金存续1年，10%年化收益1000元，每日每万份收益1000/365=2.74元。
//	这样用户10000元存续一年的话，最终年化收益是大于10%的，到期本息加起来为10000*(1+0.0274%)^365=11047.64元
	
	public static BigDecimal compound(BigDecimal corpus, BigDecimal rate, String incomeCalcBasis) {
		return compound(corpus, rate, Integer.parseInt(incomeCalcBasis));
	}
	
	public static BigDecimal compound(BigDecimal corpus, BigDecimal rate, double incomeCalcBasis) {
		return DecimalUtil.setScaleDown(corpus.multiply(new BigDecimal(Math.pow(1 + rate.doubleValue(),
				1 / incomeCalcBasis)).subtract(BigDecimal.ONE).setScale(7,
						BigDecimal.ROUND_HALF_UP)));
	}
	/**
	 * 复利计算，通过年化收益换取万份收益
	 * @param annualInterest 年化收益率
	 * @param days 一年多少天，365或360
	 * @return
	 */
	public static BigDecimal caclDayInterest(BigDecimal annualInterest, int days){
		return new BigDecimal(Math.pow(1+annualInterest.doubleValue(), 1d/days)).subtract(BigDecimal.ONE).setScale(7, BigDecimal.ROUND_HALF_UP);
	}
	
	public static void main(String[] args) {
//		BigDecimal corpus = new BigDecimal(5000);
//		BigDecimal rate = new BigDecimal(0.05);
//		System.out.println("------------compound-------------");
//		for (int i = 0; i < 3; i++) {
//			BigDecimal income = testcompound(corpus, rate, 365);
//			System.out.println("第" + i + "天本金：" + corpus.toString() + ",第" + i + "天利息:" + income);
//			corpus = corpus.add(income);
//			
//		}
//		System.out.println("------------simple-------------");
//		corpus = new BigDecimal(5000);
//		for (int i = 0; i < 3; i++) {
//			BigDecimal income = testsimple(corpus, rate, 365);
//			System.out.println("第" + i + "天本金：" + corpus.toString() + ",第" + i + "天利息:" + income);
//			corpus = corpus.add(income);
//		}
//		System.out.println(testcompound(new BigDecimal(1000), new BigDecimal(3.2934), 360));
		System.out.println(testsimple(new BigDecimal(1000), new BigDecimal("0.0880"), 365, 3));
		
	}
	
	public static BigDecimal testcompound(BigDecimal corpus, BigDecimal rate, double incomeCalcBasis) {
		return (corpus.multiply(new BigDecimal(Math.pow(1 + rate.doubleValue(),
				1 / incomeCalcBasis)).subtract(BigDecimal.ONE).setScale(7,
						BigDecimal.ROUND_HALF_UP))).setScale(2, RoundingMode.DOWN);
	}
	
	public static BigDecimal testsimple(BigDecimal corpus, BigDecimal rate, int incomeCalcBasis, int days) {
		return (corpus.multiply(rate).multiply(new BigDecimal(days))
				.divide(new BigDecimal(incomeCalcBasis), 10, RoundingMode.DOWN)).setScale(2, RoundingMode.DOWN);
	}
	
	
	public static BigDecimal simple(BigDecimal corpus, BigDecimal rate, int incomeCalcBasis) {
		return DecimalUtil.setScaleDown(corpus.multiply(rate)
				.divide(new BigDecimal(incomeCalcBasis), 10, DecimalUtil.roundMode));
	}
	
	
	public static BigDecimal simple(BigDecimal corpus, BigDecimal rate, String incomeCalcBasis, int days) {
		return simple(corpus, rate, Integer.parseInt(incomeCalcBasis), days);
	}
	
	public static BigDecimal simple(BigDecimal corpus, BigDecimal rate, int incomeCalcBasis, int days) {
		return DecimalUtil.setScaleDown(corpus.multiply(rate).multiply(new BigDecimal(days))
				.divide(new BigDecimal(incomeCalcBasis), 10, DecimalUtil.roundMode));
	}
	
	
}
