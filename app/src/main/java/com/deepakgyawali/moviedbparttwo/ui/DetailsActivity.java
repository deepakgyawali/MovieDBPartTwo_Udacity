/**
 * Created by Deepak Gyawali, www.deepakgyawali.com.np
 * Copyright (c) 2018 . All rights reserved.
 * Last modified 2018
 */
package com.deepakgyawali.moviedbparttwo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.deepakgyawali.moviedbparttwo.common.Constants;
import com.deepakgyawali.moviedbparttwo.listener.OnLoadingFragmentListener;
import com.deepakgyawali.moviedbparttwo.model.Movie;
import com.deepakgyawali.moviedbparttwo.R;

// Activity that hosts DetailsFragment and LoadingFragment
public class DetailsActivity extends AppCompatActivity implements OnLoadingFragmentListener {

    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(Constants.EXTRA_MOVIE)) {
                Movie movie = intent
                        .getParcelableExtra(Constants.EXTRA_MOVIE);

                DetailsFragment detailsFragment = DetailsFragment.newInstance(movie);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.details_fragment_container, detailsFragment).commit();
            } else {
                Log.d(LOG_TAG, "Something went wrong. Intent doesn't have Constants.EXTRA_MOVIE" +
                        " extra. Finishing DetailsActivity.");
                finish();
            }
        }
    }

    @Override
    public void onLoadingDisplay(boolean fromDetails, boolean display) {
        Fragment loadingFragment = getSupportFragmentManager()
                .findFragmentByTag(LoadingFragment.FRAGMENT_TAG);
        if (display && loadingFragment == null) {
            loadingFragment = LoadingFragment.newInstance();
            if (fromDetails) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.details_fragment_container,
                                loadingFragment, LoadingFragment.FRAGMENT_TAG).commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.movies_fragment_container,
                                loadingFragment, LoadingFragment.FRAGMENT_TAG).commit();
            }
        } else if (!display && loadingFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(loadingFragment).commit();
        }
    }
}
/**
 * Created by Deepak Gyawali, www.deepakgyawali.com.np
 * Copyright (c) 2018 . All rights reserved.
 * Last modified 2018
 */