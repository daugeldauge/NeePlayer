package com.neeplayer;

import java.util.ArrayList;

public class Artist {

    private String name;
    private Long id;
    private ArrayList<Album> albums;

    public Artist(String name, Long id, ArrayList<Album> albums) {
        this.name = name;
        this.id = id;
        this.albums = albums;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }
}
