package Servidor;
import java.io.*;
import java.net.*;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

public class Server {

	public final static int SERVER_PORT = 8000;
	private static ServerThread[] threads;
	private final static int MAX_CONNECTIONS = 25;
	public static int numberConnections = 0;
	public final static String FILE_DIR = "./files/";

	public static File fileToSend = null;
	public static int simultaneousClients = 25;
	public static Integer succesfullFilesSent = 0;

	public static void main(String argv[]) throws Exception {
		@SuppressWarnings("resource")
		ServerSocket socket = new ServerSocket(SERVER_PORT);
		serverProtocol(socket);
		while(true){
			if(succesfullFilesSent == simultaneousClients){
				succesfullFilesSent = 0;
				System.out.println();
				serverProtocol(socket);
			}
		}//end_updateCycle
	}//end_main

	public static void addSuccesfullFilesSent(){
		synchronized(succesfullFilesSent){
			succesfullFilesSent++;
		}
	}

	public static void serverProtocol(ServerSocket socket) throws Exception{
		BufferedReader serverIn = new BufferedReader(new InputStreamReader(System.in));

		/*
		 * Choose file to send
		 */
		File dir = new File(Server.FILE_DIR);
		File[] fileNames = dir.listFiles();
		System.out.println("Choose a file to send to users: ");
		int fileCounter = 1;
		for (File fileName : fileNames) {
			System.out.println(fileCounter + "." + fileName.getName() + " " + fileName.length());
			fileCounter++;
		}
		while(true){
			System.out.println("Range: 1 - 2");
			int chosenFile = Integer.parseInt(serverIn.readLine());
			if(chosenFile==1 || chosenFile ==2){
				fileToSend = fileNames[chosenFile -1];
				System.out.println("FileChosen: " + fileToSend.getName() + "\n");
				break;
			}
			else{
				System.out.println("Please choice a valid number of file");
			}

		}


		/**
		 * Choose how many clients the file should be sent simultaneously
		 */
		while(true){
			System.out.println("Write how many clients the file should be sent simultaneously: ");
			System.out.println("Min: 1 - Max: " + MAX_CONNECTIONS);
			int choice = Integer.parseInt(serverIn.readLine());
			if(choice <= MAX_CONNECTIONS && choice >= 1){
				simultaneousClients = choice;
				System.out.println("simultaneousClients: " + simultaneousClients + "\n");
				break;
			}
			else{
				System.out.println("Please choice a valid number of clients");
			}
		}


		/*
		 * Server threads initialization
		 */
		System.out.println("Server threads initialization");
		threads = new ServerThread[simultaneousClients];

		for (int i = 0; i < threads.length; i++) {
			threads[i] = new ServerThread(i);
		}

		/*
		 * Connection attendance
		 */
		while (numberConnections<simultaneousClients) {
			numberConnections = 0;
			Socket connectionSocket = socket.accept();
			ServerThread freeThread = null;

			/*
			 * Look for available threads
			 */
			for (int i = 0; i < threads.length; i++) {
				if (threads[i]!= null && !threads[i].isNotRunning()) {
					numberConnections++;
				}
				else{
					threads[i] = null;
					if(freeThread==null){
						threads[i] = new ServerThread(i);
						threads[i].receiveClientSocker(connectionSocket);
						freeThread = threads[i];
						numberConnections++;
					}
				}
			}//end_for

			System.out.println("Number of actual connections: " + numberConnections);

			freeThread.start();

		}//end_while

		for (int i = 0; i < threads.length; i++) {
			threads[i].sendFile = true;
		}
	}
}
