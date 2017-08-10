package ru.mgvk.prostoege.ui;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import ru.mgvk.prostoege.MainActivity;

/**
 * Created by mike on 28.07.17.
 */
public class MyCoordinatorLayout extends CoordinatorLayout {
    private boolean scrollEnabled = true;

    public MyCoordinatorLayout(Context context) {
        super(context);
    }

    public MyCoordinatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public boolean isScrollEnabled() {
        return scrollEnabled;
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        this.scrollEnabled = scrollEnabled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            ((MainActivity) getContext()).ui.mainScroll.setScrollEnabled(false);
        }
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            ((MainActivity) getContext()).ui.mainScroll.setScrollEnabled(true);
        }
        return isScrollEnabled() && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isScrollEnabled() && super.onInterceptTouchEvent(ev);
    }
}
