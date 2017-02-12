package ru.mgvk.prostoege;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by mihail on 22.08.16.
 */
public class PaintView extends View {

    private int lineColor, alpha, lineWidth;
    private ArrayList<Point> points = new ArrayList<>();
    private Paint paint = new Paint();
    private int backgroundColor = Color.WHITE;

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PaintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PaintView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PaintView(Context context) {
        super(context);
    }

    public void setParams(int linecolor, int alpha, int lwidth) {
        this.lineColor = linecolor;
        this.alpha = alpha;
        this.lineWidth = lwidth;
    }

    public void setParams(int linecolor, int lwidth) {
        setParams(linecolor, 255, lwidth);
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth+1;
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {

        int action = e.getAction();

        if (action == MotionEvent.ACTION_UP) {
            addPoint(null);
            addPoint(new Point(e.getX(), e.getY(), lineColor, alpha, lineWidth));
            addPoint(null);
            ((MainActivity) getContext()).ui.mainScroll.setScrollEnabled(true);

        } else if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN) {
            ((MainActivity) getContext()).ui.mainScroll.setScrollEnabled(false);
            addPoint(new Point(e.getX(), e.getY(), lineColor, alpha, lineWidth));
            postInvalidate();
        }

        return true;
    }

    public void clear() {

        points.clear();
        points.add(new Point(0, 0, lineColor, alpha, lineWidth));
        addPoint(null);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Paint p = new Paint();
        p.setColor(backgroundColor);
        canvas.drawPaint(p);
        Point currPoint = null;
        for (Point newPoint : getPoints()) {
            if (currPoint != null && newPoint != null) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(newPoint.getColor());
                paint.setAlpha(newPoint.getAlpha());
                paint.setStrokeWidth(currPoint.getWidth());

                canvas.drawLine(currPoint.getX(),currPoint.getY(),newPoint.getX(),newPoint.getY(), paint);

                canvas.drawCircle(currPoint.getX(), currPoint.getY(),currPoint.getWidth()/2, paint);
            }
            currPoint = newPoint;
        }
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        this.setMeasuredDimension(WIDTH, HEIGHT);
//    }

    void addPoint(Point point) {
        points.add(point);
    }

    ArrayList<Point> getPoints() {
        return points;
    }


    public static class Point {
        private float x, y;
        private int color, alpha, width;

        public Point(float x, float y, int color, int alpha, int width) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.alpha = alpha;
            this.width = width;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public int getColor() {
            return color;
        }

        public int getAlpha() {
            return alpha;
        }

        public int getWidth() {
            return width;
        }
    }

}
