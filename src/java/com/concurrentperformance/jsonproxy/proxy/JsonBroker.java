package com.concurrentperformance.jsonproxy.proxy;

import com.google.common.reflect.AbstractInvocationHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Using AbstractInvocationHandler to handle the Object.xxx() methods
 *
 * @author Lake
 */
public class JsonBroker extends AbstractInvocationHandler implements Broker, InvocationHandler  {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	// Used as a heterogeneous type safe container
	private final Map<Object, MessageSystem> senders = new ConcurrentHashMap();

	// Used as a heterogeneous type safe container
	private final ConcurrentMap<String, TopicReceivers> receiversForTopics = new ConcurrentHashMap();

	@Override
	public <T> T buildMessageInterface(Class<T> type, MessageSystem messageSystem) {
		checkState(type.isInterface() == true);
		checkState(type.getAnnotation(Topic.class) != null);

		T proxy = (T)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{type}, this);

		senders.put(proxy, messageSystem);
		return proxy;
	}

	@Override
	public Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {

		Topic topicAnnotation = method.getDeclaringClass().getAnnotation(Topic.class);
		checkState(topicAnnotation != null);
		String topic = topicAnnotation.topic();

		Annotation[][] parametersAnnotations = method.getParameterAnnotations();
		checkState(parametersAnnotations.length == args.length);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		MessageHolder messageHolder = new MessageHolder();

		messageHolder.action = method.getName();

		for (int i=0;i<parametersAnnotations.length;i++) {
			Annotation[] parameterAnnotations = parametersAnnotations[i];
			Object arg = args[i];
			JsonField jsonField = mineParameterOfClass(parameterAnnotations, JsonField.class);
			messageHolder.args.put(jsonField.key(), arg);
		}
		String json = gson.toJson(messageHolder);


		MessageSystem messageSystem = senders.get(proxy);
		messageSystem.send(topic, json);
		return null;
	}

	private final MessageSystem receiver = (topic, payload) -> {
		Gson gson = new Gson();
		MessageHolder messageHolder = gson.fromJson(payload, MessageHolder.class);
		log.info(messageHolder.toString());
		TopicReceivers receivers = receiversForTopics.get(topic);

		if (receivers != null) {

			receivers.stream().forEach(o -> {


				for (Method method : receivers.type.getMethods()) {
					Annotation[][] parametersAnnotations = method.getParameterAnnotations();

					for (int i = 0; i < parametersAnnotations.length; i++) {
						Annotation[] parameterAnnotations = parametersAnnotations[i];
						JsonField jsonField = mineParameterOfClass(parameterAnnotations, JsonField.class);
						if (jsonField == null ||
								!messageHolder.args.containsKey(jsonField.key())) {
							break;
						}

						log.info(method.toString());
					}

				}

			});

		}
	};

	@Override
	public MessageSystem getMessageSystem() {
		return receiver;
	}

	@Override
	public <T> void registerHandler(Class<T> type, T handler) {
		checkNotNull(type);
		checkNotNull(handler);

		String topic = type.getAnnotation(Topic.class).topic();
		checkNotNull(topic, "Missing {} annotation", Topic.class.getCanonicalName());

		log.info("Registering handler [{}] for topic [{}]", type.getCanonicalName(), topic);

		List<Object> receivers = receiversForTopics.get(topic);
		if (receivers == null) {
			receiversForTopics.putIfAbsent(topic, new TopicReceivers(type));
			receivers = receiversForTopics.get(topic);
		}

		receivers.add(handler);
	}


	private <T extends Annotation> T mineParameterOfClass(Annotation[] parameterAnnotations, Class<T> clazz) {
		checkNotNull(parameterAnnotations);
		checkNotNull(clazz);
		for (int i=0;i<parameterAnnotations.length;i++) {
			if (parameterAnnotations[i].annotationType().equals(clazz)) {
				return (T)parameterAnnotations[i];
			}
		}
		return null;
	}

	class TopicReceivers extends ArrayList<Object> {

		final Class type;


		TopicReceivers(Class type) {
			this.type = type;
		}
	}
}
