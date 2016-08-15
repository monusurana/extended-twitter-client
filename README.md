# Project 4 - *Extended Simple Twitter*

**Simple Twitter** is an android app that allows a user to view home and mentions timelines, view user profiles with user timelines, as well as compose and post a new tweet. The app utilizes [Twitter REST API](https://dev.twitter.com/rest/public).

Time spent: **15** hours spent in total

## User Stories

The following **required** functionality is completed:

* [X] The app includes **all required user stories** from Week 3 Twitter Client
* [X] User can **switch between Timeline and Mention views using tabs**
  * [X] User can view their home timeline tweets.
  * [X] User can view the recent mentions of their username.
* [X] User can navigate to **view their own profile**
  * [X] User can see picture, tagline, # of followers, # of following, and tweets on their profile.
* [X] User can **click on the profile image** in any tweet to see **another user's** profile.
 * [X] User can see picture, tagline, # of followers, # of following, and tweets of clicked user.
 * [X] Profile view includes that user's timeline
* [X] User can [infinitely paginate](http://guides.codepath.com/android/Endless-Scrolling-with-AdapterViews-and-RecyclerView) any of these timelines (home, mentions, user) by scrolling to the bottom

The following **optional** features are implemented:

* [ ] User can view following / followers list through the profile
* [X] Implements robust error handling, [check if internet is available](http://guides.codepath.com/android/Sending-and-Managing-Network-Requests#checking-for-network-connectivity), handle error cases, network failures
* [ ] When a network request is sent, user sees an [indeterminate progress indicator](http://guides.codepath.com/android/Handling-ProgressBars#progress-within-actionbar)
* [X] User can **"reply" to any tweet on their home timeline**
  * [X] The user that wrote the original tweet is automatically "@" replied in compose
* [X] User can click on a tweet to be **taken to a "detail view"** of that tweet
 * [X] User can take favorite (and unfavorite) or retweet actions on a tweet
* [X] Improve the user interface and theme the app to feel twitter branded
* [ ] User can **search for tweets matching a particular query** and see results
* [X] Usernames and hashtags are styled and clickable within tweets [using clickable spans](http://guides.codepath.com/android/Working-with-the-TextView#creating-clickable-styled-spans)

The following **bonus** features are implemented:

* [X] Use Parcelable instead of Serializable using the popular [Parceler library](http://guides.codepath.com/android/Using-Parceler).
* [ ] Leverages the [data binding support module](http://guides.codepath.com/android/Applying-Data-Binding-for-Views) to bind data into layout templates.
* [X] Apply the popular [Butterknife annotation library](http://guides.codepath.com/android/Reducing-View-Boilerplate-with-Butterknife) to reduce view boilerplate.
* [ ] User can view their direct messages (or send new ones)

The following **additional** features are implemented:

* [X] User can go to Profile from Detail View 
  * [X] If in Profile View already, User should not be able to launch the Same profile 
* [X] Used Cursor to load data into RecyclerView 

## Video Walkthrough

Here's a walkthrough of implemented user stories:

<img src='http://imgur.com/placeholder.gif' width='300'/>

## Notes

Describe any challenges encountered while building the app.

## Open-source libraries used

- [CodePath OAuth](https://github.com/codepath/android-oauth-handler) - Android library for managing OAuth requests
- [Android Async HTTP](https://github.com/loopj/android-async-http) - Simple asynchronous HTTP requests with JSON parsing
- [Active Android](http://www.activeandroid.com/) - An active record style ORM
- [Glide](https://github.com/bumptech/glide) - Image loading and caching library for Android
- [ButterKnife](http://jakewharton.github.io/butterknife/) - Injection library to reduce boilerplate view code 
- [Timber](https://github.com/JakeWharton/timber) - Logging Library 
- [Stetho](http://facebook.github.io/stetho/) - Debug bridge for Android applications
- [Parceler](http://parceler.org/) - Android Parcelables made easy through code generation

## License

    Copyright [2016] [Monu Surana]

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
