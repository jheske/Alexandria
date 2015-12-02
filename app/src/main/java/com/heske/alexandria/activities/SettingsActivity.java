/*
 * <!--
 *   ~ Copyright (C) 2015 The Android Open Source Project
 *   ~
 *   ~ Licensed under the Apache License, Version 2.0 (the "License");
 *   ~ you may not use this file except in compliance with the License.
 *   ~ You may obtain a copy of the License at
 *   ~
 *   ~      http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~ Unless required by applicable law or agreed to in writing, software
 *   ~ distributed under the License is distributed on an "AS IS" BASIS,
 *   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   ~ See the License for the specific language governing permissions and
 *   ~ limitations under the License.
 * -->
 */

package com.heske.alexandria.activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import com.heske.alexandria.R;

public class SettingsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        android.support.v7.widget.Toolbar toolbar =
                (android.support.v7.widget.Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        //Set to true to show the Up button and set com to return up by a single level
        //in your UI rather than back to the top level or front page.
        //Note that false means the button won't show up at all??? I guess I don't care.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_settings_container,
                        new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}

