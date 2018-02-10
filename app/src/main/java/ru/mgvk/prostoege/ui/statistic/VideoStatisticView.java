package ru.mgvk.prostoege.ui.statistic;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import ru.mgvk.prostoege.Task;
import ru.mgvk.prostoege.ui.ProcessDiagram;
import ru.mgvk.prostoege.ui.UI;

public class VideoStatisticView extends FrameLayout
        implements Task.OnFirstTimeExerciseIncrementListener {
    private ProcessDiagram taskCompleteDiagram, firstExercisesDiagram, videoUnderstandDiagram;
    private Task task;
    private int  h;

    public VideoStatisticView(Context context) {
        super(context);
        initViews();
    }

    public VideoStatisticView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public VideoStatisticView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    public VideoStatisticView(Context context, AttributeSet attrs, int defStyleAttr,
                              int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews();
    }


    private void initViews() {

        h = UI.calcSize(82);
        setVideoUnderstandDiagram();
        setFirstTimeExercisesDiagram();
        setTaskCompleteDiagram();
    }


    public void setTask(Task task) {

        this.task = task;

        task.setOnFirstTimeExerciseIncrementListener(this);
        firstExercisesDiagram.setMax(task.getExercisesList().size());
        firstExercisesDiagram.setProgress(task.getFirstTimeExercises());
        taskCompleteDiagram.setMax(task.getMinExercises());
        taskCompleteDiagram.setProgress(task.getCompleteExercises());
        videoUnderstandDiagram.setMax(task.getVideoData().length);
        videoUnderstandDiagram.setProgress(0);

    }

    private void setTaskCompleteDiagram() {
        taskCompleteDiagram = new ProcessDiagram(getContext());
        LayoutParams lp = new LayoutParams(h, h);
        lp.gravity = Gravity.RIGHT | Gravity.TOP;
        taskCompleteDiagram.setLayoutParams(lp);
        taskCompleteDiagram.setTextType(ProcessDiagram.PERCENT);
        addView(taskCompleteDiagram);
//        taskCompleteDiagram.setProgress();
    }

    private void setFirstTimeExercisesDiagram() {
        firstExercisesDiagram = new ProcessDiagram(getContext());
        LayoutParams lp = new LayoutParams(h, h);
        lp.gravity = Gravity.CENTER | Gravity.TOP;
        firstExercisesDiagram.setLayoutParams(lp);
        firstExercisesDiagram.setTextType(ProcessDiagram.SLASH);
        addView(firstExercisesDiagram);
    }

    private void setVideoUnderstandDiagram() {
        videoUnderstandDiagram = new ProcessDiagram(getContext());
        LayoutParams lp = new LayoutParams(h, h);
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        videoUnderstandDiagram.setLayoutParams(lp);
        videoUnderstandDiagram.setTextType(ProcessDiagram.SLASH);
        addView(videoUnderstandDiagram);
    }

    @Override
    public void onIncrement(int currentValue) {
        firstExercisesDiagram.setProgress(currentValue);
        taskCompleteDiagram.setProgress(currentValue);
    }
}
