package com.guohuai.cache;

import java.math.BigDecimal;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

@Configuration
public class CacheConfig {
	@Value("${daily.redeem.count:3}")
	public Long DAILY_REDEEM_COUNT = 3L;
	@Value("${daily.redeem.amount:500000}")
	public BigDecimal DAILY_REDEEM_AMOUNT = BigDecimal.valueOf(500000);
}