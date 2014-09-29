/*
 * Copyright (C) 2010 ZXing authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jaxbot.glass.barcode.migrated;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

import com.google.android.glass.media.Sounds;
import com.jaxbot.glass.barcode.scan.CaptureActivity;

/**
 * Manages beeps and vibrations for {@link CaptureActivity}.
 */
public final class BeepManager {

    private static final String TAG = BeepManager.class.getSimpleName();

    private final Activity activity;

    public BeepManager(Activity activity) {
        this.activity = activity;

        updatePrefs();
    }

    public synchronized void updatePrefs() {
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public synchronized void playBeepSoundAndVibrate() {
        AudioManager audio = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.SUCCESS);
    }
}

