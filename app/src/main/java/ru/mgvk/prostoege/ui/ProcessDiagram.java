package ru.mgvk.prostoege.ui;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import ru.mgvk.prostoege.R;

public class ProcessDiagram extends View {
    public static final int PERCENT = 1;
    public static final int SLASH   = 2;
    private static double realDPI;
    private final RectF oval = new RectF();
    private       int   w    = 0, h = 0, r = 0;
    private float center_x = 100, center_y = 100;
    private Paint  paint      = new Paint();
    private Paint  textPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int    startColor = Color.CYAN;
    private int    endColor   = Color.MAGENTA;
    private String text       = "TEST";

    private float progress = 0;
    private float max      = 100;
    private int   textType = PERCENT;

    private float text_x = 0;
    private float text_y = 0;

    private int liteColor   = Color.parseColor("#62FF9634");
    private int fullColor   = Color.parseColor("#ff9634");
    private int strokeWidth = 7;

    public ProcessDiagram(Context context) {
        super(context);
        realDPI = context.getResources().getDisplayMetrics().densityDpi;
//        FrameLayout.LayoutParams l = (new FrameLayout.LayoutParams(w = calcSize(200),
//                h = calcSize(200)));
//        l.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
//        setLayoutParams(l);

        h = w = UI.calcSize(82);

        setBackgroundColor(getContext().getResources().getColor(R.color.back_white_color));

        center_x = w / 2;
        center_y = h / 2;
        r = Math.min(w, h) / 2 - strokeWidth;

//        paint.setColor(Color.WHITE);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(25);
        textPaint.setTextAlign(Paint.Align.CENTER);
        text_x = w / 2;
        text_y = h / 2;

        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        oval.set(center_x - r, center_y - r, center_x + r,
                center_y + r);
    }

    public static int calcSize(int size) {
        return (int) (size * (realDPI / (double) 160));
    }

    private int getGradientColor() {

        int kr = 1;
        int kg = 1;
        int kb = 1;

        int dr = ((endColor - startColor) & 0xff0000) >> 16;
        int dg = ((endColor - startColor) & 0x00ff00) >> 8;
        int db = ((endColor - startColor) & 0x0000ff);


        Log.d("Color", "" + kr + " " + kg + " " + kb);

        return startColor
               + (((int) (dr * progress) & 0xff0000)
                  << 16 | ((int) (dg * progress) & 0x00ff00)
                          << 8 | ((int) (db * progress) & 0x0000ff));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(liteColor);
        canvas.drawArc(oval, 270, 360, false, paint);

        paint.setColor(fullColor);
        canvas.drawArc(oval, 270, progress * 360, false, paint);


        text_y = h / 2 - ((textPaint.descent() + textPaint.ascent()) / 2);
        canvas.drawText(text, text_x, text_y, textPaint);

    }


    public double getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        if (progress > max) progress = (int) max;
        this.progress = progress / max;

        if (textType == PERCENT) {
            text = (int) (this.progress * 100) + "%";
        } else {
            text = progress + "/" + (int) max;
        }

        postInvalidate();
    }

    public double getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
        if (textType == PERCENT) {
            text = "0%";
        } else {
            text = "0/" + max;
        }
    }

    public void setTextType(int type) {
        this.textType = type;
    }
}
