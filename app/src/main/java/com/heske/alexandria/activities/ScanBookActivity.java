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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanBookActivity extends ActionBarActivity implements ZXingScannerView.ResultHandler {
    private final String TAG = getClass().getSimpleName();
    private ZXingScannerView mScannerView;
    public static final String SCAN_CODE_EXTRA = "SCAN_CODE_EXTRA";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view

        List<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.EAN_13);
        mScannerView.setFormats(formats);
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v(TAG, "Scan raw results: "+ rawResult.getText()); // Prints scan results
        Log.v(TAG, "   ean = " + rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        final Intent intent = new Intent();
        intent.putExtra(SCAN_CODE_EXTRA,rawResult.getText());
        setResult(RESULT_OK,intent);
        finish();
    }
}
