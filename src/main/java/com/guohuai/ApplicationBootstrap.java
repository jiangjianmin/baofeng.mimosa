package com.guohuai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.guohuai.component.util.ClientVersionInterceptor;

@Configuration
@ComponentScan(basePackages = { "com.guohuai", "net.kaczmarzyk", "com.ghg" })
@EnableAutoConfiguration
@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
public class ApplicationBootstrap {
 
	public static void main(String[] args) {
		SpringApplication.run(ApplicationBootstrap.class, args);
	}
	
	@Configuration
	static class WebMvcConfigurer extends WebMvcConfigurerAdapter {
		public void addInterceptors(InterceptorRegistry registry) {
			registry.addInterceptor(new ClientVersionInterceptor()).addPathPatterns("/**");
	     }
	 }
}
