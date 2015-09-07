package com.concurrentperformance.jsonproxy.proxy;
/**
 * TODO Comments
 *
 * @author Lake
 */
public interface Broker {

	<T> T buildMessageInterface(Class<T> type, Sender sender);
}
