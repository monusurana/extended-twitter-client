package com.example.twitter.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by monusurana on 8/13/16.
 */
@Table(name = "Favorites")
public class Favorites extends Model {
    @Column(name = "tweetid", uniqueGroups = {"Favorites"}, onUniqueConflicts = {Column.ConflictAction.REPLACE})
    Long tweetid;
    @Column(name = "screen_name", uniqueGroups = {"Favorites"}, onUniqueConflicts = {Column.ConflictAction.REPLACE})
    String screen_name;

    public Favorites() {
        super();
    }

    public Favorites(Long tweetid, String screen_name) {
        this.tweetid = tweetid;
        this.screen_name = screen_name;
    }

    public static void saveFavorite(Long tweetid, String screen_name) {
        Favorites favorites = new Favorites(tweetid, screen_name);
        favorites.save();
    }
}
