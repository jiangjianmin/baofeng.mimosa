package com.guohuai.guess;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyGuess {
	
    private String title;
    private String productName;
    private BigDecimal expAnnualRate;
    private BigDecimal investAmount;
    private Timestamp investTime;
    private Date expLotteryDate;
    private String myAnswer;
    private String myLotteryAnswer;
}
