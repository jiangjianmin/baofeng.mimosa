package com.guohuai.bfsms.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.guohuai.bfsms.GbkEncoder;
import com.guohuai.bfsms.Utf8Encoder;

import feign.Feign;
import feign.Logger;

@lombok.Data
@Configuration
public class BfSMSSdk {

	@Value("${bfsms.host:localhost}")
	private String host;
	
	@Bean
	public BfSMSApi bfsmsSdk() {
		return Feign.builder().encoder(new Utf8Encoder())
				.logger(new Logger.JavaLogger().appendToFile("http.log")).logLevel(Logger.Level.FULL)
				.target(BfSMSApi.class, "http://" + this.host + "/");
	}
}
