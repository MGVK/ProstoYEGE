package ru.mgvk.prostoege.ru.mgvk.prostoege.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import java.util.ArrayList;

import ru.mgvk.prostoege.DataLoader;
import ru.mgvk.prostoege.InstanceController;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;
import ru.mgvk.prostoege.Task;
import ru.mgvk.prostoege.ui.UI;
import ru.mgvk.prostoege.ui.VerticalScrollView;

/**
 * Created by mihail on 09.08.16.
 */
public class TaskListFragment extends Fragment implements View.OnClickListener {

    public VerticalScrollView taskScroll;
    LinearLayout taskListLayout, mainTaskListLayout;
    MainActivity mainActivity;
    Context context;
    Task currentTask;
    ImageButton menuButton,forwardButton;
    TextView balanceView;
    ArrayList<Task> taskList = new ArrayList<>();
    ImageView rings;

    @SuppressLint("ValidFragment")
    public TaskListFragment() {

    }

    @SuppressLint("ValidFragment")
    public TaskListFragment(Context context) {
        mainActivity = (MainActivity) (this.context = context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tasklist, container, false);
        mainActivity = (MainActivity) (this.context = inflater.getContext());

        Log.d("time_onCreate", System.currentTimeMillis() - mainActivity.TIME + "");

        return rootView;

    }

    void initViews() {
        taskListLayout = (LinearLayout) mainActivity.findViewById(R.id.layout_tasklist);
        taskScroll = (VerticalScrollView) mainActivity.findViewById(R.id.task_scroll);
        (menuButton = (ImageButton) mainActivity.findViewById(R.id.btn_menu)).setOnClickListener(this);
        (forwardButton = (ImageButton) mainActivity.findViewById(R.id.btn_forward_task)).setOnClickListener(this);
        (balanceView = (TextView) mainActivity.findViewById(R.id.btn_coins)).setOnClickListener(this);
        rings = (ImageView) mainActivity.findViewById(R.id.rings);
        mainTaskListLayout = (LinearLayout) mainActivity.findViewById(R.id.main_tasklist_layout);

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitMode();
        }

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
        if (mainTaskListLayout != null) {
            FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) mainTaskListLayout.getLayoutParams());
            lp.rightMargin = 0;
            mainTaskListLayout.setLayoutParams(lp);
        }


    }

    public void setLandscapeMode() {
        if (rings != null) {
            rings.setVisibility(View.VISIBLE);
        }
        if (mainTaskListLayout != null) {
            FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) mainTaskListLayout.getLayoutParams());
            lp.rightMargin = UI.calcSize(32);
            mainTaskListLayout.setLayoutParams(lp);
        }

    }


    @Override
    public void onStart() {
        super.onStart();

        taskList = (ArrayList<Task>) InstanceController.getObject("TaskList");

//        Log.d("time_onStart", System.currentTimeMillis() - mainActivity.TIME + "");


        if (taskList == null) {
            taskList = DataLoader.__loadTasks(context);
        }


        initViews();
//        Log.d("time_initViews", System.currentTimeMillis() - mainActivity.TIME + "");

        showTasks();

//        Log.d("time_showT", System.currentTimeMillis() - mainActivity.TIME + "");

        updateCoins();

//        Log.d("time_updateCoins", System.currentTimeMillis() - mainActivity.TIME + "");

        chooseTask(0);

//        Log.d("time_endOnStart", System.currentTimeMillis() - mainActivity.TIME + "");

//        mainActivity.closeLoadScreen();

    }


    public void restoreTasks() {
        for (Task task : taskList) {
            task.restore();
        }
    }

    public void updateCoins() {
        balanceView.setText(String.valueOf(mainActivity.profile.Coins));
    }

    void showTasks() {
        try {
            taskListLayout.removeAllViews();
        } catch (NullPointerException|IndexOutOfBoundsException e) {
            //do nothing
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Task task : taskList) {
            Space sp =new Space(context);
            sp.setLayoutParams(new LinearLayout.LayoutParams(-1,UI.calcSize(4)));
            taskListLayout.addView(sp);
            taskListLayout.addView(task);
        }

    }

    public void chooseTask(int id) {
        try{
            if (id < taskList.size() && id >= 0) {
                if (currentTask != null) {
                    currentTask.setChoosed(false);
                }
                ((MainActivity) context).ui.setCurrentTask(taskList.get(id));
//            currentTask = taskList.get(id);
                (currentTask = taskList.get(id)).setChoosed(true);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }




    public Task getCurrentTask() {
        return currentTask;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ActivityState_Tasks", "onDestroy");

        taskListLayout.removeAllViews();
        taskList.clear();
        currentTask = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d("ActivityState_Tasks", "onSaveInstanceState_1");

        try {
            InstanceController.putObject("TaskList", taskList);
        } catch (InstanceController.NotInitializedError notInitializedError) {
            notInitializedError.printStackTrace();
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        Log.d("ActivityState_Tasks", "onViewStateRestored");

        if (InstanceController.getObject("TaskList") != null) {
            taskList = (ArrayList<Task>) InstanceController.getObject("TaskList");
        }

//        showTasks();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_menu: {
                mainActivity.ui.openMenu(mainActivity.ui.mainMenu);
                break;
            }
            case R.id.btn_forward_task: {
//                mainActivity.ui.openTaskOrVideoFragment(false);
                mainActivity.ui.openVideoListFragment(currentTask);
                break;
            }
            case R.id.btn_coins:{
                mainActivity.ui.openBalanceDialog();
            }
        }

    }


    @Override
    public void onPause() {
        super.onPause();

        Log.d("ActivityState_Tasks","onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        if(InstanceController.getObject("TaskList") !=null) {
            taskList = (ArrayList<Task>) InstanceController.getObject("TaskList");
        }
        Log.d("ActivityState_Tasks", "onResume");

    }


    @Override
    public void onStop() {
        super.onStop();

        Log.d("ActivityState_Tasks", "onStop");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d("ActivityState_Tasks", "onLowMemory");
//        onDestroy();
    }


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d("ActivityState_Tasks", "onTrimMemory");
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("ActivityState_Tasks", "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("ActivityState_Tasks", "onDetach");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("ActivityState_Tasks", "onDestroyView");

    }
}
