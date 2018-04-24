package com.guohuai.component.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "mail")
@PropertySource(value = "classpath:maildefine.properties")
@Component
public class MailDefineConfig {

	public Map<String, String> define = new HashMap<String, String>();

	public Map<String, String> getDefine() {
		return this.define;
	}

	public void setDefine(Map<String, String> define) {
		this.define = define;
	}

}
