package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * 
 * @author Gabriele Giunchi
 * 
 * Classe con cui è possibile inviare/ricevere stringhe di testo su una socket
 * e settare un {@link NetEventHandler} per gestire i messaggi ricevuti e gli errori
 * 
 * Adotta il pattern decorator sull'oggetto Socket e il pattern strategy per
 * definire il comportamento relativo alla gestione degli eventi.
 *
 */
public final class EndPoint {
	
	private final Socket socket;
	private final ReadThread readThread;
	private NetEventHandler handler;
	private boolean closed;
	
	/**
	 * @param socket : oggetto {@link Socket} per la comunicazione
	 * @param handler : oggetto {@link NetEventHandler} per la gestione degli eventi
	 * @throws IOException se si verifica un errore nel recuperare l'oggetto {@link InputStream} 
	 * della socket
	 */
	public EndPoint(final Socket socket, final NetEventHandler handler) throws IOException {
		this.socket = socket;
		this.handler = handler;
		this.readThread = new ReadThread(this.socket.getInputStream());
		this.readThread.start();
	}
	
	/**
	 * Invia un messaggio sulla socket. 
	 * Al messaggio viene aggiunto un carattere '\n'.
	 * @param message : messaggio da inviare
	 * @throws IOException se si verifica un errore I/O
	 */
	public void sendMessage(final String message) throws IOException {
		this.socket.getOutputStream().write((message + '\n').getBytes());
		this.socket.getOutputStream().flush();
	}
	
	/**
	 * Chiude la connessione e rilascia le risorse.
	 */
	public void closeConnection() {
		this.closed = true;
		this.readThread.stopComputing();
		try {
			this.socket.close();
		} catch (IOException e) {
			System.err.println("EndPoint: " + e.getMessage());
		}
		
		System.out.println("Socket closed");
	}
	
	/**
	 * 
	 * @return true se la connessione è chiusa
	 */
	public boolean isClosed() {
		return this.closed;
	}
	
	/**
	 * 
	 * @return {@link Socket}
	 */
	public Socket getSocket() {
		return this.socket;
	}
	
	/**
	 * Setta l'handler degli eventi associati alla socket.
	 * @param handler : oggetto {@link NetEventHandler} per la gestione degli eventi
	 */
	public void setHandler(final NetEventHandler handler) {
		this.handler = handler;
	}
	
	/**
	 * 
	 * Thread che legge messaggi sulla socket.
	 *
	 */
	private final class ReadThread extends Thread {
		private static final int SLEEP_TIME = 20;
		
		private final InputStream input;
		private volatile boolean stop;
		
		ReadThread(final InputStream input) {
			this.input = input;
		}
		
		@Override
		public void run() {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(this.input));
			while (!stop) {
				try {
					final String s = reader.readLine();
					handler.handleMessage(EndPoint.this, s);
				} catch (IOException e) {
					System.err.println("EndPoint.ReadThread : " + e.getMessage());
					if (!stop) {
						handler.handleError(EndPoint.this, e);
					}
				}
				
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			try {
				reader.close();
			} catch (IOException e) {
				System.err.println("EndPoint.ReadThread : " + e.getMessage());
			}
		}
		
		public void stopComputing() {
			this.stop = true;
		}
	}
}
