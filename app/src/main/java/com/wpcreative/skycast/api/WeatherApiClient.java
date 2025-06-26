package com.wpcreative.skycast.api;

import android.content.Context;
import com.google.gson.Gson;
import com.wpcreative.skycast.database.SettingsDbHelper;
import com.wpcreative.skycast.model.ForecastResponse;
import com.wpcreative.skycast.model.WeatherResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class WeatherApiClient {
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String DEFAULT_API_KEY = "redacted"; // Default API key
    
    private final OkHttpClient client;
    private final Gson gson;
    private final Context context;
    
    public WeatherApiClient(Context context) {
        client = new OkHttpClient();
        gson = new Gson();
        this.context = context;
    }
    
    private String getApiKey() {
        SettingsDbHelper dbHelper = new SettingsDbHelper(context);
        String customApiKey = dbHelper.getSetting(SettingsDbHelper.SETTING_API_KEY, null);
        dbHelper.close();
        
        // Return custom API key if available, otherwise use default
        return (customApiKey != null && !customApiKey.isEmpty()) ? customApiKey : DEFAULT_API_KEY;
    }

    public interface WeatherCallback {
        void onSuccess(WeatherResponse weather);
        void onError(String error);
    }
    
    public interface ForecastCallback {
        void onSuccess(ForecastResponse forecast);
        void onError(String error);
    }
    
    public void getCurrentWeather(String cityName, WeatherCallback callback) {
        String url = BASE_URL + "?q=" + cityName + "&appid=" + getApiKey() + "&units=metric";
        
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
        String url = BASE_URL + "?lat=" + lat + "&lon=" + lon + "&appid=" + getApiKey() + "&units=metric";
        
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
    
    public void getForecast(String cityName, ForecastCallback callback) {
        String url = FORECAST_URL + "?q=" + cityName + "&appid=" + getApiKey() + "&units=metric";
        
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
                        ForecastResponse forecastResponse = gson.fromJson(responseBody, ForecastResponse.class);
                        callback.onSuccess(forecastResponse);
                    } catch (Exception e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                } else {
                    callback.onError("API error: " + response.code());
                }
            }
        });
    }
    
    public void getForecastByCoords(double lat, double lon, ForecastCallback callback) {
        String url = FORECAST_URL + "?lat=" + lat + "&lon=" + lon + "&appid=" + getApiKey() + "&units=metric";
        
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
                        ForecastResponse forecastResponse = gson.fromJson(responseBody, ForecastResponse.class);
                        callback.onSuccess(forecastResponse);
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
