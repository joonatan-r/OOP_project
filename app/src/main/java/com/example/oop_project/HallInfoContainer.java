package com.example.oop_project;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/*
This class stores all halls' info in a JSONObject and provides methods for adding and removing
halls. The info is stored in a json file and parsed into the JSONObject when the app is running.
 */
public class HallInfoContainer {
    private final String hallsFileName = "hall_info.json";
    private Context context;
    private JSONObject halls;

    public HallInfoContainer(Context context) throws IOException, JSONException {
        String filesDirPath = context.getFilesDir().getPath();
        File hallsFile = new File(filesDirPath, hallsFileName);
        this.context = context;

        // If file doesn't exist (first run), copy default info from assets

        InputStream in = hallsFile.exists()
                ? context.openFileInput(hallsFileName)
                : context.getResources().getAssets().open(hallsFileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String s;

        while ((s = br.readLine()) != null) sb.append(s);

        halls = new JSONObject(sb.toString());
        br.close();
        in.close();

        if (!hallsFile.exists()) {
            OutputStreamWriter ows = new OutputStreamWriter(context.openFileOutput(hallsFileName, Context.MODE_PRIVATE));
            ows.write(sb.toString());
            ows.close();
        }
    }

    /*
    Converts this classes JSONObject of halls into an ArrayList of Hall objects and returns it. If
    there's an error, the returned list contains all Hall objects added before encountering it.
     */
    public ArrayList<Hall> getHalls() {
        ArrayList<Hall> hallsList = new ArrayList<>();

        try {
            JSONArray hallsArray = halls.getJSONArray("halls");

            for (int i = 0; i < hallsArray.length(); i++) {
                JSONObject o = (JSONObject) hallsArray.get(i);
                String id = o.getString("id");
                int maxSize = o.getInt("maxSize");
                hallsList.add(new Hall(id, maxSize));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return hallsList;
    }

    /*
    Takes a Hall object as a parameter, converts it to a JSONObject, adds it to this classes
    JSONObject containing all halls and replaces the halls file contents to reflect the change.
    Returns true if successful and false if an error occurred.
     */
    public boolean addHall(Hall hall) {
        try {
            JSONArray hallsArray = halls.getJSONArray("halls");
            JSONObject hallObject = new JSONObject();
            hallObject.put("id", hall.getId());
            hallObject.put("maxSize", hall.getMaxSize());
            hallsArray.put(hallObject);
            OutputStreamWriter ows = new OutputStreamWriter(context.openFileOutput(hallsFileName, Context.MODE_PRIVATE));
            ows.write(halls.toString());
            ows.close();
            return true;
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /*
    Takes an id String as a parameter and removes the hall with matching id from the JSONObject
    containing all halls and replaces the halls file contents to reflect the change. Returns true if
    deleted hall successfully, and false if there was an error or a hall with matching id couldn't
    be found.
     */
    public boolean removeHall(String id) {
        try {
            JSONArray hallsArray = halls.getJSONArray("halls");
            int idx = -1;

            for (int i = 0; i < hallsArray.length(); i++) {
                JSONObject o = (JSONObject) hallsArray.get(i);

                if (o.getString("id").equals(id)) {
                    idx = i;
                    break;
                }
            }

            if (idx < 0) return false;

            hallsArray.remove(idx);
            OutputStreamWriter ows = new OutputStreamWriter(context.openFileOutput(hallsFileName, Context.MODE_PRIVATE));
            ows.write(halls.toString());
            ows.close();
            return true;
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
