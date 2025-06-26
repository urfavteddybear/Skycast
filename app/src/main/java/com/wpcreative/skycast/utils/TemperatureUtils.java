package com.wpcreative.skycast.utils;

public class TemperatureUtils {
    
    // Temperature unit constants (matching OpenWeatherMap API units parameter)
    public static final String UNIT_METRIC = "metric";     // Celsius
    public static final String UNIT_IMPERIAL = "imperial"; // Fahrenheit
    public static final String UNIT_KELVIN = "standard";   // Kelvin
    
    // Temperature unit display names
    public static final String UNIT_NAME_CELSIUS = "Celsius";
    public static final String UNIT_NAME_FAHRENHEIT = "Fahrenheit";
    public static final String UNIT_NAME_KELVIN = "Kelvin";
    
    /**
     * Get the temperature unit symbol based on the API unit parameter
     * @param unit The unit parameter used in API calls (metric, imperial, standard)
     * @return The symbol to display (°C, °F, K)
     */
    public static String getTemperatureSymbol(String unit) {
        switch (unit) {
            case UNIT_METRIC:
                return "°C";
            case UNIT_IMPERIAL:
                return "°F";
            case UNIT_KELVIN:
                return "K";
            default:
                return "°C"; // Default to Celsius
        }
    }
    
    /**
     * Get the display name for a temperature unit
     * @param unit The unit parameter used in API calls (metric, imperial, standard)
     * @return The human-readable name
     */
    public static String getTemperatureUnitName(String unit) {
        switch (unit) {
            case UNIT_METRIC:
                return UNIT_NAME_CELSIUS;
            case UNIT_IMPERIAL:
                return UNIT_NAME_FAHRENHEIT;
            case UNIT_KELVIN:
                return UNIT_NAME_KELVIN;
            default:
                return UNIT_NAME_CELSIUS; // Default to Celsius
        }
    }
    
    /**
     * Format temperature with appropriate unit symbol
     * @param temperature The temperature value
     * @param unit The unit parameter used in API calls
     * @return Formatted temperature string (e.g., "25°C", "77°F", "298K")
     */
    public static String formatTemperature(double temperature, String unit) {
        return String.format("%.0f%s", temperature, getTemperatureSymbol(unit));
    }
    
    /**
     * Format temperature with one decimal place and appropriate unit symbol
     * @param temperature The temperature value
     * @param unit The unit parameter used in API calls
     * @return Formatted temperature string (e.g., "25.5°C", "77.9°F", "298.2K")
     */
    public static String formatTemperatureDecimal(double temperature, String unit) {
        return String.format("%.1f%s", temperature, getTemperatureSymbol(unit));
    }
}
