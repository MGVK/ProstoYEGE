package ru.mgvk.util;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mike on 14.08.17.
 */
public class RepetitionTimer extends TimerTask {

    Timer currentTimer;
    ArrayList<OnTimerTicking> tickings = new ArrayList<>();
    long                      period   = 1000;  // 1s
    long                      dealy    = 1000;   // 1s
    long pastTime;
    long startTime;
    long duration;
    private boolean finished = false;

    /**
     * @param duration - Duration of timer in seconds
     */
    public RepetitionTimer(long duration) {
        this.duration = duration;
        startTime = System.currentTimeMillis();
    }

    public boolean isFinished() {
        return finished;
    }

    public void start() {
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


        //TICK MOTHER FUCKER!!!!!
        pastTime++;


        //disable timer
        if (pastTime >= duration || isFinished()) {
            currentTimer.cancel();

            for (OnTimerTicking ticking : tickings) {
                ticking.onFinish();
            }

        }

        for (OnTimerTicking ticking : tickings) {

            ticking.onClockwiseTick(pastTime);
            ticking.onAnticlockwiseTick(duration - pastTime);

        }
    }


    public void removeOnTimerTicking(
            OnTimerTicking currentTicking) {
        tickings.remove(currentTicking);
    }


    public void stop() {
        this.finished = true;
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
