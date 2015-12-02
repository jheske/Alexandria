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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.heske.alexandria.api.AddBookCallback;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.heske.alexandria.R;
import com.heske.alexandria.Utils;
import com.heske.alexandria.api.AlexandriaApplication;
import com.heske.alexandria.api.ApiServiceResponse;
import com.heske.alexandria.api.BookApiService;
import com.heske.alexandria.data.DbUtils;
import com.heske.alexandria.model.Book;

import butterknife.OnClick;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class AddBookFragment extends Fragment {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private View rootView;
    private final String EAN_CONTENT = "eanContent";
    public final static int SCAN_REQUEST_CODE = 1;
    private Activity mActivity;
    private AddBookCallback mCallback = null;
    private boolean mIsInitState = true;
    public static String KEY_ADD_BOOK_TEXT = "KEY_ADD_BOOK_TEXT";
    private String mAddBookText = "";
    public static final String MESSAGE_ADD_BOOK = "MESSAGE_EVENT";
    public static final String MESSAGE_EXTRA_TITLE = "MESSAGE_EXTRA_TITLE";
    public static final String MESSAGE_EXTRA_EAN = "MESSAGE_EXTRA_EAN";
    public static final String MESSAGE_ERROR = "MESSAGE_ERROR";
    public static final String MESSAGE_EXTRA_ERROR = "MESSAGE_EXTRA_ERROR";
    public Book mBook;

    @Nullable
    @Bind(R.id.preview_container)
    public LinearLayout containerPreview;
    @Nullable
    @Bind(R.id.etv_ean)
    public EditText etvEan;
    @Nullable
    @Bind(R.id.tv_book_title)
    public TextView tvBookTitle;
    @Nullable
    @Bind(R.id.tv_book_subtitle)
    public TextView tvBookSubtitle;
    @Nullable
    @Bind(R.id.tv_authors)
    public TextView tvAuthors;
    @Nullable
    @Bind(R.id.img_book_cover)
    public ImageView imgBookCover;
    @Nullable
    @Bind(R.id.tv_no_network)
    public TextView tv_no_network;

    public AddBookFragment() {
    }

    public static AddBookFragment newInstance(String addBookText) {
        AddBookFragment fragment = new AddBookFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ADD_BOOK_TEXT, addBookText);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * http://blog.sqisland.com/2014/06/navigationdrawer-creates-fragment-twice.html
     * <p/>
     * NOTE Android anomaly where fragments get created TWICE on config changes!!!
     * Once when Activity is destroyed/recreated and it automatically recreates
     * all its fragments.
     * And again when we use add the fragment using fragmentmanager.replace, which
     * causes the fragment to be recreated.
     * It would be great to be able to prevent the replace transaction, but
     * unfortunately, I use two different layouts, so I have to call replace
     * to load fragments in the new layout.
     * <p/>
     * The only solution I can come up with is to let it call create twice, and just
     * save/restore from SharedPreferences (NEXT STOP, "DEATH TO FRAGMENTS" USING JAKE
     * WHARTON'S ALTERNATIVE DESIGN).
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (etvEan != null) {
            outState.putString(EAN_CONTENT, etvEan.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ButterKnife.bind(this, rootView);

        if (getArguments() != null) {
            String addBookText = getArguments().getString(KEY_ADD_BOOK_TEXT);
            if (!addBookText.isEmpty())
                etvEan.setText(addBookText);
        }

        etvEan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Wait for user to initiate text entry.
                //Otherwise, on config change, a 13-digit number
                //might be left over and be seen as a duplicate book
                if (mIsInitState) {
                    mIsInitState = false;
                    return;
                }
                String ean = s.toString();
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }
                if (ean.length() < 13) {
                    return;
                }
                addBook(ean);
            }
        });

        if (!Utils.isNetworkAvailable(getActivity())) {
            etvEan.setText(getString(R.string.error_no_network_short));
            etvEan.setEnabled(false);
            tv_no_network.setVisibility(View.VISIBLE);
        }
        else
          tv_no_network.setVisibility(View.GONE);
        return rootView;
    }

    public String getEanText() {
        if (etvEan != null)
            return etvEan.getText().toString();
        return "";
    }

    public void addBook(final String ean) {
        AlexandriaApplication app = (AlexandriaApplication) getActivity().getApplication();
        BookApiService apiService = app.getApiService();

        Call<ApiServiceResponse> call = apiService.getBook("isbn:" + ean);
        call.enqueue(new Callback<ApiServiceResponse>() {
            @Override
            public void onResponse(Response<ApiServiceResponse> response, Retrofit retrofit) {
                Log.i(TAG, "Got results! Count = " + response.body());
                if (response.isSuccess()) {
                    Book book = response.body().getBook(0);
                    if (!isValidBook(ean, book))
                        return;
                    confirmAddBook(ean, book);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Utils.showToast(getActivity(), "Api call failed " + t.getMessage());
                Log.i(TAG, "Api call error! " + t.getMessage());
            }
        });
    }

    private boolean isValidBook(String ean, Book book) {
        if (book == null) {
            Utils.showToast(getActivity(),
                    getResources().getString(R.string.error_lookup_empty_data));
            return false;
        }
        if (book.getVolumeInfo() == null) {
            Utils.showToast(getActivity(),
                    getResources().getString(R.string.error_lookup_empty_data));
            return false;
        }
        Book lookupBook = DbUtils.dbLookupBook(getActivity(), ean);
        if (lookupBook != null) {
            Utils.showToast(getActivity(), lookupBook.getTitle() + ": " +
                    getResources().getString(R.string.error_duplicate_book));
            return false;
        }
        return true;
    }

    private void confirmAddBook(final String ean, final Book book) {
        Utils.hideKeyboard(getActivity(),etvEan);
        mBook = book;
        mBook.setBookId(ean);
        containerPreview.setVisibility(View.VISIBLE);
        tvBookTitle.setText(book.getTitle());
        tvBookSubtitle.setText(book.getSubtitle());
        tvAuthors.setText(book.getAuthors().get(0));
        Picasso.with(getActivity()).load(book.getThumbnail())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(imgBookCover);    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_add_book)
    public void addButtonClick() {
        Log.i(TAG, "User confirmed " + mBook.getTitle());
        DbUtils.dbAddBook(getActivity(), mBook.getBookId(), mBook);
        containerPreview.setVisibility(View.INVISIBLE);
        Utils.showToast(getActivity(), mBook.getTitle() + " added to My List");
        containerPreview.setVisibility(View.INVISIBLE);
        etvEan.setText("");
    }

    @Nullable
    @SuppressWarnings("unused")
    @OnClick(R.id.btn_scan_book)
    public void callScanBookActivity() {
        Intent intent = new Intent(getActivity(), ScanBookActivity.class);
        startActivityForResult(intent, SCAN_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SCAN_REQUEST_CODE) {
                addBook(data.getStringExtra(ScanBookActivity.SCAN_CODE_EXTRA));
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        // The hosting Activity must implement
        // Callback interface.
        try {
            mCallback = (AddBookCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + getResources().getString(R.string.errorMissingCallbackMethod)
                    + " AddBookCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }
}
