package ru.mgvk.prostoege.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import ru.mgvk.prostoege.DataLoader;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;

/**
 * Created by mihail on 08.10.16.
 */
public class BalanceWindow extends DialogWindow implements View.OnClickListener {

    Context context;
    FrameLayout mainLayout;
    int count = 0;
    private AttachedLayout layout;

    public BalanceWindow(Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        mainLayout = new FrameLayout(context);
        mainLayout.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        setBackground();
        layout = new AttachedLayout(context);
        mainLayout.addView(layout);
        super.addView(mainLayout);

    }

    void setBackground() {

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
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getTag() != null && ((Integer) v.getTag()) == 1 && (++count) == 20) {
                        Toast.makeText(context, "Made by MGVK: vk.com/mihailllo", Toast.LENGTH_SHORT).show();
                        count = 0;
                    }
                }
            });
            view.setTag(i);
        }

    }

    @Override
    public void addView(View child) {
//        super.addView(child);
        layout.addView(child);
    }

    class AttachedLayout extends LinearLayout implements OnClickListener {

        int[] resIds = {R.drawable.yoz_1, R.drawable.yoz_2, R.drawable.yoz_3};
        int[] gravity = {Gravity.LEFT | Gravity.TOP, Gravity.CENTER, Gravity.RIGHT | Gravity.BOTTOM};
        int w, h;

        public AttachedLayout(Context context) {
            super(context);
            initSizes();
            setPadding(UI.calcSize(10), UI.calcSize(5), UI.calcSize(10), UI.calcSize(5));
            setOrientation(VERTICAL);
            initViews();
        }

        void initSizes() {
            if (context.getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_PORTRAIT) {
                w = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
                ViewGroup.LayoutParams lp = new LayoutParams(w, h = ((int) (5 / 6.0 * w)));
                setLayoutParams(lp);
            } else {
                h = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.7);
                ViewGroup.LayoutParams lp = new LayoutParams(w = ((int) (6 / 5.0 * h)), h);
                setLayoutParams(lp);
            }

        }

        void initViews() {
            addSpace();
            setTitle();
            addSpace();
            initBtns();
            addSpace();
            setLabel();
        }

        void setTitle() {
            TextView title = new TextView(context);
            title.setLayoutParams(new LayoutParams(-2, (int) (h / 7.0)));

            title.setText(context.getResources().getString(R.string.balance_title)
                    + ((MainActivity) context).profile.Coins);
            title.setTextSize(14);
            title.setGravity(Gravity.CENTER);
            title.setBackgroundResource(R.drawable.beige_title_back);
            title.setTextColor(Color.parseColor("#3f1b0b"));
            this.addView(title);
        }

        void addSpace() {
            Space sp = new Space(context);
            sp.setLayoutParams(new ViewGroup.LayoutParams(-1, UI.calcSize(4)));
            this.addView(sp);
        }

        void initBtns() {

            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setLayoutParams(new LayoutParams(-1, (int) (3 / 5.0 * h)));
            for (int i = 0; i < 3; i++) {
                ImageButton btn = new ImageButton(context);
                FrameLayout.LayoutParams lp = (new FrameLayout.LayoutParams((int) (w / 3.0 * 0.9), (int) (h * 2 / 5.0 * 0.9)));
                lp.gravity = gravity[i];
                btn.setLayoutParams(lp);
                btn.setBackgroundResource(resIds[i]);
                btn.setTag(i);
                btn.setOnClickListener(this);
                frameLayout.addView(btn);
            }
            this.addView(frameLayout);
        }


        private void setLabel() {
            TextView label = new TextView(context);
            label.setText(R.string.balance_label);
            label.setTextSize(16);
            label.setGravity(Gravity.RIGHT);
            label.setTextColor(Color.parseColor("#3f1b0b"));

            try {
                label.setTypeface(DataLoader.getFont(context, "SegoeScript"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.addView(label);

        }


        @Override
        public void onClick(View v) {
            try {
                ((MainActivity) context).pays.buyPack((Integer) v.getTag());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
