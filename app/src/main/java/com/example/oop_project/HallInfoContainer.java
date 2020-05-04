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

public class HallInfoContainer {
    private final String hallsFileName = "hall_info.json";
    private Context context;
    private JSONObject halls;

    public HallInfoContainer(Context context) throws IOException, JSONException {
        String filesDirPath = context.getFilesDir().getPath();
        File hallsFile = new File(filesDirPath, hallsFileName);
        this.context = context;

        // if file doesn't exist (first run), copy default info from assets

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
