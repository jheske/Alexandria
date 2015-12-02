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

/**
 * Created by saj on 22/12/14.
 */

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class AlexandriaContract{

    public static final String CONTENT_AUTHORITY = "com.heske.alexandria";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_BOOKS = "books";
    public static final String PATH_AUTHORS = "authors";
    public static final String PATH_CATEGORIES = "categories";
    public static final String PATH_FULLBOOK = "fullbook";
    public static final int ID_COL = 0;
    public static final int TITLE_COL = 1;
    public static final int SUBTITLE_COL = 2;
    public static final int IMAGEURL_COL = 3;
    public static final int DESCRIPTION_COL = 4;
    public static final int AUTHOR_COL = 5;
    public static final int CATEGORY_COL = 6;

    public static final class BookEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).build();

        public static final Uri FULL_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FULLBOOK).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public static final String TABLE_NAME = "books";

        public static final String TITLE = "title";

        public static final String IMAGE_URL = "imgurl";

        public static final String SUBTITLE = "subtitle";

        public static final String DESC = "description";

        public static Uri buildBookUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildFullBookUri(long id) {
            return ContentUris.withAppendedId(FULL_CONTENT_URI, id);
        }

    }

    public static final class AuthorEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_AUTHORS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_AUTHORS;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_AUTHORS;

        public static final String TABLE_NAME = "authors";

        public static final String AUTHOR = "author";

        public static Uri buildAuthorUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class CategoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORIES).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORIES;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORIES;

        public static final String TABLE_NAME = "categories";

        public static final String CATEGORY = "category";

        public static Uri buildCategoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}