package com.homan.examples.bezierdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.util.AttributeSet;

import java.io.IOException;
import java.util.Random;

public class DynamicBezierCircleView extends BezierView {

    private static final String MEDIA_URL = "http://mosod.sharp-stream.com/mosicyod/HOUSEPARTYTHROWBACK.mp3";

    private static final int NUMBER_OF_SLICES = 32;
    private static final int MAX_DELTA_POSITIVE = 70;
    private static final int MAX_DELTA_NEGATIVE = 30;

    private BezierView.Point2D[] points;
    private BezierView.Point2D center;
    private int radius;

    private MediaPlayer mediaPlayer;
    private Visualizer visualizer;

    public DynamicBezierCircleView(Context context) {
        super(context);
        init();
    }

    public DynamicBezierCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DynamicBezierCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mediaPlayer = new MediaPlayer();
    }

    public void play(final Context context) {
        try {
            final Uri uri = Uri.parse(MEDIA_URL);
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    startCapturingAudioSamples(mediaPlayer.getAudioSessionId());
                }
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startCapturingAudioSamples(int audioSessionId) {
        Visualizer visualizer = new Visualizer(audioSessionId);
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {

            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int i) {

            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int i) {

            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false);
        visualizer.setEnabled(true);
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        visualizer.setEnabled(false);
        visualizer = null;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        center = new BezierView.Point2D(w / 2, h / 2);
        radius = 230;

        points = new BezierView.Point2D[NUMBER_OF_SLICES + 1];
        final Random random = new Random();

        points[0] = new BezierView.Point2D(center.getX() + radius, center.getY());
        for (int i = 1; i < NUMBER_OF_SLICES; i++) {
            final int phi = i * 360 / NUMBER_OF_SLICES;
            final int direction = i % 2 == 0 ? 1 : -1;
            final int maxDelta = direction > 0 ? MAX_DELTA_POSITIVE : MAX_DELTA_NEGATIVE;
            int delta = direction * random.nextInt(maxDelta + 1);
            points[i] = fromPolar(radius + delta, phi, center);
        }
        points[NUMBER_OF_SLICES] = new BezierView.Point2D(center.getX() + radius, center.getY());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle((float) center.getX(), (float) center.getY(), radius, basePaint);
        canvas.drawPath(calculateBezier(points, true), paint);
    }
}
