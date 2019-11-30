package io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import event.Event;
import event.EventDispatcher;
import event.EventType;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import utilities.Utilities;

/**
 * 
 * @author Gabriele Giunchi
 * 
 * Classe che incapsula al suo interno le operazioni elementari per stabilire una connessione seriale 
 * con un dispostivo Arduino e comunicare con esso. 
 *
 */
public final class ArduinoCommunication {
	
	private static final long WAITING_TIME = 2000;
	
	private final SerialPort serialPort;
	private final InputStream input;
	private final OutputStream output;
	private final String portName;
	private final long started;
	private final ReadThread readThread;
	private boolean closed;
	
	/**
	 * 
	 * @param port nome della porta seriale su cui si vuole instaurare una connessione
	 * @param baud bit rade desiderato
	 * @throws NoSuchPortException se la porta non esiste
	 * @throws PortInUseException se la porta è in uso
	 * @throws IOException se si verifica un errore I/O
	 * @throws UnsupportedCommOperationException se l'operazione non è supportata dalla libreria rxtx
	 */
	public ArduinoCommunication(final String port, final int baud) throws NoSuchPortException, PortInUseException, 
				IOException, UnsupportedCommOperationException {
		
		this.serialPort = Utilities.createSerialPort(port, baud);
		this.input = this.serialPort.getInputStream();
		this.output = this.serialPort.getOutputStream();
		this.portName = port;
		this.readThread = new ReadThread(this.input);
		this.started = System.currentTimeMillis();
		this.readThread.start();
	}
	
	/**
	 * 
	 * @return true se Arduino è pronto a comunicare, false altrimenti
	 */
	public boolean isReady() {
		return (System.currentTimeMillis() - this.started) > WAITING_TIME;
	}
	
	/**
	 * Legge una stringa dalla seriale. 
	 * La funzione termina quando viene letto un carattere terminatore ('\n' o '\r') 
	 * @return stringa letta
	 * @throws IOException se si verifica un errore I/O 
	 */
	public String readString() throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(this.input));
		final String s = reader.readLine();
		reader.close();
		return s;
	}
	
	/**
	 * Scrive una stringa sulla seriale. Alla stringa viene aggiunto il carattere terminatore '\n'
	 * @param s stringa da mandare
	 * @throws IOException se si verifica un errore I/O
	 */
	public void writeString(final String s) throws IOException {
		System.out.println("Send to Arduino: " + s);
		this.output.write((s + '\n').getBytes());
		this.output.flush();
	}
	
	/**
	 * 
	 * @return oggetto {@link InputStream} associato alla porta seriale
	 */
	public InputStream getInputStream() {
		return this.input;
	}
	
	/**
	 * 
	 * @return oggetto {@link OutputStream} associato alla porta seriale
	 */
	public OutputStream getOutputStream() {
		return this.output;
	}
	
	/**
	 * Chiude la connessione con Arduino e rilascia le risorse.
	 */
	public void closeConnection() {
		System.out.println("Closing connection on port " + this.portName);
		this.readThread.stopComputing();
		this.serialPort.close();
		System.out.println("Connection closed");
		this.closed = true;
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
	 * @return oggetto {@link SerialPort} che permette la comunicazione seriale con Arduino
	 */
	public SerialPort getSerialPort() {
		return this.serialPort;
	}
	
	/**
	 * 
	 * Thread che legge messaggi sulla seriale.
	 *
	 */
	private static final class ReadThread extends Thread {
		private static final int SLEEP_TIME = 20;
		
		private final InputStream input;
		private volatile boolean stop;
		
		ReadThread(final InputStream stream) {
			this.input = stream;
		}
		
		@Override
		public void run() {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(this.input));
			while (!stop) {
				try {
					if (reader.ready()) {
						final String s = reader.readLine();
						
						if (s == null) {
							EventDispatcher.getDispatcher().dispatchEvent(new Event(EventType.exit_action, ""));
						} else {
							System.out.println("From Arduino: " + s);
							EventDispatcher.getDispatcher().dispatchEvent(new Event(EventType.message_from_serial, s));
						}
					}
				} catch (IOException e) {
					System.err.println(e.toString());
					stop = true;
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
				System.err.println(e.toString());
			}
		}
		
		public void stopComputing() {
			this.stop = true;
		}
	}
}
