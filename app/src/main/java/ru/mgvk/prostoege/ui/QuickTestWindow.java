package ru.mgvk.prostoege.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import ru.mgvk.prostoege.*;
import ru.mgvk.prostoege.ui.exercises.DescriptionWebView;

import java.util.ArrayList;
import java.util.HashMap;

public class QuickTestWindow extends Dialog
        implements View.OnClickListener {

    private static int tasksCount;
    private static HashMap<Integer, HTMLTask> map = new HashMap<>();
    private final Context context;
    int videoID = 0;

    private long testDuration = 0;
    private long startTime    = 0;

    private int                currentTaskID = 0;
    private ArrayList<Boolean> results       = new ArrayList<>();
    private DescriptionWebView   descriptionWebView;
    private OnTaskAnswerListener onTaskAnswerListener;

    public QuickTestWindow(Context context, int videoID) {
        super(context);
        this.context = context;
        this.videoID = videoID;
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_quick_test);
        descriptionWebView = (DescriptionWebView) findViewById(R.id.webview);
        descriptionWebView.init();
        findViewById(R.id.btn_no).setOnClickListener(this);
        findViewById(R.id.btn_no).setTag(false);
        findViewById(R.id.btn_yes).setOnClickListener(this);
        findViewById(R.id.btn_yes).setTag(true);

        new Loader().execute();
    }

    public static int getTasksCount() {
        return tasksCount;
    }

    public void setTasksCount(int tasksCount) {
        QuickTestWindow.tasksCount = tasksCount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onStart() {
        super.onStart();
        startTime = System.currentTimeMillis();
        openNextTask();
    }

    @Override
    public void onClick(View v) {
        try {
            onTaskAnswered((Boolean) v.getTag());
            results.add((Boolean) v.getTag());
        } catch (Exception ignored) {
        }
        openNextTask();
    }

    private void openNextTask() {
        if (currentTaskID < getTasksCount()) {
            descriptionWebView.loadHTMLFile(DataLoader.getQuickTestFolder(context)
                                            + map.get(currentTaskID++).ID + ".html");
        } else {
            finishTest();
        }
    }

    public void finishTest() {
        onAllTaskFinished(results);
    }


    private long calcDuration() {

        return testDuration = (System.currentTimeMillis() - startTime) / 1000;

    }

    public void onTaskAnswered(boolean answer) {
        if (onTaskAnswerListener != null && onTaskAnswerListener.onTaskAnswered(answer)) {
            return;
        }
    }

    public void onAllTaskFinished(ArrayList<Boolean> results) {

        if (onTaskAnswerListener != null && onTaskAnswerListener.onAllTaskFinished(results)) {
            return;
        }

        final ProgressDialog dialog = UI.openProgressDialog(context, "Проверка заданий");

        String testResult = DataLoader.getQuickTestResults(
                videoID,
                prepareResults(results),
                calcDuration());

        dialog.dismiss();

        UI.openQuickTestResultDialog(context, testResult, getTasksCount(), testDuration);

        dismiss();
    }

    private String prepareResults(ArrayList<Boolean> results) {

        StringBuilder res = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            res.append(map.get(i).ID).append(":")
                    .append(results.get(i) ? "1" : "0").append("|");
        }

        return res.toString();
    }

    public void setOnTaskAnswerListener(
            OnTaskAnswerListener onTaskAnswerListener) {
        this.onTaskAnswerListener = onTaskAnswerListener;
    }


    public interface OnTaskAnswerListener {

        boolean onTaskAnswered(boolean answer);

        boolean onAllTaskFinished(ArrayList<Boolean> results);

    }

    class Loader extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            ((MainActivity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog = UI.openProgressDialog(context, "Загрузка заданий");
                }
            });
        }

        @Override
        protected Void doInBackground(Void... voids) {

            String json = DataLoader.getQuickTestTasks(videoID);

            map = new HashMap<>();

            JsonArray a = new JsonParser().parse(json).getAsJsonArray();
            setTasksCount(a.size());

            for (int i = 0; i < a.size(); i++) {
                int     id          = a.get(i).getAsJsonObject().get("ID").getAsInt();
                String  description = a.get(i).getAsJsonObject().get("Description").getAsString();
                boolean hasImage;
                try {
                    hasImage = a.get(i).getAsJsonObject().get("Image").getAsBoolean();
                } catch (Exception e) {
                    hasImage = false;
                }

                map.put(i, new HTMLTask(id, description, hasImage, Constants.QUICK_TEST));

            }

            DataLoader.dataToHtml(DataLoader.getQuickTestFolder(context), map);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            QuickTestWindow.this.show();
        }
    }

}
