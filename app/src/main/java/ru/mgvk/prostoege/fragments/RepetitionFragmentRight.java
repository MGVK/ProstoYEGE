package ru.mgvk.prostoege.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;

/**
 * Created by mike on 28.07.17.
 */
public class RepetitionFragmentRight extends Fragment {


    private Context      context;
    private MainActivity mainActivity;
    private ViewGroup    container;

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


    }
}
