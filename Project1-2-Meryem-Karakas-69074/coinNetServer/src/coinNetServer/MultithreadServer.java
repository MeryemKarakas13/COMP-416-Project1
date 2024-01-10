package coinNetServer;

import java.io.InputStreamReader;
import java.util.Scanner;

public class MultithreadServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    	int port = 0;
        Scanner stdIn = new Scanner (new InputStreamReader ( System.in ) );
        try {
        	System.out.println("Specify the port number:");
            port = Integer.parseInt(stdIn.nextLine());
        }catch(Exception e) {
        	System.out.println("Could not scan the port number!");
        }
        stdIn.close();
        Server server = new Server(port);
        System.out.println("Waiting for client...");
    }
    
}
