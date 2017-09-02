package ru.mgvk.prostoege.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.util.RepetitionTimer;

/**
 * Created by mike on 10.08.17.
 */
public class TimeButton extends TextView implements View.OnClickListener {


    public static final int TIME_ALL       = 0;
    public static final int TIME_TO_FINISH = 1;
    public static final int EMPTY          = 2;
    RepetitionTimer currentTimer;
    private int currentState = 0;
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TimeButton(Context context, AttributeSet attrs, int defStyleAttr,
                      int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
                final String time = "осталось: "
                                    + remainingTime / 3600
                                    + ":"
                                    + (remainingTime % 3600) / 60
                                    + ":"
                                    + remainingTime % 60;

                ((MainActivity) getContext()).runOnUiThread(new Runnable() {
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
                final String time = "прошло: "
                                    + pastTime / 3600
                                    + ":"
                                    + (pastTime % 3600) / 60
                                    + ":"
                                    + pastTime % 60;
                ((MainActivity) getContext()).runOnUiThread(new Runnable() {
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