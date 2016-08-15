package com.example.twitter.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.twitter.R;
import com.example.twitter.app.TwitterApplication;
import com.example.twitter.model.Tweet;
import com.example.twitter.network.TwitterClient;
import com.example.twitter.receivers.NetworkStatus;
import com.example.twitter.utils.Constants;
import com.example.twitter.utils.DividerItemDecoration;
import com.example.twitter.utils.EndlessRecyclerViewScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import timber.log.Timber;

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.rvHomeLine)
    RecyclerView mRvHomeLine;
    @BindView(R.id.pbLoading)
    ProgressBar mPbLoading;
    @BindView(R.id.tvError)
    TextView mTvError;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private HomeRecyclerViewAdapter mAdapter;
    Constants.Type mType;
    private Cursor mCursor;
    String mScreenName;
    TwitterClient client;

    JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            super.onSuccess(statusCode, headers, response);

            Timber.d("timeline: " + response.toString());
            Tweet.fromJSON(response, getName());

            mCursor.close();
            mCursor = getCursor();
            mAdapter.changeCursor(mCursor);
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
            super.onFailure(statusCode, headers, throwable, errorResponse);
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            super.onFailure(statusCode, headers, throwable, errorResponse);

            Timber.d("failure: " + errorResponse);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    };

    public HomeFragment() {

    }

    public static HomeFragment newInstance(Constants.Type type, String screenName) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.TYPE, type.ordinal());
        args.putString(Constants.SCREEN_NAME, screenName);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mType = Constants.Type.values()[getArguments().getInt(Constants.TYPE)];
        mScreenName = getArguments().getString(Constants.SCREEN_NAME);
        mCursor = getCursor();
        client = TwitterApplication.getRestClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setOnRefreshListener(this);

            mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
        }

        setUpRecyclerView();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchData(1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mCursor != null)
            mCursor.close();
    }

    @Override
    public void onRefresh() {
        fetchData(1);
    }

    private void setUpRecyclerView() {
        mAdapter = new HomeRecyclerViewAdapter(mCursor);
        mAdapter.setOnItemClickListener(new HomeRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Long tweetid, View parent) {
                Intent intent = new Intent(getActivity(), TweetDetailActivity.class);
                intent.putExtra(Constants.TWEETID, tweetid);
                startActivity(intent);
            }

            @Override
            public void onProfileClick(String screenName) {
                if (mScreenName == null || !mScreenName.equals(screenName)) {
                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.putExtra(Constants.SCREEN_NAME, screenName);
                    startActivity(intent);
                } else {
                    Timber.d("Clicking on existing user!");
                }
            }

            @Override
            public void onReplyClick(Long tweetid, View parent) {
                Intent i = new Intent(getActivity(), ComposeActivity.class);
                i.putExtra(Constants.COMPOSE_TYPE, Constants.ComposeType.REPLY);
                i.putExtra(Constants.USER, Parcels.wrap(Tweet.getTweet(tweetid).getUser()));
                i.putExtra(Constants.TWEETID, tweetid);
                startActivity(i);
            }

            @Override
            public void onRetweetClick(Long tweetid, boolean retweet) {
                if (!retweet)
                    reTweet(String.valueOf(tweetid));
                else
                    Timber.d("Alreadt retweeted");
            }

            @Override
            public void onLikeClick(Long tweetid, boolean like) {
                likeTweet(String.valueOf(tweetid), like);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRvHomeLine.setLayoutManager(linearLayoutManager);
        mRvHomeLine.setAdapter(mAdapter);
        mRvHomeLine.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        mRvHomeLine.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Timber.d("Page " + page + "Total Count " + totalItemsCount);
                fetchData(page + 1);
            }
        });
    }

    private void fetchData(final int page) {
        if (!NetworkStatus.getInstance(getActivity()).isOnline()) {
            if (mSwipeRefreshLayout != null) {

                mSwipeRefreshLayout.setRefreshing(false);

                Snackbar snackbar = Snackbar
                        .make(getView(), getString(R.string.network_not_available), Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                fetchData(page);
                            }
                        });

                snackbar.show();
            }
        } else {
            getTimelines(page);
        }
    }

    private void getTimelines(int page) {
        if (mType == Constants.Type.HOME) {
            client.getHomeTimeline(page, jsonHttpResponseHandler);
        } else if (mType == Constants.Type.NOTIFICATIONS) {
            client.getMentionsTimeline(page, jsonHttpResponseHandler);
        } else if (mType == Constants.Type.USER_TWEETS) {
            client.getUserTimeline(mScreenName, page, jsonHttpResponseHandler);
        } else if (mType == Constants.Type.LIKES) {
            client.getLikes(mScreenName, page, jsonHttpResponseHandler);
        }
    }

    private String getName() {
        if (mType == Constants.Type.HOME) {
            return null;
        } else if (mType == Constants.Type.NOTIFICATIONS) {
            return "MENTIONS";
        } else if (mType == Constants.Type.USER_TWEETS) {
            return null;
        } else if (mType == Constants.Type.LIKES) {
            return mScreenName;
        }

        return null;
    }

    private Cursor getCursor() {
        if (mType == Constants.Type.HOME) {
            return Tweet.fetchResultCursor();
        } else if (mType == Constants.Type.NOTIFICATIONS) {
            return Tweet.fetchMentionsCursor();
        } else if (mType == Constants.Type.USER_TWEETS) {
            return Tweet.fetchUserCursor(mScreenName);
        } else if (mType == Constants.Type.LIKES) {
            return Tweet.fetchLikes(mScreenName);
        }

        return null;
    }

    private void reTweet(String tweetId) {
        client.retweet(tweetId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Timber.d("Retweet Success: " + response.toString());

                Tweet.findOrCreateFromJson(response, getName());

                mCursor.close();
                mCursor = getCursor();
                mAdapter.changeCursor(mCursor);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject
                    errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                Timber.d("Failure: " + errorResponse);
            }
        });
    }

    private void likeTweet(String tweetId, boolean like) {
        JsonHttpResponseHandler likeJsonHttpResponseHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Timber.d("Like Tweet Success: " + response.toString());

                Tweet.findOrCreateFromJson(response, getName());

                mCursor.close();
                mCursor = getCursor();
                mAdapter.changeCursor(mCursor);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject
                    errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                Timber.d("Failure: " + errorResponse);
            }
        };

        if (!like)
            client.likeTweet(tweetId, likeJsonHttpResponseHandler);
        else
            client.unlikeTweet(tweetId, likeJsonHttpResponseHandler);
    }
}
