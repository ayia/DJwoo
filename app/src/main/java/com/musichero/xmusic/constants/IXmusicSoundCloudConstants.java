package com.musichero.xmusic.constants;

/**
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.musichero.com
 * @Project:AndroidCloundMusicPlayer
 * @Date:Dec 14, 2014
 */
public interface IXmusicSoundCloudConstants {

    String URL_API = "http://api.soundcloud.com/";
    String METHOD_TRACKS = "tracks";

    String URL_TOP_MUSIC = "https://itunes.apple.com/%1$s/rss/topsongs/limit=%2$s/json";

    String FORMAT_CLIENT_ID = "?client_id=%1$s";
    String JSON_PREFIX = ".json";

    String OFFSET = "&offset=%1$s&limit=%2$s";

    String FILTER_QUERY = "&q=%1$s";
    String FILTER_GENRE = "&genres=%1$s";

    String FORMAT_URL_SONG = "http://api.soundcloud.com/tracks/%1$s/stream?client_id=%2$s";

    String URL_API_V2 = "https://api-v2.soundcloud.com/";
    String METHOD_CHARTS = "charts?";
    String PARAMS_GENRES = "&genre=soundcloud:genres:%1$s";
    String PARAMS_LINKED_PARTITION = "&linked_partitioning=1";
    String PARAMS_OFFSET = "&offset=%1$s&limit=%2$s";
    String PARAMS_KIND = "&kind=%1$s";
    String PARAMS_NEW_CLIENT_ID = "&client_id=%1$s";

    String KIND_TOP = "top";
    String KIND_TRENDING = "trending";

    String ALL_MUSIC_GENRE = "all-music";


}
