package com.wpcreative.skycast;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wpcreative.skycast.api.WeatherApiClient;
import com.wpcreative.skycast.model.WeatherResponse;
import com.wpcreative.skycast.utils.WeatherUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;    // UI Components
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvCurrentTime;
    private TextView tvLocation;
    private TextView tvTemperature;
    private TextView tvWeatherIcon;
    private TextView tvWeatherDescription;
    private TextView tvFeelsLike;
    private TextView tvWindSpeed;
    private TextView tvHumidity;
    private TextView tvVisibility;
    private TextView tvPressure;
    private TextView tvFeelsLikeDetail;
    private TextView tvFeelsLikeValue;
    private TextView tvWindValue;
    
    private ImageView btnLocation;
    private ImageView btnForecast;
    private ImageView btnSettings;
    private ImageView btnClearSearch;
    private EditText etSearchCity;
    
    // API and Location
    private WeatherApiClient weatherApiClient;
    private LocationManager locationManager;
    private Handler mainHandler;
    private boolean isLocationRequestActive = false;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SkycastPrefs";
    private static final String PREF_DEFAULT_CITY = "default_city";
    
    // Store last known coordinates for forecast
    private double lastKnownLatitude = 0;
    private double lastKnownLongitude = 0;
    private boolean hasValidCoordinates = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
          initializeViews();
        initializeServices();
        setupClickListeners();
        updateCurrentTime();
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Try to get user's current location first, fallback to Jakarta if failed
        getCurrentLocationOnStartup();
    }    private void initializeViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvLocation = findViewById(R.id.tvLocation);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvWeatherIcon = findViewById(R.id.tvWeatherIcon);
        tvWeatherDescription = findViewById(R.id.tvWeatherDescription);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvWindSpeed = findViewById(R.id.tvWindSpeed);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvVisibility = findViewById(R.id.tvVisibility);
        tvPressure = findViewById(R.id.tvPressure);
        tvFeelsLikeDetail = findViewById(R.id.tvFeelsLikeDetail);
        tvFeelsLikeValue = findViewById(R.id.tvFeelsLikeValue);
        tvWindValue = findViewById(R.id.tvWindValue);
        
        btnLocation = findViewById(R.id.btnLocation);
        btnForecast = findViewById(R.id.btnForecast);
        btnSettings = findViewById(R.id.btnSettings);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        etSearchCity = findViewById(R.id.etSearchCity);
    }
    
    private void initializeServices() {
        weatherApiClient = new WeatherApiClient();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mainHandler = new Handler(Looper.getMainLooper());
    }    private void setupClickListeners() {
        // Setup pull to refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshWeatherData);
        swipeRefreshLayout.setColorSchemeResources(
            R.color.light_blue,
            R.color.accent_orange,
            R.color.light_blue
        );
        
        btnLocation.setOnClickListener(v -> getCurrentLocation());
        btnForecast.setOnClickListener(v -> openForecast());
        btnSettings.setOnClickListener(v -> showSettingsDialog());
        btnClearSearch.setOnClickListener(v -> clearSearch());
        
        // Handle search input
        etSearchCity.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });
        
        // Show clear button when typing
        etSearchCity.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }
    
    private void updateCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, EEE MMM dd", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        tvCurrentTime.setText(currentTime);
    }
    
    private void loadWeatherData(String cityName) {
        weatherApiClient.getCurrentWeather(cityName, new WeatherApiClient.WeatherCallback() {
            @Override
            public void onSuccess(WeatherResponse weather) {
                mainHandler.post(() -> {
                    updateUI(weather);
                    stopRefreshing();
                });
            }
            
            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    // Show default/mock data if API fails
                    showDefaultData();
                    stopRefreshing();
                });
            }
        });
    }
    
    private void loadWeatherDataByLocation(double lat, double lon) {
        weatherApiClient.getCurrentWeatherByCoords(lat, lon, new WeatherApiClient.WeatherCallback() {
            @Override
            public void onSuccess(WeatherResponse weather) {
                mainHandler.post(() -> {
                    updateUI(weather);
                    stopRefreshing();
                });
            }
            
            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    showDefaultData();
                    stopRefreshing();
                });
            }
        });
    }
    
    private void updateUI(WeatherResponse weather) {
        if (weather == null) return;
        
        // Update location
        tvLocation.setText(weather.name + ", " + weather.sys.country);
        
        // Update temperature
        int temperature = (int) Math.round(weather.main.temp);
        tvTemperature.setText(temperature + "°");
        
        // Update weather icon and description
        if (weather.weather != null && !weather.weather.isEmpty()) {
            String iconCode = weather.weather.get(0).icon;
            String description = weather.weather.get(0).description;
            
            tvWeatherIcon.setText(WeatherUtils.getWeatherIcon(iconCode));
            tvWeatherDescription.setText(WeatherUtils.capitalizeFirstLetter(description));
        }
        
        // Update feels like
        int feelsLike = (int) Math.round(weather.main.feels_like);
        tvFeelsLike.setText("Feels like " + feelsLike + "°");
        tvFeelsLikeDetail.setText(feelsLike + "°C");
        tvFeelsLikeValue.setText(feelsLike + "°C");
        
        // Update wind
        double windSpeedKmh = WeatherUtils.convertMsToKmh(weather.wind.speed);
        tvWindSpeed.setText((int) Math.round(windSpeedKmh) + " km/h");
        tvWindValue.setText((int) Math.round(windSpeedKmh) + " km/h");
        
        // Update humidity
        tvHumidity.setText(weather.main.humidity + "%");
        
        // Update visibility (convert from meters to kilometers)
        double visibilityKm = weather.visibility / 1000.0;
        tvVisibility.setText((int) Math.round(visibilityKm) + " km");
        
        // Update pressure
        tvPressure.setText(weather.main.pressure + " mb");
    }
    
    private void showDefaultData() {
        // Show the data from the design as default
        tvLocation.setText("Denpasar, ID");
        tvTemperature.setText("29°");
        tvWeatherIcon.setText("🌧️");
        tvWeatherDescription.setText("Light Rain");
        tvFeelsLike.setText("Feels like 33°");
        tvWindSpeed.setText("11 km/h");
        tvHumidity.setText("74%");
        tvVisibility.setText("10 km");
        tvPressure.setText("1011 mb");
        tvFeelsLikeDetail.setText("33°C");        tvFeelsLikeValue.setText("33°C");
        tvWindValue.setText("11 km/h");
    }
    
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        
        try {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
            Toast.makeText(this, "Getting your location...", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");
        builder.setMessage("Weather App Settings\n\nNote: To use real weather data, please add your OpenWeatherMap API key in WeatherApiClient.java");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    
    private void clearSearch() {
        etSearchCity.setText("");
    }    
    private void performSearch() {
        String cityName = etSearchCity.getText().toString().trim();
        if (!cityName.isEmpty()) {
            loadWeatherData(cityName);
            etSearchCity.setText("");
        } else {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void getCurrentLocationOnStartup() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission first
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, get location
            requestLocationUpdate();
        }
    }
      private void requestLocationUpdate() {
        if (isLocationRequestActive) {
            return; // Prevent multiple simultaneous location requests
        }
        
        isLocationRequestActive = true;
        
        try {
            // Show loading message
            Toast.makeText(this, "Getting your location...", Toast.LENGTH_SHORT).show();
            
            // Try GPS first, then network provider as fallback
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        if (isLocationRequestActive) {
                            isLocationRequestActive = false;
                            loadWeatherDataByLocation(location.getLatitude(), location.getLongitude());
                        }
                    }
                }, null);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        if (isLocationRequestActive) {
                            isLocationRequestActive = false;
                            loadWeatherDataByLocation(location.getLatitude(), location.getLongitude());
                        }
                    }
                }, null);
            } else {
                // No location providers available, fallback to user default
                isLocationRequestActive = false;
                fallbackToDefaultCity("Location services disabled");
                return;
            }
            // Set a timeout - if location is not found within 10 seconds, fallback to user default
            mainHandler.postDelayed(() -> {
                if (isLocationRequestActive) {
                    isLocationRequestActive = false;
                    fallbackToDefaultCity("Location timeout");
                }
            }, 10000); // 10 seconds timeout
        } catch (SecurityException e) {
            // Permission revoked during runtime, fallback to user default
            isLocationRequestActive = false;
            fallbackToDefaultCity("Unable to get location");
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Store coordinates for forecast
        lastKnownLatitude = location.getLatitude();
        lastKnownLongitude = location.getLongitude();
        hasValidCoordinates = true;
        
        loadWeatherDataByLocation(location.getLatitude(), location.getLongitude());
    }
      @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, try to get location
                requestLocationUpdate();
            } else {
                // Permission denied, fallback to user default
                fallbackToDefaultCity("Location permission denied");
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    
    private void promptForDefaultCity(String reason) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Default City");
        builder.setMessage(reason + "\nPlease enter your preferred city:");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String city = input.getText().toString().trim();
            if (!city.isEmpty()) {
                sharedPreferences.edit().putString(PREF_DEFAULT_CITY, city).apply();
                loadWeatherData(city);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private String getDefaultCity() {
        return sharedPreferences.getString(PREF_DEFAULT_CITY, "");
    }

    private void fallbackToDefaultCity(String reason) {
        String city = getDefaultCity();
        if (!city.isEmpty()) {
            Toast.makeText(this, reason + ", showing " + city + " weather", Toast.LENGTH_SHORT).show();
            loadWeatherData(city);
        } else {
            promptForDefaultCity(reason);
        }
    }

    private void refreshWeatherData() {
        // Get current location text to determine what to refresh
        String currentLocation = tvLocation.getText().toString();
        
        if (currentLocation.isEmpty() || currentLocation.equals("Select Location")) {
            // If no location is set, try to get current location
            getCurrentLocation();
        } else {
            // Refresh weather for current location
            loadWeatherData(currentLocation);
        }
    }

    private void stopRefreshing() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void openForecast() {
        Intent intent = new Intent(this, ForecastActivity.class);
        
        // Pass current city name if available
        String currentLocation = tvLocation.getText().toString();
        if (!currentLocation.isEmpty() && !currentLocation.equals("Loading...")) {
            intent.putExtra("city_name", currentLocation);
        }
        
        // Pass coordinates if we have them
        if (hasValidCoordinates) {
            intent.putExtra("latitude", lastKnownLatitude);
            intent.putExtra("longitude", lastKnownLongitude);
            intent.putExtra("use_coordinates", true);
        }
        
        startActivity(intent);
    }
}