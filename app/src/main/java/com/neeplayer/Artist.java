package com.neeplayer;

import java.util.ArrayList;

public class Artist {

    private Long id;
    private String name;

    public Artist(Long id, String name) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

}
