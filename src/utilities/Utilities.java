package utilities;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * @author Gabriele Giunchi
 * 
 * Classe con metodi statici di utilità
 *
 */
public final class Utilities {
	
	private Utilities() { }
	
	/**
	 * Restituisce il tipo di porta associato ad un codice di tipo numerico.
	 * @param portType codice numerico della porta
	 * @return stringa contenente il tipo di porta
	 */
	public static String getPortTypeName(final int portType) {
        switch (portType) {
            case CommPortIdentifier.PORT_I2C: return "I2C";
            case CommPortIdentifier.PORT_PARALLEL: return "Parallel";
            case CommPortIdentifier.PORT_RAW: return "Raw";
            case CommPortIdentifier.PORT_RS485: return "RS485";
            case CommPortIdentifier.PORT_SERIAL: return "Serial";
            default: return "unknown type";
        }
    }
	
	/**
	 * Crea un oggetto di tipo {@link SerialPort} dato il nome della porta seriale e il bit rate voluto.
	 * 
	 * @param portName nome della porta seriale
	 * @param baud bit rate desiderato
	 * @return oggetto {@link SerialPort}
	 * @throws NoSuchPortException se non esiste una porta con il nome dato
	 * @throws PortInUseException se la porta è già in uso
	 * @throws UnsupportedCommOperationException se l'operazione non è supportata dalla libreria rxtx
	 */
	public static SerialPort createSerialPort(final String portName, final int baud) throws NoSuchPortException, 
				PortInUseException, UnsupportedCommOperationException {
		
		final CommPortIdentifier port = CommPortIdentifier.getPortIdentifier(portName);
		
        if (port.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
            throw new IllegalStateException("Port is currently in use");
        }
        
        System.out.println("Opening port " + port.getName());
        final CommPort commPort = port.open("Utilities", 2000);
        System.out.println("Creating serial port");
        final SerialPort serialPort = (SerialPort) commPort;
        serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        System.out.println("Communication set on port " + port.getName());
        
        return serialPort;
	}
}
