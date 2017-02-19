package ru.mgvk.prostoege.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;

/**
 * Created by mike on 15.02.17.
 */
@SuppressWarnings("Duplicates")
public class LicenseWindow extends DialogWindow {


    private Context context;
    private AttachedLayout layout;
    private int width;
    private FrameLayout mainLayout;

    public LicenseWindow(Context context) {
        super(context);
        this.context = context;

        initSizes();
        initViews();
    }

    private void initViews() {
        layout = new AttachedLayout(context);
        layout.setLayoutParams(new FrameLayout.LayoutParams((int) (0.9 * width), -2));

        mainLayout = new FrameLayout(context);
        mainLayout.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        setBackground();
        mainLayout.addView(layout);

        super.addView(mainLayout);

    }

    private void initSizes() {
        width = ((MainActivity) context).ui.deviceWidth;
    }


    private void setBackground() {

        mainLayout.setBackgroundResource(R.drawable.beige_window_back);

        int[] gravity = {Gravity.TOP | Gravity.LEFT, Gravity.TOP | Gravity.RIGHT,
                Gravity.BOTTOM | Gravity.LEFT, Gravity.BOTTOM | Gravity.RIGHT};
        int[] resIDs = {R.drawable.top_left, R.drawable.top_right,
                R.drawable.bottom_left, R.drawable.bottom_right};

        for (int i = 0; i <= 3; i++) {
            ImageView view = new ImageView(context);
            view.setLayoutParams(new FrameLayout.LayoutParams(UI.calcSize(25), UI.calcSize(30)));
            ((FrameLayout.LayoutParams) view.getLayoutParams()).gravity = gravity[i];
            view.setImageResource(resIDs[i]);
            mainLayout.addView(view);
        }

    }


    class AttachedLayout extends LinearLayout {

        public AttachedLayout(Context context) {
            super(context);
        }

        void initViews() {


        }

    }
}
