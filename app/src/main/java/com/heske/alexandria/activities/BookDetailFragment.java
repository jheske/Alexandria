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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.heske.alexandria.api.BookDetailCallback;
import com.heske.alexandria.data.DbUtils;
import com.heske.alexandria.model.Book;
import com.squareup.picasso.Picasso;

import com.heske.alexandria.R;
import com.heske.alexandria.data.AlexandriaContract;

import butterknife.Bind;
import butterknife.ButterKnife;


public class BookDetailFragment extends Fragment {

    private final String TAG = "BookDetailFragment";
    public static final String EAN_KEY = "EAN";
    private View rootView;
    boolean mBookDeleted = false;
    Context mActivity = null;
    Book mBook = null;
    private ShareActionProvider mShareActionProvider;
    private BookDetailCallback mCallback = null;

    @Bind(R.id.tv_book_title)
    public TextView tvBookTitle;
    @Bind(R.id.tv_book_description)
    public TextView tvBookDesc;
    @Bind(R.id.tv_authors)
    public TextView tvAuthors;
    @Bind(R.id.tv_categories)
    public TextView tvCategories;
    @Bind(R.id.tv_isbn)
    public TextView tvISBN;
    @Bind(R.id.img_book_cover)
    public ImageView imgBookCover;

    public BookDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_book_detail, container, false);
        ButterKnife.bind(this,rootView);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = context;

        // The hosting Activity must implement
        // Callback interface.
        try {
            mCallback = (BookDetailCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + getResources().getString(R.string.errorMissingCallbackMethod)
                    + mCallback.getClass().getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public String getBookTitle() {
        if (mBook != null)
            return mBook.getTitle();
        return "";
    }

    public void displayBook(String ean) {
        mBook = DbUtils.dbLookupBook(mActivity, ean);
        if (mBook == null)
            return;

        if (mShareActionProvider != null)
            mShareActionProvider.setShareIntent(getShareIntent());

        String bookSubTitle = mBook.getSubtitle();
        if (bookSubTitle.isEmpty())
            tvBookTitle.setText(mBook.getTitle());
        else
            tvBookTitle.setText(mBook.getTitle() + ": " + mBook.getSubtitle());
        tvBookDesc.setText(mBook.getDescription());
        String authors = mBook.getAuthors().get(0);
        tvAuthors.setText(authors);
        StringBuilder strISBN = new StringBuilder(mBook.getBookId());
        strISBN.insert(3, "-");
        tvISBN.setText("ISBN-13: " + strISBN);
        tvCategories.setText(mBook.getCategories().get(0));
        String imgUrl = mBook.getThumbnail();
        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            Picasso.with(getActivity()).load(imgUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(imgBookCover);
            imgBookCover.setVisibility(View.VISIBLE);
        }
    }

    /**
     * If user wants to delete the book, delete com from the database
     * and finish activity.  The calling Activity refreshes the list
     * so the deleted book will no longer show up.
     */
    public boolean deleteBook() {
        mBookDeleted = false;
        new AlertDialog.Builder(mActivity)
                .setTitle(R.string.txt_delete_book)
                .setMessage(R.string.txt_delete + mBook.getTitle() + "?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mActivity.getContentResolver()
                                .delete(AlexandriaContract
                                        .BookEntry.buildBookUri(Long.parseLong(mBook.getBookId())), null, null);
                        getActivity().getContentResolver()
                                .notifyChange(AlexandriaContract.BookEntry.CONTENT_URI, null);
                        mCallback.onBookDeleted();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "Not deleting " + mBook.getTitle());
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        return mBookDeleted;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public Intent getShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mActivity.getString(R.string.share_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + mBook.getTitle());
        return shareIntent;
    }
}