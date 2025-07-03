package com.wpcreative.skycast.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wpcreative.skycast.R;
import com.wpcreative.skycast.database.SettingsDbHelper;
import com.wpcreative.skycast.model.ForecastResponse;
import com.wpcreative.skycast.utils.TemperatureUtils;
import com.wpcreative.skycast.utils.WeatherUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {
    
    private List<ForecastResponse.ForecastItem> forecastItems = new ArrayList<>();
    private Context context;
    
    public ForecastAdapter(Context context) {
        this.context = context;
    }
    
    public void updateForecast(List<ForecastResponse.ForecastItem> newItems) {
        this.forecastItems.clear();
        this.forecastItems.addAll(newItems);
        notifyDataSetChanged();
    }
    
    private String getCurrentTemperatureUnit() {
        SettingsDbHelper dbHelper = new SettingsDbHelper(context);
        String unit = dbHelper.getSetting(SettingsDbHelper.SETTING_TEMPERATURE_UNIT, TemperatureUtils.UNIT_METRIC);
        dbHelper.close();
        return unit;
    }
    
    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast, parent, false);
        return new ForecastViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastResponse.ForecastItem item = forecastItems.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return forecastItems.size();
    }
    
    class ForecastViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvForecastDate;
        private final TextView tvForecastTime;
        private final TextView tvWeatherIcon;  // Changed from ImageView to TextView
        private final TextView tvTemperature;
        private final TextView tvWeatherDescription;
        private final TextView tvHumidity;
        private final TextView tvWindSpeed;
        
        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvForecastDate = itemView.findViewById(R.id.tvForecastDate);
            tvForecastTime = itemView.findViewById(R.id.tvForecastTime);
            tvWeatherIcon = itemView.findViewById(R.id.tvWeatherIcon);  // Changed from ivWeatherIcon
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            tvWeatherDescription = itemView.findViewById(R.id.tvWeatherDescription);
            tvHumidity = itemView.findViewById(R.id.tvHumidity);
            tvWindSpeed = itemView.findViewById(R.id.tvWindSpeed);
        }
        
        public void bind(ForecastResponse.ForecastItem item) {
            try {
                // Format date and time
                Date date = new Date(item.dt * 1000);
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                
                tvForecastDate.setText(dateFormat.format(date));
                tvForecastTime.setText(timeFormat.format(date));
                
                // Temperature
                String tempUnit = ForecastAdapter.this.getCurrentTemperatureUnit();
                tvTemperature.setText(TemperatureUtils.formatTemperature(item.main.temp, tempUnit));
                
                // Weather description
                if (item.weather != null && !item.weather.isEmpty()) {
                    String description = WeatherUtils.capitalizeFirstLetter(item.weather.get(0).description);
                    tvWeatherDescription.setText(description);
                    
                    // Weather icon - use emoji from WeatherUtils (consistent with MainActivity)
                    String iconCode = item.weather.get(0).icon;
                    String weatherEmoji = WeatherUtils.getWeatherIcon(iconCode);
                    tvWeatherIcon.setText(weatherEmoji);
                }
                
                // Humidity
                tvHumidity.setText(String.format(Locale.getDefault(), "%d%%", item.main.humidity));
                
                // Wind speed
                tvWindSpeed.setText(String.format(Locale.getDefault(), "%.0f km/h", item.wind.speed * 3.6)); // Convert m/s to km/h
                
            } catch (Exception e) {
                // Fallback values in case of any parsing errors
                String tempUnit = ForecastAdapter.this.getCurrentTemperatureUnit();
                tvForecastDate.setText("--");
                tvForecastTime.setText("--");
                tvTemperature.setText("--" + TemperatureUtils.getTemperatureSymbol(tempUnit));
                tvWeatherDescription.setText("--");
                tvHumidity.setText("--%");
                tvWindSpeed.setText("-- km/h");
            }
        }
    }
}
