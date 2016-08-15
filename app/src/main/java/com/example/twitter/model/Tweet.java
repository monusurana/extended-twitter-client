package com.example.twitter.model;

import android.database.Cursor;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.example.twitter.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by monusurana on 8/3/16.
 */
@Table(name = "Tweets")
public class Tweet extends Model {
    @Column(name = "tweetid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    Long tweetid;
    @Column(name = "created_at")
    String created_at;
    @Column(name = "text")
    String text;
    @Column(name = "retweet_count")
    int retweet_count;
    @Column(name = "favorite_count")
    int favorite_count;
    @Column(name = "favorited")
    boolean favorited;
    @Column(name = "retweeted")
    boolean retweeted;
    @Column(name = "was_retweeted")
    boolean was_retweeted;
    @Column(name = "timestamp")
    Date timestamp;
    @Column(name = "media_url_https")
    String media_url_https;
    @Column(name = "user_mentions")
    boolean user_mentions;
    @Column(name = "retweetedby_screenname")
    String retweetedby_screenname;
    @Column(name = "retweetedby_user")
    String retweetedby_user;
    @Column(name = "User", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    User user;

    public Tweet() {
        super();
    }

    public int getRetweet_count() {
        return retweet_count;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getTweetid() {
        return tweetid;
    }

    public void setTweetid(Long tweetid) {
        this.tweetid = tweetid;
    }

    public int getFavorite_count() {
        return favorite_count;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public boolean isWasRetweeted() {
        return was_retweeted;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMedia_url_https() {
        return media_url_https;
    }

    public boolean isUser_mentions() {
        return user_mentions;
    }

    public String getRetweetedby_screenname() {
        return retweetedby_screenname;
    }

    public String getRetweetedby_user() {
        return retweetedby_user;
    }

    public Tweet(JSONObject object, String screenName) {
        super();

        try {
            this.created_at = object.getString("created_at");
            this.retweet_count = object.getInt("retweet_count");
            this.favorite_count = object.getInt("favorite_count");
            this.favorited = object.getBoolean("favorited");
            this.retweeted = object.getBoolean("retweeted");
            this.timestamp = Utils.getDateFromString(object.getString("created_at"));

            if (object.has("retweeted_status")) {
                this.tweetid = object.getJSONObject("retweeted_status").getLong("id");
                this.text = object.getJSONObject("retweeted_status").getString("text");
                this.user = User.fromJSON(object.getJSONObject("retweeted_status").getJSONObject("user"));
                this.was_retweeted = true;
                this.retweetedby_screenname = object.getJSONObject("user").getString("screen_name");
                this.retweetedby_user = object.getJSONObject("user").getString("name");
            } else {
                this.tweetid = object.getLong("id");
                this.text = object.getString("text");
                this.user = User.findOrCreateFromJson(object.getJSONObject("user"));
                this.was_retweeted = false;
                this.retweetedby_screenname = null;
                this.retweetedby_user = null;
            }

            if (screenName != null && screenName != "MENTIONS") {
                Favorites.saveFavorite(this.tweetid, screenName);
            }

            if (screenName == "MENTIONS") {
                user_mentions = true;
            }

            if (object.has("extended_entities")) {
                if (object.getJSONObject("extended_entities").getJSONArray("media").length() > 0) {
                    this.media_url_https = object.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).getString("media_url_https");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Tweet> fromJSON(JSONArray jsonArray, String screenName) {
        ArrayList<Tweet> tweets = new ArrayList<Tweet>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject tweetJson;
            try {
                tweetJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            findOrCreateFromJson(tweetJson, screenName);
        }
        return tweets;
    }

    public static Tweet findOrCreateFromJson(JSONObject json, String screenName) {
        Tweet tweet = new Tweet(json, screenName);
        tweet.save();
        return tweet;
    }

    public static Cursor fetchResultCursor() {
        From query = new Select()
                .from(Tweet.class)
                .innerJoin(User.class)
                .on("Tweets.User=Users.Id")
                .orderBy("timestamp DESC");

        Cursor resultCursor = Cache.openDatabase().rawQuery(query.toSql(), query.getArguments());
        return resultCursor;
    }

    public static Cursor fetchUserCursor(String screen_name) {
        From query = new Select()
                .from(Tweet.class)
                .innerJoin(User.class)
                .on("Tweets.User=Users.Id")
                .where("Tweets.retweetedby_screenname = ? OR Users.screen_name = ?", screen_name, screen_name)
                .orderBy("timestamp DESC");

        Cursor resultCursor = Cache.openDatabase().rawQuery(query.toSql(), query.getArguments());
        return resultCursor;
    }

    public static Cursor fetchLikes(String screen_name) {
        From query = new Select()
                .from(Favorites.class)
                .innerJoin(Tweet.class)
                .on("Favorites.tweetid=Tweets.tweetid")
                .innerJoin(User.class)
                .on("Tweets.User=Users.Id")
                .where("Favorites.screen_name = ?", screen_name)
                .orderBy("timestamp DESC");

        Cursor resultCursor = Cache.openDatabase().rawQuery(query.toSql(), query.getArguments());
        return resultCursor;
    }

    public static Tweet getTweet(Long tweetid) {
        Tweet existingUser = new Select()
                .from(Tweet.class)
                .where("tweetid = ?", tweetid).executeSingle();

        if (existingUser != null) {
            return existingUser;
        }

        return null;
    }

    public static Cursor fetchMentionsCursor() {
        From query = new Select()
                .from(Tweet.class)
                .innerJoin(User.class)
                .on("Tweets.User=Users.Id")
                .where("Tweets.user_mentions = 1")
                .orderBy("timestamp DESC");

        Cursor resultCursor = Cache.openDatabase().rawQuery(query.toSql(), query.getArguments());
        return resultCursor;
    }
}