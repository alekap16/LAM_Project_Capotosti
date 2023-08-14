package com.example.lam_project.model;

public class Square {

    private long id;
    private double latitudeStart;
    private double longitudeStart;
    private double latitudeEnd;
    private double longitudeEnd;
    private int color;

    //1 LTE, 2 Wi-Fi, 3 Noise
    private int type;
    private double squareSize;

    private long timestamp;

    private double signalValue;


    public Square(double latitudeStart, double longitudeStart, double latitudeEnd,
                  double longitudeEnd, int color, int type, double squareSize, long timestamp,
                  double signalValue) {
        this.latitudeStart = latitudeStart;
        this.longitudeStart = longitudeStart;
        this.latitudeEnd = latitudeEnd;
        this.longitudeEnd = longitudeEnd;
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

    public double getLatitudeStart() {
        return latitudeStart;
    }

    public double getLongitudeStart() {
        return longitudeStart;
    }
    public double getLatitudeEnd(){ return latitudeEnd; }
    public double getLongitudeEnd(){ return longitudeEnd; }

    public int getColor() {
        return color;
    }

    public int getType() {
        return type;
    }
    public double getSquareSize() { return squareSize; }
    public long getTimestamp() { return timestamp ; }
    public double getSignalValue() { return signalValue ; }

    //Setters
    public void setColor(int color) {
        this.color = color;
    }

    public void setLatitudeStart(double latitudeStart) {
        this.latitudeStart = latitudeStart;
    }

    public void setLongitudeStart(double longitudeStart) { this.longitudeStart = longitudeStart; }
    public void setLatitudeEnd(double latitudeEnd) { this.latitudeEnd = latitudeEnd; }
    public void setLongitudeEnd(double longitudeEnd) { this.longitudeEnd = longitudeEnd; }

    public void setType(int type) {
        this.type = type;
    }
    public void setSquareSize(double squareSize) { this.squareSize = squareSize; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setSignalValue(double signalValue) { this.signalValue = signalValue; }
}
