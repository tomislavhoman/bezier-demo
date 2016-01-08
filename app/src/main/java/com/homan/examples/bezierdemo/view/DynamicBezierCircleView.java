package com.homan.examples.bezierdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;

import java.io.IOException;

public class DynamicBezierCircleView extends BezierView {

    private static final String MEDIA_URL = "http://mosod.sharp-stream.com/mosicyod/HOUSEPARTYTHROWBACK.mp3";

    private static final int UI_FPS = 60;
    private static final int DATA_FPS = 5; // max 20

    private static final int REFRESH_RATE = 1000 / UI_FPS; // ms
    private static final int CAPTURE_RATE = DATA_FPS; // Hz - max 20

    // Should be data capture rate [ms] / ui refresh rate [ms]
    private static final int NUMBER_OF_INTERPOLATED_FRAMES = 1000 / (REFRESH_RATE * CAPTURE_RATE);

    private static final int NUMBER_OF_SAMPLES = 32;
    private static final int OFFSET = 20;
    private static final double SCALE_FACTOR = 100.0;
    private static final int AVERAGING_WINDOW = 4;

    private Handler uiHandler = new Handler(Looper.getMainLooper());

    private int currentFrame = 0;
    private PointF[][] points;
    private PointF center;
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
                public void onPrepared(final MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    startCapturingAudioSamples(mediaPlayer.getAudioSessionId());
                    startAnimation();
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
        visualizer.setScalingMode(Visualizer.SCALING_MODE_AS_PLAYED);
        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {

            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {

//                int diff = 0;
//                for (int i = 0; i < waveform.length; i++) {
//                    if (waveform[i] != -128 && waveform[i] != 127) {
//                        diff++;
//                    }
//                }
//                Log.d("EQ", String.format("Diif from -128 and 127: %d", diff));
//        Log.d("EQ", Arrays.toString(bytes));
                calculateData(waveform);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
            }
        }, CAPTURE_RATE * 1000, true, false);
        visualizer.setEnabled(true);
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        visualizer.setEnabled(false);
        visualizer = null;
        stopAnimation();
    }

    private void calculateData(byte[] bytes) {
        final int inputDataLength = bytes.length;

        // Scaled data to [0..SCALE_FACTOR]
        final int[] scaledData = new int[inputDataLength];
        for (int i = 0; i < inputDataLength; i++) {
            scaledData[i] = (int) (((bytes[i] + 128) / 255.0) * SCALE_FACTOR);
        }

        // Average the data for every i as Avg(i - AVERAGING_WINDOW / 2 .. i + AVERAGING_WINDOW / 2)
        final int[] averagedData = new int[inputDataLength];
        for (int i = 0; i < inputDataLength; i++) {

            int sum = 0;
            for (int j = -AVERAGING_WINDOW / 2; j <= AVERAGING_WINDOW / 2; j++) {
                sum += scaledData[(i + j + inputDataLength) % inputDataLength];
            }
            averagedData[i] = sum / (AVERAGING_WINDOW + 1);
        }

        /*
            Get new origin frame. It is either last shown frame,
            or if nothing was shown we calculate the default frame
         */
        PointF[] newOriginData;
        if (points == null) {
            newOriginData = new PointF[NUMBER_OF_SAMPLES + 1];

            for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
                final int phi = (i * 360) / NUMBER_OF_SAMPLES;
                newOriginData[i] = fromPolar(radius + OFFSET, phi, center);
            }
            newOriginData[NUMBER_OF_SAMPLES] = newOriginData[0];

        } else {
            newOriginData = points[NUMBER_OF_INTERPOLATED_FRAMES - 1];
        }

        // Create new set of frame as {origin, interpolated, target, origin(to close the loop)}
        points = new PointF[NUMBER_OF_INTERPOLATED_FRAMES][NUMBER_OF_SAMPLES + 1];

        // Calculate the new target frame
        PointF[] newTargetData = new PointF[NUMBER_OF_SAMPLES + 1];
        newTargetData[0] = fromPolar(radius + OFFSET + averagedData[0], 0, center);
        final int step = inputDataLength / NUMBER_OF_SAMPLES;
        for (int i = step, j = 1; i < inputDataLength && j < NUMBER_OF_SAMPLES; i += step, j++) {
            final int phi = (j * 360) / NUMBER_OF_SAMPLES;
            newTargetData[j] = fromPolar(radius + OFFSET + averagedData[i], phi, center);
        }
        newTargetData[NUMBER_OF_SAMPLES] = newTargetData[0];

        // Set the new origin and target frames
        points[0] = newOriginData;
        points[NUMBER_OF_INTERPOLATED_FRAMES - 1] = newTargetData;

        // Interpolate (linear)
//        points[0] = points[NUMBER_OF_INTERPOLATED_FRAMES - 1];
        for (int j = 0; j < NUMBER_OF_SAMPLES; j++) {
            final PointF targetPoint = points[NUMBER_OF_INTERPOLATED_FRAMES - 1][j];
            final PointF originPoint = points[0][j];
            final double deltaX = (targetPoint.x - originPoint.x) / NUMBER_OF_INTERPOLATED_FRAMES;
            final double deltaY = (targetPoint.y - originPoint.y) / NUMBER_OF_INTERPOLATED_FRAMES;
            for (int i = 1; i < NUMBER_OF_INTERPOLATED_FRAMES - 1; i++) {
                points[i][j] = new PointF((float) (originPoint.x + i * deltaX), (float) (originPoint.y + i * deltaY));
            }
//            points[i] = points[NUMBER_OF_INTERPOLATED_FRAMES - 1];
        }
        for (int i = 1; i < NUMBER_OF_INTERPOLATED_FRAMES - 1; i++) {
            points[i][NUMBER_OF_SAMPLES] = points[i][0];
        }
        currentFrame = 0;
    }

    private Runnable invalidateRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
            postDelayed(this, REFRESH_RATE);
        }
    };

    private void startAnimation() {
        uiHandler.post(invalidateRunnable);
    }

    private void stopAnimation() {
        uiHandler.removeCallbacks(invalidateRunnable);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        center = new PointF(w / 2, h / 2);
        radius = 230;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(center.x, center.y, radius, basePaint);

        if (points != null && points.length > 3) {
            canvas.drawPath(calculateBezier(points[currentFrame], true), paint);
            Log.d("EQ", String.format("Current frame: %d. Point 10: %s", currentFrame, points[currentFrame][2].toString()));
        }

        currentFrame++;
        if (currentFrame >= NUMBER_OF_INTERPOLATED_FRAMES) {
            currentFrame = NUMBER_OF_INTERPOLATED_FRAMES - 1;
        }
    }
}
