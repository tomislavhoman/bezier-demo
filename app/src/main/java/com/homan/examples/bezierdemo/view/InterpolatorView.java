package com.homan.examples.bezierdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class InterpolatorView extends View {

    private static final int UI_REFRESH_RATE = 16; //ms
    private static final int DATA_REFRESH_RATE = 200; //ms

    private static final int NUMBER_OF_INTERPOLATED_POINTS = DATA_REFRESH_RATE / UI_REFRESH_RATE;
    private static final int NUMBER_OF_SAMPLES = 10;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private Point[][] data;

    private int height;
    private int width;

    private Paint paint;

    private Random random;

    int currentFrame = 0;

    public InterpolatorView(Context context) {
        super(context);
        init();
    }

    public InterpolatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InterpolatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(2.0f);

        random = new Random();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimating();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimating();
        super.onDetachedFromWindow();
    }

    private void startAnimating() {
        uiHandler.post(invalidateUI);
        uiHandler.post(fetchData);
    }

    private void stopAnimating() {
        uiHandler.removeCallbacks(invalidateUI);
        uiHandler.removeCallbacks(fetchData);
    }

    private Runnable invalidateUI = new Runnable() {
        @Override
        public void run() {
            invalidate();
            uiHandler.postDelayed(this, UI_REFRESH_RATE);
        }
    };

    private Runnable fetchData = new Runnable() {
        @Override
        public void run() {
            calculateNextDataPoints();
            uiHandler.postDelayed(this, DATA_REFRESH_RATE);
        }
    };

    private void calculateNextDataPoints() {

        Point[] oldData;
        if (data == null) {
            oldData = new Point[NUMBER_OF_SAMPLES];
            for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
                oldData[i] = new Point(i * 20, i * 20);
            }
        } else {
            oldData = data[currentFrame];
        }

        data = new Point[NUMBER_OF_INTERPOLATED_POINTS][NUMBER_OF_SAMPLES];
        data[0] = oldData;
        for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
            data[NUMBER_OF_INTERPOLATED_POINTS - 1][i] = new Point(random.nextInt(width + 1), random.nextInt(height + 1));
        }

        for (int i = 1; i < NUMBER_OF_INTERPOLATED_POINTS - 1; i++) {
            for (int j = 0; j < NUMBER_OF_SAMPLES; j++) {
                Point destination = data[NUMBER_OF_INTERPOLATED_POINTS - 1][j];
                Point origin = data[0][j];
                float deltaX = (destination.x - origin.x) / NUMBER_OF_SAMPLES;
                float deltaY = (destination.y - origin.y) / NUMBER_OF_SAMPLES;

                data[i][j] = new Point(origin.x + (int) (i * deltaX), origin.y + (int) (i * deltaY));
            }
        }
        currentFrame = 0;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
        width = w;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null) {
            return;
        }

        if (currentFrame >= NUMBER_OF_INTERPOLATED_POINTS) {
            return;
        }

        for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
            canvas.drawCircle(data[currentFrame][i].x, data[currentFrame][i].y, 3, paint);
        }
        currentFrame++;
        if (currentFrame >= NUMBER_OF_INTERPOLATED_POINTS) {
            currentFrame = NUMBER_OF_INTERPOLATED_POINTS - 1;
        }
    }
}
