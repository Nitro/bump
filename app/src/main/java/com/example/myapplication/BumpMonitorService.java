package com.example.myapplication;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;


public class BumpMonitorService extends Service implements SensorEventListener, LocationListener {
    private static final String TAG = "CycleCrowd";
    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private LocationManager locationManager;
    private Session session;
    private static PublishSubject<MainActivity.ViewModel> publisher = PublishSubject.create();

    public BumpMonitorService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "starting BumpMonitorService");

        session = new Session(this);
        session.startSession();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            onLocationChanged(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
        }

        publish();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event != null && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            session.onAccelerometerEvent(System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]);
            publish();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            session.onGeoEvent(location.getTime(), location.getLatitude(), location.getLongitude());
            publish();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG,"task removed");
        super.onTaskRemoved(rootIntent);
        publish();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
        session.stopSession();
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);
        publish();
    }

    public static Observable<MainActivity.ViewModel> getObservable(){
        return publisher;
    }

    private void publish() {

        Vector3 v = new Vector3(session.getLastDataPoint().accelerometer_x, session.getLastDataPoint().accelerometer_y, session.getLastDataPoint().accelerometer_z);
        float f = Math.abs(new Vector3(0, 0, 0).distance2(v));
        f = f < 50 ? 0 : Math.min(f, 1000.0f);

        publisher.onNext(new MainActivity.ViewModel(session.getState(), f));
    }

}
