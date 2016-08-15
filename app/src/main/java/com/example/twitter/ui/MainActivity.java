package com.example.twitter.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.twitter.R;
import com.example.twitter.app.TwitterApplication;
import com.example.twitter.model.User;
import com.example.twitter.network.TwitterClient;
import com.example.twitter.utils.Constants;
import com.example.twitter.utils.CropCircleTransformation;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tabs)
    TabLayout mTabs;
    @BindView(R.id.viewpager)
    ViewPager mViewpager;
    @BindView(R.id.nav_view)
    NavigationView mNavView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.fab)
    FloatingActionButton mFab;

    ImageView ivProfilePicture;
    TextView tvName;
    TextView tvHandle;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.d("start");

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchComposeActivity();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavView.setNavigationItemSelectedListener(this);

        View header = mNavView.getHeaderView(0);

        ivProfilePicture = (ImageView) header.findViewById(R.id.ivProfileImage);
        tvName = (TextView) header.findViewById(R.id.tvName);
        tvHandle = (TextView) header.findViewById(R.id.tvTwitterHandle);

        setupViewPager(mViewpager);

        if (!PreferenceManager.getDefaultSharedPreferences(this).contains(Constants.LOGGED_IN_USER_SCREEN_NAME)) {
            getUserProfile();
        } else {
            tvName.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.LOGGED_IN_USER_NAME, ""));
            tvHandle.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.LOGGED_IN_USER_SCREEN_NAME, ""));

            Glide.with(ivProfilePicture.getContext())
                    .load(PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.LOGGED_IN_USER_PROFILE, ""))
                    .centerCrop()
                    .bitmapTransform(new CropCircleTransformation(ivProfilePicture.getContext()))
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .crossFade()
                    .into(ivProfilePicture);
        }
    }

    private void getUserProfile() {
        TwitterClient client = TwitterApplication.getRestClient();
        client.getUserProfile(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                User user = User.fromJSON(response);

                tvName.setText(user.getName());
                tvHandle.setText(user.getScreen_name());

                Glide.with(ivProfilePicture.getContext())
                        .load(user.getProfile_image_url_https())
                        .centerCrop()
                        .bitmapTransform(new CropCircleTransformation(ivProfilePicture.getContext()))
                        .crossFade()
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(ivProfilePicture);

                saveUser(user);

                Timber.d("Account: " + response.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
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

    private void launchComposeActivity() {
        Intent i = new Intent(this, ComposeActivity.class);
        i.putExtra("COMPOSE_TYPE", Constants.ComposeType.COMPOSE);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(Constants.SCREEN_NAME, PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.LOGGED_IN_USER_SCREEN_NAME, null));
            startActivity(intent);

        } else if (id == R.id.nav_lists) {

        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void saveUser(User user) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(Constants.LOGGED_IN_USER_SCREEN_NAME, user.getScreen_name())
                .putString(Constants.LOGGED_IN_USER_NAME, user.getName())
                .putString(Constants.LOGGED_IN_USER_PROFILE, user.getProfile_image_url_https())
                .putLong(Constants.USERID, user.getId())
                .apply();
    }

    private void setupViewPager(ViewPager viewPager) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), this);

        adapter.addFrag(HomeFragment.newInstance(Constants.Type.HOME, null), "Home", R.drawable.tab_icon_home);
        adapter.addFrag(HomeFragment.newInstance(Constants.Type.NOTIFICATIONS, null), "Notifications", R.drawable.tab_icon_notifications);

        viewPager.setAdapter(adapter);
        setTitle(adapter.getTitle(0));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setTitle(adapter.getTitle(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mTabs.setupWithViewPager(mViewpager);

        for (int i = 0; i < mTabs.getTabCount(); i++) {
            mTabs.getTabAt(i).setIcon(adapter.getResource(i));
        }
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Integer> mImageResId = new ArrayList<>();
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

        public void addFrag(Fragment fragment, String title, int resId) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
            mImageResId.add(resId);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }

        public String getTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        public int getResource(int position) {
            return mImageResId.get(position);
        }
    }
}
