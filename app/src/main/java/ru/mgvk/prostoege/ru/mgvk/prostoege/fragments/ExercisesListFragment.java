package ru.mgvk.prostoege.ru.mgvk.prostoege.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import ru.mgvk.prostoege.InstanceController;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;
import ru.mgvk.prostoege.Task;
import ru.mgvk.prostoege.ui.ExerciseWindow;
import ru.mgvk.prostoege.ui.UI;

/**
 * Create by  mihail on 18.08.16.
 */
public class ExercisesListFragment extends Fragment implements View.OnClickListener {


    private MainActivity mainActivity;
    private Context context;
    private int taskId = 0;
    private ImageButton backButton,homeButton;
    private TextView videosButton, titleTextView,exercisesButton;
    private View container;
    private Task currentTask;
    private LinearLayout excercisesListLayout;
    private FrameLayout exerciseWindowLayout;
    private ImageView rings;
    private LinearLayout mainExercisesListLayout;
    private ExerciseWindow exerciseWindow;
    private ScrollView exercisesListScroll;

    @SuppressLint("ValidFragment")
    public ExercisesListFragment() {

    }

    @SuppressLint("ValidFragment")
    public ExercisesListFragment(Context context) {
        mainActivity = (MainActivity) (this.context = context);
        exerciseWindow = new ExerciseWindow(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_exerciseslist, container, false);
        this.container = container;
        mainActivity = (MainActivity) (this.context = inflater.getContext());
        return rootView;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
        if (titleTextView != null) {
            titleTextView.setText(context.getString(R.string.exercises_list_title)
                    + " " + currentTask.getNumber());
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        initViews();
        loadExcercises();
        setCurrentTask(currentTask);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        try {
            if (!hidden) {
                excercisesListLayout.removeAllViews();
                loadExcercises();
            }
        } catch (NullPointerException e) {
        }
    }

    void loadExcercises() {
        try {
            excercisesListLayout.removeAllViews();
        } catch (NullPointerException ignored) {
        }

        for (Task.Exercise exercise : getTask().getExercisesList()) {
            excercisesListLayout.addView(exercise);
        }
    }

    void initViews() {

        exerciseWindowLayout = (FrameLayout) container.findViewById(R.id.exercise_frame_layout);
        if (exerciseWindow.getParent() != null) {
            ((ViewGroup) exerciseWindow.getParent()).removeView(exerciseWindow);
        }
        exerciseWindowLayout.addView(exerciseWindow);

        mainExercisesListLayout = (LinearLayout) container.findViewById(R.id.main_exercises_layout);
        rings = (ImageView) container.findViewById(R.id.rings_ex);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitMode();
        }

        (videosButton = (TextView) container.findViewById(R.id.btn_videos))
                .setOnClickListener(this);

        (homeButton = (ImageButton) container.findViewById(R.id.btn_home))
                .setOnClickListener(this);
        container.findViewById(R.id.btn_forward).setOnClickListener(this);

        excercisesListLayout = (LinearLayout) container.findViewById(R.id.layout_exerciseslist);
        exercisesListScroll = (ScrollView) container.findViewById(R.id.exerciselist_scroll);

        (titleTextView = (TextView) container.findViewById(R.id.exerciselist_title))
                .setOnClickListener(this);
        (exercisesButton = (TextView) container.findViewById(R.id.btn_exercises))
                .setOnClickListener(this);
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

    public void scrollListUp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        exercisesListScroll.fullScroll(View.FOCUS_UP);
                    }
                });


            }
        }).start();
    }

    public void setPortraitMode() {
        if (rings != null) {
            rings.setVisibility(View.GONE);
        }
        if (mainExercisesListLayout != null) {
            FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams)
                    mainExercisesListLayout.getLayoutParams());
            lp.rightMargin = 0;
            mainExercisesListLayout.setLayoutParams(lp);
        }

    }

    public void setLandscapeMode() {
        if (rings != null) {
            rings.setVisibility(View.VISIBLE);
        }
        if (mainExercisesListLayout != null) {
            FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams)
                    mainExercisesListLayout.getLayoutParams());
            lp.rightMargin = UI.calcSize(32);
            mainExercisesListLayout.setLayoutParams(lp);
        }

    }


    public Task getTask() {
        return currentTask;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_videos: {
//                mainActivity.ui.openVideoListFragment(getTask());
                if(exerciseWindow.isOpened()){
                    mainActivity.onBackPressed();
                }
                mainActivity.onBackPressed();
                break;
            }
            case R.id.btn_forward: {
                mainActivity.ui.openToolsFragment();
                break;
            }
            case R.id.btn_home:{
                mainActivity.ui.openTaskOrVideoFragment(true);
                mainActivity.clearBackStack();
                exerciseWindow.closeExercise();
                break;
            }
            case R.id.exerciselist_title:{
                if(exerciseWindow.isOpened()) {
                    mainActivity.onBackPressed();
                }
                break;
            }
            case R.id.btn_exercises:{
                if(exerciseWindow.isOpened()) {
                    mainActivity.onBackPressed();
                }
                break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        Log.d("inst","save");
        Log.d("ActivityState_Tasks", "onSaveInstanceState_1");

        try {
            InstanceController.putObject("Task", currentTask);
        } catch (InstanceController.NotInitializedError notInitializedError) {
            notInitializedError.printStackTrace();
        }

    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d("ActivityState_Tasks", "onViewStateRestored");

        if ((Task) InstanceController.getObject("Task") != null) {
            currentTask = (Task) InstanceController.getObject("Task");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        excercisesListLayout.removeAllViews();
        currentTask = null;
    }

    public ExerciseWindow getExerciseWindow() {
        return exerciseWindow;
    }


    @Override
    public void onPause() {
        super.onPause();

        Log.d("ActivityState_Exercises", "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("ActivityState_Exercises", "onResume");

    }


    @Override
    public void onStop() {
        super.onStop();

        Log.d("ActivityState_Exercises", "onStop");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d("ActivityState_Exercises", "onLowMemory");
//        onDestroy();
    }


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d("ActivityState_Exercises", "onTrimMemory");
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("ActivityState_Exercises", "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("ActivityState_Exercises", "onDetach");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("ActivityState_Exercises", "onDestroyView");

    }


}
