package com.guohuai.component.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "accounting")
@PropertySource("classpath:accountingdefine.properties")
@Component
public class AccountingDefineConfig {

	public static Map<String, String> define = new HashMap<String, String>();

	public Map<String, String> getDefine() {
		return AccountingDefineConfig.define;
	}

	public void setDefine(Map<String, String> define) {
		AccountingDefineConfig.define = define;
	}

}

