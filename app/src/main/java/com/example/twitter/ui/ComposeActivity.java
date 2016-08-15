package com.example.twitter.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.twitter.R;
import com.example.twitter.app.TwitterApplication;
import com.example.twitter.model.Tweet;
import com.example.twitter.model.User;
import com.example.twitter.network.TwitterClient;
import com.example.twitter.utils.Constants;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import timber.log.Timber;

public class ComposeActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tvCompose)
    EditText mTvCompose;
    @BindView(R.id.btnTweet)
    Button mBtnTweet;
    @BindView(R.id.tvCharCount)
    TextView mTvCharCount;
    @BindView(R.id.tvReplyingTo)
    TextView mTvReplyingTo;
    @BindView(R.id.ivReplyingTo)
    ImageView mIvReplyingTo;

    @BindColor(R.color.red_500)
    int mRed;
    @BindColor(R.color.colorPrimaryDark)
    int mPrimaryDark;

    Constants.ComposeType mType;
    User mUser;
    Long mTweetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        ButterKnife.bind(this);

        initToolbar();

        mType = (Constants.ComposeType) getIntent().getSerializableExtra(Constants.COMPOSE_TYPE);
        mTweetId = getIntent().getLongExtra(Constants.TWEETID, 0L);

        if (mType == Constants.ComposeType.COMPOSE) {
            setTitle(getString(R.string.compose));
            mIvReplyingTo.setVisibility(View.GONE);
            mTvReplyingTo.setVisibility(View.GONE);
            mBtnTweet.setText(getString(R.string.tweet));
        } else {
            setTitle(getString(R.string.reply));
            mIvReplyingTo.setVisibility(View.VISIBLE);
            mTvReplyingTo.setVisibility(View.VISIBLE);
            mBtnTweet.setText(getString(R.string.reply));

            mUser = Parcels.unwrap(getIntent().getParcelableExtra(Constants.USER));
            mTvReplyingTo.setText(String.format(getString(R.string.replying_to), mUser.getName()));
        }

        mTvCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int remainingCount = 140 - s.length();
                mTvCharCount.setText(String.valueOf(remainingCount));

                if (remainingCount < 0) {
                    mTvCharCount.setTextColor(mRed);
                } else {
                    mTvCharCount.setTextColor(mPrimaryDark);
                }

                if (s.length() == 0 || remainingCount < 0) {
                    mBtnTweet.setEnabled(false);
                } else {
                    mBtnTweet.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //dsd
            }
        });

        if (mType == Constants.ComposeType.REPLY) {
            mTvCompose.append(mUser.getTwitterScreen_name());
            mTvCompose.setFocusable(true);
        }

        mBtnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("Tweet Button Clicked");
                if (mType == Constants.ComposeType.COMPOSE)
                    postTweet(null);
                else
                    postTweet(Long.toString(mTweetId));
            }
        });
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
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

    private void postTweet(String tweetId) {
        TwitterClient client = TwitterApplication.getRestClient();
        client.postTweet(mTvCompose.getText().toString(), tweetId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Timber.d("Success: " + response.toString());

                Tweet.findOrCreateFromJson(response, null);
                finish();
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
}
