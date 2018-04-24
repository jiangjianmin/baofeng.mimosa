package com.guohuai.mmp.schedule.cyclesplit;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * rxjava事件发布
 *
 * @author yujianlong
 * @create 2018-04-02 17:02
 **/
public class DayCutPublishEvent {
	private  String signal;
	private List<String> holds;
	private BigDecimal scale=BigDecimal.ZERO;

	public DayCutPublishEvent() {
	}

	public DayCutPublishEvent(String signal, List<String> holds) {
		this.signal = signal;
		this.holds = holds;
	}

	public DayCutPublishEvent(String signal, List<String> holds, BigDecimal scale) {
		this.signal = signal;
		this.holds = holds;
		this.scale = scale;
	}

	public String getSignal() {
		return signal;
	}

	public void setSignal(String signal) {
		this.signal = signal;
	}

	public List<String> getHolds() {
		return holds;
	}

	public void setHolds(List<String> holds) {
		this.holds = holds;
	}


	public BigDecimal getScale() {
		return scale;
	}

	public void setScale(BigDecimal scale) {
		this.scale = scale;
	}

	@Override
	public String toString() {
		return "DayCutPublishEvent{" +
				"signal='" + signal + '\'' +
				", holds=" + holds +
				", scale=" + scale +
				'}';
	}
}
