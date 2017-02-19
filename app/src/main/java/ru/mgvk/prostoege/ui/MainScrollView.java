package ru.mgvk.prostoege.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import ru.mgvk.prostoege.MainActivity;

import java.util.ArrayList;

/**
 * Created by mihail on 09.09.16.
 */
public class MainScrollView extends HorizontalScrollView{


    float scrollLevel = 0.29f;
    private int halfWidth=0;
    private boolean scrollEnabled=true;
    private int screenState = FOCUS_LEFT;
    private ArrayList<OnScreenSwitchedListener> onScreenSwitchedListeners = new ArrayList<>();

    public MainScrollView(Context context) {
        super(context);
    }

    public MainScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MainScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if(isScrollEnabled()){
            if(ev.getAction()==MotionEvent.ACTION_UP){
                if(Math.abs(getScrollX())<=getWidth()*scrollLevel){
                    switchLeft();
                    scrollLevel = 0.29f;
                }else{
                    switchRight();
                    scrollLevel = 0.71f; // 1-0.29f
                }
                return true;
            }
            return super.onTouchEvent(ev);
        }else{
            return false;
        }

    }


    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isScrollEnabled() && super.onInterceptTouchEvent(ev);
    }

    public boolean isScrollEnabled() {
        return scrollEnabled;
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        this.scrollEnabled = scrollEnabled;
    }

    private void switchRight() {
        fullScroll(screenState = FOCUS_RIGHT);
        for (OnScreenSwitchedListener onScreenSwitch : onScreenSwitchedListeners) {
            onScreenSwitch.switchedRight();
        }
    }


    /**
     * ATTENTION!!!!
     * THIS VOID DON'T CALL {@link OnScreenSwitchedListener} INTERFACE
     */
    public void toRight() {
        fullScroll(screenState = FOCUS_RIGHT);
    }

    /**
     * ATTENTION!!!!
     * THIS VOID DON'T CALL {@link OnScreenSwitchedListener} INTERFACE
     */
    public void toLeft() {
        fullScroll(screenState = FOCUS_LEFT);
    }

    private void switchLeft() {
        fullScroll(screenState = FOCUS_LEFT);
        for (OnScreenSwitchedListener onScreenSwitch : onScreenSwitchedListeners) {
            onScreenSwitch.switchedLeft();
        }
    }

    public void addOnScreenSwitchedListener(OnScreenSwitchedListener onScreenSwitchedListener) {
        this.onScreenSwitchedListeners.add(onScreenSwitchedListener);
    }
    public void removeOnScreenSwitchedListener(OnScreenSwitchedListener onScreenSwitchedListener){
        this.onScreenSwitchedListeners.remove(onScreenSwitchedListener);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((MainActivity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fullScroll(screenState);
                    }
                });
            }
        }).start();


    }

    public interface OnScreenSwitchedListener {

        public void switchedRight();

        public void switchedLeft();

    }
}
