package com.neeplayer;

import java.io.Serializable;

public class Song implements Serializable {

    private Long id;
    private String title;
    private Long duration;
    private int track;

    public Song(Long id, String title, Long duration, int track) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.track = track;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Long getDuration() {
        return duration;
    }

    public int getTrack() {
        return track;
    }
}
