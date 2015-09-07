package com.concurrentperformance.jsonproxy.api;

import com.concurrentperformance.jsonproxy.proxy.JsonField;
import com.concurrentperformance.jsonproxy.proxy.Topic;

/**
 * TODO Comments
 *
 * @author Lake
 */
@Topic(topic = "authentication")
public interface UserService {

	void addUser(@JsonField(key = "userName", mandatory = true) String userName,
	             @JsonField(key = "age") int age);
}
