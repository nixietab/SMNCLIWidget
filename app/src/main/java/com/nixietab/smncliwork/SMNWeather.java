package com.nixietab.smncliwork;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import org.json.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


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
        handleSSLHandshake(); // Blasphemy
        URL url = new URL(API_BASE);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
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
    public static void handleSSLHandshake() { // Please don't ever do this
        try { // All faith is lost
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((arg0, arg1) -> true);
        } catch (Exception ignored) { // And there's how security doesn't work anymore C:
        }
    }
}
