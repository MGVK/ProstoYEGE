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

import ru.mgvk.prostoege.DataLoader;
import ru.mgvk.prostoege.InstanceController;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;
import ru.mgvk.prostoege.Task;
import ru.mgvk.prostoege.ui.MainScrollView;
import ru.mgvk.prostoege.ui.UI;
import ru.mgvk.prostoege.ui.VideoPlayer;

/**
 * Created by mihail on 13.08.16.
 */
public class VideoListFragment extends Fragment implements View.OnClickListener, VideoPlayer.OnVideoStateChangeListener, MainScrollView.OnScreenSwitchedListener {

    private MainActivity mainActivity;
    private Context context;
    private LinearLayout videoLayout;
    private int taskId = 0;
    private ImageButton backButton;
    private TextView tasksButton, titleText, descriptionText;
    private ViewGroup container;
    private Task currentTask;
    private ScrollView videoScroll;
    private ImageView rings;
    private LinearLayout mainVideoListLayout;
    private boolean isAnyVideoPlaying = false;


    @SuppressLint("ValidFragment")
    public VideoListFragment() {

    }

    @SuppressLint("ValidFragment")
    public VideoListFragment(Context context) {
        mainActivity = (MainActivity) (this.context = context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_videolist, container, false);
        this.container = container;
        mainActivity = (MainActivity) (this.context = inflater.getContext());
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();


        initViews();
        loadVideos();
        updateSizes();
    }

    public void setPortraitMode() {
        if (rings != null) {
            rings.setVisibility(View.GONE);
        }
        if (mainVideoListLayout != null) {
            FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) mainVideoListLayout.getLayoutParams());
            lp.leftMargin = 0;
            mainVideoListLayout.setLayoutParams(lp);
        }


    }

    public void setLandscapeMode() {
        if (rings != null) {
            rings.setVisibility(View.VISIBLE);
        }
        if (mainVideoListLayout != null) {
            FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) mainVideoListLayout.getLayoutParams());
            lp.leftMargin = UI.calcSize(32);
            mainVideoListLayout.setLayoutParams(lp);
        }

    }


    public void setScrollViewEnabled(boolean enabled) {
        if (videoScroll != null) {
            videoScroll.setEnabled(enabled);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        try {
            if (!hidden) {
                videoLayout.removeAllViews();
                loadVideos();
            }
        } catch (Exception ignored) {
        }
    }

    void initViews() {
        mainVideoListLayout = (LinearLayout) container.findViewById(R.id.main_videolist_layout);
        rings = (ImageView) container.findViewById(R.id.rings_video);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitMode();
        }
        videoLayout = (LinearLayout) container.findViewById(R.id.layout_videolist);
        (backButton = (ImageButton) container.findViewById(R.id.btn_back))
                .setOnClickListener(this);
        (tasksButton = (TextView) container.findViewById(R.id.btn_exercises))
                .setOnClickListener(this);
        videoScroll = (ScrollView) container.findViewById(R.id.video_scroll);

        titleText = (TextView) container.findViewById(R.id.videolist_title);
        try {
            titleText.setTypeface(DataLoader.getFont(context, "comic"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        descriptionText = (TextView) container.findViewById(R.id.videolist_title_description);

        try {
            descriptionText.setTypeface(DataLoader.getFont(context, "comic"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mainActivity.ui.mainScroll.addOnScreenSwitchedListener(this);

        Log.d("init", "initviews");
    }

    public void stopVideos() {
        for (Task.Video video : this.currentTask.getVideoList()) {
            video.stop();
        }
    }


    public void setCurrentTask(Task currentTask) {


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
            videoScroll.fullScroll(View.FOCUS_UP);
        } catch (Exception ignored) {
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

                if (videoLayout != null) {

                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (Task.Video video : currentTask.getVideoList()) {
                                video.updateSizes(videoLayout.getWidth(), 0);
                            }

                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitMode();
        } else {
            setLandscapeMode();
        }

        final int oldW = videoLayout.getWidth();

        new Thread(new Runnable() {
            @Override
            public void run() {
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
                        if (isAnyVideoPlaying()) {
                            mainActivity.ui.mainScroll.switchRight();
                        }
                        try {
                            for (Task.Video video : currentTask.getVideoList()) {
                                video.updateSizes(videoLayout.getWidth(), 0);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                });

            }
        }).start();


    }

    public void loadVideos() {
        if (currentTask != null && videoLayout != null) {
            try {
                ((ViewGroup) currentTask.getVideoList().get(0).getParent()).removeAllViews();
            } catch (Exception ignored) {
            }
            for (Task.Video video : currentTask.getVideoList()) {
                videoLayout.addView(video);
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        videoLayout.removeAllViews();
        currentTask = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_exercises: {
                mainActivity.ui.openExercisesListFragment();
                break;
            }
            case R.id.btn_back: {
//                mainActivity.ui.openTaskListFragment();
                mainActivity.onBackPressed();
                break;
            }
        }


    }

    public Task getTask() {
        return currentTask;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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

        if (InstanceController.getObject("Task") != null) {
            currentTask = (Task) InstanceController.getObject("Task");
        }
    }

    @Override
    public void onPlay(VideoPlayer v) {
        isAnyVideoPlaying = v.isPlaying();
    }

    @Override
    public void onPause(VideoPlayer v) {
        try {
            stopVideos();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop(VideoPlayer v) {

    }

    @Override
    public void onFullScreen(VideoPlayer v) {

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
