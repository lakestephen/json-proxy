package com.concurrentperformance.jsonproxy.api;

import com.concurrentperformance.jsonproxy.proxy.JsonDefaultInteger;
import com.concurrentperformance.jsonproxy.proxy.JsonField;
import com.concurrentperformance.jsonproxy.proxy.Topic;

/**
 * TODO Comments
 *
 * @author Lake
 */
@Topic(topic = "authentication")
public interface UserService {

	default void addUser(@JsonField(key = "userName", mandatory = true) String userName,
	                    @JsonField(key = "age", mandatory = false) @JsonDefaultInteger(42) int age) {};

	default void removeUser(@JsonField(key = "userName", mandatory = true) String userName) {};

}
