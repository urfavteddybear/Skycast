package com.wpcreative.skycast.api;

import com.google.gson.Gson;
import com.wpcreative.skycast.model.WeatherResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class WeatherApiClient {
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String API_KEY = "redacted"; // Replace with your actual API key
    
    private final OkHttpClient client;
    private final Gson gson;
    
    public WeatherApiClient() {
        client = new OkHttpClient();
        gson = new Gson();
    }
    
    public interface WeatherCallback {
        void onSuccess(WeatherResponse weather);
        void onError(String error);
    }
    
    public void getCurrentWeather(String cityName, WeatherCallback callback) {
        String url = BASE_URL + "?q=" + cityName + "&appid=" + API_KEY + "&units=metric";
        
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        WeatherResponse weatherResponse = gson.fromJson(responseBody, WeatherResponse.class);
                        callback.onSuccess(weatherResponse);
                    } catch (Exception e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                } else {
                    callback.onError("API error: " + response.code());
                }
            }
        });
    }
    
    public void getCurrentWeatherByCoords(double lat, double lon, WeatherCallback callback) {
        String url = BASE_URL + "?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY + "&units=metric";
        
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        WeatherResponse weatherResponse = gson.fromJson(responseBody, WeatherResponse.class);
                        callback.onSuccess(weatherResponse);
                    } catch (Exception e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                } else {
                    callback.onError("API error: " + response.code());
                }
            }
        });
    }
}
