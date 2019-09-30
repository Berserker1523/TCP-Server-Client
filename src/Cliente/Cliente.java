package Cliente;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import Servidor.Server;

public class Cliente {

	private final static String SERVER_ADDRESS = "54.87.204.180";
	private static DataOutputStream outToServer = null;
	private static BufferedReader inFromServer = null;

	public static void main(String argv[]) throws Exception {
		String fromServer;
		boolean termino =false;

		Socket clientSocket = new Socket(SERVER_ADDRESS, Server.SERVER_PORT);

		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		outToServer.writeBytes("H" + '\n');
		System.out.println("TO SERVER Hello");

		fromServer = inFromServer.readLine();
		System.out.println("FROM SERVER: " + fromServer);


		while(!termino){

			fromServer = inFromServer.readLine();

			String[] split = fromServer.split(" ");
			String serverCommand = split[0];
			String param1 = " ";
			String param2 = " ";
			
			if(split.length > 1){
				param1=split[1];
				if(split.length > 2){
					param2=split[2];
				}
			}
			System.out.println("Server command: " + serverCommand);
			System.out.println("Params: " + param1 + " 2 " +param2);


			switch(serverCommand) {
			case "F":
				saveFile(clientSocket, param1, param2);
				termino = true;
				break;
			}
		}
		clientSocket.close();
	}
	
	
	private static void saveFile(Socket clientSock, String nameFile, String size) throws IOException {
		
		DataInputStream dis = new DataInputStream(clientSock.getInputStream());
		FileOutputStream fos = new FileOutputStream( new File("./downloads/"+ nameFile));
		System.out.println("START SAVE FILE");

		byte[] buffer = new byte[512];

		int filesize = Integer.parseInt(size); // Send file size in separate msg
		int read = 0;
		int totalRead = 0;
		int remaining = filesize;
		while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
			totalRead += read;
			remaining -= read;
			System.out.println("read " + totalRead + " bytes.");
			fos.write(buffer, 0, read);
		}
		
		
		System.out.println("DONE File Transfer ");
		
		outToServer.writeBytes("X" + '\n');
		System.out.println("TO SERVER End");

		fos.close();
		dis.close();
	}
}