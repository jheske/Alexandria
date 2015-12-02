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
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.heske.alexandria.R;
import com.heske.alexandria.api.BookListAdapter;
import com.heske.alexandria.api.BookListCallback;
import com.heske.alexandria.data.AlexandriaContract;
import com.heske.alexandria.data.DbUtils;
import com.heske.alexandria.model.Book;

public class ListOfBooksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String TAG = getClass().getSimpleName();
    private String mSearchString = "";
    private final int LOADER_ID = 10;
    private BookListCallback mCallback = null;
    private boolean mSelectBook = false;
    private boolean mUserSelected = false;
    private BookListAdapter mBookListAdapter;
    private ListView mBookListView;
    private int mLastPosition = 0;
    private String KEY_LAST_POSITION = "KEY_LAST_POSITION";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mLastPosition = savedInstanceState.getInt(KEY_LAST_POSITION,0);
        }
        Cursor cursor = getActivity().getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        // Get the first book in the list
        cursor.moveToFirst();
        mBookListAdapter = new BookListAdapter(getActivity(), cursor, 0);
        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);

        mBookListView = (ListView) rootView.findViewById(R.id.listOfBooks);
        mBookListView.setAdapter(mBookListAdapter);

        mBookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mLastPosition = position;
                mCallback.onBookSelected(getBookAtPosition(position), true);
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_LAST_POSITION, mLastPosition);
    }

    public void deleteBook() {
        Cursor cursor = (Cursor) mBookListAdapter.getItem(mLastPosition);
        final Book book = new Book(cursor, false);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.txt_delete_book)
                .setMessage(R.string.txt_delete + book.getTitle() + "?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DbUtils.dbDeleteBook(getActivity(), book.getBookId());

                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "Not deleting " + book.getTitle());
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        mSelectBook = true;
        mUserSelected = true;
    }

    public String getBookAtPosition(int position) {
        Cursor cursor = (Cursor) mBookListAdapter.getItem(mLastPosition);

        if (cursor != null && cursor.moveToPosition(position)) {
            return (cursor.getString(cursor
                    .getColumnIndex(AlexandriaContract.BookEntry._ID)));
        }
        return null;
    }

    public void restartLoader(boolean selectFirstBook, boolean userSelected) {
        mSelectBook = selectFirstBook;
        mUserSelected = userSelected;
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // The hosting Activity must implement
        // Callback interface.
        try {
            mCallback = (BookListCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + getResources().getString(R.string.errorMissingCallbackMethod)
                    + mCallback.getClass().getSimpleName());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final String selection = AlexandriaContract.BookEntry.TITLE + " LIKE ? OR "
                + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";

        if (mSearchString.length() > 0) {
            mSearchString = "%" + mSearchString + "%";
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{mSearchString, mSearchString},
                    null
            );
        }

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mBookListAdapter.swapCursor(cursor);
        if (mBookListAdapter.getCount() == 0) {
            mLastPosition = 0;
            return;
        }
        cursor.moveToPosition(mLastPosition);
        if (mLastPosition != ListView.INVALID_POSITION) {
            mBookListView.smoothScrollToPosition(mLastPosition);
            int cursorIndex = cursor.getColumnIndex(AlexandriaContract.BookEntry._ID);
            String ean = cursor.getString(cursorIndex);
            if (mSelectBook)
                mCallback.onBookSelected(ean, mUserSelected);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBookListAdapter.swapCursor(null);
    }

    public void setSearchString(String searchString) {
        mSearchString = searchString;
    }
}
