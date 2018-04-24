package com.guohuai.ams.channel;

import lombok.Data;

@Data
public class ChannelOptions {

	public ChannelOptions(Channel channel) {
		this.oid = channel.getOid();
		this.text = channel.getChannelName();
	}

	private String oid;
	private String text;

}
