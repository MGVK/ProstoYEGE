package ru.mgvk.prostoege.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;
import ru.mgvk.util.RepetitionTimer;

/**
 * Created by mike on 10.08.17.
 */
public class TimeButton extends AppCompatTextView implements View.OnClickListener {


    public static final int TIME_ALL       = 0;
    public static final int TIME_TO_FINISH = 1;
    public static final int EMPTY          = 2;
    RepetitionTimer currentTimer;
    private int                            currentState = 0;
    private OnClickListener                externalListener;
    private RepetitionTimer.OnTimerTicking currentTicking;

    public TimeButton(Context context) {
        super(context);
        init();
    }

    public TimeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        super.setOnClickListener(this);
    }

    public RepetitionTimer getTimer() {
        return currentTimer;
    }

    public void setTimer(
            RepetitionTimer currentTimer) {
        this.currentTimer = currentTimer;
        disableTicking();
        if (currentTimer != null) {
            showTimeToFinish();
        }
    }

    int incrementState() {
        return currentState < EMPTY ?
                ++currentState : 0;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        externalListener = l;
    }

    public void changeState(int newState) {
        if (currentTimer != null) {
            disableTicking();

            switch (newState) {
                case TIME_ALL: {
                    showPastTime();
                    break;
                }
                case TIME_TO_FINISH: {
                    showTimeToFinish();
                    break;
                }
                case EMPTY: {
                    showEmptyButton();
                    break;
                }
            }
            currentState = newState;
        }
    }

    private void showEmptyButton() {
        setText("^_^");
    }

    private void disableTicking() {

        try {
            currentTimer.removeOnTimerTicking(currentTicking);
        } catch (Exception ignored) {
        }

    }

    private void showTimeToFinish() {
        Toast.makeText(getContext(), "TTL", Toast.LENGTH_SHORT).show();
        final long remainingTime = currentTimer.getRemaningTime();
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                setText(""
                        + remainingTime / 3600
                        + ":"
                        + (remainingTime % 3600) / 60
                        + ":"
                        + remainingTime % 60);
            }
        });
        currentTimer.addOnTimerTicking(currentTicking = new RepetitionTimer
                .OnTimerTicking() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onClockwiseTick(final long pastTime) {

            }

            @Override
            public void onAnticlockwiseTick(long remainingTime) {
                final String time = ""
                                    + remainingTime / 3600
                                    + ":"
                                    + (remainingTime % 3600) / 60
                                    + ":"
                                    + remainingTime % 60;

                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setText(time);
                    }
                });
            }
        });
    }

    private void showPastTime() {
        Toast.makeText(getContext(), "PastTime", Toast.LENGTH_SHORT).show();
        final long pastTime = currentTimer.getPastTime();
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                setText(""
                        + pastTime / 3600
                        + ":"
                        + (pastTime % 3600) / 60
                        + ":"
                        + pastTime % 60);
            }
        });
        currentTimer.addOnTimerTicking(currentTicking = new RepetitionTimer
                .OnTimerTicking() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onClockwiseTick(final long pastTime) {
                final String time = ""
                                    + pastTime / 3600
                                    + ":"
                                    + (pastTime % 3600) / 60
                                    + ":"
                                    + pastTime % 60;
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setText(time);
                    }
                });
            }

            @Override
            public void onAnticlockwiseTick(long remainingTime) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        changeState(incrementState());
        if (externalListener != null) {
            externalListener.onClick(v);
        }
    }

}