package com.homan.examples.bezierdemo.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.homan.examples.bezierdemo.R;
import com.homan.examples.bezierdemo.view.DynamicBezierCircleView;

public class DynamicCircleActivity extends AppCompatActivity {

    private DynamicBezierCircleView equalizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_circle);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        equalizer = (DynamicBezierCircleView) findViewById(R.id.equalizer);
        equalizer.play(this);
    }

    @Override
    protected void onDestroy() {
        equalizer.stop();
        super.onDestroy();
    }
}
