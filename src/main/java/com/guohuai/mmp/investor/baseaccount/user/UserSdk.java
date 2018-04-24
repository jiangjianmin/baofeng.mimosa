package com.guohuai.mmp.investor.baseaccount.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
@Configuration
public class UserSdk {
	
	@Value("${uc.host:localhost}")
	private String host;
	
	@Bean
	public UserApi createMoneySdk() {
		return Feign.builder().encoder(new GsonEncoder()).decoder(new GsonDecoder()).logger(new Logger.JavaLogger().appendToFile("user.log")).logLevel(Logger.Level.FULL)
				.target(UserApi.class, "http://" + this.host + "/");
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
}
