package com.example.lam_project;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.lam_project.managers.DatabaseManager;
import com.example.lam_project.managers.SettingsManager;

public class SettingsActivity extends Activity {
    private Switch autoScanSwitch;
    private SeekBar minutesSeekBar;
    private SeekBar measurementsSeekBar;
    private TextView measurementsLabel;
    private TextView minutesLabel;
    private Button dumpDatabaseButton;
    private Button deleteDataButton;
    private long selectedMinutes;
    private int selectedMeasurements;
    private SettingsManager settingsManager;
    private DatabaseManager databaseManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        autoScanSwitch = findViewById(R.id.autoScanSwitch);
        minutesSeekBar = findViewById(R.id.minutesSeekBar);
        measurementsSeekBar = findViewById(R.id.measurementsSeekBar);
        measurementsLabel = findViewById(R.id.measurementsLabel);
        minutesLabel = findViewById(R.id.minutesLabel);
        dumpDatabaseButton = findViewById(R.id.dumpDatabaseButton);
        deleteDataButton = findViewById(R.id.deleteDataButton);

        databaseManager = new DatabaseManager(this);
        settingsManager = new SettingsManager(this);

        minutesSeekBar.setProgress((int) (settingsManager.getSelectedMinutes() - 5));
        measurementsSeekBar.setProgress(settingsManager.getSelectedMeasurements() - 1);

        minutesLabel.setText("Minutes for new measurements: " + settingsManager.getSelectedMinutes());
        measurementsLabel.setText("Number of measurements: " + settingsManager.getSelectedMeasurements());

        autoScanSwitch.setChecked(settingsManager.isAutoScanEnabled());

        TextView notificationSettingsButton = findViewById(R.id.notification_settings_button);

        notificationSettingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationSettingsActivity.class);
            startActivity(intent);
        });

        autoScanSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setAutoScanEnabled(isChecked);
        });
        minutesSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedMinutes = progress + 5; // Adjust to the range [5, 30]
                minutesLabel.setText("Minutes for new measurements: " + selectedMinutes);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                long selectedMinutes = seekBar.getProgress() + 5;
                settingsManager.setSelectedMinutes(selectedMinutes);
            }
        });


        measurementsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedMeasurements = progress + 1; // Adjust to the range [1, 100]
                measurementsLabel.setText("Number of measurements: " + selectedMeasurements);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int selectedMeasurements = seekBar.getProgress() + 1;
                settingsManager.setSelectedMeasurements(selectedMeasurements);
            }
        });

        dumpDatabaseButton.setOnClickListener(v -> databaseManager.showDumpDatabaseDialog
                (SettingsActivity.this));

        deleteDataButton.setOnClickListener(v -> databaseManager.showEraseConfirmationDialog
                (SettingsActivity.this));
    }


    // I need this for later usages
    private long convertMinutesToMilliseconds(long minutes) {
        return minutes * 60 * 1000; // Convert minutes to milliseconds
    }
}

