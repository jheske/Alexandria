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


/**
 * Credits:
 *
 * NavigationView:
 *    https://github.com/AkashBang/NavigationView
 * Handling Scrolls with CoordinatorLayout:
 *    https://guides.codepath.com/android/Handling-Scrolls-with-CoordinatorLayout (https://github.com/chrisbanes/cheesesquare)
 *
 */
package com.heske.alexandria.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.facebook.stetho.Stetho;

import com.heske.alexandria.R;
import com.heske.alexandria.Utils;
import com.heske.alexandria.api.AddBookCallback;
import com.heske.alexandria.api.BookDetailCallback;
import com.heske.alexandria.api.BookListCallback;
import com.heske.alexandria.model.Book;

public class MainActivity extends AppCompatActivity
        implements BookListCallback, BookDetailCallback, AddBookCallback {
    private final String TAG = getClass().getSimpleName();
    private DrawerLayout mDrawer;
    private NavigationView mNavView;
    ActionBarDrawerToggle mDrawerToggle;
    private ListOfBooksFragment mListOfBooksFragment = null;
    private BookDetailFragment mBookDetailFragment = null;
    private AddBookFragment mAddBookFragment = null;
    private Menu mMenu;
    private Toolbar mToolbar;
    private boolean mIsTwoPane;
    private String KEY_NAV_MENU_SELECTION = "KEY_NAV_POSITION";
    private String KEY_SEARCH_STRING = "KEY_SEARCH_STRING";

    private final int NAV_ITEM_LIST = 0;
    private final int NAV_ITEM_ADD_BOOKS = 1;
    private final int NAV_ITEM_SETTINGS = 2;
    private final int NAV_ITEM_ABOUT = 3;
    private int mCurrentNavPosition = NAV_ITEM_LIST;
    private String mAddBookText = "";
    private boolean mIsInitState = true;
    private int REQUEST_ADD_BOOK = 0;

    private boolean mIsSearchInProgress = false;
    private String mSearchString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);
        /**
         * On config change there will be a Nav menu selection to restore.
         * Otherwise get menu selection from shared preferences.
         * Default is ListOfBooks
         * Also restore search string text and add book text.
         */
        if (savedInstanceState != null) {
            mCurrentNavPosition = savedInstanceState.getInt(KEY_NAV_MENU_SELECTION, NAV_ITEM_LIST);
            mSearchString = savedInstanceState.getString(KEY_SEARCH_STRING, "");
            mAddBookText = savedInstanceState.getString(AddBookFragment.KEY_ADD_BOOK_TEXT, "");
        } else
            getNavMenuPreference();
        setupToolbar();
        setupStetho();
        mIsTwoPane = checkForDualPane();
        setupNavDrawer();
    }

    private void setupNavDrawer() {
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavView = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(mNavView);
        selectDrawerItem(mNavView.getMenu().getItem(mCurrentNavPosition));
        // Tie DrawerLayout events to the ActionBarToggle
        mDrawerToggle = setupDrawerToggle();
        mDrawer.setDrawerListener(mDrawerToggle);
    }

    private void setupToolbar() {
        // Set a Toolbar to replace the ActionBar.
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    private void getNavMenuPreference() {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        mCurrentNavPosition = Integer
                .valueOf(settings.getString("pref_startFragment", "0"));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mCurrentNavPosition = findNavPosition();
        outState.putInt(KEY_NAV_MENU_SELECTION, mCurrentNavPosition);
        outState.putString(KEY_SEARCH_STRING, mSearchString);
        //See comments in AddBookFragment for why I am doing
        //this here instead of the Fragment itself
        if (mAddBookFragment != null)
            outState.putString(AddBookFragment.KEY_ADD_BOOK_TEXT, mAddBookFragment.getEanText());
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(KEY_NAV_MENU_SELECTION)) {
            restoreNavPosition(savedInstanceState.getInt(KEY_NAV_MENU_SELECTION, NAV_ITEM_LIST));
        }
    }

    private int findNavPosition() {
        Menu menu = mNavView.getMenu();
        int count = menu.size();

        for (int i = 0; i < count; i++) {
            if (menu.getItem(i).isChecked())
                return i;
        }
        //Default to BookList
        return 0;
    }

    private void restoreNavPosition(int position) {
        Menu menu = mNavView.getMenu();
        menu.getItem(position).setChecked(true);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_add_book:
                selectDrawerItem(mNavView.getMenu().getItem(NAV_ITEM_ADD_BOOKS));
                return true;
            case R.id.action_delete_book:
                mListOfBooksFragment.deleteBook();
              //TODO combine the two deleteBook() methods
              //  mBookDetailFragment.deleteBook();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * WHAT A HACK!!!!
     *
     * on config change, Android shows menus for fragments
     * that have been replaced.
     * SO I have do hide all the ones that shouldn't be visible
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;

        if (!mIsTwoPane && (mCurrentNavPosition == NAV_ITEM_LIST)) {
            Utils.hideMenuItem(menu, R.id.action_delete_book);
            Utils.hideMenuItem(menu, R.id.action_share);
        }
        //This one is because the add button is visible
        //for ListOfBooksFragment, but is redundant and
        //should be hidden when AddBookFragment is showing.
        if (mCurrentNavPosition == NAV_ITEM_ADD_BOOKS) {
            Utils.hideMenuItem(mMenu, R.id.action_add_book);
            Utils.hideMenuItem(mMenu, R.id.action_delete_book);
            Utils.hideMenuItem(mMenu, R.id.action_share);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void hideMenuItem(Menu menu, int menuItemId) {
        MenuItem menuItem = menu.findItem(menuItemId);
        if (menuItem != null)
            menuItem.setVisible(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                handleSearch("");
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchText) {
                handleSearch(searchText);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String arg0) {
                return false;
            }
        });
        return true;
    }

    private void handleSearch(String searchText) {
        String mSearchString = searchText;
        if (mListOfBooksFragment != null) {
            mIsSearchInProgress = true;
            mListOfBooksFragment.setSearchString(mSearchString);
            mListOfBooksFragment.restartLoader(true, false);
        }
    }

    // Make sure this is the method with just `Bundle` as the signature
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Determine whether we are in two-pane mode, based
     * on layouts.xml-defined boolean value "has_two_panes"
     */
    private boolean checkForDualPane() {
        // has_two_panes is defined in values/layouts.xml
        if (getResources().getBoolean(R.bool.has_two_panes))
            return true;
        return false;
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        final Context context = this;
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawer,
                mToolbar, R.string.nav_drawer_open, R.string.nav_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                Utils.hideKeyboard(context, (EditText) findViewById(R.id.etv_ean));
            }
        };
        return actionBarDrawerToggle;
    }

    protected void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    protected void selectDrawerItem(MenuItem menuItem) {
        menuItem.setChecked(true);

        switch (menuItem.getItemId()) {
            case R.id.nav_list_books:
                if (mIsTwoPane) {
                    //If it's already showing then don't load it again
                    //mIsInitState = a special condition where
                    //Activity has just been created so everything has to be displayed
                    if ((mCurrentNavPosition == NAV_ITEM_LIST) && (!mIsInitState))
                        displayFragment(BookDetailFragment.class, R.id.frame_detail_container);
                    else
                        displayMasterDetailFragments(BookDetailFragment.class);
                } else
                    displayFragment(ListOfBooksFragment.class, R.id.frame_fullscreen_container);
                mCurrentNavPosition = NAV_ITEM_LIST;
                mIsInitState = false;
                //true = display first book in BookDetailFragment
                //false = user did not make selection
                mListOfBooksFragment.restartLoader(true, false);
                break;
            case R.id.nav_add_books:
                invalidateOptionsMenu();
                if (mIsTwoPane) {
                    mCurrentNavPosition = NAV_ITEM_ADD_BOOKS;
                    if (!mIsInitState)
                        displayFragment(AddBookFragment.class, R.id.frame_detail_container);
                    else
                        displayMasterDetailFragments(AddBookFragment.class);
                } else
                    //Once book is added, AddBookActivity closes
                    //and onActivityResult displays the updated list
                    startActivityForResult(new Intent(this,
                            AddBookActivity.class), REQUEST_ADD_BOOK);
                break;
            case R.id.nav_about:
                mCurrentNavPosition = NAV_ITEM_ABOUT;
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.nav_settings:
                mCurrentNavPosition = NAV_ITEM_SETTINGS;
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                mCurrentNavPosition = NAV_ITEM_LIST;
        }
        mDrawer.closeDrawers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_ADD_BOOK) {
                selectDrawerItem(mNavView.getMenu().getItem(NAV_ITEM_LIST));
            }
        }
    }

    /**
     * ListOfBooks is the only view that has master and detail
     * in two-pane mode.
     */
    private void displayMasterDetailFragments(Class detailFragmentClass) {
        findViewById(R.id.layout_master_detail).setVisibility(View.VISIBLE);
        findViewById(R.id.frame_fullscreen_container).setVisibility(View.GONE);
        displayFragment(detailFragmentClass, R.id.frame_detail_container);
        displayFragment(ListOfBooksFragment.class, R.id.frame_master_container);
        mListOfBooksFragment = (ListOfBooksFragment) getSupportFragmentManager()
                .findFragmentByTag(ListOfBooksFragment.class.getSimpleName());
        mListOfBooksFragment.setSearchString(mSearchString);
    }

    private void displayFragment(Class fragmentClass, int frameId) {
        Fragment fragment = null;
        String fragmentTag = fragmentClass.getSimpleName();

        // Insert the fragment by replacing any existing fragment
        try {
            if (fragmentTag.equals(AddBookFragment.class.getSimpleName()))
                fragment = AddBookFragment.newInstance(mAddBookText);
            else
                fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Log.i(TAG, "fragmentTag = " + fragmentTag);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(frameId, fragment, fragmentTag).commit();
        fragmentManager.executePendingTransactions();
        if (fragmentTag.equals(ListOfBooksFragment.class.getSimpleName()))
            mListOfBooksFragment = (ListOfBooksFragment) fragment;
        else if (fragmentTag.equals(BookDetailFragment.class.getSimpleName()))
            mBookDetailFragment = (BookDetailFragment) fragment;
        else if (fragmentTag.equals(AddBookFragment.class.getSimpleName()))
            mAddBookFragment = (AddBookFragment) fragment;
    }

    /**
     * The user can select a book from the list at any time in two-pane mode,
     * regardless of what is displayed in the detail pane.
     * If ListOfBooks fragment is not selected in NavView, then
     * BookDetailFragment won't be showing in the detail pane, so com
     * must be loaded before displaying the book.
     */
    @Override
    public void onBookSelected(String ean, boolean userSelected) {
        if (ean == null)
            return;
        Utils.hideKeyboard(this, (EditText) findViewById(R.id.etv_ean));
        //In two pane mode, switch over to My Books menu item and display
        //the book in the details pane.
        if (mIsTwoPane) {
            if ((mCurrentNavPosition != NAV_ITEM_LIST)) {
                // Don't try to display the first book in search results if
                // AddBookFragment is showing.  Assume the user just wants
                // to see the filtered list on the master pane.
                if (mIsSearchInProgress) {
                   mIsSearchInProgress = false;
                    return;
                }
                mCurrentNavPosition = NAV_ITEM_LIST;
                selectDrawerItem(mNavView.getMenu().getItem(NAV_ITEM_LIST));
            } else
                mBookDetailFragment.displayBook(ean);
        } else if (userSelected) {
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra(BookDetailFragment.EAN_KEY, ean);
            startActivity(intent);
           // startActivityForResult(new Intent(this, BookDetailActivity.class), REQUEST_BOOK_DETAILS);
        }
    }

    /**
     * Called from AddBookFragment as part of AddBookCallback interface.
     * @param book
     */
    public void onBookAdded(Book book) {
        //Restart the loader to refresh the list, but don't
        //let onLoadFinished display the book because that would
        //replace AddBooksFragment with BookDetailsFragment, which
        //might annoy the user
        Log.i(TAG, "New book added " + book.getTitle());
        if ((mIsTwoPane) && (mListOfBooksFragment != null))
            mListOfBooksFragment.restartLoader(false, false);
    }

    @Override
    public void onBookDeleted() {
        /** NO OP required by BookDetailFragment **/
    }

    /**
     * A useful library for debugging Android apps
     * using Chrome, even has a database inspector!
     * <p/>
     * Usage  chrome://inspect
     */
    private void setupStetho() {
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());
    }
}
