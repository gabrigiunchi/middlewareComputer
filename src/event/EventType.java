package event;

/**
 * 
 * @author Gabriele Giunchi
 * 
 * Tipi di eventi gestiti
 *
 */
public enum EventType {
	
	/**
	 * Indica che è stata stabilita una connesione con un server.
	 */
	server_connection_established,
	
	/**
	 * Indica che la connessione con il server è stata persa.
	 */
	server_connection_lost,
	
	/**
	 * Messaggio da inviare sulla seriale.
	 */
	message_to_serial,
	
	/**
	 * Messaggio ricevuto sulla socket dal server.
	 */
	message_from_server,
	
	/**
	 * Messaggio ricevuto sulla seriale.
	 */
	message_from_serial,
	
	/**
	 * Terminazione dell'applicazione.
	 */
	exit_action,
	
	/**
	 * Chiusura della connessione sulla seriale.
	 */
	close_serial_connection,
	
	/**
	 * Chiusura della connessione con il server.
	 */
	close_server_connection,
	
	/**
	 * Chiusura della connessione seriale e di quella con ii server.
	 * Vedi {@link EventType.close_serial_connection} {@link EventType.close_server_connection}
	 */
	close_all_connections
}
