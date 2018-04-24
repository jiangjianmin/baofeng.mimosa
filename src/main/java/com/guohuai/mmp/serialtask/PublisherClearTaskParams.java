package com.guohuai.mmp.serialtask;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PublisherClearTaskParams {
	private String offsetOid;
}
