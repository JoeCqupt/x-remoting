package io.github.xinfra.lab.remoting.impl.message;

import io.github.xinfra.lab.remoting.impl.handler.RequestHandlerRegistry;
import io.github.xinfra.lab.remoting.message.AbstractMessageHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemotingMessageHandler extends AbstractMessageHandler {

	public RemotingMessageHandler(RequestHandlerRegistry requestHandlerRegistry) {
		super();
		registerMessageTypeHandler(new RemotingRequestMessageTypeHandler(requestHandlerRegistry));
	}

}
