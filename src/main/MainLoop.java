package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Optional;
import event.Event;
import event.MyEventHandler;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import io.ArduinoCommunication;
import net.EndPoint;
import net.ServerConnectionService;
import utilities.Utilities;

import static event.EventDispatcher.getDispatcher;
import static event.EventType.close_all_connections;
import static event.EventType.close_serial_connection;
import static event.EventType.close_server_connection;
import static event.EventType.exit_action;
import static event.EventType.message_to_serial;
import static main.ConsoleCommands.close_all;
import static main.ConsoleCommands.exit;
import static main.ConsoleCommands.help;
import static main.ConsoleCommands.init;
import static main.ConsoleCommands.io_close;
import static main.ConsoleCommands.io_list;
import static main.ConsoleCommands.io_init;
import static main.ConsoleCommands.io_send;
import static main.ConsoleCommands.io_state;
import static main.ConsoleCommands.net_close;
import static main.ConsoleCommands.net_init;
import static main.ConsoleCommands.net_state;

/**
 * 
 * @author Gabriele Giunchi
 * 
 * Main loop dell'applicazione in cui si gestiscono gli input dell'utente sulla console
 *
 */
public final class MainLoop {
	
	private static final String ERROR_PARSING_ARGUMENT_LOG = "Error parsing arguments";
	private static final BufferedReader CONSOLE = new BufferedReader(new InputStreamReader(System.in));
	
	private MyEventHandler eventHandler;
	private volatile boolean stop;
	
	/**
	 * Main Loop dove vengono gestiti gli input dell'utente sulla linea di comando.
	 * Il parametro args può contenere:
	 * 
	 * - Porta su cui inizializzare la comunicazione seriale con Arduino
	 * - Bit rate per la comunicazione seriale
	 * - Indirizzo ip del server
	 * - Porta del server
	 * 
	 * Questi parametri devono essere tutti presenti altrimenti non verrà effettuata l'operazione di inizializzazione
	 * 
	 * @param args : array di {@link String} che può contenere i dati necessari all'inizializzazione dell'applicazione
	 */
	public void mainLoop(final String[] args) {
		eventHandler = new MyEventHandler();
		
		if (args.length >= 4) {
			try {
				final String serialPort = args[0];
				final int baud = Integer.parseInt(args[1]);
				final String serverIp = args[2];
				final int serverPort = Integer.parseInt(args[3]);
				initArduinoConnection(serialPort, baud);
				ServerConnectionService.getInstance().connect(serverIp, serverPort);
			} catch (NumberFormatException e) {
				System.err.println(ERROR_PARSING_ARGUMENT_LOG);
			} catch (IOException | NoSuchPortException | PortInUseException | UnsupportedCommOperationException e) {
				System.err.println(e);
			}
		} 
		
		getDispatcher().addEventHandler(eventHandler);
		new Thread(() -> getDispatcher().mainLoop()).start();
		System.out.println("Ready");
		
		while (!stop) {
			try {
				final String line = CONSOLE.readLine();
				if (line == null) {
					stop = true;
				} else {
					this.handleCommand(line);
				}
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}
	
	private void handleCommand(final String line) {
		final String[] values = line.split(" ");
		final String command = values[0];
		
		switch(command) {
			case init : 
				if (values.length < 5) {
					System.out.println("Arguments missing");
				} else if (isArduinoConnected() || isServerConnected()) {	
					System.out.println("Already initialized");
				} else {
					try {
						final String serialPort = values[1].toUpperCase();
						final int baud = Integer.parseInt(values[2]);
						final String serverIp = values[3].toLowerCase();
						final int serverPort = Integer.parseInt(values[4]);
						initArduinoConnection(serialPort, baud);
						ServerConnectionService.getInstance().connect(serverIp, serverPort);
					} catch (NumberFormatException e) {
						System.err.println(ERROR_PARSING_ARGUMENT_LOG);
					} catch (IOException | NoSuchPortException | PortInUseException | UnsupportedCommOperationException e) {
						System.err.println(e);
					}
				}
				break;
			
			case exit : 
				getDispatcher().dispatchEvent(new Event(exit_action)); 
				stop = true;
				break;
				
			case close_all :
				getDispatcher().dispatchEvent(new Event(close_all_connections));
				break;
				
			case io_list :
				@SuppressWarnings("unchecked")
				Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
				if (!portEnum.hasMoreElements()) {
					System.out.println("No available ports");
				}
		        while (portEnum.hasMoreElements()) {
		            CommPortIdentifier portIdentifier = portEnum.nextElement();
		            System.out.println(portIdentifier.getName() + " - " 
		            		+ Utilities.getPortTypeName(portIdentifier.getPortType()));
		        }
		 
		        break;
			
			case io_init :
				if (values.length < 3) {
					System.out.println("Arguments missing");
				} else if (isArduinoConnected()) {
					System.out.println("Connection with arduino already established");
				} else {
					final String port = values[1].toUpperCase();
					try {
						final int baud = Integer.parseInt(values[2]);
						initArduinoConnection(port, baud);
					} catch (NumberFormatException e) {
						System.out.println(ERROR_PARSING_ARGUMENT_LOG);
					}  catch (IOException | NoSuchPortException | PortInUseException | UnsupportedCommOperationException e) {
						System.err.println(e);
					}
				}
				
				break;
			
			case io_send : 
				if (values.length < 2) {
					System.err.println("Argument <message> missing");
				} else if (!isArduinoConnected()) {
					System.err.println("No serial connection");
				} else if (!eventHandler.getArduinoCommunication().get().isReady()) {
					System.out.println("Arduino isn't ready yet");
				} else {
					getDispatcher().dispatchEvent(new Event(message_to_serial, values[1])); 
				}
				break;
				
			case io_close : 
				getDispatcher().dispatchEvent(new Event(close_serial_connection)); 
				break;
				
			case io_state :
				if (isArduinoConnected()) {
					final ArduinoCommunication arduino = eventHandler.getArduinoCommunication().get();
					System.out.println("Connected to serial port " + arduino.getSerialPort().getName());
				} else {
					System.out.println("There isn't any serial connection");
				}
				break;
			
			case net_init : 
				if (values.length < 3) {
					System.err.println("Arguments missing");
				} else if (isServerConnected()) {
					System.err.println("Connection with server already established");
				} else {
					final String ip = values[1].toLowerCase();
					try {
						final int port = Integer.parseInt(values[2]);
						ServerConnectionService.getInstance().connect(ip, port);
					} catch (NumberFormatException e) {
						System.err.println(ERROR_PARSING_ARGUMENT_LOG);
					}
				}
				break;
			
			
			case net_close : 
				getDispatcher().dispatchEvent(new Event(close_server_connection)); 
				break;
			
			case net_state : 
				if (isServerConnected()) {
					final Socket socket = eventHandler.getServerConnection().get().getSocket();
					System.out.println("Connected to " + socket.getRemoteSocketAddress().toString());
				} else {
					System.out.println("There isn't any connection to the server");
				}
				break;
			
			case help : 
				showCommandsList(); 
				break;
			
			default : 
				System.out.println("Command not found"); 
				break;
		}
	}
	
	private void showCommandsList() {
		System.out.println("\n\nAvailable commands: \n");
		System.out.println(init + " <serialPort> <baud> <serverIp> <serverPort> - Initialize both Arduino connection and server connection");
		System.out.println(close_all + " - Close all the connection established");
		System.out.println(exit + " - Close all the connection and terminate the application");
		System.out.println(help + " - Show the available commands");
		System.out.println(io_list + " - Show the available serial ports");
		System.out.println(io_init + " <port> <baud> - Initialize a connection with Arduino on the given port with the given bit rate");
		System.out.println(io_send + " <message> - Send a message to Arduino");
		System.out.println(io_state + " - Show the state of the connection with Arduino");
		System.out.println(io_close + " - Close the connection with Arduino");
		System.out.println(net_init + " <ip> <port> - Connect to a server ");
		System.out.println(net_state + " - Show the state of the connection with the server");
		System.out.println(net_close + " - Close the connection with the server");
		System.out.println();
	}
	
	private boolean isArduinoConnected() {
		final Optional<ArduinoCommunication> arduinoConnection = eventHandler.getArduinoCommunication();
		return arduinoConnection.isPresent() && !arduinoConnection.get().isClosed();
	}
	
	private boolean isServerConnected() {
		final Optional<EndPoint> serverConnection = eventHandler.getServerConnection();
		return serverConnection.isPresent() && !serverConnection.get().isClosed();
	}
	
	private void initArduinoConnection(final String serialPort, final int baud) throws NoSuchPortException, 
			PortInUseException, IOException, UnsupportedCommOperationException {
		
		eventHandler.setArduino(new ArduinoCommunication(serialPort, baud));
		
		new Thread(() -> {
			// Controllo periodicamente se arduino è pronto per comunicare
			while (eventHandler.getArduinoCommunication().isPresent()
					&& !eventHandler.getArduinoCommunication().get().isReady()) {
				
				try {
					Thread.sleep(200);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			System.out.println("Arduino ready to communicate");
		}).start();
	}
}
