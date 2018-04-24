package com.guohuai.mmp.publisher.bankorder;

import java.sql.Timestamp;

import com.guohuai.component.web.view.BaseRep;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class BankOrderIsDoneRep extends BaseRep {
	private Timestamp completeTime;
}
