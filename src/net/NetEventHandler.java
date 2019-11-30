package net;

/**
 * 
 * @author Gabriele Giunchi
 * 
 * Event handler per gli eventi relativi ad una connessione internet.
 * L'event handler si occupa di gestire i messaggi ricevuti e gli errori che si verificano
 
 */
public interface NetEventHandler {
	
	/**
	 * Definisce cosa bisogna fare quando viene ricevuto un messaggio sulla socket.
	 * @param endPoint : oggetto {@link EndPoint} che ha ricevuto il messaggio
	 * @param message : {@link String} che contiene il messaggio ricevuto
	 */
	void handleMessage(EndPoint endPoint, String message);
	
	/**
	 * Definisce cosa bisogna fare quando si verifica un errore.
	 * @param endPoint : oggetto {@link EndPoint} che ha riscontrato l'errore
	 * @param e : {@link Exception} relativa all'errore
	 */
	void handleError(EndPoint endPoint, Exception e);
}
