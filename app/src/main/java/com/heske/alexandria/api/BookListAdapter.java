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


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import com.heske.alexandria.R;
import com.heske.alexandria.data.AlexandriaContract;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by saj on 11/01/15.
 */
public class BookListAdapter extends CursorAdapter {
    private Cursor mCursor;

    public static class ViewHolder {
       // @Bind(R.id.fullBookCover)
        public ImageView imgBookCover;
       // @Bind(R.id.listBookTitle)
        public TextView tvBookTitle;
        //@Bind(R.id.listBookSubTitle)
        public TextView tvBookSubtitle;

        public ViewHolder(View view) {
           // ButterKnife.bind(this,view);
            imgBookCover = (ImageView) view.findViewById(R.id.fullBookCover);
            tvBookTitle = (TextView) view.findViewById(R.id.listBookTitle);
            tvBookSubtitle = (TextView) view.findViewById(R.id.listBookSubTitle);
        }
    }

    public BookListAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        mCursor = cursor;
    }

/*    public Book getItem(int position) {
       mCursor.moveToPosition(position);
       return new Book(mCursor,false);
    } */

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String imgUrl = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));

        Picasso.with(mContext).load(imgUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(viewHolder.imgBookCover);

        String bookTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        viewHolder.tvBookTitle.setText(bookTitle);

        String bookSubTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        viewHolder.tvBookSubtitle.setText(bookSubTitle);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.book_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }
}
