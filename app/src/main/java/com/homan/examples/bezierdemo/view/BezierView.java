package com.homan.examples.bezierdemo.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

public class BezierView extends View {

    protected Paint paint;
    protected Paint basePaint;

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

        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setColor(Color.RED);
        basePaint.setStyle(Paint.Style.STROKE);
        basePaint.setStrokeWidth(2.0f);
    }

    protected Path calculateBezier(int x1, int y1, int x2, int y2, int x3, int y3) {
        final Path path = new Path();
        path.moveTo(x1, y1);
        path.quadTo(x2, y2, x3, y3);
        return path;
    }

    protected Path calculateBezier(PointF[] points, boolean closed) {
        PointF[] divided = CatmullRomSplineUtils.subdividePoints(points, 10, closed);
        final Path path = new Path();
        path.moveTo((float) divided[0].x, (float) divided[0].y);
        for (int i = 0; i < divided.length - 2; i++) {
            path.quadTo((float) divided[i + 1].x, (float) divided[i + 1].y, (float) divided[i + 2].x, (float) divided[i + 2].y);
        }
        return path;
    }

    protected Path calculateBezier(PointF[] points) {
        return calculateBezier(points, false);
    }

    protected PointF fromPolar(int r, int phi) {
        return fromPolar(r, phi, new PointF(0, 0));
    }

    protected PointF fromPolar(int r, int phi, PointF center) {
        final PointF point = new PointF();
        final double radians = Math.toRadians(phi);
        point.set((float) Math.cos(radians) * r + center.x, (float) -Math.sin(radians) * r + center.y);
        return point;
    }

    private static class CatmullRomSpline {
        private double p0, p1, p2, p3;

        public CatmullRomSpline(double p0, double p1, double p2, double p3) {
            this.p0 = p0;
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
        }

        public double q(double t) {
            return 0.5 * ((2 * p1) +
                    (p2 - p0) * t +
                    (2 * p0 - 5 * p1 + 4 * p2 - p3) * t * t +
                    (3 * p1 - p0 - 3 * p2 + p3) * t * t * t);
        }

        /**
         * @return the p0
         */
        public double getP0() {
            return p0;
        }

        /**
         * @param p0 the p0 to set
         */
        public void setP0(double p0) {
            this.p0 = p0;
        }

        /**
         * @return the p1
         */
        public double getP1() {
            return p1;
        }

        /**
         * @param p1 the p1 to set
         */
        public void setP1(double p1) {
            this.p1 = p1;
        }

        /**
         * @return the p2
         */
        public double getP2() {
            return p2;
        }

        /**
         * @param p2 the p2 to set
         */
        public void setP2(double p2) {
            this.p2 = p2;
        }

        /**
         * @return the p3
         */
        public double getP3() {
            return p3;
        }

        /**
         * @param p3 the p3 to set
         */
        public void setP3(double p3) {
            this.p3 = p3;
        }
    }

    private static class CatmullRomSpline2D {
        private CatmullRomSpline splineXVals, splineYVals;

        public CatmullRomSpline2D(PointF p0, PointF p1, PointF p2, PointF p3) {
            assert p0 != null : "p0 cannot be null";
            assert p1 != null : "p1 cannot be null";
            assert p2 != null : "p2 cannot be null";
            assert p3 != null : "p3 cannot be null";

            splineXVals = new CatmullRomSpline(p0.x, p1.x, p2.x, p3.x);
            splineYVals = new CatmullRomSpline(p0.y, p1.y, p2.y, p3.y);
        }

        public PointF q(float t) {
            return new PointF((float) splineXVals.q(t), (float) splineYVals.q(t));
        }
    }

    private static class CatmullRomSplineUtils {

        public static PointF[] subdividePoints(PointF[] points, int subdivisions) {
            return subdividePoints(points, subdivisions, false);
        }

        /**
         * Creates catmull spline curves between the points array.
         *
         * @param points       The current 2D points array
         * @param subdivisions The number of subdivisions to add between each of the points.
         * @return A larger array with the points subdivided.
         */
        public static PointF[] subdividePoints(PointF[] points, int subdivisions, boolean closed) {
            assert points != null;
            assert points.length >= 3;

            PointF[] subdividedPoints = new PointF[((points.length - 1) * subdivisions) + 1];

            float increments = 1f / (float) subdivisions;

            for (int i = 0; i < points.length - 1; i++) {
                PointF p0 = i == 0 ? (closed ? points[points.length - 2] : points[i]) : points[i - 1];
                PointF p1 = points[i];
                PointF p2 = points[i + 1];
                PointF p3 = (i + 2 == points.length) ? (closed ? points[1] : points[i + 1]) : points[i + 2];

                CatmullRomSpline2D crs = new CatmullRomSpline2D(p0, p1, p2, p3);

                for (int j = 0; j <= subdivisions; j++) {
                    subdividedPoints[(i * subdivisions) + j] = crs.q(j * increments);
                }
            }

            return subdividedPoints;
        }
    }
}
