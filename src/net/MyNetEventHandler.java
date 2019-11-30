package net;

import event.Event;
import event.EventDispatcher;
import event.EventType;

/**
 * 
 * @author Gabriele Giunchi
 * 
 * Implementazione di {@link NetEventHandler} che adotta il seguente comportamento:
 * 
 * - Quando la socket riceve un messaggio viene creato un evento di tipo {@link EventType.message_from_server} 
 * 	 e mandato all' {@link EventDispatcher}
 * 
 * - Quando si verifica un errore viene chiusa la socket
 *
 */
public final class MyNetEventHandler implements NetEventHandler {

	@Override
	public void handleMessage(final EndPoint endPoint, final String message) {
		if (message == null) {
			EventDispatcher.getDispatcher().dispatchEvent(new Event(EventType.server_connection_lost));
		} else {
			System.out.println(
				new StringBuilder()
				 	.append(endPoint.getSocket().getRemoteSocketAddress().toString())
				 	.append(" : ")
				 	.append(message).toString()
			);
			EventDispatcher.getDispatcher().dispatchEvent(new Event(EventType.message_from_server, message));
		}
		
	}

	@Override
	public void handleError(final EndPoint endPoint, final Exception e) {
		EventDispatcher.getDispatcher().dispatchEvent(new Event(EventType.server_connection_lost));
	}

}
