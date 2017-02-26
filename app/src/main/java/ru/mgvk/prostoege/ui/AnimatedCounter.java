package ru.mgvk.prostoege.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import ru.mgvk.prostoege.DataLoader;

/**
 * Created by mike on 22.02.17.
 */
public class AnimatedCounter extends TextView {

    private final static String TAG = "AnimatedCounter";
    private Context context;
    private ObjectAnimator animator;


    public AnimatedCounter(Context context) {
        this(context, null);
    }

    public AnimatedCounter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AnimatedCounter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public AnimatedCounter(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
//        setFont();
        initAnimation();
    }

    private void setFont() {
        DataLoader.getFont(context, "comic");
    }

    private void initAnimation() {
        animator = new ObjectAnimator();
        animator.setTarget(this);
        animator.setPropertyName("stringtext");

//        String[] v = new String[101];
//        for (int i = 0; i < 101; i++) {
//            v[i]= String.valueOf(i);
//        }
//        animator.setObjectValues((Object[]) v);
        animator.setIntValues(0, 100);
        animator.setDuration(2000);
    }

    public void setStringtext(int i) {
        setText(String.valueOf(i));
    }


    public void startCounting() {
        animator.start();
    }
}
