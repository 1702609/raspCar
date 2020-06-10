 

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
    private static String RESPONSE = "";
    private static String lineRead = "";

    // start server
    private void startServer() throws IOException {
        // Create a UUID for SPP
        UUID uuid = new UUID("0000110100001000800000805F9B34FB", false);
        // Create the servicve url
        String connectionString = "btspp://localhost:" + uuid + ";name=Sample SPP Server";

        // open server url
        StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector.open(connectionString);

        // Wait for client connection
        System.out.println("\nServer Started. Waiting for clients to connect...");
        StreamConnection connection = streamConnNotifier.acceptAndOpen();

        RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
        System.out.println("Remote device address: " + dev.getBluetoothAddress());
        System.out.println("Remote device name: " + dev.getFriendlyName(true));

        // read string from spp client
        InputStream inStream = connection.openInputStream();
        BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
        lineRead = bReader.readLine();
        System.out.println("Message from mobile device: "+lineRead);
        
        Process p = Runtime.getRuntime().exec("python motor.py " +lineRead);
        InputStream stdIn = p.getInputStream();
        InputStreamReader isr = new InputStreamReader(stdIn);
        BufferedReader br = new BufferedReader(isr);
	
        String pythonMsg = null;
        while ((pythonMsg = br.readLine()) != null)
        System.out.println("The response is "+pythonMsg);

        // send response to spp client
        OutputStream outStream = connection.openOutputStream();
        PrintWriter pWriter = new PrintWriter(new OutputStreamWriter(outStream));
        System.out.println("Sending response (" + RESPONSE + ")");
        pWriter.write(RESPONSE + "\r\n");
        pWriter.flush();
        pWriter.close();
        streamConnNotifier.close();
        
    }

    public static void main(String[] args) throws IOException {
        // display local device address and name
        try 
            {
            Runtime.getRuntime().exec("sudo chmod 777 /var/run/sdp");
            } 
        catch (IOException e) 
            {
            e.printStackTrace();
            }
        
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Address: " + localDevice.getBluetoothAddress());
        System.out.println("Name: " + localDevice.getFriendlyName());
    
        BluetoothServer sampleSPPServer = new BluetoothServer();
        while (true) {
            sampleSPPServer.startServer();
        }

    }
}
