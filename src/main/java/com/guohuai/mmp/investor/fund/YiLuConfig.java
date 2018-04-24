package com.guohuai.mmp.investor.fund;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author huangzijian
 * @since: 16-12-22 下午2:40
 * @version: 1.0.0
 */
@Configuration
@Data
public class YiLuConfig {
	
	@Value("${yilu.fund.customkey}")
    public String fund_customkey;
	
	@Value("${yilu.fund.sign}")
	public String fund_sign;
	
	@Value("${yilu.fund.sign_key}")
	public String fund_sign_key;
	
	@Value("${yilu.fund.privatekey}")
	public String fund_privatekey;
	
	@Value("${yilu.fund.fundTrading}")
	public String fund_fundTrading;
	
	@Value("${yilu.fund.myfund}")
	public String fund_myfund;
	
	@Value("${yilu.fund.myfund_code}")
	public String fund_myfund_code;
	
	@Value("${yilu.fund.isregist_code}")
	public String fund_isregist_code;
	
	@Value("${yilu.fund.getAccountStatusByuuid}")
	public String yilu_fund_getAccountStatusByuuid;
	
	@Value("${yilu.fund.transaccountCloseAcctApi:http://baofeng.yilucaifu.com/api2/myWealth/fund/transaccountCloseAcctApi.html}")
	public String yilu_fund_transaccountCloseAcctApi;
	
	@Value("${yilu.fund.closeacc_code:fw0005}")
	public String yilu_fund_closeacc_code;

}