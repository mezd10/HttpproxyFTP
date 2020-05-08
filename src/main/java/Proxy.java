import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;


public class Proxy implements Runnable{


	private ServerSocket serverSocket;
	private boolean running = true;
	private ArrayList<Thread> threads;


	public Proxy(int port) {

		new Thread(this).start();
		try {
			File logger = new File("Logger.txt");
			logger.createNewFile();
			threads = new ArrayList<>();

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

		while (running) {
			try {
				Socket socket = serverSocket.accept();

				Thread thread = new Thread(new ProxyRequestHandler(socket));
				threads.add(thread);
				thread.start();
			} catch (SocketException e) {

				System.out.println("Server closed");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void closeServer() {
		try {
			// Close all servicing threads
			for (Thread thread : threads) {
				if (thread.isAlive()) {
					System.out.print("Waiting on " + thread.getId() + " to close..");
					thread.join();
					System.out.println("closed");
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Close Server Socket
		try {
			System.out.println("Terminating Connection");
			serverSocket.close();
		} catch (Exception e) {
			System.out.println("Exception closing proxy's server socket");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		Scanner scanner = new Scanner(System.in);

		String command;

		while (running) {
			command = scanner.nextLine();
			if (command.equals("close")) {
				running = false;
				closeServer();
			}
		}
		scanner.close();
	}
}

