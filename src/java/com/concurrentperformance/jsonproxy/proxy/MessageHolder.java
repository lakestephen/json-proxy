package com.concurrentperformance.jsonproxy.proxy;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO Comments
 *
 * @author Lake
 */
public class MessageHolder {
	String action;
	Map<String, Object> args = new HashMap<>();

	MessageHolder(){};

	@Override
	public String toString() {
		return "MessageHolder{" +
				"action='" + action + '\'' +
				", args=" + args +
				'}';
	}
}
