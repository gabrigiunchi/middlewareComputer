package net;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import event.Event;
import event.EventDispatcher;
import event.EventType;

/**
 * 
 * @author Gabriele Giunchi
 * 
 * Classe singleton usata per delegare il compito di connettersi ad un server.
 * Attraverso il metodo connect(String ip, int port) viene eseguito
 * periodicamente un tentativo di connessione su un thread separato
 * L'utilità di questa classe è il fatto che da diverse parti del codice
 * è possibile interrompere il tentativo di connessione ed instaurarne di nuovi
 * senza avere thread in conflitto tra di loro.
 */
public final class ServerConnectionService {
	
	private static ServerConnectionService singleton;
	private ConnectionThread connectionThread;
	
	private ServerConnectionService() { }
	
	/**
	 * 
	 * @return unica istanza di {@link ServerConnectionService}
	 */
	public static synchronized ServerConnectionService getInstance() {
		if (singleton == null) {
			singleton = new ServerConnectionService();
		}
		
		return singleton;
	}
	
	/**
	 * Questo metodo fa partire un thread separato che tenta di connetersi
	 * all'indirizzo ip dato finchè non viene stabilita una connessione o 
	 * non viene interrotto invocando il metodo stopRunning().
	 * Quando la connesione viene stabilita viene creato un evento {@link EventType.server_connection_established}
	 * @param ip : indirizzo ip al quale ci si vuole connettere
	 * @param port : porta del server
	 */
	public void connect(final String ip, final int port) {
		if (this.connectionThread != null) {
			this.connectionThread.stopRunning();
		}
		
		this.connectionThread = new ConnectionThread(ip, port);
		this.connectionThread.start();
	}
	
	/**
	 * Interrompe il tentativo di connessione precedentemente creato attraverso
	 * il metodo connect(String ip, int port).
	 */
	public void stopRunning() {
		this.connectionThread.stopRunning();
	}

	/**
	 * 
	 * Thread che legge messaggi sulla socket.
	 *
	 */
	private static class ConnectionThread extends Thread {
		
		private static final int SLEEP_TIME = 5000;
		
		private volatile boolean stop;
		private final String ip;
		private final int port;
		
		ConnectionThread(final String ip, final int port) {
			this.ip = ip;
			this.port = port;
			this.stop = false;
		}
		
		@Override
		public void run() {
			while (!stop) {
				try {
					System.out.println("Trying to connect to " + ip + " on port " + port);
					final Socket socket = new Socket(ip, port);
					if (!stop) {
						System.out.println("Connected to " + socket.getRemoteSocketAddress().toString());
						EventDispatcher.getDispatcher()
							.dispatchEvent(new Event(EventType.server_connection_established, socket));
					}
					
					stop = true;
				} catch (UnknownHostException e) { 
					System.err.println(e.getMessage());
					this.stopRunning();
				} catch (IOException e) { }
					
				if (!stop) {
					try {
						Thread.sleep(SLEEP_TIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}	
				}	
			}
		}
		
		public void stopRunning() {
			if (!stop) {
				System.out.println("Attempt to connect to " + ip + " canceled");
				this.stop = true;
			}
			
		}
	}
}
