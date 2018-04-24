package com.guohuai.mmp.serialtask;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class PublisherCloseTaskParams {
	private String offsetOid;
}
