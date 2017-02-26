package ru.mgvk.util;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by mike on 20.02.17.
 */
public class Stopwatch {

    private final static String TAG = "STOPWATCH";
    private final static String TAG_START = "[START]";
    private final static String TAG_FINISH = "[FINISH]";
    private final static String TAG_CHECKPOINT = "[CHKPOINT]";
    private ArrayList<String> list = new ArrayList<>();
    private long startTime = 0;
    private long lastPoint = 0;
    private long finishTime = 0;
    private boolean logging = false;

    public Stopwatch() {

    }

    public Stopwatch(boolean logging) {
        this.logging = logging;
    }

    public void start(String mess) {
        list.add(formatTime(TAG_START, lastPoint = startTime = System.currentTimeMillis(), mess));
        if (logging) log();
    }

    public void checkpoint(String mess) {
        long tmp = lastPoint;
        list.add(formatTime(TAG_CHECKPOINT, (lastPoint = System.currentTimeMillis()) - tmp, mess));
        if (logging) log();
    }

    public void finish(String mess) {
        checkpoint(mess);
        list.add(formatTime(TAG_FINISH, (finishTime = lastPoint) - startTime, mess));
        if (logging) log();
    }

    private void log() {
        Log.d(TAG, list.get(list.size() - 1));
    }

    private String formatTime(String tag, long time, String mess) {
        return tag + (tag.equals(TAG_CHECKPOINT) ? ": | +" : ": | ") + time + " | :" + mess;
    }


}
