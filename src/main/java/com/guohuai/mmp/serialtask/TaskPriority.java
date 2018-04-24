package com.guohuai.mmp.serialtask;

public enum TaskPriority {
	INVEST(9),REDEEM(9),REDEEMINVEST(9);
	private int value;
	private TaskPriority(int v) {
		this.value=v;
	}
	public int getValue() {
		return value;
	}
}
