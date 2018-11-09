import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Main {

    private static int PORT;                        //connection port
    private static String DB_URL;                   //host
    private static String DB_NAME;                  //database name
    private static String DB_USER;                  //database user
    private static String DB_PASSWD;                //database user password
    private static final int NUM_PARAMS=5;

    public static void main(String[] args) {


        System.out.print("\nGetting connection parameters from ./config/diagnostics.conf ... ");
        //retrieve above parameters from file ./config/diagnostics.conf
        HashMap<String, String> params = null;
        try {
            params = (HashMap<String, String>) readParams("config/diagnostics.conf");
        } catch (WrongFileFormat e) {
            e.printStackTrace();
        }
        if ((params.size()) != NUM_PARAMS) {
            System.err.println("Wrong file format");
        }

        //get the parameters from the returned map
        bindParams(params);
        System.out.print("DONE\n");

        //creation of listener
        Listener listener = new Listener(PORT);
        listener.init();

        //creation of updater
        DBUpdater updater = new DBUpdater(DB_URL, DB_NAME, DB_USER, DB_PASSWD);
        updater.init();

        //queue used to store Records before being used by updater
        LinkedList<Record> queue = new LinkedList<>();

        //starting both listen and update thread
        Thread listenThread = new Thread(Listener.produce(listener, queue));
        listenThread.start();

        Thread updateThread = new Thread(DBUpdater.consume(updater, queue));
        updateThread.start();

    }

    static Map<String, String> readParams(String path) throws WrongFileFormat {

        Map<String, String> params = new HashMap<>();
        BufferedReader br = null;

        /*
        Config file can be accessed by relative path only if the .jar is executed while the working directory is the jar directory.
        If is executed from "outside" the path needs to be absolute.
        Function getJarPath retrieve the absolute path of jar file that is being executed
         */
        try {   //try with relative path
            br = new BufferedReader(new FileReader(path));
        } catch (IOException e) {
            try {   //try with absolute path
                String jarPath = getJarPath();
                br = new BufferedReader(new FileReader(jarPath + "/" + path));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        }
        try {
            String param;
            while ((param = br.readLine()) != null) {
                String[] param_entry = param.split(":");
                if (param_entry.length != 2) {
                    throw new WrongFileFormat();
                }

                param_entry[0] = param_entry[0].trim();
                param_entry[1] = param_entry[1].trim();

                switch (param_entry[0]) {
                    case "PORT":
                    case "DB_URL":
                    case "DB_NAME":
                    case "DB_USER":
                    case "DB_PASSWD":
                        params.put(param_entry[0], param_entry[1]);
                        break;

                    default:
                        throw new WrongFileFormat();
                }
            }

        } catch (IOException e) {
            System.err.println("Cannot find config file (supposed to be in ./config/diagnostics.conf)");
            e.printStackTrace();
            return null;
        }
        return params;
    }

    static String getJarPath() {
        File f = new File(System.getProperty("java.class.path"));
        File dir = f.getAbsoluteFile().getParentFile();
        String jarPath = dir.toString();
        return jarPath;
    }

    static void bindParams(HashMap<String, String> params) {
        PORT = Integer.parseInt(params.get("PORT"));
        DB_URL = params.get("DB_URL");
        DB_NAME = params.get("DB_NAME");
        DB_USER = params.get("DB_USER");
        DB_PASSWD = params.get("DB_PASSWD");
    }

    static class WrongFileFormat extends Exception {
        WrongFileFormat() {
            System.err.println("Wrong file format");
            System.out.println("Wrong file format");
        }
    }

}
