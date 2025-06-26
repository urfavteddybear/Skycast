package com.wpcreative.skycast;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wpcreative.skycast.adapter.ForecastAdapter;
import com.wpcreative.skycast.api.WeatherApiClient;
import com.wpcreative.skycast.model.ForecastResponse;

import java.util.ArrayList;
import java.util.List;

public class ForecastActivity extends AppCompatActivity {
    
    // UI Components
    private TextView tvLocation;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewForecast;
    private View loadingIndicator;
    private CardView errorCard;
    private TextView tvErrorMessage;
    private ImageView btnBack;
    
    // Data and API
    private WeatherApiClient weatherApiClient;
    private ForecastAdapter forecastAdapter;
    private Handler mainHandler;
    private String cityName;
    private double latitude = 0;
    private double longitude = 0;
    private boolean useCoordinates = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forecast);
        
        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forecastMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initializeViews();
        initializeServices();
        setupClickListeners();
        getIntentData();
        loadForecast();
    }
    
    private void initializeViews() {
        tvLocation = findViewById(R.id.tvForecastLocation);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerViewForecast = findViewById(R.id.recyclerViewForecast);
        loadingIndicator = findViewById(R.id.loadingCard);
        errorCard = findViewById(R.id.errorCard);
        tvErrorMessage = findViewById(R.id.tvError);
        btnBack = findViewById(R.id.btnBack);
        
        // Setup RecyclerView
        forecastAdapter = new ForecastAdapter();
        recyclerViewForecast.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewForecast.setAdapter(forecastAdapter);
    }
    
    private void initializeServices() {
        weatherApiClient = new WeatherApiClient();
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadForecast();
        });
        
        // Error card retry button
        errorCard.setOnClickListener(v -> {
            hideError();
            loadForecast();
        });
    }
    
    private void getIntentData() {
        Intent intent = getIntent();
        cityName = intent.getStringExtra("city_name");
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);
        useCoordinates = intent.getBooleanExtra("use_coordinates", false);
        
        // Set the location text
        if (cityName != null && !cityName.isEmpty()) {
            tvLocation.setText(cityName);
        } else {
            tvLocation.setText("Current Location");
        }
    }
    
    private void loadForecast() {
        showLoading();
        hideError();
        
        WeatherApiClient.ForecastCallback callback = new WeatherApiClient.ForecastCallback() {
            @Override
            public void onSuccess(ForecastResponse forecast) {
                mainHandler.post(() -> {
                    hideLoading();
                    swipeRefreshLayout.setRefreshing(false);
                    
                    if (forecast != null && forecast.list != null && !forecast.list.isEmpty()) {
                        // Filter to show only one forecast per day (around noon)
                        List<ForecastResponse.ForecastItem> dailyForecasts = filterDailyForecasts(forecast.list);
                        forecastAdapter.updateForecast(dailyForecasts);
                        
                        // Update location if we have city info
                        if (forecast.city != null && forecast.city.name != null) {
                            tvLocation.setText(forecast.city.name + ", " + forecast.city.country);
                        }
                    } else {
                        showError("No forecast data available");
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    hideLoading();
                    swipeRefreshLayout.setRefreshing(false);
                    showError(error);
                });
            }
        };
        
        // Make API call based on available data
        if (useCoordinates && latitude != 0 && longitude != 0) {
            weatherApiClient.getForecastByCoords(latitude, longitude, callback);
        } else if (cityName != null && !cityName.isEmpty()) {
            weatherApiClient.getForecast(cityName, callback);
        } else {
            showError("No location data available");
        }
    }
    
    private List<ForecastResponse.ForecastItem> filterDailyForecasts(List<ForecastResponse.ForecastItem> allForecasts) {
        List<ForecastResponse.ForecastItem> dailyForecasts = new ArrayList<>();
        String lastDate = "";
        
        for (ForecastResponse.ForecastItem item : allForecasts) {
            // Extract date from dt_txt (format: "2023-06-26 12:00:00")
            String currentDate = item.dt_txt.split(" ")[0];
            
            // Take the first forecast of each day (or closest to noon)
            if (!currentDate.equals(lastDate)) {
                dailyForecasts.add(item);
                lastDate = currentDate;
                
                // Limit to 5 days
                if (dailyForecasts.size() >= 5) {
                    break;
                }
            }
        }
        
        return dailyForecasts;
    }
    
    private void showLoading() {
        loadingIndicator.setVisibility(View.VISIBLE);
        recyclerViewForecast.setVisibility(View.GONE);
        errorCard.setVisibility(View.GONE);
    }
    
    private void hideLoading() {
        loadingIndicator.setVisibility(View.GONE);
        recyclerViewForecast.setVisibility(View.VISIBLE);
    }
    
    private void showError(String message) {
        errorCard.setVisibility(View.VISIBLE);
        recyclerViewForecast.setVisibility(View.GONE);
        tvErrorMessage.setText(message);
    }
    
    private void hideError() {
        errorCard.setVisibility(View.GONE);
    }
}
