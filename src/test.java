import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import static java.lang.System.currentTimeMillis;

public class test {

    private static int port = 7777;
    private static String host = "www.lorenzodenisi.com";

    /*
    Test class
    I try to connect to the TCP server sending a Record object with ID1, ID2 and current time (ignored)
    The object is sent as a string over a BufferedWriter
    Record extends JSONObject so the compatibility is complete
    */
    public static void main(String[] args) {


        try {
            Record testMsg = new Record("1", "2", currentTimeMillis());

            InetAddress address = InetAddress.getByName(host);
            String ip = address.getHostAddress();

            System.out.println("Opening connection...\n");
            Socket s1 = new Socket(ip, port);

            OutputStream os = s1.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

            bw.write(testMsg.toJSONString());

            System.out.println("Message sent");
            bw.close();
            s1.close();
            System.out.println("Closed connection\n");
        } catch (ConnectException connExc) {
            System.err.println("Failed to connect to server (" + host + ")\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
