package com.neeplayer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

public class Artist {

    private Long id;
    private String name;
    private int numberOfSongs;
    private int numberOfAlbums;
    private String imageURL;

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

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }


}
