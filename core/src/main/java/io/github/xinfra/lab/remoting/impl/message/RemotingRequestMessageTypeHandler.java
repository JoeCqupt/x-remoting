package io.github.xinfra.lab.remoting.impl.message;

import io.github.xinfra.lab.remoting.connection.Connection;
import io.github.xinfra.lab.remoting.exception.DeserializeException;
import io.github.xinfra.lab.remoting.exception.ResponseStatusRuntimeException;
import io.github.xinfra.lab.remoting.impl.handler.RequestHandler;
import io.github.xinfra.lab.remoting.impl.handler.RequestHandlerRegistry;
import io.github.xinfra.lab.remoting.impl.handler.ResponseObserver;
import io.github.xinfra.lab.remoting.message.AbstractRequestMessageTypeHandler;
import io.github.xinfra.lab.remoting.message.Message;
import io.github.xinfra.lab.remoting.message.RequestMessage;
import io.github.xinfra.lab.remoting.message.ResponseStatus;
import io.github.xinfra.lab.remoting.message.Responses;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

@Slf4j
public class RemotingRequestMessageTypeHandler extends AbstractRequestMessageTypeHandler {

	private RequestHandlerRegistry requestHandlerRegistry;

	public RemotingRequestMessageTypeHandler(RequestHandlerRegistry requestHandlerRegistry) {
		this.requestHandlerRegistry = requestHandlerRegistry;
	}

	@Override
	public void handleMessage(Connection connection, RequestMessage requestMessage) {
		RemotingRequestMessage remotingRequestMessage = (RemotingRequestMessage) requestMessage;
		remotingRequestMessage.deserializePath(); // TODO：其实还是在io线程中解码，不如直接放到解码器中进行？
		RequestHandler requestHandler = requestHandlerRegistry.lookup(remotingRequestMessage.getPath());
		if (requestHandler == null) {
			log.warn("RequestHandler not found for path: {}", remotingRequestMessage.getPath());
			throw new ResponseStatusRuntimeException(ResponseStatus.NotFound);
		}
		Executor executor = requestHandler.getExecutor();
		if (executor == null) {
			executor = connection.getExecutor();
		}

		Runnable task = () -> {
			try {
				try {
					remotingRequestMessage.deserialize();
				}
				catch (DeserializeException e) {
					throw new ResponseStatusRuntimeException(ResponseStatus.DeserializeException, e);
				}
				ResponseObserver responseObserver = new ResponseObserver(connection, remotingRequestMessage);
				requestHandler.asyncHandle(remotingRequestMessage.getBody().getBodyValue(), responseObserver);
			}
			catch (Exception e) {
				log.error("RemotingRequestMessageTypeHandler handleMessage ex", e);
				Responses.handleExceptionResponse(connection, remotingRequestMessage, e);
			}
		};

		executor.execute(task);
	}

}
