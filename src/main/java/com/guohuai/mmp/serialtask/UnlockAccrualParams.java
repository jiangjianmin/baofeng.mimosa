package com.guohuai.mmp.serialtask;

import java.sql.Date;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class UnlockAccrualParams {
	Date accrualBaseDate;
}
