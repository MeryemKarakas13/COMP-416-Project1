package coinNetClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ConnectionToServer
{
    //public static final String DEFAULT_SERVER_ADDRESS = "localhost";
    //public static final int DEFAULT_SERVER_PORT = 4444;
    private Socket s;
    //private BufferedReader br;
    protected BufferedReader is;
    protected PrintWriter os;

    protected String serverAddress;
    protected int serverPort;
    
    private String serverPacket = new String();
    private String clientPacket = new String();

    /**
     *
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
     * @param port port number of the server
     */
    public ConnectionToServer(String address, int port)
    {
        serverAddress = address;
        serverPort    = port;
    }

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     */
    public void Connect()
    {
        try
        {
            s=new Socket(serverAddress, serverPort);
            //br= new BufferedReader(new InputStreamReader(System.in));
            /*
            Read and write buffers on the socket
             */
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new PrintWriter(s.getOutputStream());

            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
            System.out.println("\nYou can write QUIT to terminate the connection to the server\n");
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
    }

    /**
     * sends the message String to the server and retrives the answer
     * @param message input message string to the server
     * @return the received server answer
     */
    public String SendForAnswer(String message)
    {
        String response = new String();
        try
        {
            /*
            Sends the message to the server via PrintWriter
             */
        	if(Character.compare(message.charAt(0),'2')==0) {
        		message = message.substring(1);
        		//If message size is grater than maximum size of TCP packet
        		if(message.length() > 65000) {
	            	int numOfPacket = message.length() / 65000;
	            	boolean hasRemainder = ((message.length() % 65000.0) != 0);
	            	if(hasRemainder) {
	            		numOfPacket++;
	            	}
	            	for(int i=1; i<numOfPacket; i++) {
	            		clientPacket = "12,"+message.substring(0,65000);
	            		os.println(clientPacket);
		            	os.flush();
		            	message = message.substring(65000);
	            	}
	            	clientPacket = "02,"+message;
	            	os.println(clientPacket);
	            	os.flush();
	            	
	            }else {
	            	clientPacket = "02,"+message;
	            	os.println(clientPacket);
	            	os.flush();
	            	
	            }
        	}else {
        		clientPacket = message;
        		os.println(clientPacket);
                os.flush();
        	}
            
            /*
            Reads a line from the server via Buffer Reader
             */
        	try {
        		serverPacket = is.readLine();
	        	response = serverPacket;
	        	//if more than one packet in the queue waiting to be read
	        	while(Character.compare(serverPacket.charAt(0),'1')==0) {
	        		serverPacket = is.readLine();
	        		response = response.concat(serverPacket.substring(3));
	        	}
        	}catch(SocketException e) {
        		System.out.println ( "Time has expired to communicate! " );
        	}
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.out.println("ConnectionToServer. SendForAnswer. Socket read Error");
        }
        return response;
    }


    /**
     * Disconnects the socket and closes the buffers
     */
    public void Disconnect()
    {
        try
        {
            is.close();
            os.close();
            //br.close();
            s.close();
            System.out.println("ConnectionToServer. SendForAnswer. Connection Closed");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
