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

package com.heske.alexandria.model;

import android.database.Cursor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import com.heske.alexandria.data.AlexandriaContract;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    String mBookId;
    @JsonProperty("volumeInfo")
    VolumeInfo mVolumeInfo;

    @JsonIgnoreProperties(ignoreUnknown = true)
    class VolumeInfo {
        @JsonProperty("title")
        String mTitle;
        @JsonProperty("subtitle")
        String mSubtitle;
        @JsonProperty("description")
        String mDescription;
        @JsonProperty("authors")
        List<String> mAuthors;
        @JsonProperty("categories")
        List<String> mCategories;
        @JsonProperty("imageLinks")
        ImageLinks mImageLinks;

        @JsonIgnoreProperties(ignoreUnknown = true)
        class ImageLinks {
            @JsonProperty("thumbnail")
            String mThumbnail;

            public ImageLinks() {
                /* NO-OP dummy constructor to appease jsonMapper */
            }

            public ImageLinks(String thumbnail) {
                /* Constructor to appease jsonMapper */
                mThumbnail = thumbnail;
            }

            public ImageLinks(Cursor cursor) {
                mThumbnail = cursor.getString(AlexandriaContract.IMAGEURL_COL);
            }

            public String getThumbnail() {
                return mThumbnail;
            }
        }

        public VolumeInfo() {
            /* dummy constructor to appease jsonMapper */
            mTitle = "";
            mSubtitle = "";
            mDescription = "";
            mAuthors = null;
            mCategories = null;
            mImageLinks = null;
        }

        //fullDetails = true = author and category details are available
        public VolumeInfo(Cursor cursor, boolean fullDetails) {
            mTitle = cursor.getString(AlexandriaContract.TITLE_COL);
            mSubtitle = cursor.getString(AlexandriaContract.SUBTITLE_COL);
            mDescription = cursor.getString(AlexandriaContract.DESCRIPTION_COL);
            mImageLinks = new ImageLinks(cursor);
            if (!fullDetails)
                return;
            mAuthors = new ArrayList<>();
            mAuthors.add(cursor.getString(AlexandriaContract.AUTHOR_COL));
            mCategories = new ArrayList<>();
            mCategories.add(cursor.getString(AlexandriaContract.CATEGORY_COL));
        }

        public String getTitle() {
            return mTitle;
        }

        public String getSubtitle() {
            return mSubtitle;
        }

        public String getDescription() {
            return mDescription;
        }

        public List<String> getAuthors() {
            return mAuthors;
        }

        public List<String> getCategories() {
            return mCategories;
        }

        public String getThumbnail() {
            return mImageLinks.getThumbnail();
        }
    }

    public Book() {
        /* NO-OP dummy constructor to appease jsonMapper */
    }

    public Book(Cursor cursor,boolean fullDetails) {
        mBookId =cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID));
        mVolumeInfo = new VolumeInfo(cursor,fullDetails);
    }

    public String getBookId() {
        return mBookId;
    }

    public void setBookId(String ean) {
        mBookId = ean;
    }

    public VolumeInfo getVolumeInfo() {
        return mVolumeInfo;
    }

    public String getTitle() {
        return mVolumeInfo.getTitle();
    }

    public String getSubtitle() {
        return mVolumeInfo.getSubtitle();
    }

    public String getDescription() {
        return mVolumeInfo.getDescription();
    }

    public List<String> getAuthors() {
        return mVolumeInfo.getAuthors();
    }

    public List<String> getCategories() {
        return mVolumeInfo.getCategories();
    }

    public String getThumbnail() {
        return mVolumeInfo.getThumbnail();
    }
}
