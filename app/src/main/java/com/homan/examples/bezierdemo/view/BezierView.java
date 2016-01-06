package com.homan.examples.bezierdemo.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class BezierView extends View {

    protected Paint paint;

    public BezierView(Context context) {
        super(context);
        init();
    }

    public BezierView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BezierView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
    }

    protected Path drawBezier(int x1, int y1, int x2, int y2, int x3, int y3) {
        final Path path = new Path();
        path.moveTo(x1, y1);
        path.quadTo(x2, y2, x3, y3);
        return path;
    }
}
