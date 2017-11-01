package com.musichero.xmusic.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by bzouiri on 10/31/2017.
 */

public class TopChartCollectionModel {
    @SerializedName("song")
    private ArrayList<SongModel> song;

    public TopChartCollectionModel(ArrayList<SongModel> song) {
        this.song = song;
    }

    public ArrayList<SongModel> getSong() {
        return song;
    }

    public void setSong(ArrayList<SongModel> song) {
        this.song = song;
    }
}
