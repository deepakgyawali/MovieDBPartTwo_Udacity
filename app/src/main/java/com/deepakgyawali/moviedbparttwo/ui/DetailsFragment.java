/**
 * Created by Deepak Gyawali, www.deepakgyawali.com.np
 * Copyright (c) 2018 . All rights reserved.
 * Last modified 2018
 */
package com.deepakgyawali.moviedbparttwo.ui;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.deepakgyawali.moviedbparttwo.common.Constants;
import com.deepakgyawali.moviedbparttwo.common.Utils;
import com.deepakgyawali.moviedbparttwo.data.FavoriteMoviesContract;
import com.deepakgyawali.moviedbparttwo.listener.OnLoadingFragmentListener;
import com.deepakgyawali.moviedbparttwo.model.Movie;
import com.deepakgyawali.moviedbparttwo.model.Review;
import com.deepakgyawali.moviedbparttwo.model.Video;
import com.deepakgyawali.moviedbparttwo.service.MoviesIntentService;
import com.deepakgyawali.moviedbparttwo.R;

// Fragment that displays detailed info about selected movie
public class DetailsFragment extends Fragment {

    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();
    private static final String ARG_MOVIE = "arg_movie";
    private static final String SAVE_MOVIE = "save_movie";
    private static final String SAVE_FAVORITE_MOVIE = "save_favorite_movie";
    private static final String SAVE_FAVORITE_SORT = "save_favorite_sort";
    private static final String SAVE_FULLY_LOADED = "save_fully_loaded";
    private static final String SAVE_VIDEOS_EXPANDED = "save_videos_expanded";
    private static final String SAVE_REVIEWS_EXPANDED = "save_reviews_expanded";
    private static final String SAVE_SHARE_MENU_VISIBILITY = "save_share_menu_visibility";

    private final ResponseReceiver mReceiver = new ResponseReceiver();
    private Context mContext;
    private Movie mMovie;
    private LinearLayout mVideosExpandable;
    private LinearLayout mVideosContainer;
    private LinearLayout mReviewsExpandable;
    private LinearLayout mReviewsContainer;
    private ShareActionProvider mShareActionProvider;
    private MenuItem mShareMenuItem;
    private ImageView mPosterImageView;
    private OnLoadingFragmentListener mLoadingListener;
    private boolean mIsFavoriteMovie;
    private boolean mIsFavoriteSort;
    private boolean mIsFullyLoaded;
    private boolean mVideosExpanded;
    private boolean mReviewsExpanded;
    private boolean mIsShareMenuItemVisible;

    public DetailsFragment() {
        // Required empty public constructor
    }

    // Create new Fragment instance
    public static DetailsFragment newInstance(Movie movieSelected) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movieSelected);
        fragment.setArguments(args);
        return fragment;
    }

    public static DetailsFragment newInstance() {
        DetailsFragment fragment = new DetailsFragment();
        return fragment;
    }

    // Listener to handle star button clicks. This button adds and remove movies from
    // content provider
    private final View.OnClickListener mStarButtonOnClickListener = new View.OnClickListener() {
        public void onClick(View view) {

            // Can't save it to favorites db if movie poster is not ready yet
            if (mPosterImageView != null && !Utils.hasImage(mPosterImageView)) {
                Toast.makeText(mContext, R.string.please_wait_poster_download,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (mIsFavoriteMovie) {
                if (removeFavoriteMovie(mMovie) > 0) {
                    Toast.makeText(mContext, R.string.success_remove_favorites, Toast
                            .LENGTH_SHORT)
                            .show();
                    ((ImageButton) view).setImageResource(R.drawable.ic_star_border);

                    // Delete poster image stored in internal storage
                    Utils.deleteFileFromInternalStorage(mContext, mMovie.getId());

                    mIsFavoriteMovie = false;
                } else {
                    Toast.makeText(mContext, R.string.fail_remove_favorites,
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                if (addFavoriteMovie(mMovie) != null) {
                    Toast.makeText(mContext, R.string.success_add_favorites, Toast
                            .LENGTH_SHORT).show();
                    ((ImageButton) view).setImageResource(R.drawable.ic_star);

                    // Save poster image to internal storage
                    Bitmap posterBitmap = Utils.getBitmapFromImageView(mPosterImageView);
                    Utils.saveBitmapToInternalStorage(mContext, posterBitmap, mMovie.getId());

                    mIsFavoriteMovie = true;
                } else {
                    Toast.makeText(mContext, R.string.fail_add_favorites, Toast
                            .LENGTH_SHORT).show();
                }
            }
        }
    };

    // Listener to handle custom expandable layout. This layout is used to store Videos and Reviews.
    private final View.OnClickListener mExpandableLayoutOnClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (view.getId() == R.id.videos_expandable) {
                if (mVideosContainer != null && mVideosExpandable != null) {
                    ImageView expandIndicator = (ImageView) mVideosExpandable
                            .findViewById(R.id.videos_expand_indicator);
                    if (mVideosContainer.getVisibility() == View.GONE) {
                        mVideosContainer.setVisibility(View.VISIBLE);
                        mVideosExpanded = true;
                        setExpandIndicator(expandIndicator, mVideosExpanded);
                    } else {
                        mVideosContainer.setVisibility(View.GONE);
                        mVideosExpanded = false;
                        setExpandIndicator(expandIndicator, mVideosExpanded);
                    }
                }
            } else if (view.getId() == R.id.reviews_expandable) {
                if (mReviewsContainer != null && mReviewsExpandable != null) {
                    ImageView expandIndicator = (ImageView) mReviewsExpandable
                            .findViewById(R.id.reviews_expand_indicator);
                    if (mReviewsContainer.getVisibility() == View.GONE) {
                        mReviewsContainer.setVisibility(View.VISIBLE);
                        mReviewsExpanded = true;
                        setExpandIndicator(expandIndicator, mReviewsExpanded);
                    } else {
                        mReviewsContainer.setVisibility(View.GONE);
                        mReviewsExpanded = false;
                        setExpandIndicator(expandIndicator, mReviewsExpanded);
                    }
                }
            }
        }
    };

    // Listener to handle Video buttons. It launches YouTube/Browser.
    private final View.OnClickListener mVideoButtonOnClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (view.getTag() instanceof String) {
                String videoId = (String) view.getTag();
                try {
                    Intent videoIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(Constants.YOUTUBE_BASE_URL + videoId));
                    startActivity(videoIntent);

                } catch (ActivityNotFoundException ex) {
                    Log.d(LOG_TAG, "ActivityNotFoundException. Could not find activity to handle " +
                            "this intent.");
                    ex.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoadingFragmentListener) {
            mLoadingListener = (OnLoadingFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoadingInteractionListener");
        }

        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mLoadingListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
            mIsFavoriteMovie = isFavoriteMovie(mContext, mMovie);
            mIsFavoriteSort = Utils.isFavoriteSort(mContext);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.details_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        mShareMenuItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider
                (mShareMenuItem);

        setShareMenuItemAction();
        super.onPrepareOptionsMenu(menu);
    }

    private void setShareMenuItemAction() {
        if (mMovie != null && mMovie.getVideos() != null && mMovie.getVideos().length > 0) {
            String videoKey = mMovie.getVideos()[0].getKey();
            if (!TextUtils.isEmpty(videoKey) && mShareActionProvider != null
                    && mShareMenuItem != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, Constants.YOUTUBE_BASE_URL + videoKey);
                mShareActionProvider.setShareIntent(shareIntent);
                mShareMenuItem.setVisible(true);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_MOVIE, mMovie);
        outState.putBoolean(SAVE_FAVORITE_MOVIE, mIsFavoriteMovie);
        outState.putBoolean(SAVE_FAVORITE_SORT, mIsFavoriteSort);
        outState.putBoolean(SAVE_FULLY_LOADED, mIsFullyLoaded);
        outState.putBoolean(SAVE_VIDEOS_EXPANDED, mVideosExpanded);
        outState.putBoolean(SAVE_REVIEWS_EXPANDED, mReviewsExpanded);
        outState.putBoolean(SAVE_SHARE_MENU_VISIBILITY, mIsShareMenuItemVisible);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mMovie == null) {
            return null;
        }

        // Restore objects value
        if (savedInstanceState != null) {
            mMovie = savedInstanceState.getParcelable(SAVE_MOVIE);
            mIsFavoriteMovie = savedInstanceState.getBoolean(SAVE_FAVORITE_MOVIE);
            mIsFavoriteSort = savedInstanceState.getBoolean(SAVE_FAVORITE_SORT);
            mIsFullyLoaded = savedInstanceState.getBoolean(SAVE_FULLY_LOADED);
            mVideosExpanded = savedInstanceState.getBoolean(SAVE_VIDEOS_EXPANDED);
            mReviewsExpanded = savedInstanceState.getBoolean(SAVE_REVIEWS_EXPANDED);
            mIsShareMenuItemVisible = savedInstanceState.getBoolean(SAVE_SHARE_MENU_VISIBILITY);
        }

        View view = inflater.inflate(R.layout.fragment_details, container, false);

        mPosterImageView = (ImageView) view.findViewById(R.id.poster);

        mVideosContainer = (LinearLayout) view.findViewById(R.id.videos_container);
        mVideosExpandable = (LinearLayout) view.findViewById(R.id.videos_expandable);
        mReviewsContainer = (LinearLayout) view.findViewById(R.id.reviews_container);
        mReviewsExpandable = (LinearLayout) view.findViewById(R.id.reviews_expandable);

        setExpandListener();

        Glide.with(mContext).load(mMovie.getPosterUri())
                .dontAnimate().into(mPosterImageView);

        TextView titleView = (TextView) view.findViewById(R.id.title_content);
        titleView.setText(mMovie.getTitle());

        TextView releaseDateView = (TextView) view.findViewById(R.id.release_date_content);
        String date = Utils.formatDateForLocale(mMovie.getReleaseDate());
        releaseDateView.setText(date);

        TextView averageView = (TextView) view.findViewById(R.id.vote_average_content);
        averageView.setText(mMovie.getVoteAverage());

        TextView overviewView = (TextView) view.findViewById(R.id.overview_content);

        // In portuguese, some movies does not contain overview data. In that case, displays
        // default text: @string/overview_not_available
        if (!TextUtils.isEmpty(mMovie.getOverview())) {
            overviewView.setText(mMovie.getOverview());
        }

        ImageButton starButton = (ImageButton) view.findViewById(R.id.star_button);
        starButton.setOnClickListener(mStarButtonOnClickListener);

        if (mIsFavoriteMovie) {
            starButton.setImageResource(R.drawable.ic_star);
        } else {
            starButton.setImageResource(R.drawable.ic_star_border);
        }

        starButton.setVisibility(View.VISIBLE);

        FrameLayout detailFrame = (FrameLayout) view.findViewById(R.id.detail_frame);
        detailFrame.setVisibility(View.VISIBLE);

        populateVideosLayout(mContext);
        populateReviewsLayout(mContext);
        return view;
    }

    // Method that adds a Movie to content provider
    private Uri addFavoriteMovie(Movie movie) {

        Uri movieReturnUri = null;
        try {
            ContentValues movieContentValues = createMovieValues(movie);
            movieReturnUri = mContext.getContentResolver().insert(FavoriteMoviesContract
                    .MoviesEntry
                    .CONTENT_URI, movieContentValues);

            if (movie.getVideos() != null && movie.getVideos().length > 0) {
                ContentValues[] videosContentValuesArray = createVideosValues(movie);
                mContext.getContentResolver().bulkInsert(FavoriteMoviesContract.VideosEntry
                        .CONTENT_URI, videosContentValuesArray);
            }

            if (movie.getReviews() != null && movie.getReviews().length > 0) {
                ContentValues[] reviewContentValuesArray = createReviewsValues(movie);
                mContext.getContentResolver().bulkInsert(FavoriteMoviesContract.ReviewsEntry
                        .CONTENT_URI, reviewContentValuesArray);
            }
        } catch (SQLException e) {
            Log.d(LOG_TAG, "SQLException while adding movies to Favorite db");
            e.printStackTrace();
        }

        return movieReturnUri;
    }

    // Method that removes a Movie from content provider
    private int removeFavoriteMovie(Movie movie) {

        int moviesRemoved = mContext.getContentResolver().delete(FavoriteMoviesContract
                        .MoviesEntry.CONTENT_URI,
                FavoriteMoviesContract.MoviesEntry._ID + " = ?", new String[]{movie.getId()});

        return moviesRemoved;
    }

    // Create movie content values
    private ContentValues createMovieValues(Movie movie) {
        ContentValues movieContentValues = new ContentValues();
        movieContentValues.put(FavoriteMoviesContract.MoviesEntry._ID, Integer.parseInt(movie
                .getId()));
        movieContentValues.put(FavoriteMoviesContract.MoviesEntry.COLUMN_TITLE, movie.getTitle());
        movieContentValues.put(FavoriteMoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, movie
                .getReleaseDate());
        movieContentValues.put(FavoriteMoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, movie
                .getVoteAverage());
        movieContentValues.put(FavoriteMoviesContract.MoviesEntry.COLUMN_OVERVIEW, movie
                .getOverview());
        movieContentValues.put(FavoriteMoviesContract.MoviesEntry.COLUMN_PORTER_URI, movie
                .getPosterUri()
                .toString());
        return movieContentValues;
    }

    // Create videos content values array
    private ContentValues[] createVideosValues(Movie movie) {
        Video[] videos = mMovie.getVideos();
        ContentValues[] videoContentValuesArray = new ContentValues[videos.length];
        for (int i = 0; i < videos.length; i++) {
            videoContentValuesArray[i] = new ContentValues();
            videoContentValuesArray[i].put(FavoriteMoviesContract.VideosEntry._ID, videos[i]
                    .getId());
            videoContentValuesArray[i].put(FavoriteMoviesContract.VideosEntry.COLUMN_MOVIE_ID,
                    movie.getId());
            videoContentValuesArray[i].put(FavoriteMoviesContract.VideosEntry.COLUMN_KEY,
                    videos[i].getKey());
            videoContentValuesArray[i].put(FavoriteMoviesContract.VideosEntry.COLUMN_NAME,
                    videos[i].getName());
        }

        return videoContentValuesArray;
    }

    // Create reviews content values array
    private ContentValues[] createReviewsValues(Movie movie) {
        Review[] reviews = mMovie.getReviews();
        ContentValues[] reviewContentValuesArray = new ContentValues[reviews.length];
        for (int i = 0; i < reviews.length; i++) {
            reviewContentValuesArray[i] = new ContentValues();
            reviewContentValuesArray[i].put(FavoriteMoviesContract.ReviewsEntry._ID, reviews[i]
                    .getId());
            reviewContentValuesArray[i].put(FavoriteMoviesContract.ReviewsEntry.COLUMN_MOVIE_ID,
                    movie.getId());
            reviewContentValuesArray[i].put(FavoriteMoviesContract.ReviewsEntry.COLUMN_AUTHOR,
                    reviews[i].getAuthor());
            reviewContentValuesArray[i].put(FavoriteMoviesContract.ReviewsEntry.COLUMN_CONTENT,
                    reviews[i].getContent());
        }

        return reviewContentValuesArray;
    }

    // Method that query content provider and checks whether is a Favorite movie or not
    private boolean isFavoriteMovie(Context ctx, Movie movie) {
        String movieID = movie.getId();
        Cursor cursor = ctx.getContentResolver().query(FavoriteMoviesContract.MoviesEntry
                        .CONTENT_URI, null,
                FavoriteMoviesContract.MoviesEntry._ID + " = " + movieID, null, null);
        if (cursor != null && cursor.moveToNext()) {
            int movieIdColumnIndex = cursor.getColumnIndex(FavoriteMoviesContract.MoviesEntry._ID);
            if (TextUtils.equals(movieID, cursor.getString(movieIdColumnIndex))) {
                return true;
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mMovie != null) {
            if (mReceiver != null) {
                LocalBroadcastManager.getInstance(mContext)
                        .registerReceiver(mReceiver, new IntentFilter(Constants
                                .ACTION_EXTRA_INFO_RESULT));
            }
            if (!mIsFullyLoaded && !mIsFavoriteSort) {
                Intent intent = new Intent(mContext, MoviesIntentService.class);
                intent.setAction(Constants.ACTION_EXTRA_INFO_REQUEST);
                intent.putExtra(MoviesIntentService.EXTRA_INFO_MOVIE_ID, mMovie.getId());
                mContext.startService(intent);

                if (mLoadingListener != null) {
                    mLoadingListener.onLoadingDisplay(true, true);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
        }
    }

    // Method that populates Videos expandable layout after the network request
    private void populateVideosLayout(Context ctx) {
        Video[] videos = mMovie.getVideos();

        if (mVideosContainer != null && mVideosExpandable != null) {
            if (videos != null && videos.length > 0) {
                if (mVideosContainer.getChildCount() > 0) {
                    mVideosContainer.removeAllViews();
                }

                LayoutInflater layoutInflater = (LayoutInflater)
                        ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                for (int i = 0; i < videos.length; i++) {
                    LinearLayout videoLayout = (LinearLayout) layoutInflater.inflate(R.layout
                            .video_item, null);
                    Button videoButton = (Button) videoLayout.findViewById(R.id.video_button);
                    videoButton.setText(String.format(ctx.getString(R.string.trailer_item),
                            i + 1));
                    // Set View's tag with YouTube video id
                    videoButton.setTag(videos[i].getKey());
                    videoButton.setOnClickListener(mVideoButtonOnClickListener);
                    mVideosContainer.addView(videoLayout);
                }

                TextView reviewsHeader = (TextView) mVideosExpandable
                        .findViewById(R.id.videos_header);
                reviewsHeader.setText(String.format(getString(R.string.header_videos),
                        videos.length));
                ImageView expandIndicator = (ImageView) mVideosExpandable
                        .findViewById(R.id.videos_expand_indicator);
                setExpandIndicator(expandIndicator, mVideosExpanded);

                if (mVideosExpanded) {
                    mVideosContainer.setVisibility(View.VISIBLE);
                } else {
                    mVideosContainer.setVisibility(View.GONE);
                }

            } else {

                TextView reviewsHeader = (TextView) mVideosExpandable
                        .findViewById(R.id.videos_header);
                reviewsHeader.setText(String.format(getString(R.string.header_videos), 0));
            }
        }
    }

    // Method that populates Reviews expandable layout after the network request
    private void populateReviewsLayout(Context ctx) {
        Review[] reviews = mMovie.getReviews();

        if (mReviewsContainer != null && mReviewsExpandable != null) {
            if (reviews != null && reviews.length > 0) {
                if (mReviewsContainer.getChildCount() > 0) {
                    mReviewsContainer.removeAllViews();
                }

                LayoutInflater layoutInflater = (LayoutInflater)
                        ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                for (Review review : reviews) {
                    LinearLayout reviewLayout = (LinearLayout) layoutInflater.inflate(R.layout
                            .review_item, null);
                    TextView authorTextView = (TextView) reviewLayout.findViewById(R.id
                            .author_name);
                    TextView contentTextView = (TextView) reviewLayout.findViewById(R.id.content);
                    authorTextView.setText(review.getAuthor());
                    contentTextView.setText(review.getContent());
                    mReviewsContainer.addView(reviewLayout);
                }

                TextView reviewsHeader = (TextView) mReviewsExpandable
                        .findViewById(R.id.reviews_header);
                reviewsHeader.setText(String.format(getString(R.string.header_reviews),
                        reviews.length));
                ImageView expandIndicator = (ImageView) mReviewsExpandable
                        .findViewById(R.id.reviews_expand_indicator);
                setExpandIndicator(expandIndicator, mReviewsExpanded);

                if (mReviewsExpanded) {
                    mReviewsContainer.setVisibility(View.VISIBLE);
                } else {
                    mReviewsContainer.setVisibility(View.GONE);
                }

            } else {
                TextView reviewsHeader = (TextView) mReviewsExpandable
                        .findViewById(R.id.reviews_header);
                reviewsHeader.setText(String.format(getString(R.string.header_reviews), 0));
            }
        }
    }

    // Method to set Background Resource based on current state of expandable layout
    private void setExpandIndicator(ImageView imageView, boolean isExpanded) {
        if (isExpanded) {
            imageView.setBackgroundResource(R.drawable.ic_collapse);
        } else {
            imageView.setBackgroundResource(R.drawable.ic_expand);
        }
    }

    // Method to set the expandable layout listener
    private void setExpandListener() {
        if (mMovie.getVideos() != null && mMovie.getVideos().length > 0) {
            mVideosExpandable.setOnClickListener(mExpandableLayoutOnClickListener);
        } else {
            mVideosExpandable.setOnClickListener(null);
        }

        if (mMovie.getReviews() != null && mMovie.getReviews().length > 0) {
            mReviewsExpandable.setOnClickListener(mExpandableLayoutOnClickListener);
        } else {
            mReviewsExpandable.setOnClickListener(null);
        }
    }

    // BroadcastReceiver for network call
    public class ResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }

            if (intent.getAction().equals(Constants.ACTION_EXTRA_INFO_RESULT)
                    && intent.hasExtra(MoviesIntentService.EXTRA_INFO_VIDEOS_RESULT)
                    && intent.hasExtra(MoviesIntentService.EXTRA_INFO_REVIEWS_RESULT)) {

                Video[] videos = (Video[]) intent.getParcelableArrayExtra(MoviesIntentService
                        .EXTRA_INFO_VIDEOS_RESULT);
                Review[] reviews = (Review[]) intent.getParcelableArrayExtra(MoviesIntentService
                        .EXTRA_INFO_REVIEWS_RESULT);

                mMovie.setVideos(videos);
                mMovie.setReviews(reviews);

                setExpandListener();
                populateVideosLayout(mContext);
                populateReviewsLayout(mContext);
                setShareMenuItemAction();
            } else {
                Toast.makeText(mContext, R.string.toast_failed_to_retrieve_data,
                        Toast.LENGTH_SHORT).show();
            }

            if (mLoadingListener != null) {
                mLoadingListener.onLoadingDisplay(true, false);
            }

            mIsFullyLoaded = true;
        }
    }
}
/**
 * Created by Deepak Gyawali, www.deepakgyawali.com.np
 * Copyright (c) 2018 . All rights reserved.
 * Last modified 2018
 */