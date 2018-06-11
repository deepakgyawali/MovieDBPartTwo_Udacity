/**
 * Created by Deepak Gyawali, www.deepakgyawali.com.np
 * Copyright (c) 2018 . All rights reserved.
 * Last modified 2018
 */
package com.deepakgyawali.moviedbparttwo.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


// DB Helper for content provider
public class FavoriteMoviesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorite_movies.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE "
            + FavoriteMoviesContract.MoviesEntry.TABLE_NAME
            + " (" + FavoriteMoviesContract.MoviesEntry._ID + " TEXT PRIMARY KEY, "
            + FavoriteMoviesContract.MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, "
            + FavoriteMoviesContract.MoviesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, "
            + FavoriteMoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE + " TEXT NOT NULL, "
            + FavoriteMoviesContract.MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, "
            + FavoriteMoviesContract.MoviesEntry.COLUMN_PORTER_URI + " TEXT NOT NULL "
            + " );";

    private static final String SQL_CREATE_VIDEOS_TABLE = "CREATE TABLE "
            + FavoriteMoviesContract.VideosEntry.TABLE_NAME
            + " (" + FavoriteMoviesContract.VideosEntry._ID + " TEXT PRIMARY KEY, "
            + FavoriteMoviesContract.VideosEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, "
            + FavoriteMoviesContract.VideosEntry.COLUMN_KEY + " TEXT NOT NULL, "
            + FavoriteMoviesContract.VideosEntry.COLUMN_NAME + " TEXT NOT NULL, "
            + "FOREIGN KEY (" + FavoriteMoviesContract.VideosEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
            FavoriteMoviesContract.MoviesEntry.TABLE_NAME + " (" + FavoriteMoviesContract.MoviesEntry._ID + ") ON DELETE CASCADE"
            + " );";

    private static final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE "
            + FavoriteMoviesContract.ReviewsEntry.TABLE_NAME
            + " (" + FavoriteMoviesContract.ReviewsEntry._ID + " TEXT PRIMARY KEY, "
            + FavoriteMoviesContract.ReviewsEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, "
            + FavoriteMoviesContract.ReviewsEntry.COLUMN_AUTHOR + " TEXT NOT NULL, "
            + FavoriteMoviesContract.ReviewsEntry.COLUMN_CONTENT + " TEXT NOT NULL, "
            + "FOREIGN KEY (" + FavoriteMoviesContract.ReviewsEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
            FavoriteMoviesContract.MoviesEntry.TABLE_NAME + " (" + FavoriteMoviesContract.MoviesEntry._ID + ") ON DELETE CASCADE"
            + " );";

    private static final String SQL_DROP_MOVIES_TABLE = "DROP TABLE IS EXISTS " +
            FavoriteMoviesContract.MoviesEntry.TABLE_NAME;
    private static final String SQL_DROP_VIDEOS_TABLE = "DROP TABLE IS EXISTS " +
            FavoriteMoviesContract.MoviesEntry.TABLE_NAME;
    private static final String SQL_DROP_REVIEWS_TABLE = "DROP TABLE IS EXISTS " +
            FavoriteMoviesContract.ReviewsEntry.TABLE_NAME;

    FavoriteMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_VIDEOS_TABLE);
        db.execSQL(SQL_CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_MOVIES_TABLE);
        db.execSQL(SQL_DROP_VIDEOS_TABLE);
        db.execSQL(SQL_DROP_REVIEWS_TABLE);
        onCreate(db);
    }

    // From: http://stackoverflow.com/questions/2545558/
    // foreign-key-constraints-in-android-using-sqlite-on-delete-cascade
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}
/**
 * Created by Deepak Gyawali, www.deepakgyawali.com.np
 * Copyright (c) 2018 . All rights reserved.
 * Last modified 2018
 */