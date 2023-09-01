package com.example.lam_project.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Switch;

import com.example.lam_project.R;

public class NotificationSettingsActivity extends SettingsActivity {
    private Switch notificationSwitch;
    private CheckBox notify10mCheckbox;
    private CheckBox notify100mCheckbox;
    private CheckBox notify1kmCheckbox;
    private CheckBox notifyExpiresCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        // Initialize UI components
        notificationSwitch = findViewById(R.id.notification_switch);
        notify10mCheckbox = findViewById(R.id.notify_10m_checkbox);
        notify100mCheckbox = findViewById(R.id.notify_100m_checkbox);
        notify1kmCheckbox = findViewById(R.id.notify_1km_checkbox);
        notifyExpiresCheckbox = findViewById(R.id.notify_expires_checkbox);

        // Load notification settings from shared preferences and set UI states
        SharedPreferences sharedPreferences = getSharedPreferences("notifications_preferences", Context.MODE_PRIVATE);
        notificationSwitch.setChecked(sharedPreferences.getBoolean("Notification", true));
        notify10mCheckbox.setChecked(sharedPreferences.getBoolean("Notify10m", false));
        notify100mCheckbox.setChecked(sharedPreferences.getBoolean("Notify100m", false));
        notify1kmCheckbox.setChecked(sharedPreferences.getBoolean("Notify1km", false));
        notifyExpiresCheckbox.setChecked(sharedPreferences.getBoolean("NotifyExpires", false));

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("Notification", isChecked);
            editor.apply();
            setCheckboxesEnabled(isChecked);

            if (!isChecked) {
                notify10mCheckbox.setChecked(false);
                notify100mCheckbox.setChecked(false);
                notify1kmCheckbox.setChecked(false);
                notifyExpiresCheckbox.setChecked(false);
            }
        });
        setCheckboxesEnabled(notificationSwitch.isChecked());

        notify10mCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("Notify10m", isChecked).apply();
        });

        notify100mCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("Notify100m", isChecked).apply();
        });

        notify1kmCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("Notify1km", isChecked).apply();
        });

        notifyExpiresCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("NotifyExpires", isChecked).apply();
        });
    }

    private void setCheckboxesEnabled(boolean enabled) {

        notify10mCheckbox.setEnabled(enabled);
        notify100mCheckbox.setEnabled(enabled);
        notify1kmCheckbox.setEnabled(enabled);
        notifyExpiresCheckbox.setEnabled(enabled);

        int textColor = enabled ? android.R.color.black : android.R.color.darker_gray;
        int backgroundResource = enabled ? android.R.drawable.btn_default : android.R.color.transparent;

        notify10mCheckbox.setTextColor(getResources().getColor(textColor));
        notify100mCheckbox.setTextColor(getResources().getColor(textColor));
        notify1kmCheckbox.setTextColor(getResources().getColor(textColor));
        notifyExpiresCheckbox.setTextColor(getResources().getColor(textColor));

        notify10mCheckbox.setBackgroundResource(backgroundResource);
        notify100mCheckbox.setBackgroundResource(backgroundResource);
        notify1kmCheckbox.setBackgroundResource(backgroundResource);
        notifyExpiresCheckbox.setBackgroundResource(backgroundResource);


    }
    @Override
    protected void onStop() {
        super.onStop();

        // Save checkbox states to shared preferences when the activity is stopped
        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit();
        editor.putBoolean("Notify10m", notify10mCheckbox.isChecked());
        editor.putBoolean("Notify100m", notify100mCheckbox.isChecked());
        editor.putBoolean("Notify1km", notify1kmCheckbox.isChecked());
        editor.putBoolean("NotifyExpires", notifyExpiresCheckbox.isChecked());
        editor.apply();
    }
}
