package com.guohuai;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.guohuai.account.api.AccountSdk;
import com.guohuai.account.api.request.CreateUserRequest;
import com.guohuai.account.api.response.CreateUserResponse;
import com.guohuai.mmp.platform.accment.AccParam;

public class Test {
	
	public static void main(String[] args) throws IOException {
		BigDecimal d = new BigDecimal("0")
		.subtract(new BigDecimal("0"))
		.divide(new BigDecimal("1"))
		.multiply(new BigDecimal(100))
		.setScale(2, BigDecimal.ROUND_UP); 
		System.out.println(d);
	}
	
	public static int daysBetween(Date smdate, Date bdate) {
		

		StringBuffer sb = new StringBuffer();
		sb.append("a|b|c").append("\r");
		sb.append("a2|b2|c2").append("\r");
		sb.append("a3|b3|c3").append("\r");
		sb.append("a4|b4|c4").append("\r");
		System.out.println(sb.toString());
		InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
		System.out.println(is);
		
		
		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			smdate = sdf.parse(sdf.format(smdate));
			bdate = sdf.parse(sdf.format(bdate));

			Calendar cal = Calendar.getInstance();

			cal.setTime(smdate);
			long time1 = cal.getTimeInMillis();
			cal.setTime(bdate);
			long time2 = cal.getTimeInMillis();

			long between_days = (time1 - time2) / (1000 * 3600 * 24);

			return Integer.parseInt(String.valueOf(between_days));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public void accountQueryList() {
			// UID2016112200000007
//		UserQueryRequest req = new UserQueryRequest();
//		req.setSystemSource(AccParam.SystemSource.MIMOSA.toString());
//		req.setUserType(AccParam.UserType.PLATFORM.toString());
//		req.setSystemUid(null);
//		UserListResponse	orep = new AccountSdk("http://115.28.58.108:80").userQueryList(req);
//		/**
//		 * {"errorCode":0,"errorMessage":null,"total":1,"rows":[{"returnCode":null,"errorMess
//age":null,"oid":"402880ec588a9fb001588aa876a40001","userType":"T3","systemUid":null,"userOid":"UID2016112200000007","systemSource":"mimosa","createTim
//e":"2016-11-22 14:10:46"}]}
//		 */
//		orep.getRows().get(0).getUserOid();
//			System.out.println(orep);
		//UID2016112200000007
//		AccountQueryRequest req = new AccountQueryRequest();
//		req.setUserOid("UID2016112200000007");
//		req.setUserType(AccParam.UserType.PLATFORM.toString());
//		req.setAccountType(AccParam.AccountType.SUPERACCOUNT.toString());
//		AccountListResponse resp = new AccountSdk("http://115.28.58.108:80").accountQueryList(req);
		
		CreateUserRequest oreq = new CreateUserRequest();
		oreq.setSystemUid("111");
		oreq.setRemark("创建发行人");
		oreq.setUserType(AccParam.UserType.SPV.toString());
		oreq.setSystemSource(AccParam.SystemSource.MIMOSA.toString());
		
		try {
			
			CreateUserResponse orep = new AccountSdk("http://115.28.58.108:80").addUser(oreq);
		} catch (Exception e) {
			
		}
		
	}
	

}
