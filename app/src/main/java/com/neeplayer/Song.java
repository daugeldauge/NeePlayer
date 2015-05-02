package com.neeplayer;

public class Song {

    private Long id;
    private String title;
    private String artist;

    public Song(Long songId, String songTitle, String songArtist) {
        id = songId;
        title = songTitle;
        artist = songArtist;
    }

    public Long getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }
}
