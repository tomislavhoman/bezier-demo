package com.homan.examples.bezierdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;

public class CompositeBezierLineView extends BezierView {

    private int centerX1 = 0;
    private int centerY1 = 0;
    private int centerX2 = 0;
    private int centerY2 = 0;

    private PointF[] points;

    public CompositeBezierLineView(Context context) {
        super(context);
    }

    public CompositeBezierLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CompositeBezierLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        final int deltaX = w / 7;
        final int baseY = h / 2;
        final int deltaY = 50;

        centerX1 = 0;
        centerX2 = w;
        centerY1 = baseY;
        centerY2 = baseY;

        int x1 = 0;
        int y1 = baseY;

        int x2 = x1 + deltaX;
        int y2 = baseY + deltaY;

        int x3 = x2 + deltaX;
        int y3 = baseY - 2 * deltaY;

        int x4 = x3 + deltaX;
        int y4 = baseY + 3 * deltaY;

        int x5 = x4 + deltaX;
        int y5 = baseY - 4 * deltaY;

        int x6 = x5 + deltaX;
        int y6 = baseY + 5 * deltaY;

        points = new PointF[]{
                new PointF(x1, y1),
                new PointF(x2, y2),
                new PointF(x3, y3),
                new PointF(x4, y4),
                new PointF(x5, y5),
                new PointF(x6, y6),
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(centerX1, centerY1, centerX2, centerY2, basePaint);
        canvas.drawPath(calculateBezier(points), paint);
    }
}
