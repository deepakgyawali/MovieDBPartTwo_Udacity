/**
 * Created by Deepak Gyawali, www.deepakgyawali.com.np
 * Copyright (c) 2018 . All rights reserved.
 * Last modified 2018
 */

package com.deepakgyawali.moviedbparttwo.listener;


import com.deepakgyawali.moviedbparttwo.model.Movie;

public interface OnMoviesListFragmentListener {
    void onMoviesSelected(Movie movie);
    void onFavoriteMovieSelected(Movie movie);
    void onUpdateMoviesListVisibility();
    void onUpdateMovieDetails();
}

