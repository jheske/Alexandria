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

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.heske.alexandria.data.AlexandriaContract;

/**
 * Created by saj on 23/12/14.
 */
public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void setUp() {
        deleteAllRecords();
    }

    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                AlexandriaContract.CategoryEntry.CONTENT_URI,
                null,
                null
        );

        mContext.getContentResolver().delete(
                AlexandriaContract.AuthorEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                AlexandriaContract.AuthorEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                AlexandriaContract.CategoryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    public void testGetType() {

        String type = mContext.getContentResolver().getType(AlexandriaContract.BookEntry.CONTENT_URI);
        assertEquals(AlexandriaContract.BookEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(AlexandriaContract.AuthorEntry.CONTENT_URI);
        assertEquals(AlexandriaContract.AuthorEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(AlexandriaContract.CategoryEntry.CONTENT_URI);
        assertEquals(AlexandriaContract.CategoryEntry.CONTENT_TYPE, type);

        long id = 9780137903955L;
        type = mContext.getContentResolver().getType(AlexandriaContract.BookEntry.buildBookUri(id));
        assertEquals(AlexandriaContract.BookEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(AlexandriaContract.BookEntry.buildFullBookUri(id));
        assertEquals(AlexandriaContract.BookEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(AlexandriaContract.AuthorEntry.buildAuthorUri(id));
        assertEquals(AlexandriaContract.AuthorEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(AlexandriaContract.CategoryEntry.buildCategoryUri(id));
        assertEquals(AlexandriaContract.CategoryEntry.CONTENT_ITEM_TYPE, type);

    }

    public void testInsertRead(){

        insertReadBook();
        insertReadAuthor();
        insertReadCategory();

        readFullBook();
        readFullList();
    }

    public void insertReadBook(){
        ContentValues bookValues = TestDb.getBookValues();

        Uri bookUri = mContext.getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI, bookValues);
        long bookRowId = ContentUris.parseId(bookUri);
        assertTrue(bookRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, bookValues);

        cursor = mContext.getContentResolver().query(
                AlexandriaContract.BookEntry.buildBookUri(bookRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, bookValues);

    }

    public void insertReadAuthor(){
        ContentValues authorValues = TestDb.getAuthorValues();

        Uri authorUri = mContext.getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, authorValues);
        long authorRowId = ContentUris.parseId(authorUri);
        assertTrue(authorRowId != -1);
        assertEquals(authorRowId,TestDb.ean);

        Cursor cursor = mContext.getContentResolver().query(
                AlexandriaContract.AuthorEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, authorValues);

        cursor = mContext.getContentResolver().query(
                AlexandriaContract.AuthorEntry.buildAuthorUri(authorRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, authorValues);

    }

    public void insertReadCategory(){
        ContentValues categoryValues = TestDb.getCategoryValues();

        Uri categoryUri = mContext.getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, categoryValues);
        long categoryRowId = ContentUris.parseId(categoryUri);
        assertTrue(categoryRowId != -1);
        assertEquals(categoryRowId,TestDb.ean);

        Cursor cursor = mContext.getContentResolver().query(
                AlexandriaContract.CategoryEntry.CONTENT_URI,
                null, // projection
                null, // selection
                null, // selection args
                null  // sort order
        );

        TestDb.validateCursor(cursor, categoryValues);

        cursor = mContext.getContentResolver().query(
                AlexandriaContract.CategoryEntry.buildCategoryUri(categoryRowId),
                null, // projection
                null, // selection
                null, // selection args
                null  // sort order
        );

        TestDb.validateCursor(cursor, categoryValues);

    }

    public void readFullBook(){

        Cursor cursor = mContext.getContentResolver().query(
                AlexandriaContract.BookEntry.buildFullBookUri(TestDb.ean),
                null, // projection
                null, // selection
                null, // selection args
                null  // sort order
        );

         TestDb.validateCursor(cursor, TestDb.getFullDetailValues());
    }

    public void readFullList(){

        Cursor cursor = mContext.getContentResolver().query(
                AlexandriaContract.BookEntry.FULL_CONTENT_URI,
                null, // projection
                null, // selection
                null, // selection args
                null  // sort order
        );

        TestDb.validateCursor(cursor, TestDb.getFullListValues());
    }


}