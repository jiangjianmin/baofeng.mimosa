package com.guohuai.mmp.publisher.hold;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class PublisherHoldViewPage {

	private long total;

	private List<PublisherHoldView> rows = new ArrayList<PublisherHoldView>();

}
