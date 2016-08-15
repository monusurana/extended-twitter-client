package com.example.twitter.utils;

/**
 * Created by monusurana on 8/4/16.
 */
public class Constants {

    public static final String COMPOSE_TYPE = "COMPOSE_TYPE";
    public static final String USER = "USER";
    public static final String TWEETID = "ID";
    public static final String TYPE = "TYPE";
    public static final String LOGGED_IN_USER_SCREEN_NAME = "LOGGED_IN_USER_SCREEN_NAME";
    public static final String LOGGED_IN_USER_NAME = "LOGGED_IN_USER_NAME";
    public static final String LOGGED_IN_USER_PROFILE = "LOGGED_IN_USER_PROFILE";
    public static final String USERID = "USER_ID";
    public static final String SCREEN_NAME = "SCREEN_NAME";

    public enum Type {
        HOME,
        NOTIFICATIONS,
        USER_TWEETS,
        LIKES
    }

    public enum ComposeType {
        COMPOSE,
        REPLY
    }
}
