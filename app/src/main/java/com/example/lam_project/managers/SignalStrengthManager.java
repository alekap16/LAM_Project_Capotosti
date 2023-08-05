package com.example.lam_project.managers;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class SignalStrengthManager {
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 10 * 60 * 1000; //la prima variabile sono i minuti
    private Context context;
    private TelephonyManager telephonyManager;
    private OnSignalStrengthChangeListener signalStrengthChangeListener;

    public SignalStrengthManager(Context context) {
        this.context = context;
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public void requestSignalStrengthUpdates(OnSignalStrengthChangeListener listener) {
        this.signalStrengthChangeListener = listener;
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    public void stopSignalStrengthUpdates() {
        if (signalStrengthChangeListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            signalStrengthChangeListener = null;
        }
    }

    private final PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            int lteSignalStrength = getLteSignalStrength(signalStrength);

            // Get the current time
            long currentTime = System.currentTimeMillis();

            // Check if enough time has passed since the last update
            if (currentTime - lastUpdateTime >= UPDATE_INTERVAL) {
                // Update the last update time
                lastUpdateTime = currentTime;

                if (signalStrengthChangeListener != null) {
                    signalStrengthChangeListener.onSignalStrengthChanged(lteSignalStrength);
                }
            }
        }
    };

    private int getLteSignalStrength(SignalStrength signalStrength) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return signalStrength.getLevel();
        } else {
            try {
                // Note: This method is deprecated in API level 23
                return (int) signalStrength.getClass().getMethod("getLteLevel").invoke(signalStrength);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public interface OnSignalStrengthChangeListener {
        void onSignalStrengthChanged(int signalStrength);
    }
}
