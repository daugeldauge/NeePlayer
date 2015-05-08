package com.neeplayer;

import java.io.Serializable;
import java.util.ArrayList;

public class Album implements Serializable {

    private final Long id;
    private final String title;
    private final int year;
    private final String art;
    private final ArrayList<Song> songs;

    public Album(Long id, String title, int year, String art, ArrayList<Song> songs) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.art = art;
        this.songs = songs;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public String getArt() {
        return art;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }
}
