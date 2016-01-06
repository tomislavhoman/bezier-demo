package com.homan.examples.bezierdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class BezierSimpleCircleView extends BezierView {

    private Point2D[] points;
    private Point2D center;
    private int radius;

    public BezierSimpleCircleView(Context context) {
        super(context);
    }

    public BezierSimpleCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BezierSimpleCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        center = new Point2D(w / 2, h / 2);
        radius = 250;
        final int delta = 30;

        points = new Point2D[]{
                new Point2D(center.getX() + radius, center.getY()),
                fromPolar(radius + delta, 45, center),
                new Point2D(center.getX(), center.getY() - radius),
                fromPolar(radius - delta, 135, center),
                new Point2D(center.getX() - radius, center.getY()),
                fromPolar(radius + delta, 225, center),
                new Point2D(center.getX(), center.getY() + radius),
                fromPolar(radius - delta, 315, center),
                new Point2D(center.getX() + radius, center.getY()),
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle((float) center.getX(), (float) center.getY(), radius, basePaint);
        canvas.drawPath(calculateBezier(points, true), paint);
    }
}
