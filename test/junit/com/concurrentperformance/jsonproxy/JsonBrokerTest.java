package com.concurrentperformance.jsonproxy;

import com.concurrentperformance.jsonproxy.api.UserService;
import com.concurrentperformance.jsonproxy.proxy.Broker;
import com.concurrentperformance.jsonproxy.proxy.JsonBroker;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO Comments
 *
 * @author Lake
 */
public class JsonBrokerTest {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final Broker broker = new JsonBroker();

	@Test
	public void makeCallAndSend() {

		UserService userService = broker.buildMessageInterface(UserService.class, (topic, value) -> {
			log.info("Write json to Topic [{}], json {}{}", topic, System.lineSeparator(), value );
		});
		
		userService.addUser("STEVE", 42);
	}


}