package com.neeplayer;

import java.util.ArrayList;

public class Album {

    private int year;
    private Long id;
    private ArrayList<Song> songs;

    public Album(int year, Long id, ArrayList<Song> songs) {
        this.year = year;
        this.id = id;
        this.songs = songs;
    }

    public int getYear() {

        return year;
    }

    public Long getId() {
        return id;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }
}
