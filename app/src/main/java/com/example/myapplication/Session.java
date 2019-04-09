package com.example.myapplication;

import android.content.Context;

import java.util.UUID;

public class Session {

    public enum State {
        NONE,
        STARTED,
        STOPPED
    }

    private State state;
    private String sessionId;
    private SessionLog sessionLog;

    private DataPoint lastDataPoint;

    public Session(Context context) {
        sessionLog = new SessionLog(context);
        state = State.NONE;
        lastDataPoint = new DataPoint();
    }

    public void startSession() {
        state = State.STARTED;
        sessionId = UUID.randomUUID().toString();
        sessionLog.reset();
    }

    public void stopSession() {
        state = State.STOPPED;
        sessionLog.flush();
    }

    public State getState() {
        return state;
    }

    public DataPoint getLastDataPoint() {
        return lastDataPoint;
    }

    public void onAccelerometerEvent(long time, float x, float y, float z) {
        if(getState() != State.STARTED) {
            return;
        }

        lastDataPoint.time = time;
        lastDataPoint.accelerometer_x = x;
        lastDataPoint.accelerometer_y = y;
        lastDataPoint.accelerometer_z = z;

        sessionLog.write(lastDataPoint);
    }

    public void onGeoEvent(long time, double latitude, double longitude) {
        if(getState() != State.STARTED) {
            return;
        }

        lastDataPoint.time = time;
        lastDataPoint.latitude = latitude;
        lastDataPoint.longitude = longitude;

        sessionLog.write(lastDataPoint);
    }
}
