package com.example.lam_project;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.lam_project.managers.SettingsManager;

public class SettingsActivity extends Activity {
    private Switch notificationSwitch;
    private SeekBar minutesSeekBar;
    private SeekBar measurementsSeekBar;
    private TextView measurementsLabel;
    private TextView minutesLabel;
    private Button dumpDatabaseButton;
    private Button deleteDataButton;

    private boolean isNotificationEnabled;
    private long selectedMinutes;
    private int selectedMeasurements;
    private SettingsManager settingsManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        notificationSwitch = findViewById(R.id.notificationSwitch);
        minutesSeekBar = findViewById(R.id.minutesSeekBar);
        measurementsSeekBar = findViewById(R.id.measurementsSeekBar);
        measurementsLabel = findViewById(R.id.measurementsLabel);
        minutesLabel = findViewById(R.id.minutesLabel);
        dumpDatabaseButton = findViewById(R.id.dumpDatabaseButton);
        deleteDataButton = findViewById(R.id.deleteDataButton);

        settingsManager = new SettingsManager(this);

        notificationSwitch.setChecked(settingsManager.isNotificationEnabled());
        minutesSeekBar.setProgress((int) (settingsManager.getSelectedMinutes() - 5));
        measurementsSeekBar.setProgress(settingsManager.getSelectedMeasurements() - 1);

        minutesLabel.setText("Minutes for new measurements: " + settingsManager.getSelectedMinutes());
        measurementsLabel.setText("Number of measurements: " + settingsManager.getSelectedMeasurements());

        notificationSwitch.setChecked(settingsManager.isNotificationEnabled());

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setNotificationEnabled(isChecked);
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

        dumpDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        deleteDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    // I need this for later usages
    private long convertMinutesToMilliseconds(long minutes) {
        return minutes * 60 * 1000; // Convert minutes to milliseconds
    }
}

