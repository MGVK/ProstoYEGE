package ru.mgvk.prostoege.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by mihail on 18.10.16.
 */
public class ToolButton extends ImageView {
    public ToolButton(Context context) {
        super(context);
    }

    public ToolButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToolButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ToolButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(widthMeasureSpec));
    }

    boolean pressed=false;

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
//        if(event.getAction()==MotionEvent.ACTION_DOWN){
//            pressed = true;
//
//        }
//        if(event.getAction()==MotionEvent.ACTION_UP){
//            pressed = false;
////            callOnClick();
//        }
//        return super.onTouchEvent(event);
//    }

}
