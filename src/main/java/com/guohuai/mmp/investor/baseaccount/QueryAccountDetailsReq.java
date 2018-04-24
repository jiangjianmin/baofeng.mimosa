package com.guohuai.mmp.investor.baseaccount;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;




@lombok.Data
@EqualsAndHashCode(callSuper = false)
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryAccountDetailsReq {
    String phoneNum;
    String identityId;
    String startTime;
    String endTime;
    String extendsParam;
    int pageNo;
    int pageSize;
}