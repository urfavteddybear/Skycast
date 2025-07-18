package com.wpcreative.skycast;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.wpcreative.skycast.database.SettingsDbHelper;
import com.wpcreative.skycast.utils.TemperatureUtils;

public class SettingsActivity extends AppCompatActivity {
    
    // UI Components
    private ImageView btnBack;
    private ImageView btnApiInfo;
    private TextView tvCurrentApiKey;
    private TextView tvApiKeyName;
    private Button btnAddApiKey;
    private Button btnEditApiKey;
    private Button btnRemoveApiKey;
    private CardView cardApiKeyInfo;
    private LinearLayout layoutNoApiKey;
    
    // Temperature Unit Components
    private RadioGroup radioGroupTemperatureUnit;
    private RadioButton radioCelsius;
    private RadioButton radioFahrenheit;
    private RadioButton radioKelvin;
    
    // Database
    private SettingsDbHelper dbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        
        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settingsMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initializeViews();
        initializeDatabase();
        setupClickListeners();
        updateUI();
    }
    
    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnApiInfo = findViewById(R.id.btnApiInfo);
        tvCurrentApiKey = findViewById(R.id.tvCurrentApiKey);
        tvApiKeyName = findViewById(R.id.tvApiKeyName);
        btnAddApiKey = findViewById(R.id.btnAddApiKey);
        btnEditApiKey = findViewById(R.id.btnEditApiKey);
        btnRemoveApiKey = findViewById(R.id.btnRemoveApiKey);
        cardApiKeyInfo = findViewById(R.id.cardApiKeyInfo);
        layoutNoApiKey = findViewById(R.id.layoutNoApiKey);
        
        // Temperature unit components
        radioGroupTemperatureUnit = findViewById(R.id.radioGroupTemperatureUnit);
        radioCelsius = findViewById(R.id.radioCelsius);
        radioFahrenheit = findViewById(R.id.radioFahrenheit);
        radioKelvin = findViewById(R.id.radioKelvin);
    }
    
    private void initializeDatabase() {
        dbHelper = new SettingsDbHelper(this);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnApiInfo.setOnClickListener(v -> showApiInfoDialog());
        btnAddApiKey.setOnClickListener(v -> showAddApiKeyDialog());
        btnEditApiKey.setOnClickListener(v -> showEditApiKeyDialog());
        btnRemoveApiKey.setOnClickListener(v -> showRemoveApiKeyConfirmation());
        
        // Setup temperature unit listener
        setupTemperatureUnitListener();
    }
    
    private void updateUI() {
        String apiKey = dbHelper.getSetting(SettingsDbHelper.SETTING_API_KEY, null);
        String apiKeyName = dbHelper.getSetting(SettingsDbHelper.SETTING_API_KEY_NAME, "My API Key");
        
        if (apiKey != null && !apiKey.isEmpty()) {
            // Show API key info
            cardApiKeyInfo.setVisibility(View.VISIBLE);
            layoutNoApiKey.setVisibility(View.GONE);
            
            // Mask the API key for security (show only first 8 and last 4 characters)
            String maskedKey = maskApiKey(apiKey);
            tvCurrentApiKey.setText(maskedKey);
            tvApiKeyName.setText(apiKeyName);
        } else {
            // Show no API key state
            cardApiKeyInfo.setVisibility(View.GONE);
            layoutNoApiKey.setVisibility(View.VISIBLE);
        }
        
        // Update temperature unit selection
        updateTemperatureUnitSelection();
    }
    
    private void updateTemperatureUnitSelection() {
        String currentUnit = dbHelper.getSetting(SettingsDbHelper.SETTING_TEMPERATURE_UNIT, TemperatureUtils.UNIT_METRIC);
        
        // Temporarily disable the listener to avoid triggering it while setting the selection
        radioGroupTemperatureUnit.setOnCheckedChangeListener(null);
        
        switch (currentUnit) {
            case TemperatureUtils.UNIT_METRIC:
                radioCelsius.setChecked(true);
                break;
            case TemperatureUtils.UNIT_IMPERIAL:
                radioFahrenheit.setChecked(true);
                break;
            case TemperatureUtils.UNIT_KELVIN:
                radioKelvin.setChecked(true);
                break;
            default:
                radioCelsius.setChecked(true); // Default to Celsius
                break;
        }
        
        // Re-enable the listener
        setupTemperatureUnitListener();
    }
    
    private void setupTemperatureUnitListener() {
        radioGroupTemperatureUnit.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedUnit;
            if (checkedId == R.id.radioCelsius) {
                selectedUnit = TemperatureUtils.UNIT_METRIC;
            } else if (checkedId == R.id.radioFahrenheit) {
                selectedUnit = TemperatureUtils.UNIT_IMPERIAL;
            } else if (checkedId == R.id.radioKelvin) {
                selectedUnit = TemperatureUtils.UNIT_KELVIN;
            } else {
                return; // Invalid selection
            }
            
            // Save the selected temperature unit
            dbHelper.saveSetting(SettingsDbHelper.SETTING_TEMPERATURE_UNIT, selectedUnit);
            Toast.makeText(this, "Temperature unit updated", Toast.LENGTH_SHORT).show();
        });
    }
    
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 12) {
            return "••••••••••••••••";
        }
        
        String start = apiKey.substring(0, 8);
        String end = apiKey.substring(apiKey.length() - 4);
        return start + "••••••••••••" + end;
    }
    
    private void showAddApiKeyDialog() {
        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_api_key, null);
        
        // Get references to input fields
        TextInputEditText etKeyName = dialogView.findViewById(R.id.etKeyName);
        TextInputEditText etApiKey = dialogView.findViewById(R.id.etApiKey);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        
        // Create dialog with custom theme
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        
        // Set transparent background to avoid white corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        // Set button listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSave.setOnClickListener(v -> {
            String keyName = etKeyName.getText().toString().trim();
            String apiKey = etApiKey.getText().toString().trim();
            
            if (apiKey.isEmpty()) {
                Toast.makeText(this, "Please enter an API key", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (apiKey.length() != 32) {
                Toast.makeText(this, "API key must be 32 characters long", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (keyName.isEmpty()) {
                keyName = "My API Key";
            }
            
            // Save to database
            dbHelper.saveSetting(SettingsDbHelper.SETTING_API_KEY, apiKey);
            dbHelper.saveSetting(SettingsDbHelper.SETTING_API_KEY_NAME, keyName);
            
            Toast.makeText(this, "API key saved successfully", Toast.LENGTH_SHORT).show();
            updateUI();
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    private void showEditApiKeyDialog() {
        String currentApiKey = dbHelper.getSetting(SettingsDbHelper.SETTING_API_KEY, "");
        String currentKeyName = dbHelper.getSetting(SettingsDbHelper.SETTING_API_KEY_NAME, "My API Key");
        
        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_api_key, null);
        
        // Get references to input fields
        TextInputEditText etKeyName = dialogView.findViewById(R.id.etKeyName);
        TextInputEditText etApiKey = dialogView.findViewById(R.id.etApiKey);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);
        
        // Pre-fill with current values
        etKeyName.setText(currentKeyName);
        etApiKey.setText(currentApiKey);
        
        // Create dialog with custom theme
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        
        // Set transparent background to avoid white corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        // Set button listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnUpdate.setOnClickListener(v -> {
            String keyName = etKeyName.getText().toString().trim();
            String apiKey = etApiKey.getText().toString().trim();
            
            if (apiKey.isEmpty()) {
                Toast.makeText(this, "Please enter an API key", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (apiKey.length() != 32) {
                Toast.makeText(this, "API key must be 32 characters long", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (keyName.isEmpty()) {
                keyName = "My API Key";
            }
            
            // Update in database
            dbHelper.saveSetting(SettingsDbHelper.SETTING_API_KEY, apiKey);
            dbHelper.saveSetting(SettingsDbHelper.SETTING_API_KEY_NAME, keyName);
            
            Toast.makeText(this, "API key updated successfully", Toast.LENGTH_SHORT).show();
            updateUI();
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    private void showRemoveApiKeyConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.StandardDialogTheme);
        builder.setTitle("Remove API Key");
        builder.setMessage("Are you sure you want to remove your API key? The app will use the default API key.");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        
        builder.setPositiveButton("Remove", (dialog, which) -> {
            dbHelper.deleteSetting(SettingsDbHelper.SETTING_API_KEY);
            dbHelper.deleteSetting(SettingsDbHelper.SETTING_API_KEY_NAME);
            
            Toast.makeText(this, "API key removed successfully", Toast.LENGTH_SHORT).show();
            updateUI();
        });
        
        builder.setNegativeButton("Cancel", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void showApiInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.StandardDialogTheme);
        builder.setTitle("About API Keys");
        builder.setMessage("• Get your free API key at openweathermap.org\n" +
                          "• Free tier: 1,000 calls/day\n" +
                          "• Your API key is stored securely on your device\n" +
                          "• Replace the default key to avoid rate limits");
        builder.setPositiveButton("Got it", null);
        builder.setIcon(R.drawable.ic_info);
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
