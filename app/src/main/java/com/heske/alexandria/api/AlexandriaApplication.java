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

package com.heske.alexandria.api;

import android.app.Application;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import com.heske.alexandria.Utils;
import retrofit.JacksonConverterFactory;
import retrofit.Retrofit;

public class AlexandriaApplication extends Application {
   // http://gatherer.wizards.com/Handlers/Image.ashx?type=card&multiverseid=397690

    //End with / as per recommendation at
    //http://inthecheesefactory.com/blog/retrofit-2.0/en
    private static final String BOOKAPI_BASE_URL = "https://www.googleapis.com/books/v1/";


    /**
     * API key query parameter name.
     * This has to be appended to every request.
     * Its value is in ApiKey.java, which is not
     * in my git repo and thus will never
     * be shared publicly
     */
    private final String TAG = getClass().getSimpleName();
    private BookApiService mApiService;
    private boolean mIsNetworkAvailable=false;
    private boolean isDebug;
    private Retrofit mRetrofit;

    /**
     * Create the Retrofit object
     * See https://snow.dog/blog/make-life-easier-retrofit/
     * for error handling code.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mIsNetworkAvailable = Utils.isNetworkAvailable(getApplicationContext());
        setupRestAdapter();
    }

    private void setupRestAdapter() {
        OkHttpClient client = new OkHttpClient();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.interceptors().add(interceptor);

        mRetrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/books/")
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client)
                .build();

        mApiService = mRetrofit.create(BookApiService.class);
    }

    public boolean isNetworkAvailable() {
        return mIsNetworkAvailable;
    }


    /**
     * Proxy to the service.
     * There are other proxies I can add later,
     * like Search and TV.
     * <p/>
     * This should be called one time only in the app
     * and then referenced from other parts of the app.
     */
    public BookApiService getApiService() {
        return mApiService;
    }
}
