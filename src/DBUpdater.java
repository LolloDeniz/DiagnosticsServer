import java.sql.*;
import java.util.LinkedList;


class DBUpdater {

    private static final int PORT = 3306;     //database port
    private String db_url;               //database host
    private String db_name;              //database name
    private String user;                 //database user
    private String passwd;               //database user password

    //parameters for DB connection (fix timezone problems)
    private String params = "useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

    private Connection conn = null;

    //prepared statements
    private PreparedStatement update = null;
    private String update_string = "UPDATE DIAGNOSTICS SET DIAGNOSTICS.TIMES=? WHERE ID1=? AND ID2=?";

    private PreparedStatement checkPresence = null;
    private String checkPresence_string = "SELECT * FROM DIAGNOSTICS WHERE ID1=? AND ID2=?";

    private PreparedStatement create = null;
    private String create_string = "INSERT INTO DIAGNOSTICS (ID1, ID2, TIMES) VALUE (?,?,1)";


    public DBUpdater(String dbUrl, String db_name, String user, String passwd) {
        this.db_url = dbUrl;
        this.db_name = db_name;
        this.user = user;
        this.passwd = passwd;
    }

    public boolean init() {
        try {

            System.out.println("\nConnecting to database " + db_url + "...");

            //connection to DB
            String URL = "jdbc:mysql://" + db_url + ":" + PORT + "/" + db_name + "?" + params;
            conn = DriverManager.getConnection(URL, user, passwd);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        if (conn == null) return false;

        try {
            update = conn.prepareStatement(update_string);
            checkPresence = conn.prepareStatement(checkPresence_string);
            create = conn.prepareStatement(create_string);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    boolean isReady() {
        return (conn != null && update != null || create != null || checkPresence != null);
    }

    boolean update(Record record) {

        String ID1 = record.getID1();
        String ID2 = record.getID2();
        int times = -1;

        try {
            //check the presence of ID1-ID2 row
            times = checkPresence(ID1, ID2);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        if (times < 0) return false;

        //if not present
        if (times == 0) {
            try {
                create.setString(1, ID1);
                create.setString(2, ID2);
                return (create.executeUpdate() == 1);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        //if is present
        //increment the "TIMES" value and update row
        times++;

        try {
            update.setInt(1, times);
            update.setString(2, ID1);
            update.setString(3, ID2);

            return (update.executeUpdate() == 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    private int checkPresence(String ID1, String ID2) throws SQLException {

        checkPresence.setString(1, ID1);
        checkPresence.setString(2, ID2);

        ResultSet res = checkPresence.executeQuery();

        if (!res.next()) return 0;

        return res.getInt("TIMES");
    }

    //the runnable is the code executed by the thread
    static Runnable consume(DBUpdater updater, LinkedList<Record> queue) {

        boolean initDone = true;

        if (updater == null || queue == null) return null;

        if (!updater.isReady()) {
            initDone = updater.init();
        }

        if (!initDone) return null;

        return () -> {
            System.out.println("Updater ready");
            while (true) {
                Record res;
                //if the queue is not empty, the first Record is consumed
                if (!queue.isEmpty()) {

                    res = queue.poll();   //getting the first Record

                    if (updater.update(res))
                        System.out.println("Row updated correctly (ID1=" + res.getID1() + " ID2=" + res.getID2() + ") Queue size:"+ queue.size());
                    else
                        System.out.println("Update failed");
                }
            }
        };
    }
}
