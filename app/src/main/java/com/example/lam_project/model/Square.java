package com.example.lam_project.model;

public class Square {

    private long id;
    private double latitude;
    private double longitude;
    private int color;

    private String type;

    public Square(double latitude, double longitude, int color, String type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.color = color;
        this.type = type;
    }

    //Getters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getColor() {
        return color;
    }

    public String getType() {
        return type;
    }

    //Setters
    public void setColor(int color) {
        this.color = color;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setType(String type) {
        this.type = type;
    }
}
