package com.example.twitter.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.twitter.R;
import com.example.twitter.app.TwitterApplication;
import com.example.twitter.model.User;
import com.example.twitter.network.TwitterClient;
import com.example.twitter.utils.Constants;
import com.example.twitter.utils.RoundedCornersTransformation;
import com.example.twitter.utils.Utils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import timber.log.Timber;

public class ProfileActivity extends AppCompatActivity {

    @BindView(R.id.ivProfileBanner)
    ImageView mIvProfileBanner;
    @BindView(R.id.ivProfileImage)
    ImageView mIvProfileImage;
    @BindView(R.id.tvUserName)
    TextView mTvUserName;
    @BindView(R.id.tvHandle)
    TextView mTvHandle;
    @BindView(R.id.tvDescription)
    TextView mTvDescription;
    @BindView(R.id.tvLocation)
    TextView mTvLocation;
    @BindView(R.id.tvFollowingCount)
    TextView mTvFollowingCount;
    @BindView(R.id.tvFollowersCount)
    TextView mTvFollowersCount;
    @BindView(R.id.vpProfile)
    ViewPager mVpProfile;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.tabs)
    TabLayout mTabs;
    @BindView(R.id.view)
    AppBarLayout mAppBarLayout;

    @BindColor(android.R.color.transparent)
    int mTransparent;
    @BindColor(R.color.colorAccent)
    int accentColor;
    @BindColor(R.color.colorPrimaryDark)
    int mPrimaryDark;
    @BindColor(R.color.colorPrimary)
    int mColorPrimary;

    private String mScreenName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        initToolbar();

        mScreenName = getIntent().getExtras().getString(Constants.SCREEN_NAME);

        Timber.d(mScreenName);

        final User user = User.getUserFromDb(mScreenName);

        if (user == null) {
            getUserProfile(mScreenName);
        } else {
            setProfile(user);
        }

        setupViewPager(mVpProfile);
    }

    private void setProfile(final User user) {
        mTvUserName.setText(user.getName());
        mCollapsingToolbarLayout.setTitleEnabled(false);
        mCollapsingToolbarLayout.setExpandedTitleColor(mTransparent);

        mTvHandle.setText(user.getTwitterScreen_name());
        mTvDescription.setText(user.getDescription());
        mTvLocation.setText(user.getLocation());
        mTvFollowersCount.setText(String.valueOf(user.getFollowers_count()));
        mTvFollowingCount.setText(String.valueOf(user.getFriends_count()));

        if (user.getProfile_banner_url() != null) {
            Glide.with(mIvProfileBanner.getContext())
                    .load(user.getProfile_banner_url())
                    .asBitmap()
                    .centerCrop()
                    .into(new BitmapImageViewTarget(mIvProfileBanner) {
                              @Override
                              public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                  super.onResourceReady(resource, glideAnimation);
                                  Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                      public void onGenerated(Palette palette) {
                                          applyPalette(palette);
                                      }
                                  });
                              }
                          }
                    );
        } else {
            mIvProfileBanner.setBackgroundColor(accentColor);
            mToolbar.setBackgroundColor(accentColor);
        }


        Glide.with(mIvProfileImage.getContext())
                .load(user.getProfile_image_url_https())
                .bitmapTransform(new RoundedCornersTransformation(mIvProfileImage.getContext(), 4, 2))
                .crossFade()
                .into(mIvProfileImage);

//        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//                if (verticalOffset == -mCollapsingToolbarLayout.getHeight() + mToolbar.getHeight()) {
//                    getSupportActionBar().setDisplayShowTitleEnabled(true);
//                    getSupportActionBar().setTitle(user.getName());
//                    getSupportActionBar().setSubtitle(String.valueOf(user.getStatuses_count()) + " Tweets");
//
//                } else {
//                    getSupportActionBar().setDisplayShowTitleEnabled(false);
//                }
//            }
//        });
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void applyPalette(Palette palette) {
        if (Utils.isColorDark(palette.getMutedColor(mColorPrimary))) {
            mCollapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(mColorPrimary));
            mCollapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(mPrimaryDark));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(palette.getDarkMutedColor(mPrimaryDark));
            }
        } else {
            mCollapsingToolbarLayout.setContentScrimColor(accentColor);
            mCollapsingToolbarLayout.setStatusBarScrimColor(mPrimaryDark);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), this);
        adapter.addFrag(HomeFragment.newInstance(Constants.Type.USER_TWEETS, mScreenName), "Tweets");
        adapter.addFrag(HomeFragment.newInstance(Constants.Type.LIKES, mScreenName), "Likes");

        viewPager.setAdapter(adapter);

        mTabs.setupWithViewPager(mVpProfile);
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        private final Context mContext;

        public ViewPagerAdapter(FragmentManager manager, Context context) {
            super(manager);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private void getUserProfile(String screenName) {
        TwitterClient client = TwitterApplication.getRestClient();
        client.getUser(screenName, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                User user = User.fromJSON(response);
                setProfile(user);

                Timber.d("Account: " + response.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);

                User user = null;
                try {
                    user = User.fromJSON((JSONObject) response.get(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setProfile(user);

                Timber.d("Account: " + response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Timber.d("failure: " + errorResponse);
            }
        });
    }
}
