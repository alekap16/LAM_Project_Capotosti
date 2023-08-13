package com.example.lam_project.managers;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;

public class AcousticNoiseManager {
    private MediaRecorder mRecorder;
    private boolean mIsRecording = false;

    public void startRecording(Context context, final NoiseLevelCallback callback) {
        if (mIsRecording) {
            return;
        }

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile("/dev/null");

        try {
            mRecorder.prepare();
            mRecorder.start();
            mIsRecording = true;

            // Schedule a task to update the noise level every second
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mIsRecording) {
                        double amplitude = mRecorder.getMaxAmplitude();
                        double noiseLevel = 20 * Math.log10(amplitude / 32767.0);
                        callback.onNoiseLevelMeasured(noiseLevel);
                        handler.postDelayed(this, 1000); // Update every second
                    }
                }
            }, 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (mIsRecording) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            mIsRecording = false;
        }
    }

    public interface NoiseLevelCallback {
        void onNoiseLevelMeasured(double noiseLevel);
    }
}