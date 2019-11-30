package main;

import java.io.IOException;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

/**
 *
 * @author Gabriele Giunchi
 * 
 * Classe contenente il main dell'applicazione
 *
 */
public final class Main {
	
	private Main() { }
	
	public static void main(final String[] args) throws NoSuchPortException, PortInUseException, IOException, UnsupportedCommOperationException {
		new MainLoop().mainLoop(args);
	}
}
