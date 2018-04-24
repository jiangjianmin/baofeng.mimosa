package com.guohuai.mmp.schedule.cyclesplit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.reactivex.subjects.PublishSubject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


/**
 *
 *日切配置，配置监听器
 * @author yujianlong
 * @date 2018/4/1 18:32
 * @param
 * @return
 */
@Configuration
@Component
public class DayCutConfig {

//	@Bean
//	public ObjectMapper objectMapper() {
//		return new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
//	}

	@Bean
	public PublishSubject<DayCutPublishEvent> publishSubject(){
		PublishSubject<DayCutPublishEvent> publishSubject = PublishSubject.create();
		return publishSubject;
	}

	@Bean
	@Qualifier(value="electronicSignatureSubject")
	public PublishSubject<String> electronicSignatureSubject(){
		PublishSubject<String> publishSubject = PublishSubject.create();
		return publishSubject;
	}

}

