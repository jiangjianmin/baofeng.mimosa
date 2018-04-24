package com.guohuai.sms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.guohuai.ApplicationBootstrap;
import com.guohuai.bfsms.api.BfSMSApi;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApplicationBootstrap.class)
public class TestSms {
	@Value("${bfsms.OperID}")
	private String operID;
	@Value("${bfsms.OperPass}")
	private String operPass;
	@Value("${bfsms.AppendID}")
	private String appendID;
	@Value("${bfsms.ContentType}")
	private String contentType;
	@Value("${bfsms.types}")
	private String smsTypes;
	@Value("${bfsms.comcontent}")
	private String comcontent;
	
	@Autowired
	private BfSMSApi bfSMSApi;
	
	@Test
	public void testMySms() {
		String smsXmlRep = bfSMSApi.sendSMS(this.operID, this.operPass, "18701691306", "【暴风金融】报告主人，有惊喜'东方吹'已进入您的账户，不用白不用 https://8.baofeng.com/bfh5/html/share/invite_page.html?inviteCode=46656&channelid=appshare&telnum=18701691306 退订回TD");
		System.out.println("################################");
		System.out.println("smsXmlRep:"+smsXmlRep);
		System.out.println("################################");
	}
	
}
