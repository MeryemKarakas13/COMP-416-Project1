package coinNetClient;


import java.io.InputStreamReader;
import java.util.Scanner;

/**
 *
 * @author AbdulrazakZakieh
 */
public class MultithreadClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    	String host = "localhost";
        int port = 0;
        Scanner stdIn = new Scanner (new InputStreamReader ( System.in ) );
        System.out.println("Specify the port number:");
        try {
            port = Integer.parseInt(stdIn.nextLine());
        }catch(Exception e) {
        	System.out.println("Could not scan the port number!");
        }
        
        System.out.println("Specify the host address:");
        try {
        	host = stdIn.nextLine();
        }catch(Exception e) {
        	System.out.println("Could not scan the host address!");
        }
        ConnectionToServer connectionToServer = new ConnectionToServer(host, port);
        connectionToServer.Connect();
        
        String clientRequest = null;
        //prompt client request type
        System.out.println("You can send API requests to the server.");
        System.out.println("-> Write 1 to list cryptocurrencies data API.");
        System.out.println("-> Write 2 to search for a cryptocurrency price API.");
        String  messageType = stdIn.nextLine();
        
        while (!messageType.equals("QUIT"))
        {
            while(!(messageType.equals("1") || messageType.equals("2") || messageType.equals("QUIT"))) {
            	System.out.println("You entered wrong number for request type.");
        		System.out.println("If you want to list cryptocurrencies data API, write 1.");
                System.out.println("If you want to search for a cryptocurrency price API, write 2.");
                messageType = stdIn.nextLine();	
            	
            }
            if(messageType.equals("QUIT")) {
        		continue;
        	}
                        
            String  message = "";
          //prompt currency ids if client chose price API
            if(messageType.equals("1")) {
            	clientRequest = "0"+messageType;
            }else if(messageType.equals("2")) {
            	clientRequest = "2";
            	System.out.println("How many cryptocurrencies do you want to query the price of ?");
            	message = stdIn.nextLine();
            	int numOfCurr = Integer.parseInt(message);
            	for(int j=1; j<numOfCurr; j++) {
            		System.out.println("Enter the ID of the #"+j+" cryptocurrency:");
            		message = stdIn.nextLine();
            		clientRequest += message+",";
            	}
            	System.out.println("Enter the ID of the #"+numOfCurr+" cryptocurrency:");
        		message = stdIn.nextLine();
        		clientRequest += message;
            	
            }
            
            String serverResp = connectionToServer.SendForAnswer(clientRequest);
            //to check time has expired, then response will be null
            try {
            	//if the response type is cyrptocurrency list
                if((Character.compare(serverResp.charAt(1),'l')==0)) {
                	String[] list = serverResp.split(",");
                	for(int i=1; i<list.length; i++) {
                		System.out.println(list[i]);
                	}
                //if the response type is cyrptocurrency price
                }else if((Character.compare(serverResp.charAt(1),'p')==0)) {
                	System.out.println("\nPrices of the cryptocurrencies queried: ");
                	System.out.println("--------------------------------------- ");
                	String[] priceTable = serverResp.split(",");
                	for(int i=1; i<priceTable.length; i++) {
                		System.out.println(priceTable[i]);
                	}
                }else {
                	System.out.println("The message type of the server answer is invalid!");
                }
                
                //ask for new query
                System.out.println("\nYou can send API requests to the server.");
                System.out.println("-> Write 1 to list cryptocurrencies data API.");
                System.out.println("-> Write 2 to search for a cryptocurrency price API.");
                messageType = stdIn.nextLine();
                
            }catch(StringIndexOutOfBoundsException e){
            	messageType = "QUIT";
            }
            
        }
        connectionToServer.Disconnect();
        stdIn.close();
    }
    
}
