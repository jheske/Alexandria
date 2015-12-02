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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.heske.alexandria.R;
import com.heske.alexandria.api.AddBookCallback;
import com.heske.alexandria.model.Book;

public class AddBookActivity extends AppCompatActivity implements AddBookCallback {
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        setupToolbar();
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                setResult(Activity.RESULT_OK,new Intent());
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Once the book is added close the Activity.
     * @param book
     */
    @Override
    public void onBookAdded(Book book) {
        setResult(Activity.RESULT_OK,new Intent()
                .putExtra(AddBookFragment.MESSAGE_EXTRA_EAN,book.getTitle()));
        finish();
    }
}
