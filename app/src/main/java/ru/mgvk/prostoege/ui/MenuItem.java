package ru.mgvk.prostoege.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.mgvk.prostoege.R;

/**
 * Created by mihail on 07.10.16.
 */
public class MenuItem extends LinearLayout {

    Context context;

    private ImageView image;
    private TextView textView;

    public MenuItem(Context context) {
        super(context);

        this.context = context;

        setOrientation(VERTICAL);

        initImage();
        initText();

        ViewGroup.LayoutParams lp = new FrameLayout.LayoutParams(-2,-2);

//        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.white_background));
    }

    void initImage() {
        image = new ImageView(context);
        LayoutParams lp = new LayoutParams(-1, -2);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        lp.setMargins(0, UI.calcSize(10), 0, 0);
        image.setLayoutParams(lp);
        this.addView(image);
    }

    void initText() {
        textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        LayoutParams lp = new LayoutParams(-1, -2);
        lp.setMargins(0, UI.calcSize(5), 0, 0);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        textView.setLayoutParams(lp);
        this.addView(textView);
    }


    void setText(String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }
    void setText(int resID){
        setText(context.getResources().getString(resID));
    }

    public void setImage(Drawable drawable) {
        this.image.setImageDrawable(drawable);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction()==MotionEvent.ACTION_DOWN){
            setAlpha((float) 0.7);
        }
        if(event.getAction()==MotionEvent.ACTION_UP
                ||event.getAction()==MotionEvent.ACTION_BUTTON_RELEASE
                ||event.getAction()==MotionEvent.ACTION_CANCEL){
            setAlpha((float) 1);
        }

        return super.onTouchEvent(event);
    }
}
