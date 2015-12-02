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

package com.heske.alexandria.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.heske.alexandria.activities.AddBookFragment;
import com.heske.alexandria.R;
import com.heske.alexandria.Utils;
import com.heske.alexandria.data.AlexandriaContract;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {
    private final String TAG = BookService.class.getSimpleName();
    public static final String FETCH_BOOK = "com.heske.alexandria.services.action.FETCH_BOOK";
    public static final String DELETE_BOOK = "com.heske.alexandria.services.action.DELETE_BOOK";

    public static final String EAN = "com.heske.alexandria.services.extra.EAN";

    public BookService() {
        super("Alexandria");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_BOOK.equals(action)) {
                if (!Utils.isNetworkAvailable(this)) {
                    sendBroadcastMessage(AddBookFragment.MESSAGE_ERROR,
                            getString(R.string.error_no_network));
                }
                else {
                    final String ean = intent.getStringExtra(EAN);
                    fetchBook(ean);
                }
            } else if (DELETE_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                deleteBook(ean);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String ean) {
        if(ean!=null) {
            getContentResolver()
                    .delete(AlexandriaContract
                            .BookEntry.buildBookUri(Long.parseLong(ean)), null, null);
        }
    }


    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     */
    private void fetchBook(String ean) {

        if(ean.length()!= 13){
            Log.d(TAG, "Invalid code: " + ean);
            sendBroadcastMessage(AddBookFragment.MESSAGE_ERROR,
                    ean + " " +  getResources().getString(R.string.error_code_not_supported));
            return;
        }
        if (existsInDatabase(ean))
            return;

        lookupBookOnGoogle(ean);
    }

    private boolean existsInDatabase(String ean) {
        Log.d(TAG, "Querying database for " + ean);
        Cursor bookEntry = getContentResolver().query(
                AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if(bookEntry.getCount()>0){
            bookEntry.moveToFirst();
            Log.d(TAG, bookEntry.getString(AlexandriaContract.TITLE_COL) + " is already in the database ");
            sendBroadcastMessage(AddBookFragment.MESSAGE_ERROR
                    ,ean + " " +  getResources().getString(R.string.error_duplicate_book));
            bookEntry.close();
            return true;
        }
        bookEntry.close();
        return false;
    }

    private void lookupBookOnGoogle(String ean) {
        String bookJsonString = getBookJsonString(ean);
        if (bookJsonString == null) {
            Log.d(TAG, "bookJsonString is NULL");
            sendBroadcastMessage(AddBookFragment.MESSAGE_ERROR,
                    ean + " " +  getResources().getString(R.string.error_lookup_empty_data));
            return;
        }
        processBookJsonString(ean, bookJsonString);
    }

    private String getBookJsonString(String ean) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJsonString = null;

        try {
            final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
            final String QUERY_PARAM = "q";

            final String ISBN_PARAM = "isbn:" + ean;

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                    .build();

            URL url = new URL(builtUri.toString());

            Log.i(TAG,"Fetching book " + url);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            //Works (I have all but last the one), but they seem to come and go
            //https://www.googleapis.com/books/v1/volumes?q=isbn:9780553293371
            //https://www.googleapis.com/books/v1/volumes?q=isbn:9780765365286
            //https://www.googleapis.com/books/v1/volumes?q=isbn:9780679747048
            //https://www.googleapis.com/books/v1/volumes?q=isbn:9780553293357
            //https://www.googleapis.com/books/v1/volumes?q=isbn:9780735619678
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            bookJsonString = buffer.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
        return bookJsonString;
    }

    private boolean processBookJsonString(String ean, String bookJsonString) {
        final String ITEMS = "items";
        final String VOLUME_INFO = "volumeInfo";
        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String DESC = "description";
        final String CATEGORIES = "categories";
        final String IMG_URL_PATH = "imageLinks";
        final String IMG_URL = "thumbnail";

        try {
            JSONObject bookJson = new JSONObject(bookJsonString);
            JSONArray bookArray;
            if(bookJson.has(ITEMS)){
                bookArray = bookJson.getJSONArray(ITEMS);
            }else{
                sendBroadcastMessage(AddBookFragment.MESSAGE_ERROR,
                        ean + " " + getResources().getString(R.string.error_book_not_found));
                return false;
            }

            JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

            String title = bookInfo.getString(TITLE);

            String subtitle = "";
            if(bookInfo.has(SUBTITLE)) {
                subtitle = bookInfo.getString(SUBTITLE);
            }

            String desc="";
            if(bookInfo.has(DESC)){
                desc = bookInfo.getString(DESC);
            }

            String imgUrl = "";
            if(bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
            }

            writeBackBook(ean, title, subtitle, desc, imgUrl);

            if(bookInfo.has(AUTHORS)) {
                writeBackAuthors(ean, bookInfo.getJSONArray(AUTHORS));
            }
            if(bookInfo.has(CATEGORIES)){
                writeBackCategories(ean,bookInfo.getJSONArray(CATEGORIES) );
            }
            Intent intent = new Intent(AddBookFragment.MESSAGE_ADD_BOOK);
            intent.putExtra(AddBookFragment.MESSAGE_EXTRA_EAN,ean);
            intent.putExtra(AddBookFragment.MESSAGE_EXTRA_TITLE,title);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } catch (JSONException e) {
            Log.e(TAG, "Error ", e);
            return false;
        }
        return true;
    }

    private void sendBroadcastMessage(String action, String message) {
        Intent intent = new Intent(action);
        intent.putExtra(AddBookFragment.MESSAGE_EXTRA_ERROR,message);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void writeBackBook(String ean, String title, String subtitle, String desc, String imgUrl) {
        ContentValues values= new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);
        getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI, values);
    }

    private void writeBackAuthors(String ean, JSONArray jsonArray) throws JSONException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, jsonArray.getString(i));
            getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }

    private void writeBackCategories(String ean, JSONArray jsonArray) throws JSONException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, jsonArray.getString(i));
            getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }
 }