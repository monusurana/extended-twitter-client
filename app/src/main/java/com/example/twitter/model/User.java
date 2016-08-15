package com.example.twitter.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

/**
 * Created by monusurana on 8/3/16.
 */
@Parcel(analyze = User.class)
@Table(name = "Users")
public class User extends Model {
    @Column(name = "screen_name", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    String screen_name;
    @Column(name = "name")
    String name;
    @Column(name = "description")
    String description;
    @Column(name = "location")
    String location;
    @Column(name = "followers_count")
    int followers_count;
    @Column(name = "friends_count")
    int friends_count;
    @Column(name = "statuses_count")
    int statuses_count;
    @Column(name = "profile_banner_url")
    String profile_banner_url;
    @Column(name = "profile_image_url_https")
    String profile_image_url_https;

    public User() {

    }

    public User(JSONObject object) {
        super();

        try {
            this.screen_name = object.getString("screen_name");
            this.name = object.getString("name");
            this.description = object.getString("description");
            this.followers_count = object.getInt("followers_count");
            this.friends_count = object.getInt("friends_count");
            this.statuses_count = object.getInt("statuses_count");
            this.location = object.getString("location");

            if (object.has("profile_banner_url"))
                this.profile_banner_url = object.getString("profile_banner_url");

            if (object.has("profile_image_url_https"))
                this.profile_image_url_https = object.getString("profile_image_url_https");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreen_name() {
        return screen_name;
    }

    public String getTwitterScreen_name() {
        return "@" + screen_name;
    }

    public void setScreen_name(String screen_name) {
        this.screen_name = screen_name;
    }

    public String getProfile_image_url_https() {
        return profile_image_url_https;
    }

    public String getDescription() {
        return description;
    }

    public int getFollowers_count() {
        return followers_count;
    }

    public int getFriends_count() {
        return friends_count;
    }

    public String getProfile_banner_url() {
        return profile_banner_url;
    }

    public String getLocation() {
        return location;
    }

    public int getStatuses_count() {
        return statuses_count;
    }

    public static User fromJSON(JSONObject jsonObject) {
        User user = new User(jsonObject);
        user.save();

        return user;
    }

    public static User findOrCreateFromJson(JSONObject json) {
        String rId;
        try {
            rId = json.getString("screen_name");

            User existingUser =
                    new Select().from(User.class).where("screen_name = ?", rId).executeSingle();
            if (existingUser != null) {
                return existingUser;
            } else {
                User user = User.fromJSON(json);
                return user;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static User getUserFromDb(String screen_name) {
        User existingUser =
                new Select().from(User.class).where("screen_name = ?", screen_name).executeSingle();
        if (existingUser != null) {
            return existingUser;
        }

        return null;
    }
}
