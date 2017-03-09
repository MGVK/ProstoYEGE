package ru.mgvk.prostoege.ru.mgvk.prostoege.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ru.mgvk.prostoege.*;
import ru.mgvk.prostoege.ui.UI;
import ru.mgvk.util.Reporter;

/**
 * Created by mihail on 23.08.16.
 */
public class ToolsFragment extends Fragment implements OnClickListener {

    public PaintView paintView;
    public PenResizer penResizer;
    MainActivity mainActivity;
    Context context;
    View[] toolViews = new View[4];
    private ViewGroup container;
    private Task currentTask;
    private ColorChooser colorChooser;
    private ImageView rings;
    private FrameLayout mainToolsListLayout;
    private LinearLayout toolsTitle;
    private TextView toolsButton;

    @SuppressLint("ValidFragment")
    public ToolsFragment() {

    }

    @SuppressLint("ValidFragment")
    public ToolsFragment(Context context) {
        mainActivity = (MainActivity) (this.context = context);
        Log.d("ex", "s");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tools, container, false);
        this.container = container;
        mainActivity = (MainActivity) (this.context = inflater.getContext());
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        initViews();
    }

    void initViews() {

        mainToolsListLayout = (FrameLayout) container.findViewById(R.id.main_tools_layout);
        toolsTitle = (LinearLayout) container.findViewById(R.id.tools_title);
        (toolsButton = (TextView) container.findViewById(R.id.btn_tools)).setOnClickListener(this);
        rings = (ImageView) container.findViewById(R.id.rings_tools);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitMode();
        }

        paintView = (PaintView) container.findViewById(R.id.paintView);
        paintView.setParams(Color.BLACK, 1);
        penResizer = (PenResizer) container.findViewById(R.id.penResizer);
        penResizer.setPaintView(paintView);
        colorChooser = (ColorChooser) container.findViewById(R.id.colorChooser);
        colorChooser.setPaintView(paintView);

        container.findViewById(R.id.btn_back_tools).setOnClickListener(this);
        (toolViews[0] = container.findViewById(R.id.btn_tools_pen)).setOnClickListener(this);
        (toolViews[1] = container.findViewById(R.id.btn_tools_color)).setOnClickListener(this);
        (toolViews[2] = container.findViewById(R.id.btn_tools_erase)).setOnClickListener(this);
        (toolViews[3] = container.findViewById(R.id.btn_tools_basket)).setOnClickListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitMode();
        } else {
            setLandscapeMode();
        }
    }

    public void setPortraitMode() {
        if (rings != null) {
            rings.setVisibility(View.GONE);
        }
        if (mainToolsListLayout != null && toolsTitle != null) {
            FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) mainToolsListLayout.getLayoutParams());
            lp.leftMargin = 0;
            mainToolsListLayout.setLayoutParams(lp);
            lp = (FrameLayout.LayoutParams) toolsTitle.getLayoutParams();
            lp.leftMargin = 0;
            toolsTitle.setLayoutParams(lp);
        }


    }

    public void setLandscapeMode() {
        if (rings != null) {
            rings.setVisibility(View.VISIBLE);
        }
        if (mainToolsListLayout != null && toolsTitle != null) {
            FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) mainToolsListLayout.getLayoutParams());
            lp.leftMargin = UI.calcSize(32);
            mainToolsListLayout.setLayoutParams(lp);
            lp = (FrameLayout.LayoutParams) toolsTitle.getLayoutParams();
            lp.leftMargin = UI.calcSize(32);
            toolsTitle.setLayoutParams(lp);
        }

    }

    void setPressed(View v){
        for (View toolView : toolViews) {
            toolView.setAlpha((float) 0.5);
        }
        v.setAlpha(1);
    }

    void toggleViewsVisibility(View v) {
        if(v==null){
            penResizer.setVisibility(View.GONE);
            colorChooser.setVisibility(View.GONE);
            return;
        }
        if (v.getVisibility() == View.GONE) {
            penResizer.setVisibility(View.GONE);
            colorChooser.setVisibility(View.GONE);
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        try {
            if (!hidden) {
//                excercisesListLayout.removeAllViews();
            }
        } catch (NullPointerException e) {
        }
    }


    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.btn_back_tools) {
//            mainActivity.ui.openExercisesListFragment();
                mainActivity.onBackPressed();
                return;
            }
        } catch (Exception e) {
            Reporter.report(context, e, ((MainActivity) context).reportSubject);
        }

        if (v.getId() == R.id.btn_tools_pen) {
            toggleViewsVisibility(penResizer);
            setPressed(v);
            return;
        }
        if (v.getId() == R.id.btn_tools_color) {
            toggleViewsVisibility(colorChooser);
            setPressed(v);
            return;
        }

        if (v.getId() == R.id.btn_tools_erase) {
            paintView.setLineColor(Color.WHITE);
            toggleViewsVisibility(null);
            setPressed(v);
            return;
        }
        if (v.getId() == R.id.btn_tools_basket) {
            paintView.clear();
            toggleViewsVisibility(null);
            setPressed(v);
            return;
        }
        if (v.getId() == R.id.btn_tools) {
            animateToolsTitle(toolsTitle.getVisibility() == View.GONE);
        }

    }

    void animateToolsTitle(final boolean appear) {
        ObjectAnimator a;
        if (appear) {
            a = ObjectAnimator.ofFloat(toolsTitle, "alpha", 0, 1);
            toolsTitle.setVisibility(View.VISIBLE);
        } else {
            a = ObjectAnimator.ofFloat(toolsTitle, "alpha", 1, 0);
            a.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                        toolsTitle.setVisibility(View.GONE);
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

    public Task getTask() {
        return currentTask;
    }

    public void setTask(Task currentTask) {
        this.currentTask = currentTask;
    }


}
