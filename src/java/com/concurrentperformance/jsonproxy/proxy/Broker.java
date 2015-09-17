package com.concurrentperformance.jsonproxy.proxy;

/**
 * TODO Comments
 *
 * @author Lake
 */
public interface Broker {

	<T> T buildMessageInterface(Class<T> type, MessageSystem messageSystem);

	<T> void registerHandler(Class<T> type, T handler);

	MessageSystem getMessageSystem();

}
