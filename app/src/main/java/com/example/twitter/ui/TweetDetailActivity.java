package com.example.twitter.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.twitter.R;
import com.example.twitter.app.TwitterApplication;
import com.example.twitter.model.Tweet;
import com.example.twitter.model.User;
import com.example.twitter.network.TwitterClient;
import com.example.twitter.utils.Constants;
import com.example.twitter.utils.RoundedCornersTransformation;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import timber.log.Timber;

public class TweetDetailActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.ivRetweeted)
    ImageView mIvRetweeted;
    @BindView(R.id.tvRetweetedBy)
    TextView mTvRetweetedBy;
    @BindView(R.id.tvUserName)
    TextView mTvUserName;
    @BindView(R.id.imUserImage)
    ImageView mImUserImage;
    @BindView(R.id.tvTwitterHandle)
    TextView mTvTwitterHandle;
    @BindView(R.id.tvDescription)
    TextView mTvDescription;
    @BindView(R.id.ivMedia)
    ImageView mIvMedia;
    @BindView(R.id.tvRetweetsCount)
    TextView mTvRetweetsCount;
    @BindView(R.id.tvLikesCount)
    TextView mTvLikesCount;
    @BindView(R.id.ivReply)
    ImageView mIvReply;
    @BindView(R.id.ivRetweet)
    ImageView mIvRetweet;
    @BindView(R.id.ivLike)
    ImageView mIvLike;

    private User mUser;
    private Long mTweetId;
    private Tweet t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);
        ButterKnife.bind(this);

        initToolbar();

        Long tweetid = getIntent().getLongExtra(Constants.TWEETID, 0L);

        t = Tweet.getTweet(tweetid);

        mUser = t.getUser();
        mTweetId = t.getId();

        mTvUserName.setText(t.getUser().getName());
        mTvDescription.setText(t.getText());
        mTvTwitterHandle.setText(t.getUser().getTwitterScreen_name());

        if (t.isWasRetweeted()) {
            mIvRetweeted.setVisibility(View.VISIBLE);
            mTvRetweetedBy.setVisibility(View.VISIBLE);

            mTvRetweetedBy.setText(t.getRetweetedby_user() + " Retweeted");
        } else {
            mIvRetweeted.setVisibility(View.GONE);
            mTvRetweetedBy.setVisibility(View.GONE);
        }

        if (t.isFavorited()) {
            mIvLike.setImageResource(R.drawable.ic_like_red);
        } else {
            mIvLike.setImageResource(R.drawable.ic_like);
        }

        if (t.isRetweeted()) {
            mIvRetweet.setImageResource(R.drawable.ic_retweet_green);
        } else {
            mIvRetweet.setImageResource(R.drawable.ic_retweet);
        }

        mTvRetweetsCount.setText(String.valueOf(t.getRetweet_count()));
        mTvLikesCount.setText(String.valueOf(t.getFavorite_count()));

        if (t.getMedia_url_https() != null) {
            mIvMedia.setVisibility(View.VISIBLE);
            Glide.with(mIvMedia.getContext())
                    .load(t.getMedia_url_https())
                    .placeholder(R.color.colorAccent)
                    .centerCrop()
                    .crossFade()
                    .into(mIvMedia);
        } else {
            mIvMedia.setVisibility(View.GONE);
        }

        Glide.with(mImUserImage.getContext())
                .load(t.getUser().getProfile_image_url_https())
                .centerCrop()
                .placeholder(R.color.grey_200)
                .bitmapTransform(new RoundedCornersTransformation(mImUserImage.getContext(), 8, 2))
                .crossFade()
                .into(mImUserImage);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @OnClick(R.id.ivReply)
    public void reply() {
        Intent i = new Intent(this, ComposeActivity.class);
        i.putExtra(Constants.COMPOSE_TYPE, Constants.ComposeType.REPLY);
        i.putExtra(Constants.USER, Parcels.wrap(mUser));
        i.putExtra(Constants.TWEETID, mTweetId);
        startActivity(i);
    }

    @OnClick(R.id.imUserImage)
    public void profile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(Constants.SCREEN_NAME, mUser.getScreen_name());
        startActivity(intent);
    }

    @OnClick(R.id.ivRetweet)
    public void reTweet() {
        TwitterClient client = TwitterApplication.getRestClient();
        client.retweet(mTweetId.toString(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Timber.d("Retweet Success: " + response.toString());

                Tweet.findOrCreateFromJson(response, null);

                if (!t.isRetweeted()) {
                    mIvRetweet.setImageResource(R.drawable.ic_retweet_green);
                } else {
                    mIvRetweet.setImageResource(R.drawable.ic_retweet);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);

                Tweet.fromJSON(response, null);

                if (!t.isRetweeted()) {
                    mIvRetweet.setImageResource(R.drawable.ic_retweet_green);
                } else {
                    mIvRetweet.setImageResource(R.drawable.ic_retweet);
                }
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

    @OnClick(R.id.ivLike)
    public void likeTweet() {
        TwitterClient client = TwitterApplication.getRestClient();
        JsonHttpResponseHandler likeJsonHttpResponseHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Timber.d("Like Tweet Success: " + response.toString());

                Tweet.findOrCreateFromJson(response, null);

                if (!t.isFavorited()) {
                    mIvLike.setImageResource(R.drawable.ic_like_red);
                } else {
                    mIvLike.setImageResource(R.drawable.ic_like);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);

                Timber.d("Like Tweet Success: " + response.toString());

                Tweet.fromJSON(response, null);

                if (!t.isFavorited()) {
                    mIvLike.setImageResource(R.drawable.ic_like_red);
                } else {
                    mIvLike.setImageResource(R.drawable.ic_like);
                }
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

        if (!t.isFavorited()) {
            Timber.d("Liked");
            client.likeTweet(mTweetId.toString(), likeJsonHttpResponseHandler);
        } else {
            Timber.d("UnLiked");
            client.unlikeTweet(mTweetId.toString(), likeJsonHttpResponseHandler);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
