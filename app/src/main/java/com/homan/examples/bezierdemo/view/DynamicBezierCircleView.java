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

import java.io.IOException;

public class DynamicBezierCircleView extends BezierView {

    private static final String MEDIA_URL = "http://mosod.sharp-stream.com/mosicyod/HOUSEPARTYTHROWBACK.mp3";
//    private static final String MEDIA_URL = "http://www.audiocheck.net/Audio/audiocheck.net_frequencycheckhigh_44100.mp3";
//    private static final String MEDIA_URL = "http://www.audiocheck.net/Audio/audiocheck.net_sweep20-20klog.mp3";

    // Refresh rates setup
    private static final int UI_FPS = 60;
    private static final int DATA_FPS = 20; // max 20

    private static final int REFRESH_RATE = 1000 / UI_FPS; // ms
    private static final int CAPTURE_RATE = DATA_FPS; // Hz - max 20

    // Truncations setup
    private static final int BOTTOM_OFFSET = 32;

    //Scaling setup
    private static final double SCALE_REFERENCE_FACTOR = 100.0;
    private static final double SCALE_TARGET = 100.0; // [px]
    private static final int MAXIMAL_VALUE = 100; // [px]
    private static final double[] SCALE_FACTORS = new double[]{1.0, 1.0, 1.0, 1.0, 1.5, 1.5, 1.5}; // fraction of maximal
    private static final int NUMBER_OF_SCALE_FACTORS = SCALE_FACTORS.length;

    // Averaging setup
    private static final int AVERAGING_WINDOW = 4;

    // Sampling setup
    private static final int NUMBER_OF_SAMPLES = 64;
    private static final int OFFSET = 20; // [px]

    // Interpolation setup
    // Should be data capture rate [ms] / ui refresh rate [ms]
    private static final int NUMBER_OF_INTERPOLATED_FRAMES = 1000 / (REFRESH_RATE * CAPTURE_RATE);

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
//        visualizer.setScalingMode(Visualizer.SCALING_MODE_AS_PLAYED);
        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {

            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                calculateData(waveform);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                calculateData(fft);
            }
        }, CAPTURE_RATE * 1000, false, true);
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

        final int[] truncatedData = truncateData(bytes);
        final int[] magnitudes = calculateMagnitudes(truncatedData);
        final int[] scaledData = scaleData(magnitudes);
        final int[] averagedData = averageData(scaledData);

        final PointF[] newOriginData = calculateNewOriginFrame();
        final PointF[] newTargetData = calculateNewTargetFrame(averagedData);

        // Create new set of frames as {origin, interpolated, target, origin(to close the loop)}
        final PointF[][] newFrames = new PointF[NUMBER_OF_INTERPOLATED_FRAMES][NUMBER_OF_SAMPLES + 1];

        // Set the new origin and target frames
        newFrames[0] = newOriginData;
        newFrames[NUMBER_OF_INTERPOLATED_FRAMES - 1] = newTargetData;

        fillWithLinearyInterpolatedFrames(newFrames);

        this.points = newFrames;
        currentFrame = 0;
    }

    private int[] truncateData(final byte[] bytes) {
        final int[] truncatedData = new int[bytes.length / 2 - BOTTOM_OFFSET];
        for (int i = 0; i < truncatedData.length; i++) {
            truncatedData[i] = bytes[i + BOTTOM_OFFSET];
        }
        return truncatedData;
    }

    private int[] calculateMagnitudes(final int[] truncatedData) {
        final int[] magnitudes = new int[truncatedData.length / 2];
        for (int i = 0; i < magnitudes.length; i++) {
            magnitudes[i] = truncatedData[2 * i] * truncatedData[2 * i] + truncatedData[2 * i + 1] * truncatedData[2 * i + 1];
        }
        return magnitudes;
    }

    private int[] scaleData(final int[] magnitudes) {
        final int[] scaledData = new int[magnitudes.length];
        for (int i = 0; i < scaledData.length; i++) {
            final int bucket = (i * NUMBER_OF_SCALE_FACTORS) / scaledData.length;
            double magnitudeRatio = (double) magnitudes[i] / SCALE_REFERENCE_FACTOR;
            if (magnitudeRatio > 1.0) {
                magnitudeRatio = 1.0;
            }

            scaledData[i] = (int) (magnitudeRatio * SCALE_TARGET * SCALE_FACTORS[bucket]);

            // Cut of excess data
            if (scaledData[i] > MAXIMAL_VALUE) {
                scaledData[i] = MAXIMAL_VALUE;
            }
        }
        return scaledData;
    }

    private int[] averageData(final int[] scaledData) {
        // Average the data for every i as Avg(i - AVERAGING_WINDOW / 2 .. i + AVERAGING_WINDOW / 2)
        final int[] averagedData = new int[scaledData.length];
        for (int i = 0; i < averagedData.length; i++) {

            int sum = 0;
            for (int j = -AVERAGING_WINDOW / 2; j <= AVERAGING_WINDOW / 2; j++) {
                sum += scaledData[(i + j + averagedData.length) % averagedData.length];
            }
            averagedData[i] = sum / (AVERAGING_WINDOW + 1);
        }
        return averagedData;
    }

    private PointF[] calculateNewOriginFrame() {
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

        return newOriginData;
    }

    private PointF[] calculateNewTargetFrame(final int[] averagedData) {
        // Calculate the new target frame
        final PointF[] newTargetData = new PointF[NUMBER_OF_SAMPLES + 1];
        newTargetData[0] = fromPolar(radius + OFFSET + averagedData[0], 0, center);
        final int step = averagedData.length / NUMBER_OF_SAMPLES;
        for (int i = step, j = 1; i < averagedData.length && j < NUMBER_OF_SAMPLES; i += step, j++) {
            final int phi = (j * 360) / NUMBER_OF_SAMPLES;
            newTargetData[j] = fromPolar(radius + OFFSET + averagedData[i], phi, center);
        }
        newTargetData[NUMBER_OF_SAMPLES] = newTargetData[0];

        return newTargetData;
    }

    private void fillWithLinearyInterpolatedFrames(final PointF[][] data) {
        // Interpolate (linear)
        for (int j = 0; j < NUMBER_OF_SAMPLES; j++) {
            final PointF targetPoint = data[NUMBER_OF_INTERPOLATED_FRAMES - 1][j];
            final PointF originPoint = data[0][j];
            final double deltaX = (targetPoint.x - originPoint.x) / NUMBER_OF_INTERPOLATED_FRAMES;
            final double deltaY = (targetPoint.y - originPoint.y) / NUMBER_OF_INTERPOLATED_FRAMES;
            for (int i = 1; i < NUMBER_OF_INTERPOLATED_FRAMES - 1; i++) {
                data[i][j] = new PointF((float) (originPoint.x + i * deltaX), (float) (originPoint.y + i * deltaY));
            }
        }

        for (int i = 1; i < NUMBER_OF_INTERPOLATED_FRAMES - 1; i++) {
            data[i][NUMBER_OF_SAMPLES] = data[i][0];
        }
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

        if (points != null && points.length >= 3) {
            canvas.drawPath(calculateBezier(points[currentFrame], true), paint);
        }

        currentFrame++;
        if (currentFrame >= NUMBER_OF_INTERPOLATED_FRAMES) {
            currentFrame = NUMBER_OF_INTERPOLATED_FRAMES - 1;
        }
    }
}
