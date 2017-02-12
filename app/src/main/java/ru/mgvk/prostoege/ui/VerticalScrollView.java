package ru.mgvk.prostoege.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import ru.mgvk.prostoege.MainActivity;

/**
 * Created by mihail on 12.09.16.
 */
public class VerticalScrollView extends ScrollView {
    public VerticalScrollView(Context context) {
        super(context);
    }

    public VerticalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VerticalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    boolean scrollEnabled = true;

    public boolean isScrollEnabled() {
        return scrollEnabled;
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        this.scrollEnabled = scrollEnabled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(ev.getAction()==MotionEvent.ACTION_DOWN) {
            ((MainActivity) getContext()).ui.mainScroll.setScrollEnabled(false);
        }
        if(ev.getAction()==MotionEvent.ACTION_UP) {
            ((MainActivity) getContext()).ui.mainScroll.setScrollEnabled(true);
        }
        return isScrollEnabled() && super.onTouchEvent(ev);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isScrollEnabled() && super.onInterceptTouchEvent(ev);
    }
}
