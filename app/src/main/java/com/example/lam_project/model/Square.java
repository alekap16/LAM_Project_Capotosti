package com.example.lam_project.model;

public class Square {

    private long id;
    private double latitude;
    private double longitude;
    private int color;

    //1 LTE, 2 Wi-Fi, 3 Noise
    private int type;
    private double squareSize;

    public Square(double latitude, double longitude, int color, int type, double squareSize) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.color = color;
        this.type = type;
        this.squareSize = squareSize;
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

    public int getType() {
        return type;
    }
    public double getSquareSize() { return squareSize; }

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

    public void setType(int type) {
        this.type = type;
    }
    public void setSquareSize(double squareSize) { this.squareSize = squareSize; }
}
