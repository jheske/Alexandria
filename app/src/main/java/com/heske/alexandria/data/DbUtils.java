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

package com.heske.alexandria.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.List;

import com.heske.alexandria.model.Book;

public class DbUtils {
    private final static String TAG = "DbUtils";

    /**
     * Don't call
     * context.getContentResolver().notifyChange(AlexandriaContract.BookEntry.CONTENT_URI, null);
     *
     * @param context
     * @param ean
     * @param book
     */
    public static void dbAddBook(Context context, String ean, Book book) {
        ContentValues values = new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, book.getTitle());
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, book.getThumbnail());
        values.put(AlexandriaContract.BookEntry.SUBTITLE, book.getSubtitle());
        values.put(AlexandriaContract.BookEntry.DESC, book.getDescription());
        context.getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI, values);
        dbAddAuthors(context, ean, book.getAuthors());
        dbAddCategories(context, ean, book.getCategories());
       // context.getContentResolver()
       //        .notifyChange(AlexandriaContract.BookEntry.CONTENT_URI, null);
    }

    private static void dbAddAuthors(Context context, String ean, List<String> authors) {
        ContentValues values = new ContentValues();
        for (int i = 0; i < authors.size(); i++) {
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, authors.get(i));
            context.getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
            values = new ContentValues();
        }
    }

    private static void dbAddCategories(Context context, String ean, List<String> categories) {
        ContentValues values = new ContentValues();
        for (int i = 0; i < categories.size(); i++) {
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, categories.get(i));
            context.getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
            values = new ContentValues();
        }
    }

    public static Book dbLookupBook(Context context, String ean) {
        Book lookupBook;

        if (ean == null)
            return null;
        Cursor cursor = context.getContentResolver().query(
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null, // leaving "columns" null returns all columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Log.d(TAG, cursor.getString(AlexandriaContract.TITLE_COL) + " is already in the database ");
            lookupBook = new Book(cursor, true);
            cursor.close();
            return lookupBook;
        }
        cursor.close();
        return null;
    }

    public static void dbDeleteBook(Context context, String bookId) {
        context.getContentResolver()
                .delete(AlexandriaContract
                        .BookEntry.buildBookUri(Long.parseLong(bookId)), null, null);
        context.getContentResolver()
                .notifyChange(AlexandriaContract.BookEntry.CONTENT_URI, null);
    }

    /**
     * Make DbUtils a utility class by preventing instantiation.
     */
    private DbUtils() {
        throw new AssertionError();
    }
}
