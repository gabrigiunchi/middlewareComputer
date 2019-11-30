package event;

/**
 * 
 * @author Gabriele Giunchi
 * 
 * Interfaccia di un oggetto che deve occuparsi di gestire un evento
 *
 */
public interface EventHandler {
	
	/**
	 * Gestisce l'evento.
	 * @param e evento da gestire
	 */
	void handleEvent(Event e);
	
	/**
	 * 
	 * @param event : oggetto {@link Event} da analizzare
	 * @return true se l'event-handler è in grado di gestire l'evento, false altrimenti
	 */
	boolean isTriggered(Event event);

}
