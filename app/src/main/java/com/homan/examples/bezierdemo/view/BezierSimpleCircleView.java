package com.homan.examples.bezierdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;

public class BezierSimpleCircleView extends BezierView {

    private PointF[] points;
    private PointF center;
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

        center = new PointF(w / 2, h / 2);
        radius = 250;
        final int delta = 30;

        points = new PointF[]{
                new PointF(center.x + radius, center.y),
                fromPolar(radius + delta, 45, center),
                new PointF(center.x, center.y - radius),
                fromPolar(radius - delta, 135, center),
                new PointF(center.x - radius, center.y),
                fromPolar(radius + delta, 225, center),
                new PointF(center.x, center.y + radius),
                fromPolar(radius - delta, 315, center),
                new PointF(center.x + radius, center.y),
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(center.x, center.y, radius, basePaint);
        canvas.drawPath(calculateBezier(points, true), paint);
    }
}
