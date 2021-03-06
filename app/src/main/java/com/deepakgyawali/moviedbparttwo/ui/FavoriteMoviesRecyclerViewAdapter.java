/**
 * Created by Deepak Gyawali, www.deepakgyawali.com.np
 * Copyright (c) 2018 . All rights reserved.
 * Last modified 2018
 */
package com.deepakgyawali.moviedbparttwo.ui;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.deepakgyawali.moviedbparttwo.data.FavoriteMoviesContract;
import com.deepakgyawali.moviedbparttwo.R;
import com.deepakgyawali.moviedbparttwo.common.Utils;
import com.deepakgyawali.moviedbparttwo.listener.OnMoviesListFragmentListener;
import com.deepakgyawali.moviedbparttwo.model.Movie;

// Class that manages the list of favorite movies (RecyclerView).
// Implementation based on http://www.blogc.at/2015/10/13/
// recyclerview-adapters-part-2-recyclerview-cursor-adapter/
public class FavoriteMoviesRecyclerViewAdapter extends RecyclerView
        .Adapter<FavoriteMoviesRecyclerViewAdapter.ViewHolder> {

    private static final String LOG_TAG = FavoriteMoviesRecyclerViewAdapter.class.getSimpleName();

    private final OnMoviesListFragmentListener mOnMoviesListFragmentListener;
    private Cursor mCursor;

    @Override
    public final void onBindViewHolder(final ViewHolder holder, final int position) {
        final Cursor cursor = getItem(position);
        onBindViewHolder(holder, cursor);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_movie_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final ImageView mPosterView;
        public final TextView mTitle;

        public int mCursorPosition;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mPosterView = (ImageView) view.findViewById(R.id.poster);
            mTitle = (TextView) view.findViewById(R.id.title);
            mCursorPosition = -1;
        }
    }

    public Cursor getItem(final int position) {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.moveToPosition(position);
        }

        return mCursor;
    }

    public FavoriteMoviesRecyclerViewAdapter(
            OnMoviesListFragmentListener listener) {
        mOnMoviesListFragmentListener = listener;
    }

    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }

    private void onBindViewHolder(final ViewHolder holder, final Cursor cursor) {
        String movieTitle = cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract
                .MoviesEntry.COLUMN_TITLE));
        String posterUri = cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract
                .MoviesEntry.COLUMN_PORTER_URI));
        int cursorPosition = cursor.getPosition();

        holder.mCursorPosition = cursorPosition;
        holder.mTitle.setText(movieTitle);

        Glide.with(holder.mPosterView.getContext()).load(posterUri)
                .dontTransform().diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate().into(holder.mPosterView);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnMoviesListFragmentListener != null) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    cursor.moveToPosition(holder.mCursorPosition);
                    Movie movie = Utils.createMovieFromCursor(cursor);
                    mOnMoviesListFragmentListener.onFavoriteMovieSelected(movie);
                }
            }
        });
    }
}
/**
 * Created by Deepak Gyawali, www.deepakgyawali.com.np
 * Copyright (c) 2018 . All rights reserved.
 * Last modified 2018
 */