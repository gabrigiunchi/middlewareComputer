package main;

/**
 * 
 * @author Gabriele Giunchi
 * 
 * Lista di comandi utilizzabili dall'utente.
 *
 */
public final class ConsoleCommands {
	
	/**
	 * Inizializza sia la comunicazione serialec con Arduino 
	 * sia la connessione con il server.
	 */
	public static final String init = "init";
	
	/**
	 * Chiude sia la connessione con Arduino sia quella con il server.
	 */
	public static final String close_all = "close_all";
	
	/**
	 * Termina l'applicazione e chiude tutte le connessioni.
	 */
	public static final String exit = "exit";
	
	/**
	 * Stampa la lista dei comandi disponibili.
	 */
	public static final String help = "help";
	
	/* ******************** IO COMMANDS ***************/
	public static final String io_list = "io_list";
	public static final String io_init = "io_init";
	public static final String io_send = "io_send";
	public static final String io_close = "io_close";
	public static final String io_state = "io_state";
	
	/* ******************** NET COMMANDS ***************/
	public static final String net_init = "net_init";
	public static final String net_close = "net_close";
	public static final String net_state = "net_state";
	
	private ConsoleCommands() { }

}
