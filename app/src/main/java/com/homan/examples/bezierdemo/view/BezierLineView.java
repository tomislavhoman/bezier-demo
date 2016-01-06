package com.homan.examples.bezierdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class BezierLineView extends BezierView {

    private int x1 = 0;
    private int y1 = 0;

    private int x2 = 0;
    private int y2 = 0;

    private int x3 = 0;
    private int y3 = 0;

    public BezierLineView(Context context) {
        super(context);
    }

    public BezierLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BezierLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        x1 = 0;
        y1 = h / 2 + 150;

        x2 = w / 2;
        y2 = h / 2 - 150;

        x3 = w;
        y3 = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(calculateBezier(x1, y1, x2, y2, x3, y3), paint);
    }
}
