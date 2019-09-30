package Cliente;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import Servidor.Server;

public class Cliente {

	private final static String SERVER_ADDRESS = "localhost";
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
	
	
	private static void saveFile(Socket clientSock, String nameFile, String size) throws IOException, NoSuchAlgorithmException {
		
		DataInputStream dis = new DataInputStream(clientSock.getInputStream());
		FileOutputStream fos = new FileOutputStream( new File("./downloads/"+ nameFile));
		System.out.println("START SAVE FILE");

		byte[] buffer = new byte[65535];

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
		outToServer.writeBytes("R" + '\n');
		System.out.println("TO SERVER Received");
		
		String serverHash = inFromServer.readLine();
		System.out.println("Server hash: " + serverHash);
		
		File receipt = new File("./downloads/"+ nameFile);
		//Use MD5 algorithm
		MessageDigest md5Digest = MessageDigest.getInstance("MD5");
		
		//Get the checksum
		String checksum = getFileChecksum(md5Digest, receipt);
		System.out.println("Client hash: " + checksum);
		
		if(checksum.equals(serverHash)){
			System.out.println("The server and client hashes are equal");
			outToServer.writeBytes("E" + '\n');
			System.out.println("TO SERVER hashEqual");
		}
		else{
			if(checksum.equals(serverHash)){
				System.out.println("The server and client hashes are equal");
				outToServer.writeBytes("W" + '\n');
				System.out.println("TO SERVER hashWrong");
			}
		}
		
		outToServer.writeBytes("X" + '\n');
		System.out.println("TO SERVER End");

		fos.close();
		dis.close();
	}
	
	private static String getFileChecksum(MessageDigest digest, File file) throws IOException
	{
	    //Get file input stream for reading the file content
	    FileInputStream fis = new FileInputStream(file);
	     
	    //Create byte array to read data in chunks
	    byte[] byteArray = new byte[1024];
	    int bytesCount = 0;
	      
	    //Read file data and update in message digest
	    while ((bytesCount = fis.read(byteArray)) != -1) {
	        digest.update(byteArray, 0, bytesCount);
	    };
	     
	    //close the stream; We don't need it now.
	    fis.close();
	     
	    //Get the hash's bytes
	    byte[] bytes = digest.digest();
	     
	    //This bytes[] has bytes in decimal format;
	    //Convert it to hexadecimal format
	    StringBuilder sb = new StringBuilder();
	    for(int i=0; i< bytes.length ;i++)
	    {
	        sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
	    }
	     
	    //return complete hash
	   return sb.toString();
	}
}