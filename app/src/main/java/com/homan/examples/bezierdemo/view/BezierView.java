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

    protected Path calculateBezier(int x1, int y1, int x2, int y2, int x3, int y3) {
        final Path path = new Path();
        path.moveTo(x1, y1);
        path.quadTo(x2, y2, x3, y3);
        return path;
    }

    protected Path calculateBezier(Point2D[] points) {
        Point2D[] divided = CatmullRomSplineUtils.subdividePoints(points, 10);
        final Path path = new Path();
        path.moveTo(divided[0].x, divided[0].y);
        for (int i = 0; i < divided.length - 2; i++) {
            path.quadTo(divided[i + 1].x, divided[i + 1].y, divided[i + 2].x, divided[i + 2].y);
        }
        return path;
    }

    private static class CatmullRomSpline {
        private float p0, p1, p2, p3;

        public CatmullRomSpline(float p0, float p1, float p2, float p3) {
            this.p0 = p0;
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
        }

        public float q(float t) {
            return 0.5f * ((2 * p1) +
                    (p2 - p0) * t +
                    (2 * p0 - 5 * p1 + 4 * p2 - p3) * t * t +
                    (3 * p1 - p0 - 3 * p2 + p3) * t * t * t);
        }

        /**
         * @return the p0
         */
        public float getP0() {
            return p0;
        }

        /**
         * @param p0 the p0 to set
         */
        public void setP0(float p0) {
            this.p0 = p0;
        }

        /**
         * @return the p1
         */
        public float getP1() {
            return p1;
        }

        /**
         * @param p1 the p1 to set
         */
        public void setP1(float p1) {
            this.p1 = p1;
        }

        /**
         * @return the p2
         */
        public float getP2() {
            return p2;
        }

        /**
         * @param p2 the p2 to set
         */
        public void setP2(float p2) {
            this.p2 = p2;
        }

        /**
         * @return the p3
         */
        public float getP3() {
            return p3;
        }

        /**
         * @param p3 the p3 to set
         */
        public void setP3(float p3) {
            this.p3 = p3;
        }
    }

    private static class CatmullRomSpline2D {
        private CatmullRomSpline splineXVals, splineYVals;

        public CatmullRomSpline2D(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
            assert p0 != null : "p0 cannot be null";
            assert p1 != null : "p1 cannot be null";
            assert p2 != null : "p2 cannot be null";
            assert p3 != null : "p3 cannot be null";

            splineXVals = new CatmullRomSpline(p0.getX(), p1.getX(), p2.getX(), p3.getX());
            splineYVals = new CatmullRomSpline(p0.getY(), p1.getY(), p2.getY(), p3.getY());
        }

        public Point2D q(float t) {
            return new Point2D(splineXVals.q(t), splineYVals.q(t));
        }
    }

    protected static class Point2D {
        private float x, y;

        public Point2D() {
            this(0f, 0f);
        }

        public Point2D(float x, float y) {
            this.x = x;
            this.y = y;
        }

        /**
         * @return the x
         */
        public float getX() {
            return x;
        }

        /**
         * @param x the x to set
         */
        public void setX(float x) {
            this.x = x;
        }

        /**
         * @return the y
         */
        public float getY() {
            return y;
        }

        /**
         * @param y the y to set
         */
        public void setY(float y) {
            this.y = y;
        }

        @Override
        public String toString() {
            return String.format("(X0:%f, Y0:%3f)", x, y);
        }
    }

    private static class CatmullRomSplineUtils {
        /**
         * Creates catmull spline curves between the points array.
         *
         * @param points       The current 2D points array
         * @param subdivisions The number of subdivisions to add between each of the points.
         * @return A larger array with the points subdivided.
         */
        public static Point2D[] subdividePoints(Point2D[] points, int subdivisions) {
            assert points != null;
            assert points.length >= 3;

            Point2D[] subdividedPoints = new Point2D[((points.length - 1) * subdivisions) + 1];

            float increments = 1f / (float) subdivisions;

            for (int i = 0; i < points.length - 1; i++) {
                Point2D p0 = i == 0 ? points[i] : points[i - 1];
                Point2D p1 = points[i];
                Point2D p2 = points[i + 1];
                Point2D p3 = (i + 2 == points.length) ? points[i + 1] : points[i + 2];

                CatmullRomSpline2D crs = new CatmullRomSpline2D(p0, p1, p2, p3);

                for (int j = 0; j <= subdivisions; j++) {
                    subdividedPoints[(i * subdivisions) + j] = crs.q(j * increments);
                }
            }

            return subdividedPoints;
        }
    }
}
