package ru.mgvk.prostoege.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import ru.mgvk.prostoege.*;
import ru.mgvk.prostoege.ui.ExoPlayer;
import ru.mgvk.prostoege.ui.MainScrollView;
import ru.mgvk.prostoege.ui.UI;
import ru.mgvk.prostoege.ui.VideoLayout;
import ru.mgvk.prostoege.ui.statistic.VideoStatisticView;
import ru.mgvk.util.Reporter;
import ru.mgvk.util.StateTags;

/**
 * Created by mihail on 13.08.16.
 */
public class VideoListFragment extends Fragment implements View.OnClickListener,
        ExoPlayer.OnVideoStateChangeListener, MainScrollView.OnScreenSwitchedListener {

    private MainActivity mainActivity;
    private Context      context;
    private VideoLayout  videoLayout;
    private int taskId = 0;
    private ImageView backButton;
    private TextView  tasksButton, titleText, descriptionText;
    private ViewGroup    container;
    private Task         currentTask;
    private ScrollView   videoScrollView;
    //    private ImageView    rings;
    private LinearLayout mainVideoListLayout;
    private boolean isAnyVideoPlaying = false;
    private VideoStatisticView videoStatisticView;
    private LinearLayout       mainLayout;


    @SuppressLint("ValidFragment")
    public VideoListFragment() {

    }

    @SuppressLint("ValidFragment")
    public VideoListFragment(Context context) {
        mainActivity = (MainActivity) (this.context = context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_videolist, container, false);
        this.container = container;
        mainActivity = (MainActivity) (this.context = inflater.getContext());
        return rootView;

    }

    @Override
    public void onStart() {
        super.onStart();
        try {

            mainActivity.stopwatch.checkpoint("VideoListFragment_onStart");
            initViews();
            loadVideos();
            updateSizes();
        } catch (Exception e) {
            Reporter.report(context, e, mainActivity.reportSubject);
        }
    }

    public void setPortraitMode() {
//        if (mainVideoListLayout != null) {
//            LinearLayout.LayoutParams lp = ((LinearLayout.LayoutParams) mainVideoListLayout
//                    .getLayoutParams());
//            lp.leftMargin = 0;
//            mainVideoListLayout.setLayoutParams(lp);
//        }
        //stub!


    }

    public void setLandscapeMode() {

//        if (mainVideoListLayout != null) {
//            FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) mainVideoListLayout
//                    .getLayoutParams());
//            lp.leftMargin = UI.calcSize(32);
//            mainVideoListLayout.setLayoutParams(lp);
//        }
        //stub!
    }


    public void setScrollViewEnabled(boolean enabled) {
        //STUB!
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

//        try {
//            if (!hidden) {
//                videoLayout.removeAllViews();
//                loadVideos();
//            }
//        } catch (Exception ignored) {
//        }

    }

    private void initViews() {

        mainLayout = (LinearLayout) container.findViewById(R.id.layout);

        if (context.getResources().getConfiguration().orientation
            == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitMode();
        }

        videoScrollView = (ScrollView) container.findViewById(R.id.video_scroll);
//        videoLayout = (LinearLayout) container.findViewById(R.id.layout_videolist);

        videoStatisticView = (VideoStatisticView) container
                .findViewById(R.id.videostatistic);

//        container.findViewById(R.id.action_bar).setLayoutParams(new FrameLayout.LayoutParams(-1,
//                UI.calcSize(UI.getStatusBarHeight() + 55)));
        container.findViewById(R.id.action_bar).setPadding(
                0, UI.getStatusBarHeight(), 0, UI.calcSize(10));

        (backButton = (ImageView) container.findViewById(R.id.btn_back))
                .setOnClickListener(this);
        (tasksButton = (TextView) container.findViewById(R.id.btn_exercises))
                .setOnClickListener(this);

        if ((videoLayout = (VideoLayout) InstanceController.getObject("VideoLayout")) == null) {
            videoLayout = new VideoLayout(context);
        }

        int i = 0;
        if ((i = mainLayout.getChildCount()) != 0
            && mainLayout.getChildAt(i - 1) != videoLayout) {
            mainLayout.addView(videoLayout);
        }

        titleText = (TextView) container.findViewById(R.id.videolist_title);
        try {
            titleText.setTypeface(DataLoader.getFont(context, "comic"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        descriptionText = (TextView) container.findViewById(R.id.videolist_title_description);

        mainActivity.ui.mainScroll.addOnScreenSwitchedListener(this);

        Log.d("setTask", "initviews");
    }

    public void stopVideos() {
        try {
            if (videoLayout != null) {
                for (VideoLayout.VideoCard video : this.videoLayout.getCurrentVideosList()) {
                    video.stop();
                }
            }
        } catch (Exception e) {
            Reporter.report(context, e, ((MainActivity) context).reportSubject);
        }
    }

    void updateSizes() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {

                    if (videoLayout != null) {

                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                try {

                                    for (VideoLayout.VideoCard video : videoLayout
                                            .getCurrentVideosList()) {
                                        video.updateSizes(videoLayout.getWidth(), 0);
                                    }
                                } catch (Exception e) {
                                    Reporter.report(context, e, mainActivity.reportSubject);
                                }

                            }
                        });
                    }

                } catch (Exception e) {
                    Reporter.report(context, e, mainActivity.reportSubject);
                }
            }
        }).start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        try {

            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                setPortraitMode();
            } else {
                setLandscapeMode();
            }


            final int oldW = videoLayout.getWidth();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int count = 0;
                        while (videoLayout.getWidth() == oldW && count < 100) {
                            try {
                                count++;
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (isAnyVideoPlaying()) {
                                        mainActivity.ui.mainScroll.toRight();
                                    }
                                } catch (Exception e) {
                                    Reporter.report(context, e,
                                            ((MainActivity) context).reportSubject);
                                }
                                try {
                                    for (VideoLayout.VideoCard video : videoLayout
                                            .getCurrentVideosList()) {
                                        video.updateSizes(videoLayout.getWidth(), 0);
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        });
                    } catch (Exception e) {
                        Reporter.report(context, e, ((MainActivity) context).reportSubject);
                    }
                }
            }).start();

        } catch (Exception e) {
            Reporter.report(context, e, ((MainActivity) context).reportSubject);
        }

    }

    public void loadVideos() {

        if (currentTask != null && videoLayout != null) {
//                try {
//                    ((ViewGroup) currentTask.getVideoList().returnTo(0).getParent()).removeAllViews();
//                } catch (Exception ignored) {
//                }
//                for (VideoLayout.VideoCard video : currentTask.getVideoList()) {
//                    videoLayout.addView(video);
//                }
            videoLayout.openVideosFromTask(currentTask);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        videoLayout.removeAllViews();
        currentTask = null;
        Log.d("VideoListFragment", "onDestroy");
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btn_exercises: {
                    mainActivity.ui.openExercisesListFragment();
                    break;
                }
                case R.id.btn_back: {
//                mainActivity.ui.openTaskListFragment();
//                mainActivity.onBackPressed();
//                    mainActivity.ui.mainScroll.toLeft();
                    mainActivity.getBackStack().returnToState(
                            StateTags.TASK_LIST_FRAGMENT);
                    break;
                }
            }
        } catch (Exception e) {
            Reporter.report(context, e, ((MainActivity) context).reportSubject);
        }

    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task currentTask) {

        try {

            if (this.currentTask != null) {
                stopVideos();
            }

            this.taskId = currentTask.getId();
            this.currentTask = currentTask;
            if (titleText != null) {
                titleText.setText("Задание " + currentTask.getNumber());
            }
            if (descriptionText != null) {
                descriptionText.setText(currentTask.getDescription());
            }

            loadVideos();
            updateSizes();

            try {
                videoScrollView.fullScroll(View.FOCUS_UP);
            } catch (Exception ignored) {
            }

            videoStatisticView.setTask(getCurrentTask());

        } catch (Exception e) {
            Reporter.report(context, e, ((MainActivity) context).reportSubject);
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("ActivityState_Tasks", "onSaveInstanceState_1");
        try {
            InstanceController.putObject("Task", currentTask);
            InstanceController.putObject("VideoLayout", videoLayout);
        } catch (InstanceController.NotInitializedError notInitializedError) {
            notInitializedError.printStackTrace();
        }
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d("ActivityState_Tasks", "onViewStateRestored");

        if (InstanceController.getObject("Task") != null) {
            currentTask = (Task) InstanceController.getObject("Task");
        }
    }

    @Override
    public void onPlay(ExoPlayer v) {
        try {
            isAnyVideoPlaying = v.isPlaying();
        } catch (Exception e) {
            Reporter.report(context, e, mainActivity.reportSubject);
        }
    }

    @Override
    public void onPause(ExoPlayer v) {
        try {
            stopVideos();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isAnyVideoPlaying() {
        return isAnyVideoPlaying;
    }


    @Override
    public void onPause() {
        super.onPause();
        stopVideos();
        Log.d("ActivityState_Videos", "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("ActivityState_Videos", "onResume");

    }


    @Override
    public void onStop() {
        super.onStop();

        Log.d("ActivityState_Videos", "onStop");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d("ActivityState_Videos", "onLowMemory");
//        onDestroy();
    }


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d("ActivityState_Videos", "onTrimMemory");
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("ActivityState_Videos", "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("ActivityState_Videos", "onDetach");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("ActivityState_Videos", "onDestroyView");

    }


    @Override
    public void switchedRight() {

    }

    @Override
    public void switchedLeft() {
        stopVideos();
    }
}
