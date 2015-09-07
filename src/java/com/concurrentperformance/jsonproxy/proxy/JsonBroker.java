package com.concurrentperformance.jsonproxy.proxy;

import com.google.common.reflect.AbstractInvocationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	private final Map<Object, Sender> senders = new ConcurrentHashMap();

	@Override
	public <T> T buildMessageInterface(Class<T> type, Sender sender) {
		T proxy = (T)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{type}, this);

		senders.put(proxy, sender);
		return proxy;
	}

	@Override
	public Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {

			Annotation[][] parametersAnnotations = method.getParameterAnnotations();
			checkState(parametersAnnotations.length == args.length);

			StringBuffer json = new StringBuffer();
			json.append("{").append(System.lineSeparator());
			buildJsonParameter(json, "action", method.getName(), " ");
			json.append("  ").append("{").append(System.lineSeparator());


			for (int i=0;i<parametersAnnotations.length;i++) {
				Annotation[] parameterAnnotations = parametersAnnotations[i];
				Object arg = args[i];
				JsonField jsonField = mineParameterOfClass(parameterAnnotations, JsonField.class);
				buildJsonParameter(json, jsonField.key(), arg, "   ");
			}
			json.append("  ").append("}").append(System.lineSeparator());
			json.append("}").append(System.lineSeparator());

			String topic = method.getDeclaringClass().getAnnotation(Topic.class).topic();

			Sender sender = senders.get(proxy);
			sender.send(topic, json);
			return null;
		}

	private void buildJsonParameter(StringBuffer buffer,String key, Object arg, String indent) {
		buffer.append(indent)
				.append("\"").append(key).append("\"")
				.append(": ")
				.append(arg)
				.append(",").append(System.lineSeparator());
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
}
