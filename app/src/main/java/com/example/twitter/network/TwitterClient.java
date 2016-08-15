package com.example.twitter.network;

import android.content.Context;

import com.codepath.oauth.OAuthBaseClient;
import com.example.twitter.BuildConfig;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

/*
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
    public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class;
    public static final String REST_URL = "https://api.twitter.com/1.1";
    public static final String REST_CONSUMER_KEY = BuildConfig.CONSUMER_KEY;
    public static final String REST_CONSUMER_SECRET = BuildConfig.CONSUMER_SECRET;
    public static final String REST_CALLBACK_URL = "https://letmetweettoo.com";

    public TwitterClient(Context context) {
        super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
    }

    public void getHomeTimeline(int page, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        RequestParams params = new RequestParams();
        params.put("page", String.valueOf(page));
        getClient().get(apiUrl, params, handler);
    }

    public void getMentionsTimeline(int page, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/mentions_timeline.json");
        RequestParams params = new RequestParams();
        params.put("page", String.valueOf(page));
        getClient().get(apiUrl, params, handler);
    }

    public void getUser(String screenName, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("users/search.json");
        RequestParams params = new RequestParams();
        params.put("q", screenName);
        getClient().get(apiUrl, params, handler);
    }

    public void getUserTimeline(String screenName, int page, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/user_timeline.json");
        RequestParams params = new RequestParams();
        params.put("screen_name", screenName);
        params.put("page", String.valueOf(page));
        getClient().get(apiUrl, params, handler);
    }

    public void getLikes(String screenName, int page, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("favorites/list.json");
        RequestParams params = new RequestParams();
        params.put("screen_name", screenName);
        params.put("page", String.valueOf(page));
        getClient().get(apiUrl, params, handler);
    }

    public void getUserProfile(AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("account/verify_credentials.json");
        getClient().get(apiUrl, null, handler);
    }

    public void retweet(String tweetid, AsyncHttpResponseHandler handler) {
        String api_url = getApiUrl("statuses/retweet/" + tweetid + ".json");
        RequestParams params = new RequestParams();
        getClient().post(api_url, params, handler);
    }

    public void likeTweet(String tweetid, AsyncHttpResponseHandler handler) {
        String api_url = getApiUrl("favorites/create.json");
        RequestParams params = new RequestParams();
        params.put("id", tweetid);
        getClient().post(api_url, params, handler);
    }

    public void unlikeTweet(String tweetid, AsyncHttpResponseHandler handler) {
        String api_url = getApiUrl("favorites/destroy.json");
        RequestParams params = new RequestParams();
        params.put("id", tweetid);
        getClient().post(api_url, params, handler);
    }

    public void postTweet(String body, String tweetId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", body);

        if (tweetId != null)
            params.put("in_reply_to_status_id", tweetId);

        getClient().post(apiUrl, params, handler);
    }
}