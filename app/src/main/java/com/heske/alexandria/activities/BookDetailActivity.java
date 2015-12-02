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
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.heske.alexandria.R;
import com.heske.alexandria.Utils;
import com.heske.alexandria.api.BookDetailCallback;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BookDetailActivity extends AppCompatActivity implements BookDetailCallback {
    private final String TAG = "BookDetailFragment";
    private String mEan;
    private Toolbar mToolbar;
    private BookDetailFragment mBookDetailFragment = null;
    private FloatingActionButton fabShare;
    private FloatingActionButton fabDelete;

    @Nullable
    @Bind(R.id.collapsing_toolbar)
    public CollapsingToolbarLayout collapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        ButterKnife.bind(this);

        setupToolbar();
        setupFabs();
        loadBackdrop();
        mEan = getIntent().getStringExtra(BookDetailFragment.EAN_KEY);
        mBookDetailFragment = (BookDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_full_book);
        mBookDetailFragment.displayBook(mEan);
        collapsingToolbar.setTitle(mBookDetailFragment.getBookTitle());

    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void setupFabs() {
        fabShare = (FloatingActionButton) findViewById(R.id.fab_share);

        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = mBookDetailFragment.getShareIntent();
                startActivity(Intent.createChooser(shareIntent,"Share Book"));
            }
        });

        fabDelete = (FloatingActionButton) findViewById(R.id.fab_delete);
        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBookDetailFragment.deleteBook();
            }
        });
    }

    private void loadBackdrop() {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        Glide.with(this).load(R.drawable.toolbar_backdrop).centerCrop().into(imageView);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //This Activity has a FAB button for share
        Utils.hideMenuItem(menu,R.id.action_share);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_share:
                // Defer to Fragment menu
                return false;
            case R.id.action_delete_book:
                mBookDetailFragment.deleteBook();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }

    /**
     * Once book has been deleted the Activity closes.
     */
    @Override
    public void onBookDeleted() {
        finish();
    }
}
