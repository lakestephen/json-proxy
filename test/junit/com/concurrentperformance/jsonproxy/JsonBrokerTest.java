package com.concurrentperformance.jsonproxy;

import com.concurrentperformance.jsonproxy.api.UserService;
import com.concurrentperformance.jsonproxy.proxy.Broker;
import com.concurrentperformance.jsonproxy.proxy.JsonBroker;
import com.concurrentperformance.jsonproxy.proxy.MessageSystem;
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

		// A dummy message system that will just log the message it receives whe a call is made
		MessageSystem loggingMessageSystem = (topic, payload) -> log.info("Writing json to Topic [{}], json {}{}", topic, System.lineSeparator(), payload);


		// Build a new proxy instance of the service UserService, and pass in a message system that
		// in the real world would route the message to the destination
		UserService userService = broker.buildMessageInterface(UserService.class, loggingMessageSystem);


		// Make a call that translates into Json and is received in the loggingMessageSystem
		userService.addUser("STEVE", 42);
	}


	@Test
	public void receiveAndInvoke() {

		// The broket act to receive the Json. This will be hidden from the user.
		MessageSystem messageSystem = broker.getMessageSystem();

		// It should be notes that this interface under Java 8 makes use of the default fields allowing
		// me to just supply the ones I am interested in here. This will be different in earlier versions.
		UserService userServiceCallback = new UserService() {
			@Override
			public void addUser(String userName, int age) {
				log.info("Add User [{}], age [{}]" + userName, age);
			}
		};

		// Register the handler - You can register many handlers for each
		broker.registerHandler(UserService.class, userServiceCallback);

		// This is simulating the OpenFin bus writing to the reliever for any topic.
		messageSystem.send("authentication",
						"{\n" +
						"  \"action\": \"addUser\",\n" +
						"  \"args\": {\n" +
						"    \"userName\": \"STEVE\",\n" +
						"    \"age\": 42\n" +
						"  }\n" +
						"}");
	}
}