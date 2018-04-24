package com.guohuai.mmp.captcha;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

@Configuration
public class CaptchaConfig {
     
	@Value("${captcha.isborder:yes}")
	private String isBorder;
	
	@Value("${captcha.borderColor:blue}")
	private String borderColor;
	
	@Value("${captcha.width:125}")
	private String width;
	
	@Value("${captcha.height:45}")
	private String height;
	
	@Value("${captcha.charlength:4}")
	private String charLength;
	
	@Value("${captcha.fontcolor:blue}")
	private String fontColor;
	
    @Bean(name="captchaProducer")
    public DefaultKaptcha getKaptchaBean() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", this.isBorder);
        properties.setProperty("kaptcha.border.color", this.borderColor);
        properties.setProperty("kaptcha.textproducer.font.color", this.fontColor);
        properties.setProperty("kaptcha.image.width", this.width);
        properties.setProperty("kaptcha.image.height", this.height);
        properties.setProperty("kaptcha.session.key", "code");
        properties.setProperty("kaptcha.textproducer.char.length", this.charLength);
        properties.setProperty("kaptcha.textproducer.font.names", "宋体,楷体,微软雅黑");        
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}