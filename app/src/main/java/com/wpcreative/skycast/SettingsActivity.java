package com.wpcreative.skycast;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wpcreative.skycast.database.SettingsDbHelper;

public class SettingsActivity extends AppCompatActivity {
    
    // UI Components
    private ImageView btnBack;
    private TextView tvCurrentApiKey;
    private TextView tvApiKeyName;
    private Button btnAddApiKey;
    private Button btnEditApiKey;
    private Button btnRemoveApiKey;
    private CardView cardApiKeyInfo;
    private LinearLayout layoutNoApiKey;
    
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
        tvCurrentApiKey = findViewById(R.id.tvCurrentApiKey);
        tvApiKeyName = findViewById(R.id.tvApiKeyName);
        btnAddApiKey = findViewById(R.id.btnAddApiKey);
        btnEditApiKey = findViewById(R.id.btnEditApiKey);
        btnRemoveApiKey = findViewById(R.id.btnRemoveApiKey);
        cardApiKeyInfo = findViewById(R.id.cardApiKeyInfo);
        layoutNoApiKey = findViewById(R.id.layoutNoApiKey);
    }
    
    private void initializeDatabase() {
        dbHelper = new SettingsDbHelper(this);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAddApiKey.setOnClickListener(v -> showAddApiKeyDialog());
        btnEditApiKey.setOnClickListener(v -> showEditApiKeyDialog());
        btnRemoveApiKey.setOnClickListener(v -> showRemoveApiKeyConfirmation());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add OpenWeatherMap API Key");
        builder.setMessage("Enter your OpenWeatherMap API key to use your own quota:");
        
        // Create input layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        // API Key Name input
        EditText etKeyName = new EditText(this);
        etKeyName.setHint("API Key Name (optional)");
        etKeyName.setText("My API Key");
        layout.addView(etKeyName);
        
        // API Key input
        EditText etApiKey = new EditText(this);
        etApiKey.setHint("API Key (32 characters)");
        etApiKey.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etApiKey);
        
        builder.setView(layout);
        
        builder.setPositiveButton("Save", (dialog, which) -> {
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
        });
        
        builder.setNegativeButton("Cancel", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void showEditApiKeyDialog() {
        String currentApiKey = dbHelper.getSetting(SettingsDbHelper.SETTING_API_KEY, "");
        String currentKeyName = dbHelper.getSetting(SettingsDbHelper.SETTING_API_KEY_NAME, "My API Key");
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit API Key");
        builder.setMessage("Update your OpenWeatherMap API key:");
        
        // Create input layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        // API Key Name input
        EditText etKeyName = new EditText(this);
        etKeyName.setHint("API Key Name");
        etKeyName.setText(currentKeyName);
        layout.addView(etKeyName);
        
        // API Key input
        EditText etApiKey = new EditText(this);
        etApiKey.setHint("API Key (32 characters)");
        etApiKey.setText(currentApiKey);
        etApiKey.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etApiKey);
        
        builder.setView(layout);
        
        builder.setPositiveButton("Update", (dialog, which) -> {
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
        });
        
        builder.setNegativeButton("Cancel", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void showRemoveApiKeyConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
