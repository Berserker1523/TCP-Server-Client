package Servidor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerThread extends Thread {
	private int id;
	private Socket clientSocket;
	private BufferedReader inFromClient = null;
	private DataOutputStream outToClient = null;
	private boolean hello = false;
	public boolean sendFile = false;
	public boolean end = false;

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
			byte[] buffer = new byte[512];
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
}