package com.guohuai.mmp.job.log;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@lombok.Builder
@AllArgsConstructor
public class JobRep {
	String jobStatus;
	String jobMessage;
}
