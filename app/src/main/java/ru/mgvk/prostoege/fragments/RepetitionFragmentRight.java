package ru.mgvk.prostoege.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;

import java.util.ArrayList;

/**
 * Created by mike on 28.07.17.
 */
public class RepetitionFragmentRight extends Fragment implements View.OnClickListener {


    private Context      context;
    private MainActivity mainActivity;
    private ViewGroup    container;
    private LinearLayout mainLayout;
    private ImageButton  LeftButton;
    private ArrayList<OnMainLayoutInited> onMainLayoutIniteds = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_repetition_right, container, false);
        mainActivity = (MainActivity) (this.context = inflater.getContext());
        this.container = container;

        return view;

    }

    @Override
    public void onStart() {

        initViews();

        super.onStart();

    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_left: {
                mainActivity.ui.openLeftRepetitionComponent();
                break;
            }
            default: {

            }
        }
    }

    private void initViews() {
        mainLayout = (LinearLayout) container.findViewById(R.id.main_right_layout);
        for (OnMainLayoutInited onMainLayoutInited : onMainLayoutIniteds) {
            onMainLayoutInited.onInited(mainLayout);
        }
        LeftButton = (ImageButton) container.findViewById(R.id.btn_left);
        LeftButton.setOnClickListener(this);
    }

    public LinearLayout getLayout() {
        return mainLayout;
    }

    public void addToMainLayout(final View v) {
        if (mainLayout == null) {
            addOnMainLayoutIniteds(new OnMainLayoutInited() {
                @Override
                public void onInited(LinearLayout mainLyout) {
                    if (mainLyout.indexOfChild(v) == -1) {
                        mainLyout.addView(v);
                    }
                }
            });
        } else {
            mainLayout.addView(v);
        }

    }

    public void removeFromMainLayout(final View v) {
        if (mainLayout == null) {
            addOnMainLayoutIniteds(new OnMainLayoutInited() {
                @Override
                public void onInited(LinearLayout mainLyout) {
                    if (mainLyout.indexOfChild(v) != -1) {
                        mainLyout.removeView(v);
                    }
                }
            });
        } else {
            mainLayout.removeView(v);
        }

    }

    public void addOnMainLayoutIniteds(
            OnMainLayoutInited onMainLayoutInited) {
        this.onMainLayoutIniteds.add(onMainLayoutInited);
    }

    interface OnMainLayoutInited {

        void onInited(LinearLayout mainLyout);
    }

}
