import org.json.simple.JSONObject;

public class Record extends JSONObject {

    private static final String ID1_KEY = "ID1";
    private static final String ID2_KEY = "ID2";
    private static final String TIMESTAMP_KEY = "TIMESTAMP";

    public Record(String ID1, String ID2, long Timestamp) {
        this.put(ID1_KEY, ID1);
        this.put(ID2_KEY, ID2);
        this.put(TIMESTAMP_KEY, Timestamp);
    }

    public Record(JSONObject json) {
        super(json);
    }

    public void setID1(String ID) {
        this.put(ID1_KEY, ID);
    }

    public void setID2(String ID) {
        this.put(ID2_KEY, ID);
    }

    public void setID1(Long timestamp) {
        this.put(TIMESTAMP_KEY, timestamp);
    }

    public String getID1() {
        return (String) this.get(ID1_KEY);
    }

    public String getID2() {
        return (String) this.get(ID2_KEY);
    }

    public long getTimestamp() {
        return (Long) this.get(TIMESTAMP_KEY);
    }
}
