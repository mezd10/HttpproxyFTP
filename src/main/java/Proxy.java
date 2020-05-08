import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;




public class Proxy {


	private ServerSocket serverSocket;


	public Proxy(int port) {

		try {
			File logger = new File("Logger.txt");
			logger.createNewFile();

			serverSocket = new ServerSocket(port);


			System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "..");

		} catch (SocketException se) {
			System.out.println("Socket Exception when connecting to client");
			se.printStackTrace();
		} catch (SocketTimeoutException ste) {
			System.out.println("Timeout occured while connecting to client");
		} catch (IOException e) {
			System.out.println("Error loading previously cached sites file");
			e.printStackTrace();

		}
	}


	public void listen() {


		try {
			Socket socket = serverSocket.accept();

			new ProxyRequestHandler(socket).run();

		} catch (SocketException e) {

			System.out.println("Server closed");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

