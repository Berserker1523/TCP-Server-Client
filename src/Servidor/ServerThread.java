package Servidor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.MessageDigest;

public class ServerThread extends Thread {
	private int id;
	private Socket clientSocket;
	private BufferedReader inFromClient = null;
	private DataOutputStream outToClient = null;
	private boolean hello = false;
	public boolean sendFile = false;
	public boolean end = false;
	private long startFileTransferTime = 0;
	private long endFileTransferTime = 0;

	public ServerThread(int i) {
		this.id = i;
	}

	public void receiveClientSocker(Socket clientSocket) {
		this.clientSocket = clientSocket;
		try {
			inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outToClient = new DataOutputStream(clientSocket.getOutputStream());			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isNotRunning () {
		return clientSocket == null || clientSocket.isClosed();
	}

	public void run() {
		try {	
			log("Connection");
			Server.fileOut.println("Connection to client: " + id);
			String clientSentence = "";
			while(true){
				if(!hello){
					clientSentence = inFromClient.readLine();
					if (clientSentence == null) {
						break;
					}
					else {
						String[] splitBySpace = clientSentence.split(" ");
						String command = splitBySpace[0];
						String param = "";
						if (splitBySpace.length != 1){ 
							param = splitBySpace[1];
						}

						log("IN: " + command + " " + param);

						if(command.equals("H")){
							outToClient.writeBytes("H\n");
							hello=true;
						}
					}
				}
				else if(sendFile == true){
					writeFile2Client(Server.fileToSend);
					
					clientSentence = inFromClient.readLine();
					if (clientSentence == null) {
						break;
					}
					else {
						endFileTransferTime = System.currentTimeMillis();
						String[] splitBySpace = clientSentence.split(" ");
						String command = splitBySpace[0];
						String param = "";
						if (splitBySpace.length != 1){ 
							param = splitBySpace[1];
						}

						log("IN: " + command + " " + param);

						if(command.equals("R")){
							long transferTime = (endFileTransferTime - startFileTransferTime);
							log("The client has received the file");
							log("Transfer time: "  + transferTime + "ms");
							Server.fileOut.println("File sent succesfully to client: " + id);
							Server.fileOut.println("Transfer time of " + id + ": " + transferTime + "ms");
						}
					}
					
					File sent = new File(Server.fileToSend.getAbsolutePath());
					//Use MD5 algorithm
					MessageDigest md5Digest = MessageDigest.getInstance("MD5");
					
					//Get the checksum
					String checksum = getFileChecksum(md5Digest, sent);
					log("Server hash: " + checksum);
					
					outToClient.writeBytes(checksum + "\n");
					
					clientSentence = inFromClient.readLine();
					if (clientSentence == null) {
						break;
					}
					else {
						String[] splitBySpace = clientSentence.split(" ");
						String command = splitBySpace[0];
						String param = "";
						if (splitBySpace.length != 1){ 
							param = splitBySpace[1];
						}

						log("IN: " + command + " " + param);

						if(command.equals("E")){
							log("Hash is equal to the received file");
							Server.fileOut.println("File integrity maintained: " + id);
							
						}
						else if(command.equals("W")){
							log("Hash is NOT equal to the received file");
							Server.fileOut.println("File integrity was not maintained: " + id);
						}
					}
					
					clientSentence = inFromClient.readLine();
					if (clientSentence == null) {
						break;
					}
					else {
						String[] splitBySpace = clientSentence.split(" ");
						String command = splitBySpace[0];
						String param = "";
						if (splitBySpace.length != 1){ 
							param = splitBySpace[1];
						}

						log("IN: " + command + " " + param);

						if(command.equals("X")){
							log("Ending File Transfer");
							Server.addSuccesfullFilesSent();
							break;
						}
					}
				}

			}//end_while

			clientSocket.close();
			inFromClient.close();
			outToClient.close();

		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public void writeFile2Client(File file2send){
		log("Sending file to client");
		try {
			String[] fileName = file2send.getName().split("\\.");
			String sendingFileName = fileName[0] + id + "." + fileName[1];
			outToClient.writeBytes("F " + sendingFileName + " " + file2send.length()  + "\n");
			FileInputStream fis = new FileInputStream(file2send);

			int count;
			byte[] buffer = new byte[65535];
			startFileTransferTime = System.currentTimeMillis();
			while ((count=fis.read(buffer)) > 0) {
				outToClient.write(buffer, 0, count);
			}
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void log(String wat) {
		System.out.println(id + ": " + wat);
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