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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;
import ru.mgvk.prostoege.ui.ExerciseWindow;
import ru.mgvk.prostoege.ui.TimeButton;
import ru.mgvk.prostoege.ui.UI;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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

        openStartDialog();

        super.onStart();

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
    }

    private void initTimer() {
        repetitionTimer = new RepetitionTimer(repetitionDuration);
        repetitionTimer.addOnTimerTicking(new RepetitionTimer.OnTimerTicking() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

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
        mainLayout.addView(titleLayout);
    }

    private void setTaskDescription() {

        ExerciseWindow.DescriptionWebView description = new ExerciseWindow.DescriptionWebView
                (context);
        description.loadUrl("yandex.ru");
        description.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

        mainLayout.addView(description);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_time: {
                break;
            }
            case R.id.btn_right: {

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


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            switchMode(true);
        } else {
            switchMode(false);
        }

        super.onConfigurationChanged(newConfig);
    }

    private void switchMode(boolean portrait) {
        if (portrait) {

        } else {

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

    private static class TitleLayout extends LinearLayout implements View.OnClickListener {


        private Context           context;
        private TextView          textView;
        private RepetitionControl repetitionControl;


        public TitleLayout(Context context) {
            super(context);
            this.context = context;
            setOrientation(HORIZONTAL);
            setLayoutParams(new LayoutParams(-1, UI.calcSize(100)));
            setTitleTextView();
            setButtons();
        }

        private void setTitleTextView() {
            textView = new TextView(context);
            textView.setTextSize(20);
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(new LayoutParams(-1, -1, 3));
        }

        private void setButtons() {
            Button prevBtn   = new Button(context);
            Button finishBtn = new Button(context);
            Button nextBtn   = new Button(context);

            prevBtn.setLayoutParams(new LayoutParams(-1, -1, 1));
            finishBtn.setLayoutParams(new LayoutParams(-1, -1, 1));
            nextBtn.setLayoutParams(new LayoutParams(-1, -1, 1));

            prevBtn.setOnClickListener(this);
            finishBtn.setOnClickListener(this);
            nextBtn.setOnClickListener(this);

            prevBtn.setTag(0);
            finishBtn.setTag(1);
            nextBtn.setTag(2);

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

                }
                case 1: {

                }
                case 2: {

                }
            }


        }

        public static interface RepetitionControl {

            void onNextTask();

            void onPrevTask();

            void onFinish();

        }
    }


    public static class RepetitionTimer extends TimerTask {

        Timer currentTimer;
        //        OnTimerTicking onTimerTicking;
        ArrayList<OnTimerTicking> tickings = new ArrayList<>();
        long                      period   = 1000;  // 1s
        long                      dealy    = 1000;   // 1s
        long pastTime;
        long startTime;
        long duration;


        /**
         * @param duration - Duration of timer in seconds
         */
        public RepetitionTimer(long duration) {
            this.duration = duration * 1000;
            startTime = System.currentTimeMillis();
        }

        void start() {
            (currentTimer = new Timer()).schedule(this, dealy, period);
            for (OnTimerTicking ticking : tickings) {
                ticking.onStart();
            }
        }

        public void addOnTimerTicking(
                OnTimerTicking onTimerTicking) {
            this.tickings.add(onTimerTicking);
        }

        @Override
        public void run() {
            //disable timer
            if (pastTime >= duration) {
                currentTimer.cancel();

                for (OnTimerTicking ticking : tickings) {
                    ticking.onFinish();
                }

            }

            for (OnTimerTicking ticking : tickings) {

                ticking.onClockwiseTick(pastTime / 1000);
                ticking.onAnticlockwiseTick((duration - pastTime) / 1000);

            }

        }

        public void removeOnTimerTicking(
                OnTimerTicking currentTicking) {
            tickings.remove(currentTicking);
        }


        public interface OnTimerTicking {

            void onStart();

            void onFinish();

            /**
             * @param pastTime time from the start in seconds
             */
            void onClockwiseTick(long pastTime);

            /**
             * @param remainingTime time to the finish in seconds
             */
            void onAnticlockwiseTick(long remainingTime);


        }
    }

}
