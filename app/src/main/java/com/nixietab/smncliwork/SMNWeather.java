package com.nixietab.smncliwork;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.json.*;




public class SMNWeather {

    private static final String API_BASE = "https://ws.smn.gob.ar/map_items/weather";
    // Returns a string list of all the locations
    public static List<String> getAllLocations() throws IOException, JSONException {
        JSONArray data = fetchWeatherData();
        List<String> locations = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.getJSONObject(i);
            if (item.has("name")) {
                locations.add(item.getString("name"));
            }
        }
        Collections.sort(locations);
        return locations;
    }
    // asks for a string of a city name ej "Capital Federal" and returns climate info in structure
    public static JSONObject getWeatherForLocation(String cityName) throws IOException, JSONException {
        JSONArray data = fetchWeatherData();
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.getJSONObject(i);
            if (item.has("name") && item.getString("name").toLowerCase().contains(cityName.toLowerCase())) {
                return item;
            }
        }
        return null;
    }

    public static Map<String, String> getWeatherInfo(String cityName) throws IOException, JSONException {
        JSONObject w = getWeatherForLocation(cityName);
        if (w == null) return null;

        Map<String, String> info = new HashMap<>();
        info.put("name", w.optString("name", "Unknown"));

        JSONObject weather = w.optJSONObject("weather");
        if (weather == null) weather = new JSONObject();

        info.put("description", capitalize(weather.optString("description", "N/A")));
        info.put("temp", weather.optString("temp", "N/A"));
        info.put("humidity", weather.optString("humidity", "N/A"));
        info.put("pressure", weather.optString("pressure", "N/A"));
        info.put("wind_speed", weather.optString("wind_speed", "N/A"));
        info.put("wind_dir", weather.optString("wind_dir", ""));
        return info;
    }

    private static JSONArray fetchWeatherData() throws IOException, JSONException {
        URL url = new URL(API_BASE);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            return new JSONArray(response.toString());
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
