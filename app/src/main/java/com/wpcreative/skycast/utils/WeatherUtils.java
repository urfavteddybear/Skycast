package com.wpcreative.skycast.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherUtils {
    
    public static String getWeatherIcon(String iconCode) {
        // Map weather icon codes to appropriate icons
        switch (iconCode) {
            case "01d":
            case "01n":
                return "☀️";
            case "02d":
            case "02n":
                return "⛅";
            case "03d":
            case "03n":
            case "04d":
            case "04n":
                return "☁️";
            case "09d":
            case "09n":
                return "🌧️";
            case "10d":
            case "10n":
                return "🌦️";
            case "11d":
            case "11n":
                return "⛈️";
            case "13d":
            case "13n":
                return "❄️";
            case "50d":
            case "50n":
                return "🌫️";
            default:
                return "🌤️";
        }
    }
    
    public static String formatTime(long timestamp) {
        Date date = new Date(timestamp * 1000);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm, EEE MMM dd", Locale.getDefault());
        return format.format(date);
    }
    
    public static String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
    
    public static double convertKmhToMs(double kmh) {
        return kmh * 1000 / 3600;
    }
    
    public static double convertMsToKmh(double ms) {
        return ms * 3600 / 1000;
    }
}
