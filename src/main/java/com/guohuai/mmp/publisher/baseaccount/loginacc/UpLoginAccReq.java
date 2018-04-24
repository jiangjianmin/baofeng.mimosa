package com.guohuai.mmp.publisher.baseaccount.loginacc;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UpLoginAccReq {
	String[] userOids;
	String corperateOid;
}
