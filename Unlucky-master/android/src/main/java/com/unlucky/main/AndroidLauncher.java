package com.unlucky.main;

import android.media.AudioManager;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.unlucky.main.Unlucky;

/**
 * Android version access
 */
public class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // AUDIO FIX: Set volume control stream to MUSIC for proper volume handling
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        config.useAccelerometer = false;
        config.useCompass = false;
        
        // AUDIO FIX: These settings can significantly impact audio volume and quality
        config.useWakelock = false;
        config.useGL30 = false; // Some devices have audio issues with GL30
        config.numSamples = 0; // Disable anti-aliasing which can interfere with audio
        
        // Audio-related optimizations
        config.disableAudio = false; // Make sure audio is explicitly enabled
        
        initialize(new Unlucky(), config);
    }

}