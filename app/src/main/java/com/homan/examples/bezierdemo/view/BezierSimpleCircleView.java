package com.homan.examples.bezierdemo.view;

import android.content.Context;
import android.util.AttributeSet;

public class BezierSimpleCircleView extends BezierView {

    private int x1 = 0;
    private int y1 = 0;

    private int x2 = 0;
    private int y2 = 0;

    private int x3 = 0;
    private int y3 = 0;

    private int x4 = 0;
    private int y5 = 0;

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


    }
}
