package com.homan.examples.bezierdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import java.util.Random;

public class Bezier128CircleView extends BezierView {

    private static final int NUMBER_OF_SLICES = 32;
    private static final int MAX_DELTA_POSITIVE = 70;
    private static final int MAX_DELTA_NEGATIVE = 30;

    private BezierView.Point2D[] points;
    private BezierView.Point2D center;
    private int radius;

    public Bezier128CircleView(Context context) {
        super(context);
    }

    public Bezier128CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Bezier128CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        center = new BezierView.Point2D(w / 2, h / 2);
        radius = 230;

        points = new Point2D[NUMBER_OF_SLICES + 1];
        final Random random = new Random();

        points[0] = new Point2D(center.getX() + radius, center.getY());
        for (int i = 1; i < NUMBER_OF_SLICES; i++) {
            final int phi = i * 360 / NUMBER_OF_SLICES;
            final int direction = i % 2 == 0 ? 1 : -1;
            final int maxDelta = direction > 0 ? MAX_DELTA_POSITIVE : MAX_DELTA_NEGATIVE;
            int delta = direction * random.nextInt(maxDelta + 1);
            points[i] = fromPolar(radius + delta, phi, center);
        }
        points[NUMBER_OF_SLICES] = new Point2D(center.getX() + radius, center.getY());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle((float) center.getX(), (float) center.getY(), radius, basePaint);
        canvas.drawPath(calculateBezier(points, true), paint);
    }
}
