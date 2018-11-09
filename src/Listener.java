import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class Listener {

    private int port;               //connection port
    private ServerSocket socket;    //socket used to listen data


    public Listener(int port) {
        this.port = port;
    }


    boolean init() {
        try {
            socket = new ServerSocket(this.port);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("Connection established\n");
        return true;
    }

    Record listen() {
        Record result;          //data received is reconverted to a Record obj
        try {
            System.out.println("\nListen on port " + this.port + " ...");

            //wait until a client is connected
            Socket s1 = socket.accept();
            System.out.println("A client is connected");


            //the input stream is parsed to get back the JSONObject
            InputStream is = s1.getInputStream();
            JSONParser parser = new JSONParser();
            result = new Record((JSONObject) parser.parse(new InputStreamReader(is, StandardCharsets.UTF_8)));

            is.close();
            s1.close();
            System.out.println("Closed connection with client");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    boolean isReady() {
        return (this.socket != null);
    }

    public void stop() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //the runnable is the code executed by the thread
    static Runnable produce(Listener listener, LinkedList<Record> queue) {

        boolean initDone = true;

        if (listener == null || queue == null)
            return null;

        if (!listener.isReady())
            initDone = listener.init();

        if (!initDone) return null;

        System.out.println("Start listening...");
        return () -> {
            while (true) {
                //keep listening until a client is connected, then produce the Record object and add it to the queue
                queue.add(listener.listen());
            }
        };
    }

}
