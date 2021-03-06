package com.example.myapplication;

public class DataPoint {
    long time = 0;
    double latitude = 0;
    double longitude = 0;
    float accelerometer_x = 0.0f;
    float accelerometer_y = 0.0f;
    float accelerometer_z = 0.0f;

    public String toCSV() {
        return String.format("%d,%f,%f,%f,%f,%f\n", time, latitude, longitude, accelerometer_x, accelerometer_y, accelerometer_z);
    }
}
