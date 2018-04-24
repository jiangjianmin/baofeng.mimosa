package com.guohuai.sms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.guohuai.ApplicationBootstrap;
import com.guohuai.bfsms.api.BfSMSApi;
import com.guohuai.cardvo.service.MimosaService;

import ch.qos.logback.core.net.SyslogOutputStream;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApplicationBootstrap.class)
public class TestProduct {
	
	
	@Autowired
	private MimosaService mimosaService;
	
	@Test
	public void testMySms() {
		String productOid="612b3f2631b84caba31d8d3e3a4839a8";
		String isAuto="1";
		String orderType="invest";
		String orderStatus="paySuccess";
		String code=mimosaService.getTriggerCode(productOid, isAuto, orderType, orderStatus);
		System.out.println("code="+code);
	}
	
}
