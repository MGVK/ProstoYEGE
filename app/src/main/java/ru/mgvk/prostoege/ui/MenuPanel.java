package ru.mgvk.prostoege.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Space;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;

/**
 * Created by mihail on 30.09.16.
 */
public class MenuPanel extends FrameLayout {

    private MainActivity        mainActivity;
    private Context             context;
    private ItemsLayout         itemsLayout;
    private VerticalScrollView  scroll;
    private ImageButton         backBtn;
    private OnBackClickListener listener;


    public MenuPanel(Context context) {
        super(context);
        mainActivity = (MainActivity) (this.context = context);
        initViews();
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
        try {
            scroll.setLayoutParams(new LayoutParams(mainActivity.ui.rootView1.getWidth() / 2, -1));
        } catch (Exception e) {
        }
    }

    private void initViews() {
        initScroll();
        initItemsLayout();
        setBackBtn();


        final LayoutParams lp = new LayoutParams(
                context.getResources().getDisplayMetrics().widthPixels, -1);
        lp.gravity = Gravity.LEFT;
        setLayoutParams(lp);

        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(MenuPanel.this);
                }
            }
        });
    }

    private void initItemsLayout() {
        itemsLayout = new ItemsLayout(context);
        itemsLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        itemsLayout.setGravity(Gravity.CENTER);
        scroll.addView(itemsLayout);
    }

    private void initScroll() {
        scroll = new VerticalScrollView(context);
        scroll.setBackgroundColor(Color.parseColor("#C60D0D0D"));
        scroll.setLayoutParams(
                new LayoutParams(context.getResources().getDisplayMetrics().widthPixels / 2, -1));
        this.addView(scroll);
    }

    private void setBackBtn() {
        backBtn = new ImageButton(context);
        FrameLayout.LayoutParams lp = new LayoutParams(-2, -2);
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        backBtn.setLayoutParams(lp);
        backBtn.setImageDrawable(
                context.getResources().getDrawable(R.drawable.button_answer_clear));
        backBtn.setBackgroundColor(Color.argb(0, 0, 0, 0));
        backBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(MenuPanel.this);
                }
            }
        });

        this.addView(backBtn);
    }


    public VerticalScrollView getScroll() {
        return scroll;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        itemsLayout.getSpace()
                .setLayoutParams(new LinearLayout.LayoutParams(-1, backBtn.getHeight()));
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
//        super.setOnClickListener(l);
        scroll.setOnClickListener(l);
    }

    /**
     * вызывается при клике кнопки возврата или свободной области экрана
     *
     * @param l
     */

    public void setOnBackClickListener(OnBackClickListener l) {
        listener = l;
    }

    public void addItem(MenuItem item) {
        itemsLayout.addView(item);
    }


    public interface OnBackClickListener {

        void onClick(MenuPanel menu);

    }

    private class ItemsLayout extends LinearLayout {

        private Space space;


        public ItemsLayout(Context context) {
            super(context);
            setOrientation(VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
//            lp.setMargins(0, (int) UI.calcSize(40), 0, 0);
            setLayoutParams(lp);
            addSpace();
        }

        private void addSpace() {
            space = new Space(context);
            space.setLayoutParams(new FrameLayout.LayoutParams(-1, UI.calcSize(40)));
            super.addView(space);
        }

        @Override
        public void addView(View child) {
            super.addView(child);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    child.getLayoutParams().width,
                    child.getLayoutParams().height, 1);
            lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

//            child.setLayoutParams(lp);

        }

        @Override
        public void removeAllViews() {
            super.removeAllViews();
            addSpace();
        }

        public Space getSpace() {
            if (space == null) {
                addSpace();
            }
            return space;
        }
    }


}
