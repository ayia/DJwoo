package com.musichero.xmusic.constants;


public interface IXMusicConstants {

    boolean DEBUG = false;

    boolean SHOW_ADS = true; //enable all ads
    boolean SHOW_NATIVE_ADS = true; // enable show native admob banner
    boolean SHOW_BANNER_ADS_IN_HOME = true; // show admob banner in home screen

    boolean STOP_MUSIC_WHEN_EXITS_APP = false; // stop music app when exiting app

    /**
     * Configure for app rate dialog
     */

    int NUMBER_INSTALL_DAYS = 0;//it is the number install days to show dialog rate.default is 0
    int NUMBER_LAUNCH_TIMES = 3;//it is the number launch times to show dialog rate.default is 3
    int REMIND_TIME_INTERVAL = 1;//it is the number repeat days to show dialog rate.default is 1

    String SOUND_CLOUD_CLIENT_ID = "SOUND_CLOUD_CLIENT_ID";
    String ADMOB_BANNER_ID = "ADMOB_BANNER_ID";
    String ADMOB_INTERSTITIAL_ID = "ADMOB_INTERSTITIAL_ID";
    String DIR_CACHE = "xmusic_app";

    boolean SHOW_SOUND_CLOUD_TAB = true; //enable sound cloud tab

    int[] ADS_FREQ = {1, 8, 18};

    String URL_WEBSITE = "URL_WEBSITE";
    String YOUR_CONTACT_EMAIL = "YOUR_CONTACT_EMAIL";
    String PREFIX_UNKNOWN = "<unknown>";

    int MAX_SONG_CACHED = 50;//5 min
    int MAX_TOP_PLAYLIST_SONG = 25;//5 min
    int MAX_SEARCH_SONG = 80;//5 min

    String TEST_DEVICE = "51F0A3F4C13F05DD49DE0D71F2B369FB";

    int NOTIFICATION_ID = 1;

    String ACTION_FAVORITE = ".action.ACTION_FAVORITE";
    String ACTION_PLAYLIST = ".action.ACTION_PLAYLIST";
    String ACTION_DELETE_SONG = ".action.ACTION_DELETE_SONG";

    String TAG_FRAGMENT_TOP_PLAYLIST = "TAG_FRAGMENT_TOP_PLAYLIST";
    String TAG_FRAGMENT_RECOMMENDED_LIST = "TAG_FRAGMENT_RECOMMENDED_LIST";
    String TAG_FRAGMENT_DETAIL_GENRE = "TAG_FRAGMENT_DETAIL_GENRE";
    String TAG_FRAGMENT_SEARCH = "TAG_FRAGMENT_SEARCH";
    String TAG_FRAGMENT_DETAIL_PLAYLIST = "TAG_FRAGMENT_DETAIL_PLAYLIST";
    String TAG_FRAGMENT_FAVORITE = "TAG_FRAGMENT_FAVORITE";

    String URL_FORMAT_LINK_APP = "https://play.google.com/store/apps/details?id=%1$s";

    String KEY_HEADER = "KEY_HEADER";
    String KEY_SHOW_URL = "KEY_SHOW_URL";
    String KEY_SONG_ID = "KEY_SONG_ID";
    String KEY_BONUS = "bonus_data";
    String KEY_TYPE = "type";

    int TYPE_FILTER_SAVED = 5;

    int TYPE_PLAYLIST = 9;
    int TYPE_ADD_FAVORITE = 7;
    int TYPE_REMOVE_FAVORITE = 8;
    int TYPE_DELETE = 11;

    int TYPE_DETAIL_PLAYLIST = 12;
    int TYPE_DETAIL_TOP_PLAYLIST = 13;
    int TYPE_DETAIL_RECOMMENDED = 15;
    int TYPE_DETAIL_GENRE = 16;

    int TYPE_UI_LIST = 1;
    int TYPE_UI_GRID = 2;

    String FILE_GENRE = "genre.dat";
    String FILE_PLAYLIST = "playlists.dat";
    String FILE_SAVED_TRACK = "tracks.dat";
    String FILE_CONFIGURE = "config.json";

    String DIR_TEMP = ".temp";

    int RATE_EFFECT = 10;
    int ONE_MINUTE = 60000;
    int MAX_SLEEP_MODE = 120;
    int MIN_SLEEP_MODE = 5;
    int STEP_SLEEP_MODE = 5;

    String PREFIX_ACTION = "super.android.musiconline.stream";
    String URL_FORMAT_SUGESSTION = "http://suggestqueries.google.com/complete/search?ds=yt&output=toolbar&hl=%1$s&q=%2$s";


}
