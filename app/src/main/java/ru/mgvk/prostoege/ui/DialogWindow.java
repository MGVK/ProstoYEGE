package ru.mgvk.prostoege.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import ru.mgvk.prostoege.MainActivity;

/**
 * Created by mihail on 19.08.16.
 */
public class DialogWindow extends RelativeLayout implements View.OnClickListener {

    private final static boolean APPEAR=true,DISAPPEAR = false;
    private ViewGroup rootView;
    private OnClosingListener onClosingListener;
    private Context context;


    public DialogWindow(Context context) {
        super(context);
        this.context = context;
        setBackgroundColor(Color.argb(194, 0, 0, 0));
        setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        setGravity(Gravity.CENTER);
        setOnClickListener(this);
    }

    protected void open(){
        (rootView = ((ViewGroup) ((Activity) context).getWindow().getDecorView().getRootView()))
                .addView(this);
        animateAlpha(APPEAR);
        ((MainActivity) context).addToBackStack(new Runnable() {
            @Override
            public void run() {
                close();
            }
        });

    }

    @Override
    public void onClick(View v) {
        close();
    }


    protected void animateAlpha(boolean type) {
        ObjectAnimator a = new ObjectAnimator();
        a.setTarget(this);
        a.setPropertyName("alpha");
        if(type){
            a.setFloatValues(0, 1);
        }else{
            a.setFloatValues(1, 0);
            a.addListener(new Animator.AnimatorListener() {


                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    rootView.removeView(DialogWindow.this);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        a.setDuration(200);
        a.start();
    }

    public void close(){
        if(onClosingListener != null){
            onClosingListener.onClose(this);
        }
        animateAlpha(DISAPPEAR);
        ((MainActivity) context).getBackStack().removeLastAction();
    }

    public void forceClose() {
        animateAlpha(DISAPPEAR);
    }

    public void closeWithDelay(final long ms) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(ms);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        close();
                    }
                });
            }
        }).start();
    }

    public OnClosingListener getOnClosingListener() {
        return onClosingListener;
    }

    public void setOnClosingListener(OnClosingListener onClosingListener) {
        this.onClosingListener = onClosingListener;
    }

    public interface OnClosingListener {
        void onClose(DialogWindow window);
    }
}
