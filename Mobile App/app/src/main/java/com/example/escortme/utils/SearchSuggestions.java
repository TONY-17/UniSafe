package com.example.escortme.utils;

import com.mapbox.geojson.Point;

public class SearchSuggestions {
    private String name;
    private String address;
    private Point coordinates;

    public SearchSuggestions(String name, String address) {
        this.name = name;
        this.address = address;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
