package ru.mgvk.prostoege.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import ru.mgvk.prostoege.Constants;
import ru.mgvk.prostoege.DataLoader;
import ru.mgvk.prostoege.HTMLTask;
import ru.mgvk.prostoege.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class QuickTestWindow extends DialogWindow {

    private static int tasksCount;
    private static HashMap<Integer, HTMLTask> map = new HashMap<>();
    int videoID = 0;
    Context context;
    private FrameLayout    mainLayout;
    private AttachedLayout layout;
    private long testDuration = 0;
    private long startTime    = 0;

    public QuickTestWindow(Context context, int videoID) {
        super(context);
        this.context = context;
        this.videoID = videoID;
        prepare();
        init();
    }

    public static int getTasksCount() {
        return tasksCount;
    }

    public void setTasksCount(int tasksCount) {
        QuickTestWindow.tasksCount = tasksCount;
    }

    public void prepare() {
        new Loader().execute();
    }

    private void init() {
        mainLayout = new FrameLayout(context);
        mainLayout.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        layout = new AttachedLayout(context);
        mainLayout.addView(layout);
        layout.setOnTaskAnswerListener(new AttachedLayout.OnTaskAnswerListener() {
            @Override
            public void onTaskAnswered(boolean answer) {

            }

            @Override
            public void onAllTaskFinished(ArrayList<Boolean> results) {

                final ProgressDialog dialog = UI.openProgressDialog(context, "Проверка заданий");

                String testResult = DataLoader.getQuickTestResults(
                        videoID,
                        prepareResults(results),
                        calcDuration());

                dialog.dismiss();

                UI.openQuickTestResultDialog(context, testResult, getTasksCount(), testDuration);
                close();

            }

            private String prepareResults(ArrayList<Boolean> results) {
                StringBuilder res = new StringBuilder();
                for (int i = 0; i < results.size(); i++) {
                    res.append(map.get(i).ID).append(":")
                            .append(results.get(i) ? "1" : "0").append("|");
                }

                return res.toString();
            }
        });


        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Закончить тест?")
                        .setCancelable(false)
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                layout.finishTest();
                            }
                        })
                        .setNegativeButton("Нет", null)
                        .create().show();
            }
        });


        super.addView(mainLayout);
    }

    private long calcDuration() {

        return testDuration = (System.currentTimeMillis() - startTime) / 1000;

    }

    @Override
    protected void open() {
        startTime = System.currentTimeMillis();
        layout.openNextTask();
        super.open();
    }

    private static class AttachedLayout extends LinearLayout implements OnClickListener {

        private ExerciseWindow.DescriptionWebView descriptionWebView;
        private OnTaskAnswerListener              onTaskAnswerListener;
        private int                currentTaskID = 0;
        private ArrayList<Boolean> results       = new ArrayList<>();
        private Context context;

        public AttachedLayout(Context context) {
            super(context);
            this.context = context;
            setOrientation(VERTICAL);

            setTaskDescription();
            initButtons();
        }

        private void setTaskDescription() {

            descriptionWebView = new ExerciseWindow.DescriptionWebView
                    (context);
            descriptionWebView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            descriptionWebView.setMinimumHeight(UI.calcSize(100));
            descriptionWebView.getSettings().setSupportZoom(true);
            addView(descriptionWebView);
        }

        private void initButtons() {

            LinearLayout btnLayout = new LinearLayout(context);
            btnLayout.setOrientation(HORIZONTAL);
            btnLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

            LayoutParams lp = new LinearLayout.LayoutParams(-1, -2, 1);


            Button yesBtn = new Button(context);
            yesBtn.setLayoutParams(lp);
            yesBtn.setOnClickListener(this);
            yesBtn.setText("Да");
            yesBtn.setTag(true);

            Button noBtn = new Button(context);
            noBtn.setLayoutParams(lp);
            noBtn.setOnClickListener(this);
            noBtn.setText("Нет");
            noBtn.setTag(false);

            btnLayout.addView(yesBtn);
            btnLayout.addView(noBtn);

            addView(btnLayout);

        }


        @Override
        public void onClick(View v) {
            try {
                if (onTaskAnswerListener != null) {
                    onTaskAnswerListener.onTaskAnswered((Boolean) v.getTag());
                    results.add((Boolean) v.getTag());
                }
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
            if (onTaskAnswerListener != null) {
                onTaskAnswerListener.onAllTaskFinished(results);
            }
        }

        public void setOnTaskAnswerListener(
                OnTaskAnswerListener onTaskAnswerListener) {
            this.onTaskAnswerListener = onTaskAnswerListener;
        }

        interface OnTaskAnswerListener {

            void onTaskAnswered(boolean answer);

            void onAllTaskFinished(ArrayList<Boolean> results);

        }

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
            open();

        }
    }
}



