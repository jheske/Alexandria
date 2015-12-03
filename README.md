<a href="https://www.linkedin.com/pub/jill-heske/13/836/635">
                <img src="https://static.licdn.com/scds/common/u/img/webpromo/btn_viewmy_160x33.png" width="160" height="33" border="0" alt="View Jill Heske's profile on LinkedIn"></a>
## Synopsis

This repository contains a book cataloging app based on **Alexandria**, my Udacity Android Developer Project #3.  It is a nearly complete redesign/rewrite of the original app, which was rudimentary, contained a variety of errors and design flaws, and did not use a Material design.

## Features

Features may or may not be completed and include, but are not limited to:

Material Design conforms to Google's latest UI/UX standards. 

Material components include CoordinatorLayout, AppBarLayout, CollapsingToolbarLayout, Toolbar, NestedScrollView, DrawerLayout, NavigationView, FloatingActionButton, and CardView.  It still uses a ListView, but I plan to replace that with a RecyclerView when time permits.

Allows user to add, view, delete, and share books.

User adds books by either scanning a barcode or entering a 13 digit ISBN number.

Provides navigation drawer for displaying the app's main navigation options, including My Books, Add Books, Settings, and About.

Uses Retrofit 2.0 to retrieve current movies from googleapis.com/books and parse JSON results into POJOs. 

Uses Picasso and Glide libraries for image download (not necessary, but I wanted to compare them). 

Uses a SQLite database and a ContentProvider for storing and retrieving books.

A book detail screen shows title, subtitle, authors, genres and a Share option.
 
Provides a variety of portrait and landscape layouts to support both phones and tablets in a variety of screen resolutions.  The tablet version provides a master/detail layout in landscape mode.

## Screenshots

<img src="https://github.com/jheske/Alexandria/blob/master/master-detail.png?raw=true" alt="Alexandria master-detail" width="600">

<img src="https://github.com/jheske/Alexandria/blob/master/portrait-nav-drawer.png?raw=true" alt="Alexandria nav drawer" width="300"> <img src="https://github.com/jheske/Alexandria/blob/master/portrait-add-book.png?raw=true" alt="Alexandria master-detail" width="300">


## build.gradle 

    compile 'com.android.support:appcompat-v7:23.0.0'
    compile 'com.android.support:design:23.0.0'
    compile 'com.android.support:cardview-v7:23.0.0'
    compile 'com.squareup.picasso:picasso:2.5.2'
    //Yes, this is a second image library. I want to compare them.
    compile 'com.github.bumptech.glide:glide:3.6.0'
    compile 'me.dm7.barcodescanner:zxing:1.8.2'
    compile 'com.facebook.stetho:stetho:1.1.1'
    //http://www.android4devs.com/2015/06/navigation-view-material-design-support.html
    compile 'de.hdodenhof:circleimageview:1.3.0'
    compile 'com.jakewharton:butterknife:7.0.1'
    compile 'com.squareup.retrofit:retrofit:2.0.0-beta2'
    compile 'com.squareup.retrofit:converter-jackson:2.0.0-beta2'
    //New debugging library--gotta love Mr Wharton!!
    compile 'com.squareup.okhttp:logging-interceptor:2.6.0-SNAPSHOT'

## Icon 

http://www.iconarchive.com/show/circle-icons-by-martz90/books-icon.html




