package coinNetServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONArray;
import org.json.JSONObject;
import sun.net.www.http.HttpClient;

class ServerThread extends Thread
{
    protected BufferedReader is;
    protected PrintWriter os;
    protected Socket s;
    private String line = new String();
    private String lines = new String();
    private String serverPacket = new String();
    private String clientPacket = new String();

    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public ServerThread(Socket s)
    {
        this.s = s;
    }

    /**
     * The server thread, listens client until it receives the QUIT string from the client
     */
    public void run()
    {
    	String baseURL = "https://api.coingecko.com/api/v3/";
        try
        {
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new PrintWriter(s.getOutputStream());
        }
        catch (IOException e)
        {
            System.err.println("Server Thread. Run. IO error in server thread");
        }
        

        try
        {
        	clientPacket = is.readLine();
        	line = clientPacket;
        	//keep reading until there is no remaining packet
        	while(Character.compare(clientPacket.charAt(0),'1')==0) {
        		clientPacket = is.readLine();
        		line = line.concat(clientPacket.substring(3));
        	}
            
            while (line.compareTo("QUIT") != 0)
            {
            	if(Character.compare(line.charAt(1),'1')==0) {
					//os.println("You query list");
					String listURL = baseURL.concat("coins/list");
					URL url = new URL(listURL);
		            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		            conn.setRequestMethod("GET");
		            conn.connect();
		            String inline = "";
		            Scanner scanner = new Scanner(url.openStream());
		            while (scanner.hasNext()) {
		                inline += scanner.nextLine();
		            }
		            //Close the scanner
		            scanner.close();  
		            // the number of bytes to represent the string
		            //The maximum size of a TCP packet is 64K (65535 bytes)
		            //In many cases, String.length() will return the same value as String.getBytes().length
		            //but in some cases it's not the same
		            /*In Java, String.length() is to return the number of characters in the string, 
		            while String.getBytes().length is to return the number of bytes to represent the 
		            string with the specified encoding.*/
		            
		            
		            JSONArray jsonObject = new JSONArray(inline);
		            String cList = "";
		            for (int i = 0; i < jsonObject.length()-1; i++)
		            {
		                String id = jsonObject.getJSONObject(i).getString("id");
		                String name = jsonObject.getJSONObject(i).getString("name");
		                cList += id + "\t" + name + ",";
		            }
		            String id = jsonObject.getJSONObject(jsonObject.length()-1).getString("id");
	                String name = jsonObject.getJSONObject(jsonObject.length()-1).getString("name");
	                cList += id + "\t" + name;
	                
	                //gives 311130
	                //System.out.println(cList.getBytes().length);
	                //gives 311082
	                //System.out.println(cList.length());
	                // 65535*(311130/311082) = 65545
		            // I will choose size of 65002 chars for each packet
	                
		            // first 2 chars will represent hasNextPacket and message type
		            if(cList.length() > 65000) {
		            	int numOfPacket = cList.length() / 65000;
		            	boolean hasRemainder = ((cList.length() % 65000.0) != 0);
		            	if(hasRemainder) {
		            		numOfPacket++;
		            	}
		            	for(int i=1; i<numOfPacket; i++) {
		            		serverPacket = "1l,"+cList.substring(0,65000);
		            		os.println(serverPacket);
			            	os.flush();
			            	cList = cList.substring(65000);
		            	}
		            	serverPacket = "0l,"+cList;
		            	os.println(serverPacket);
		            	os.flush();		            	
		            }else {
		            	serverPacket = "0l,"+cList;
		            	os.println(serverPacket);
		            	os.flush();		            	
		            }     
				}else if(Character.compare(line.charAt(1),'2')==0){
					//os.println("You query price");
					String priceURL = baseURL.concat("simple/price?ids=");
					//adding ids of cryptocurrencies
					String[] parameters = line.split(",");
					int size = parameters.length;
					if(size>2) {
						for(int i=1; i<(size-1);i++) {
							priceURL = priceURL.concat(parameters[i]+"%2C");
						}
						priceURL = priceURL.concat(parameters[size-1]+"&vs_currencies=try");
					}else {
						priceURL = priceURL.concat(parameters[1]+"&vs_currencies=try");
					}
					
					URL url = new URL(priceURL);
		            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		            conn.setRequestMethod("GET");
		            conn.connect();
		            
		            String inline = "";
		            Scanner scanner = new Scanner(url.openStream());
		            while (scanner.hasNext()) {
		                inline += scanner.nextLine();
		            }
		            //Close the scanner
		            scanner.close();  
		            
		            JSONObject jsonObject = new JSONObject(inline);
		            String prices = "";
		            for(int i=1; i<(size-1);i++) {
		            	JSONObject jsonObject2 = jsonObject.getJSONObject(parameters[i]);
		            	prices += "id: "+parameters[i] +"\t"+" price(try): "+jsonObject2.getDouble("try")+",";
					}
		            JSONObject jsonObject2 = jsonObject.getJSONObject(parameters[size-1]);
	            	prices += "id: "+parameters[size-1] +"\t"+" price(try): "+jsonObject2.getDouble("try");
		            
	            	if(prices.length() > 65000) {
		            	int numOfPacket = prices.length() / 65000;
		            	boolean hasRemainder = ((prices.length() % 65000.0) != 0);
		            	if(hasRemainder) {
		            		numOfPacket++;
		            	}
		            	for(int i=1; i<numOfPacket; i++) {
		            		serverPacket = "1p,"+prices.substring(0,65000);
		            		os.println(serverPacket);
			            	os.flush();
			            	prices = prices.substring(65000);
		            	}
		            	serverPacket = "0p,"+prices;
		            	os.println(serverPacket);
		            	os.flush();		            	
		            }else {
		            	serverPacket = "0p,"+prices;
		            	os.println(serverPacket);
		            	os.flush();
		            	
		            }
				}else {
					os.println("You did invalid query!");
					os.flush();
				}
                
                
            	lines = "Client messaged : " + line + " at  : " + Thread.currentThread().getId();
                System.out.println("Client " + s.getRemoteSocketAddress() + " sent :  " + lines);
                
                clientPacket = is.readLine();
            	line = clientPacket;
            	while(Character.compare(clientPacket.charAt(0),'1')==0) {
            		clientPacket = is.readLine();
            		line = line.concat(clientPacket.substring(3));
            	}
            }
        }
        catch (IOException e)
        {
            line = this.getName(); //reused String line for getting thread name
            System.err.println("Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
        }
        catch (NullPointerException e)
        {
            line = this.getName(); //reused String line for getting thread name
            System.err.println("Server Thread. Run.Client " + line + " Closed");
        } finally
        {
            try
            {
                System.out.println("Closing the connection");
                if (is != null)
                {
                    is.close();
                    System.err.println(" Socket Input Stream Closed");
                }

                if (os != null)
                {
                    os.close();
                    System.err.println("Socket Out Closed");
                }
                if (s != null)
                {
                    s.close();
                    System.err.println("Socket Closed");
                }

            }
            catch (IOException ie)
            {
                System.err.println("Socket Close Error");
            }
        }//end finally
    }
}
