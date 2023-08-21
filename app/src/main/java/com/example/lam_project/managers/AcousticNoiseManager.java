package com.example.lam_project.managers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

public class AcousticNoiseManager {
    private boolean mIsRecording = false;
    private AudioRecord mAudioRecord;

    public interface NoiseLevelCallback {
        void onNoiseLevelMeasured(double noiseLevelInDb);
    }

    public void startRecording(Context context, final NoiseLevelCallback callback) {
        if (mIsRecording) {
            return;
        }

        int sampleRate = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mAudioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
        );

        if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            // Handle initialization error
            return;
        }

        mAudioRecord.startRecording();
        mIsRecording = true;
        final double humanThresholdDb = 60.0;

        final double offsetAmplitude = -22.0;

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsRecording) {
                    short[] audioBuffer = new short[bufferSize];
                    mAudioRecord.read(audioBuffer, 0, bufferSize);

                    double rms = 0;
                    for (short sample : audioBuffer) {
                        rms += sample * sample;
                    }
                    rms = Math.sqrt(rms / bufferSize);


                    double amplitude = (rms / Math.sqrt(2)) - offsetAmplitude;

                    double noiseLevelInDb = 20 * Math.log10(amplitude) - humanThresholdDb;

                    callback.onNoiseLevelMeasured(noiseLevelInDb);

                    handler.postDelayed(this, 1000); // Update
                }
            }
        }, 1000);
    }

    public void stopRecording() {
        if (mIsRecording) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
            mIsRecording = false;
        }
    }
}
