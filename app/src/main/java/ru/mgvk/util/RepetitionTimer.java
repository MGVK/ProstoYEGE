package ru.mgvk.util;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mike on 14.08.17.
 */
public class RepetitionTimer extends TimerTask {

    private Timer currentTimer;
    private ArrayList<OnTimerTicking> tickings = new ArrayList<>();
    private long                      period   = 1000;  // 1s
    private long                      dealy    = 1000;   // 1s
    private long pastTime;
    private long startTime;
    private long duration;
    private boolean started  = false;
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
        setStarted(true);
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

        if (pastTime >= duration) {
            stop();
        }


        if (isFinished()) {

            for (OnTimerTicking ticking : tickings) {
                ticking.onFinish();
            }

            cancel();

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


    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void stop() {
        if (isStarted()) {
            this.finished = true;
        }
    }

    @Override
    public boolean cancel() {
        currentTimer.cancel();
        currentTimer.purge();
        return super.cancel();
    }

    public String getTime() {
        return pastTime / 60 + ":" + pastTime % 60;
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
