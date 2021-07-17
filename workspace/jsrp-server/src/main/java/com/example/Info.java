package com.example;

public class Info {
	int priority;
	String cmdElement;

	public Info(int priority,String cmdElement) {
		this.priority = priority;
		this.cmdElement = cmdElement;
	}
	
	public int getPriority() {
		return this.priority;
	}
}
