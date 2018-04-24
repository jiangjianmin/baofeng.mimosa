package com.guohuai.ams.channel;

import java.sql.Timestamp;

import lombok.EqualsAndHashCode;

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@lombok.Builder
public class ChannelRemarksRep {

	 private String remark;

	 private Timestamp time;
}
