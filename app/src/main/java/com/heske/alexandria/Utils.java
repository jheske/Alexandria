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

package com.heske.alexandria;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class Utils {

    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static void showToast(Context context,
                                 String message) {
        Toast.makeText(context,
                message,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Hide the keyboard associated with resourceId
     */
    //public static void hideKeyboard(Context context,IBinder windowToken) {
    public static void hideKeyboard(Context context,EditText editText) {
        if (editText == null)
            return;
        InputMethodManager mgr =
                (InputMethodManager) context.getSystemService
                        (Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }


    public static void hideMenuItem(Menu menu, int menuItemId) {
        MenuItem menuItem = menu.findItem(menuItemId);
        if (menuItem != null)
            menuItem.setVisible(false);
    }

    /**
     * Make Utils a utility class by preventing instantiation.
     */
    private Utils() {
        throw new AssertionError();
    }
}
