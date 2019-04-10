package com.example.myapplication;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;
import com.robinhood.spark.animation.LineSparkAnimator;
import com.robinhood.spark.animation.MorphSparkAnimator;
import com.robinhood.spark.animation.SparkAnimator;

import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity  {
    public static class ViewModel
    {
        Session.State state = Session.State.NONE;
        float bumpScore = 0;

        public ViewModel() {
        }

        public ViewModel(Session.State state, float bumpScore) {
            this.state = state;
            this.bumpScore = bumpScore;
        }
    }

    private static class SparklineAdapter extends SparkAdapter {
        private final int size = 32;
        private float data[] = new float[size];
        private int idx = 0;

        public SparklineAdapter() {
            for(int i = 0; i < size; i++) {
                data[i] = 0;
            }
        }

        public void addData(float f) {
            data[idx++ % size] = f;
        }

        @Override
        public int getCount() {
            return size;
        }

        @Override
        public Object getItem(int index) {
            //data[0] = 1000;
            return data[Math.abs(index - idx + 1) % size];
        }

        @Override
        public float getY(int index) {
          //  data[0] = 1000;
            return data[Math.abs(index - idx + 1) % size];
        }

        @Override
        public RectF getDataBounds() {
            final int count = getCount();
            final boolean hasBaseLine = hasBaseLine();

            float minY = hasBaseLine ? getBaseLine() : Float.MAX_VALUE;
            float maxY = hasBaseLine ? minY : -Float.MAX_VALUE;
            float minX = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE;
            for (int i = 0; i < count; i++) {
                final float x = getX(i);
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = 0;
                maxY = 1000;
            }

            // set values on the return object
            return new RectF(minX, minY, maxX, maxY);
        }
    }

    public class LineSparkAnimatorCustom implements SparkAnimator {

        @Override
        public Animator getAnimation(final SparkView sparkView) {
            ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
            return animator;
        }

    }

    private TextView bump_score_txt;
    private TextView start_button_txt;
    private FloatingActionButton start_button;
    private Button share_button;
    private SparkView sparkView;
    private PendingIntent scheduledIntent;
    private AlarmManager scheduler;
    private Intent myService;
    private ViewModel viewModel;
    private SparklineAdapter sparklineAdapter;
    private Handler handler;

    Runnable drawLoop = new Runnable() {
        @Override
        public void run() {
            try {
                redraw();
            } finally {
                handler.postDelayed(drawLoop, 20);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModel();

        bump_score_txt = findViewById(R.id.bump_score_txt);
        start_button = findViewById(R.id.start_button);
        start_button_txt = findViewById(R.id.start_button_txt);
        share_button = findViewById(R.id.share_button);
        sparkView = findViewById(R.id.sparkview);

        sparklineAdapter = new SparklineAdapter();
        sparkView.setSparkAnimator(new LineSparkAnimatorCustom());
        sparkView.setAdapter(sparklineAdapter);
        handler = new Handler();
        drawLoop.run();

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
        handler.removeCallbacks(drawLoop);
    }

    protected void onResume() {
        super.onResume();
        drawLoop.run();
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

    private int xx = 0;

    private void createObservable(){
        Observable<ViewModel> observable = BumpMonitorService.getObservable();
        observable.subscribe(new Observer<ViewModel>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(ViewModel model) {
                viewModel = model;
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
        sparklineAdapter.addData(viewModel.bumpScore);
        sparklineAdapter.notifyDataSetChanged();
        bump_score_txt.setText(""+(int)viewModel.bumpScore);

        switch (viewModel.state) {
            case STARTED:
                start_button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                share_button.setVisibility(View.INVISIBLE);
                start_button_txt.setText("■");
                break;
            case STOPPED:
                start_button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
                share_button.setVisibility(View.VISIBLE);
                start_button_txt.setText("⬤");
                break;
            case NONE:
                start_button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
                share_button.setVisibility(View.INVISIBLE);
                start_button_txt.setText("⬤");
                break;
        }
    }
}
