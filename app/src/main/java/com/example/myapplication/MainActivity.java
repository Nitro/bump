package com.example.myapplication;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity  {
    public static class ViewModel
    {
        Session.State state = Session.State.NONE;
        DataPoint dataPoint = new DataPoint();

        public ViewModel() {
        }

        public ViewModel(Session.State state, DataPoint dataPoint) {
            this.state = state;
            this.dataPoint = dataPoint;
        }
    }

    private TextView location_txt;
    private TextView bump_score_txt;
    private Button start_button;
    private Button share_button;
    private PendingIntent scheduledIntent;
    private AlarmManager scheduler;
    private Intent myService;
    private ViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModel();

        bump_score_txt = findViewById(R.id.bump_score_txt);
        location_txt = findViewById(R.id.location_txt);
        start_button = findViewById(R.id.start_button);
        share_button = findViewById(R.id.share_button);

        redraw();

        createObservable();

        start_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onStartButtonClick();
            }
        });

        share_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onShareButtonClick();
            }
        });

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    private void stopMonitorService() {
        stopService(new Intent(this, BumpMonitorService.class));
        /*
        scheduler = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if(scheduledIntent != null) {
            scheduler.cancel(scheduledIntent);
        }

        if(myService != null) {
            stopService(myService);
        }

        myService = new Intent(this, BumpMonitorService.class);
        stopService(myService);
        */
    }

    private void startMonitorService() {
        startService(new Intent(this, BumpMonitorService.class));
/*
        scheduler = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if(scheduledIntent != null) {
            scheduler.cancel(scheduledIntent);
        }

        if(myService != null) {
            stopService(myService);
        }

        myService = new Intent(this, BumpMonitorService.class);
        stopService(myService);

        scheduledIntent = PendingIntent.getService(this,0, myService, PendingIntent.FLAG_UPDATE_CURRENT);
        scheduler.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 , scheduledIntent);
        */
    }

    private void onStartButtonClick() {
        switch (viewModel.state) {
            case STARTED:
                stopMonitorService();
                break;
            case STOPPED:
            case NONE:
                startMonitorService();
                break;
        }
    }

    private void onShareButtonClick() {
        Uri uri = FileProvider.getUriForFile(this, "com.example.myapplication", this.getFileStreamPath("session.txt"));
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

    private void createObservable(){
        Observable<ViewModel> observable = BumpMonitorService.getObservable();
        observable.subscribe(new Observer<ViewModel>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(ViewModel model) {
                viewModel = model;
                redraw();
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private void redraw() {
        location_txt.setText("" + viewModel.dataPoint.latitude + "," + viewModel.dataPoint.longitude);
        bump_score_txt.setText("" + viewModel.dataPoint.accelerometer_x);
        switch (viewModel.state) {
            case STARTED:
                start_button.setText("stop");
                share_button.setVisibility(View.INVISIBLE);
                break;
            case STOPPED:
                start_button.setText("start again");
                share_button.setVisibility(View.VISIBLE);
                break;
            case NONE:
                start_button.setText("start");
                share_button.setVisibility(View.INVISIBLE);
                break;
        }
    }
}
