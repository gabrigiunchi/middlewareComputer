package event;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 
 * @author Gabriele Giunchi
 * 
 * Event dispatcher dell'applicazione che si occupa di memorizzare gli eventi e gli event handler.
 * Adotta il pattern singleton
 * 
 */
public final class EventDispatcher {
	
	private static final int SLEEP_TIME = 20;
	private static final EventDispatcher SINGLETON = new EventDispatcher();
	
	private final List<EventHandler> handlers;
	private final Queue<Event> events;
	private volatile boolean stop;
	
	private EventDispatcher() {
		this.events = new ConcurrentLinkedQueue<>();
		this.handlers = new ArrayList<>();
		this.stop = false;
	}
	
	/**
	 * 
	 * @return unica istanza di EventDispatcher
	 */
	public static EventDispatcher getDispatcher() {
		return SINGLETON;
	}
	
	/**
	 * Aggiunge un evento alla coda degli eventi da gestire.
	 * @param newEvent : oggetto {@link Event} da aggiungere
	 */
	public void dispatchEvent(final Event newEvent) {
		this.events.add(newEvent);
	}
	
	/**
	 * Aggiunge un event handler che verrà usato per la gestione degli eventi.
	 * @param handler : oggetto {@link EventHandler} da aggiungere
	 */
	public void addEventHandler(final EventHandler handler) {
		this.handlers.add(handler);
	}
	
	/**
	 * Termina l'esecuzione dell'event dispatcher.
	 */
	public void terminate() {
		this.stop = true;
	}
	
	/**
	 * Esegue il loop di event dispatcher.
	 */
	public void mainLoop() {
		System.out.println("Event Dispatcher started");
		while (!stop) {
			if (!this.events.isEmpty()) {
				final Event e = this.events.poll();
				this.handlers.stream().filter(h -> h.isTriggered(e)).forEach(h1 -> h1.handleEvent(e));
			}
			
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {
				System.err.println(e1);
			}
		}
		
		System.out.println("Event Dispatcher terminated");
	}
}
