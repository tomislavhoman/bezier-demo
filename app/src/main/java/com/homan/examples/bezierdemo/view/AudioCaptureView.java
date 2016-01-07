package com.homan.examples.bezierdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

import java.io.IOException;

public class AudioCaptureView extends View {

    private static final String MEDIA_URL = "http://mosod.sharp-stream.com/mosicyod/HOUSEPARTYTHROWBACK.mp3";

    private MediaPlayer mediaPlayer;
    private Visualizer visualizer;

    private int width;
    private int height;

    private int[] data = new int[]{};

    private Paint paint;
    private Paint basePaint;

    public AudioCaptureView(Context context) {
        super(context);
        init();
    }

    public AudioCaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AudioCaptureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mediaPlayer = new MediaPlayer();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);

        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setColor(Color.RED);
        basePaint.setStyle(Paint.Style.STROKE);
        basePaint.setStrokeWidth(2.0f);
    }

    public void play(final Context context) {
        try {
            final Uri uri = Uri.parse(MEDIA_URL);
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mediaPlayer) {
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
        visualizer = new Visualizer(audioSessionId);
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {

            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int j) {

                final int length = bytes.length;
                final int[] transformed = new int[length];
                for (int i = 0; i < length; i++) {
                    transformed[i] = (int) (((bytes[i] + 127) / 256.0) * 300.0);
                }


                for (int i = 0; i < length; i++) {
                    transformed[i] = (transformed[(i - 2 + length) % length] + transformed[(i - 1 + length) % length] + transformed[i] + transformed[(i + 1 + length) % length]) / 4;
                }

                data = new int[128];
                for (int i = 0; i < length; i += 8) {
                    data[i / 8] = transformed[i];
                }
                invalidate();
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
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(0, height / 2, width, height / 2, basePaint);

        for (int i = 0; i < data.length; i++) {
            canvas.drawLine(i * 4, height / 2, i * 4, height / 2 - data[i], paint);
        }
    }
}
