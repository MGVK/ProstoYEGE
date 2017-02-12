package ru.mgvk.prostoege.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;

/**
 * Created by mihail on 19.10.16.
 */
public class HintWindow extends DialogWindow {

    Context context;
    public AttachedLayout layout;


    public HintWindow(Context context) {
        super(context);
        this.context = context;
        initViews();
    }

    @Override
    public void addView(View child) {
        layout.addView(child);
    }

    void initViews() {
        layout = new AttachedLayout(context);
        super.addView(layout);
    }



    class AttachedLayout extends FrameLayout {

        int w, h;

        public AttachedLayout(Context context) {
            super(context);
            setBackgroundResource(R.drawable.hint_back);
            setLayoutParams(new ViewGroup.LayoutParams(-2, - 2));
            int p = UI.calcSize(10);
            setPadding(p, p, p, p);
//            initSizes();
        }

//        @Override
//        public void setBackgroundResource(int resid) {
//            ImageView imageView = new ImageView(context);
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setLayoutParams(new LinearLayout.LayoutParams(-2,-2));
////            imageView.setAdjustViewBounds(true);
//            imageView.setImageDrawable(context.getResources().getDrawable(resid));
//            this.addView(imageView);
////            super.setBackgroundResource(resid);
//        }

        void initSizes() {
            if (context.getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_PORTRAIT) {
                w = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
                ViewGroup.LayoutParams lp = new LinearLayout.LayoutParams(w, h = ((int) (5 / 6.0 * w)));
                setLayoutParams(lp);
            } else {
                h = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.7);
                ViewGroup.LayoutParams lp = new LinearLayout.LayoutParams(w = ((int) (6 / 5.0 * h)), h);
                setLayoutParams(lp);
            }

        }


    }


}
