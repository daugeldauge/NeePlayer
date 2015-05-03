package com.neeplayer;

import java.util.ArrayList;

public class Artist {

    private Long id;
    private String name;
    private int numberOfSongs;
    private int numberOfAlbums;

    public Artist(Long id, String name, int numberOfSongs, int numberOfAlbums) {
        this.id = id;
        this.name = name;
        this.numberOfSongs = numberOfSongs;
        this.numberOfAlbums = numberOfAlbums;
    }

    public int getNumberOfAlbums() {
        return numberOfAlbums;
    }

    public int getNumberOfSongs() {
        return numberOfSongs;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

}
