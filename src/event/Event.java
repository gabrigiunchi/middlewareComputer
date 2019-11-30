package event;

import java.util.Optional;

/**
 * @author Gabriele Giunchi
 * 
 * Evento dotato di un tipo (vedi {@link EventType}) che è gestito da un event handler
 * Fa parte dell'architettura event-loop
 */
public class Event {
	
	private final EventType type;
	private final Object data;
	
	/**
	 * Costruisce un istanza di {@link Event} con il tipo dato.
	 * @param type : oggetto {@link EventType} che rappresenta il tipo di evento
	 */
	public Event(final EventType type) {
		this(type, null);
	}
	
	/**
	 * Costruisce un istanza di {@link Event} con il tipo dato e il payload.
	 * @param type : oggetto {@link EventType} che rappresenta il tipo di evento
	 * @param data : dati aggiuntivi associati all'evento
	 */
	public Event(final EventType type, final Object data) {
		this.type = type;
		this.data = data;
	}
	
	/**
	 * 
	 * @return tipo dell'evento
	 */
	public final EventType getType() {
		return this.type;
	}
	
	/**
	 * 
	 * @return dati opzionali relativi all'evento
	 */
	public final Optional<Object> getData() {
		return Optional.ofNullable(this.data);
	}

}
