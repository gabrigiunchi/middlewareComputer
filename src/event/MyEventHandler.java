package event;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Optional;
import io.ArduinoCommunication;
import net.EndPoint;
import net.MyNetEventHandler;
import net.ServerConnectionService;

/**
 * 
 * @author Gabriele Giunchi
 * 
 * Event handler specifico dell'applicazione. 
 * Si occupa di fare da ponte tra il server ed Arduino
 *
 */
public final class MyEventHandler implements EventHandler {
	
	private EndPoint server;
	private ArduinoCommunication arduino;
	
	/**
	 * Setta l'oggetto {@link ArduinoCommunication} con cui è possibile comunicare con Arduino.
	 * @param arduino : istanza di {@link ArduinoCommunication}
	 */
	public void setArduino(final ArduinoCommunication arduino) {
		this.arduino = arduino;
	}
	
	/**
	 * Setta l'oggetto {@link EndPoint} con cui è possibile comunicare con il server.
	 * @param server : istanza di {@link EndPoint}
	 */
	public void setServer(final EndPoint server) {
		this.server = server;
	}
	
	/**
	 * 
	 * @return istanza di {@link ArduinoCommunication} usata per la comunicazione con Arduino
	 */
	public Optional<ArduinoCommunication> getArduinoCommunication() {
		return Optional.ofNullable(this.arduino);
	}
	
	/**
	 * 
	 * @return istanza di {@link Endpoint} usata per la comunicazione con il server
	 */
	public Optional<EndPoint> getServerConnection() {
		return Optional.ofNullable(this.server);
	}

	@Override
	public void handleEvent(final Event event) {
		final EventType type = event.getType();
		
		switch(type) {
			case server_connection_established :
				if (this.server != null && !this.server.isClosed()) {
					this.server.closeConnection();
				}
				
				final Socket socket = (Socket) event.getData().get();
				try {
					this.setServer(new EndPoint(socket, new MyNetEventHandler()));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			
			case server_connection_lost:
				this.server.closeConnection();
				final String ip = this.server.getSocket().getInetAddress().getHostAddress();
				final int port = this.server.getSocket().getPort();
				ServerConnectionService.getInstance().connect(ip, port);
				break;
				
			case message_from_serial : 
				if (this.server != null) {
					try {
						this.server.sendMessage((String) event.getData().get());
					} catch (IOException e) {
						System.err.println("MyEventHandler : " + e.getMessage());
					}
				}
				break;
				
			case message_from_server: 
				if (this.arduino != null && this.arduino.isReady()) {
					try {
						this.arduino.writeString((String) event.getData().get());
					} catch (IOException e) {
						System.err.println("MyEventHandler : " + e.getMessage());
					}
				}
				break;
				
			case exit_action :
				this.arduino.closeConnection();
				this.server.closeConnection();
				EventDispatcher.getDispatcher().terminate();
				break;
				
			case close_serial_connection : this.arduino.closeConnection(); break;
			
			case close_server_connection: 
				this.server.closeConnection(); 
				ServerConnectionService.getInstance().stopRunning();
				break;
			
			case close_all_connections : 
				this.arduino.closeConnection();
				this.server.closeConnection(); 
				break;
		
			case message_to_serial:
				if (this.arduino != null) {
					try {
						this.arduino.writeString((String) event.getData().get());
					} catch (IOException e) {
						EventDispatcher.getDispatcher().dispatchEvent(new Event(EventType.close_all_connections));
					}
				}
				break;		
				
			default: break;
		}
	}

	@Override
	public boolean isTriggered(final Event e) {
		return Arrays.asList(EventType.values()).contains(e.getType());
	}
}
