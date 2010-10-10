package net.sourcewalker.syncdemo.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NumbersData {

    public String revision;
    public int[] numbers;

    public NumbersData(JSONObject data) {
        if (!data.has("_rev")) {
            throw new IllegalArgumentException("Revision missing: " + data);
        }
        if (!data.has("numbers")) {
            throw new IllegalArgumentException("Numbers array missing: " + data);
        }
        try {
            this.revision = data.getString("_rev");
            JSONArray jsonNumbers = data.getJSONArray("numbers");
            this.numbers = new int[jsonNumbers.length()];
            for (int i = 0; i < jsonNumbers.length(); i++) {
                this.numbers[i] = jsonNumbers.getInt(i);
            }
        } catch (JSONException e) {
            throw new RuntimeException("Uncaught JSON exception: "
                    + e.getMessage(), e);
        }
    }

    public JSONObject toJSON() {
        JSONObject result;
        try {
            JSONArray jsonNumbers = new JSONArray();
            for (int i = 0; i < numbers.length; i++) {
                jsonNumbers.put(i, numbers[i]);
            }
            result = new JSONObject();
            result.put("_rev", revision);
            result.put("numbers", jsonNumbers);
        } catch (JSONException e) {
            throw new RuntimeException("Uncaught JSON Exception: "
                    + e.getMessage(), e);
        }
        return result;
    }
}
