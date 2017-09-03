package ru.mgvk.prostoege.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ru.mgvk.prostoege.DataLoader;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;
import ru.mgvk.prostoege.ui.ExerciseWindow;
import ru.mgvk.prostoege.ui.TimeButton;
import ru.mgvk.prostoege.ui.UI;
import ru.mgvk.util.RepetitionTimer;
import ru.mgvk.util.Reporter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by mike on 28.07.17.
 */
public class RepetitionFragmentLeft extends Fragment implements View.OnClickListener {


    private Context                     context;
    private MainActivity                mainActivity;
    private TimeButton                  timeButton;
    private ViewGroup                   container;
    private ImageButton                 rightButton;
    private LinearLayout                mainLayout;
    private TitleLayout                 titleLayout;
    private ExerciseWindow.NumPad       numPad;
    private ExerciseWindow.AnswerLayout answerLayout;
    private RepetitionTimer             repetitionTimer;
    private boolean stopped            = false;
    // duration in  seconds
    private int     repetitionDuration = (3/*hour*/ * 60 + 55)/*minutes*/ * 60;
    private RepetitionData data;
    private Result         currentResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_repetition_left, container, false);
        mainActivity = (MainActivity) (this.context = inflater.getContext());
        this.container = container;

        return view;

    }

    @Override
    public void onStart() {


        if (!isStopped()) {

            initViews();

            initTimer();

        }
        setStopped(false);

        prepareData();

        openStartDialog();

        super.onStart();

    }

    private void prepareData() {
        if (data == null) {
            data = RepetitionData.fromFuckingJSON(context, DataLoader
                    .getRepetitionTasksJson());
        }


    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {        // start

            onStart();
            mainActivity.ui.mainScroll.setScrollEnabled(false);

        } else {              // stop

            finishRepetition();
            mainActivity.ui.mainScroll.setScrollEnabled(true);

        }
    }

    private void onRepetitionFinished() {
        Toast.makeText(context, getString(R.string.toast_repetition_finish), Toast.LENGTH_SHORT)
                .show();

        // TODO: 12.08.17 Выкидываем результат в статистику

        currentResult = new Result((int) (Math.random() * 100));

//        mainActivity.ui.taskListFragment.getMainStatistic().addResult(currentResult);

    }


    private void finishRepetition() {
        repetitionTimer.stop();
    }

    private void openStartDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Начать подготовку к ЕГЭ?")
                .setPositiveButton("Старт", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRepetition();
                    }
                })
                .setNegativeButton("Отмена", null)
                .create().show();
    }


    private void startRepetition() {
        repetitionTimer.start();
        timeButton.changeState(TimeButton.TIME_ALL);
    }

    private void initTimer() {
        repetitionTimer = new RepetitionTimer(repetitionDuration);
        repetitionTimer.addOnTimerTicking(new RepetitionTimer.OnTimerTicking() {
            @Override
            public void onStart() {
                onRepetitionStarted();
            }

            @Override
            public void onFinish() {
                onRepetitionFinished();
            }

            @Override
            public void onClockwiseTick(long pastTime) {

            }

            @Override
            public void onAnticlockwiseTick(long remainingTime) {

            }
        });

        timeButton.setTimer(repetitionTimer);


    }

    private void onRepetitionStarted() {

    }

    @Override
    public void onStop() {
        super.onStop();
        setStopped(true);
    }

    private boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    private void initViews() {
        timeButton = (TimeButton) container.findViewById(R.id.btn_time);
        timeButton.setOnClickListener(this);
        rightButton = (ImageButton) container.findViewById(R.id.btn_right);
        rightButton.setOnClickListener(this);
        mainLayout = (LinearLayout) container.findViewById(R.id.main_repetition_layout);

        if (context.getResources().getConfiguration().orientation
            == Configuration.ORIENTATION_PORTRAIT) {
            setPotraitMode();
        } else {
            setLanscapeMode();
        }

        setTitleLayout();
        setTaskDescription();
        setAnswerLayout();
        setNumPad();
    }

    private void setAnswerLayout() {
        answerLayout = new ExerciseWindow.AnswerLayout(context, this);
        answerLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

        mainLayout.addView(answerLayout);
    }

    private void setNumPad() {
        numPad = new ExerciseWindow.NumPad(context, this);

        mainLayout.addView(numPad);
    }

    private void setTitleLayout() {
        titleLayout = new TitleLayout(context);
        titleLayout.setRepetitionControl(new TitleLayout.RepetitionControl() {
            @Override
            public void onNextTask() {
                openNextTask();
            }

            @Override
            public void onPrevTask() {
                openPrevTask();
            }

            @Override
            public void onFinish() {
                finishRepetition();
            }
        });
        mainLayout.addView(titleLayout);
    }

    private void openPrevTask() {

    }

    private void openNextTask() {

    }

    private void setTaskDescription() {

        ExerciseWindow.DescriptionWebView description = new ExerciseWindow.DescriptionWebView
                (context);
        description
                .loadUrl("file://" + context.getApplicationContext().getFilesDir() + "/test.html");
//        description.loadUrl();
        description.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        description.setMinimumHeight(UI.calcSize(100));

        mainLayout.addView(description);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_time: {
                break;
            }
            case R.id.btn_right: {
                mainActivity.ui.openRightRepetitionComponent();
                break;
            }
            default: {
                if ((Integer) v.getTag() == 10) {
                    answerLayout.togglePositive_Negative();
                } else if ((Integer) v.getTag() == 12) {
                    answerLayout.setComma();
                } else {
                    answerLayout.getAnswerTextView().setText(
                            String.format("%s%s",
                                    answerLayout.getAnswerTextView().getText().toString()
                                            .replace("|",
                                                    ""),
                                    String.valueOf(v.getTag())));
                }
            }
        }
    }

    private void setLanscapeMode() {

        mainLayout.removeView(answerLayout);
        mainLayout.removeView(numPad);
        mainActivity.ui.mainScroll.setScrollEnabled(true);
        mainActivity.ui.repetitionFragmentRight.getLayout().addView(answerLayout);
        mainActivity.ui.repetitionFragmentRight.getLayout().addView(numPad);
    }

    private void setPotraitMode() {
        mainActivity.ui.repetitionFragmentRight.getLayout().removeView(answerLayout);
        mainActivity.ui.repetitionFragmentRight.getLayout().removeView(numPad);
        mainActivity.ui.mainScroll.setScrollEnabled(false);
        mainLayout.addView(answerLayout);
        mainLayout.addView(numPad);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPotraitMode();
        } else {
            setLanscapeMode();
        }

        super.onConfigurationChanged(newConfig);
    }

    private void switchMode(boolean portrait) {

        if (portrait) {

        } else {

        }

    }


    public static class RepetitionData {

        private static HashMap<Integer, RepetitionTask> map = new HashMap<>();

        static RepetitionData fromFuckingJSON(Context context, String json) {

            if (json.length() < 3) {
                return null;
            }
            RepetitionData repetitionData = new RepetitionData();

            json = json.substring(1, json.length() - 1);

            int i = 0, currentIndex = 0;

            try {

                for (String s : json.split(":")) {
                    for (String s1 : s.split(",")) {
                        if (s1.equals("null")) {
                            i = 0;
                        } else {
                            if (i == 0) {
                                //number
                                map.put(currentIndex = Integer
                                                .parseInt(s1.substring(1, s1.length() - 1)),
                                        new RepetitionFragmentLeft.RepetitionData.RepetitionTask());
                            }
                            if (i == 2) {
                                //ID
                                map.get(currentIndex).ID = Integer
                                        .parseInt(s1.substring(1, s1.length() - 1));
                            }
                            if (i == 4) {
                                //description
                                map.get(currentIndex).Description = s1
                                        .substring(1, s1.length() - 2);
                            }
                            i = increment(i);
                        }

                    }
                }
            } catch (Exception e) {
                Reporter.report(context, e, MainActivity.PID);
            }

            repetitionData.setMap(map);

            return repetitionData;
        }

        private static int increment(int i) {
            return i >= 4 ? 0 : ++i;
        }

        public void setMap(
                HashMap<Integer, RepetitionTask> map) {
            this.map = map;
        }

        public static class RepetitionTask {

            int    ID          = 0;
            String Description = "";

        }

    }

    private static class TitleLayout extends LinearLayout implements View.OnClickListener {

        private Context           context;
        private TextView          textView;
        private RepetitionControl repetitionControl;

        public TitleLayout(Context context) {
            super(context);
            this.context = context;
            setOrientation(HORIZONTAL);
            setLayoutParams(new LayoutParams(-1, UI.calcSize(65)));
            setTitleTextView();
            setButtons();

        }

        public void setRepetitionControl(
                RepetitionControl repetitionControl) {
            this.repetitionControl = repetitionControl;
        }

        private void setTitleTextView() {
            textView = new TextView(context);
            textView.setTextSize(20);
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(new LayoutParams(-1, -1, 3));
            textView.setText("Заданиe ");
            addView(textView);
        }

        private void setButtons() {

            Button prevBtn   = new Button(context);
            Button finishBtn = new Button(context);
            Button nextBtn   = new Button(context);

            prevBtn.setLayoutParams(new LayoutParams(-1, -1, 5));
            finishBtn.setLayoutParams(new LayoutParams(-1, -1, 5));
            nextBtn.setLayoutParams(new LayoutParams(-1, -1, 5));

            prevBtn.setOnClickListener(this);
            finishBtn.setOnClickListener(this);
            nextBtn.setOnClickListener(this);

            prevBtn.setTag(0);
            finishBtn.setTag(1);
            nextBtn.setTag(2);

            prevBtn.setBackgroundResource(R.drawable.repetition_left);
            finishBtn.setBackgroundResource(R.drawable.repetition_finish);
            nextBtn.setBackgroundResource(R.drawable.repetition_right);

            addView(prevBtn);
            addView(finishBtn);
            addView(nextBtn);

        }


        public void setTitle(String title) {
            if (textView != null) {
                textView.setText(title);
            }
        }

        @Override
        public void onClick(View v) {
            switch ((Integer) v.getTag()) {
                case 0: {
                    onPrevTask();
                }
                case 1: {
                    onFinish();
                }
                case 2: {
                    onNextTask();

                }
            }


        }

        private void onPrevTask() {
            if (repetitionControl != null) {
                repetitionControl.onPrevTask();
            }
        }

        private void onFinish() {
            if (repetitionControl != null) {
                repetitionControl.onFinish();
            }
        }

        private void onNextTask() {
            if (repetitionControl != null) {
                repetitionControl.onNextTask();
            }
        }

        public interface RepetitionControl {

            void onNextTask();

            void onPrevTask();

            void onFinish();

        }
    }

    public static class Result {

        private int scoreSecondary;
        private int scorePrimary;

        public Result(int scorePrimary) {
            this.scorePrimary = scorePrimary;
        }

        public Result(int scorePrimary, int scoreSecondary) {
            this.scoreSecondary = scoreSecondary;
            this.scorePrimary = scorePrimary;
        }

        public int getScoreSecondary() {
            return scoreSecondary;
        }

        public int getScorePrimary() {
            return scorePrimary;
        }
    }

    public static class RepetitionTask {

        String ID;
        String Description;
        String URL;
        private Context context;

        public RepetitionTask(Context context, String ID, String description) {
            this.ID = ID;
            Description = description;
            this.context = context;
        }

        public String getURL() {
            return URL;
        }

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public String getDescription() {
            return Description;
        }

        public void setDescription(String description) {
            Description = description;
            writeToFile();
            URL = "file://" + URL;
        }

        private void writeToFile() {
            try {
                FileWriter writer = new FileWriter(URL = (DataLoader.getRepetitionFolder(context) +
                                                          ID + ".html"));
                writer.write(Description);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
