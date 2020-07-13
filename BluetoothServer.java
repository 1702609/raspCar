import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class BluetoothServer {
    private static String lineRead = "";
    private static String pythonMsg = null;

    // start server
    private void startServer(StreamConnection socket) throws IOException {
       
        InputStream inStream = socket.openInputStream();
        BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
        OutputStream outStream = socket.openOutputStream();
        
        while (true)
	        {
	        lineRead = bReader.readLine();
	        System.out.println("Message from mobile device: "+lineRead);
	        
	        Process p = Runtime.getRuntime().exec("python motor.py " +lineRead);
	        InputStream stdIn = p.getInputStream();
	        InputStreamReader isr = new InputStreamReader(stdIn);
	        BufferedReader br = new BufferedReader(isr);
		
	        pythonMsg = br.readLine();
	        System.out.println("The response is "+ pythonMsg);
	
	        // send response to spp client
	        PrintWriter pWriter = new PrintWriter(new OutputStreamWriter(outStream));
	        System.out.println("Sending response (" + pythonMsg + ")");
	        pWriter.write(pythonMsg + "\r\n");
	        pWriter.flush();
	        outStream.flush();
	        }
    }

    public static void main(String[] args) throws IOException {
        //give server access to bluetooth driver 
    	try 
            {
            Runtime.getRuntime().exec("sudo chmod 777 /var/run/sdp");
            } 
        catch (IOException e) 
            {
            e.printStackTrace();
            }        
        
    	//Prepare server
    	LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Address: " + localDevice.getBluetoothAddress());
        System.out.println("Name: " + localDevice.getFriendlyName());
        
        //Prepare client 
        UUID uuid = new UUID("0000110100001000800000805F9B34FB", false);
        String connectionString = "btspp://localhost:" + uuid + ";name=Sample SPP Server";
        StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector.open(connectionString);
        System.out.println("\nServer Started. Waiting for clients to connect...");
        StreamConnection socket = streamConnNotifier.acceptAndOpen();
        System.out.println("\nDevice has been connected");
        
        BluetoothServer sampleSPPServer = new BluetoothServer();       
        sampleSPPServer.startServer(socket);
                    
      
    }
}
