package ru.mgvk.prostoege.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;
import ru.mgvk.prostoege.ui.ExerciseWindow;

/**
 * Created by mike on 28.07.17.
 */
public class RepetitionFragmentRight extends Fragment {


    private Context      context;
    private MainActivity mainActivity;
    private ViewGroup    container;

    private ExerciseWindow.NumPad       numPad;
    private ExerciseWindow.AnswerLayout answerLayout;
    private LinearLayout                mainLayout;

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

    private void initViews() {
        mainLayout = (LinearLayout) container.findViewById(R.id.main_right_layout);
    }

    public LinearLayout getLayout() {
        return mainLayout;
    }


}
