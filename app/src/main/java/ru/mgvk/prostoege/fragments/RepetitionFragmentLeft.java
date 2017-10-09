package ru.mgvk.prostoege.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.AsyncTask;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.mgvk.prostoege.DataLoader;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;
import ru.mgvk.prostoege.ui.ExerciseWindow;
import ru.mgvk.prostoege.ui.TimeButton;
import ru.mgvk.prostoege.ui.UI;
import ru.mgvk.util.RepetitionTimer;
import ru.mgvk.util.Reporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by mike on 28.07.17.
 */
public class RepetitionFragmentLeft extends Fragment implements View.OnClickListener {


    private static final int                      MAX_TASK_NUMBER   = 19;
    private static final int                      MIN_TASK_NUMBER   = 1;
    private static       ArrayList<OnTaskChanged> onTaskChangedList = new ArrayList<>();
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
    private boolean inited             = false;
    // duration in  seconds
    private int     repetitionDuration = (3/*hour*/ * 60 + 55)/*minutes*/ * 60;
    private RepetitionData                    data;
    private Result                            currentResult;
    private ExerciseWindow.DescriptionWebView descriptionWebView;
    private int               currentTaskNumber = 1;
    private ArrayList<String> answers           = new ArrayList<>();

    public static void addOnTaskChangedList(OnTaskChanged onTaskChanged) {
        if (onTaskChanged != null) {
            RepetitionFragmentLeft.onTaskChangedList.add(onTaskChanged);
        }
    }

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

        prepareData();

        if (!isStopped() && !isInited()) {

            initViews();

            initTimer();

        }

        openStartDialog();

        setStopped(false);

        super.onStart();

    }

    private void prepareData() {
        if (data == null) {
            data = RepetitionData.fromFuckingJSON(context, DataLoader
                    .getRepetitionTasksJson());
        }
        if (answers.size() == 0 || answers.get(0) == null) {
            answers.clear();
            for (int i = 0; i < MAX_TASK_NUMBER; i++) {
                answers.add("");
            }
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

        new AsyncTask<Void, Void, String>() {

            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog = new ProgressDialog(context);
                        dialog.setMessage("Пожалуйста, подождите...");
                        dialog.show();
                    }
                });


            }


            @Override
            protected String doInBackground(Void... voids) {

                String s = "";
                try {
                    s = DataLoader
                            .sendRepetitionAnswers(completeAnswers(), repetitionTimer.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                StringBuilder results = new StringBuilder("Номер задания | Баллы\n");

                JsonElement element = new JsonParser().parse(s);
                JsonObject  object  = element.getAsJsonObject().get("Answers").getAsJsonObject();
                for (int i = 1; i <= 19; i++) {

                    results.append("\t").append(i).append("\t->\t")
                            .append(object.get(i + "").getAsString())
                            .append("\n");

                }

                return results.toString();

            }

            @Override
            protected void onPostExecute(final String result) {

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (dialog.isShowing()) dialog.dismiss();

                        new AlertDialog.Builder(context)
                                .setPositiveButton("Ok!", null)
                                .setTitle("Ваши Результаты")
                                .setMessage(result)
                                .create().show();
                    }
                });

            }
        }.execute();


//
//        currentResult = new Result((int) (Math.random() * 100));
//
//        mainActivity.ui.taskListFragment.getMainStatistic().addResult(currentResult);
//

    }

    private String completeAnswers() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < answers.size(); i++) {
            result.append(data.getMap().get(i + 1).ID).append(":").append(answers.get(i))
                    .append("|");
        }

        return result.toString();
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
        repetitionTimer = new RepetitionTimer(data.getRepetitionDuration());
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

        setInited(true);

    }

    private void onRepetitionStarted() {
        currentTaskNumber = 0;
        openNextTask();
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

        if (context.getResources().getConfiguration().orientation
            == Configuration.ORIENTATION_PORTRAIT) {
            setPotraitMode();
        } else {
            setLanscapeMode();
        }
        setInited(true);

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
        descriptionWebView
                .loadHTMLFile(DataLoader.getRepetitionFolder(context) + data.getHtmlFilePath
                        (decrementTaskNumber()));
        changeTask();
    }

    private void openNextTask() {
        descriptionWebView
                .loadHTMLFile(DataLoader.getRepetitionFolder(context) + data.getHtmlFilePath
                        (incrementTaskNumber()));
        changeTask();
    }

    private void changeTask() {
        titleLayout.setTaskNumber(currentTaskNumber);
        try {
            answerLayout.getAnswerTextView().setText(answers.get(currentTaskNumber - 1));
        } catch (Exception ignored) {
        }
    }

    private int incrementTaskNumber() {
        if (currentTaskNumber < MAX_TASK_NUMBER) {
            changeTaskNumber(++currentTaskNumber);
            return currentTaskNumber;
        }
        return MAX_TASK_NUMBER;
    }

    private int decrementTaskNumber() {
        if (currentTaskNumber > MIN_TASK_NUMBER) {
            changeTaskNumber(--currentTaskNumber);
            return currentTaskNumber;
        }
        return MIN_TASK_NUMBER;
    }

    void changeTaskNumber(int newNumber) {
        currentTaskNumber = newNumber;
        for (OnTaskChanged onTaskChanged : onTaskChangedList) {
            onTaskChanged.onChanged(currentTaskNumber);
        }

    }

    private void setTaskDescription() {

        descriptionWebView = new ExerciseWindow.DescriptionWebView
                (context);
//        descriptionWebView.loadUrl();
        descriptionWebView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        descriptionWebView.setMinimumHeight(UI.calcSize(100));

        mainLayout.addView(descriptionWebView);
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
                answers.set(currentTaskNumber - 1,
                        answerLayout.getAnswerTextView().getText().toString());
            }
        }
    }

    private void setLanscapeMode() {
        try {
            mainLayout.removeView(answerLayout);
            mainLayout.removeView(numPad);
            mainActivity.ui.mainScroll.setScrollEnabled(true);
            mainActivity.ui.repetitionFragmentRight.getLayout().addView(answerLayout);
            mainActivity.ui.repetitionFragmentRight.getLayout().addView(numPad);
        } catch (Exception e) {
            Reporter.report(context, e, MainActivity.PID);
        }
    }

    private void setPotraitMode() {
        try {
            mainActivity.ui.repetitionFragmentRight.getLayout().removeView(answerLayout);
            mainActivity.ui.repetitionFragmentRight.getLayout().removeView(numPad);
            mainActivity.ui.mainScroll.setScrollEnabled(false);
            mainLayout.addView(answerLayout);
            mainLayout.addView(numPad);
        } catch (Exception e) {
            Reporter.report(context, e, MainActivity.PID);
        }
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

    public boolean isInited() {
        return inited;
    }

    public void setInited(boolean inited) {
        this.inited = inited;
    }

    private static interface OnTaskChanged {

        void onChanged(int taskNumber);

    }

    public static class RepetitionData {

        private static LinkedHashMap<Integer, RepetitionTask> map = new LinkedHashMap<>();
        private int repetitionDuration;


        static RepetitionData fromFuckingJSON(Context context, String json) {

            if (json.length() < 3) {
                return null;
            }

            RepetitionData repetitionData = new RepetitionData();

            try {

                JsonElement element = new JsonParser().parse(json);


                for (int j = 1; j <= 19; j++) {

                    JsonObject o           = element.getAsJsonObject().getAsJsonObject(j + "");
                    int        id          = o.get("ID").getAsInt();
                    String     description = o.get("Description").getAsString();

                    map.put(j, new RepetitionFragmentLeft.RepetitionData.RepetitionTask(id,
                            description));

                }

                repetitionData.setRepetitionDuration(element.getAsJsonObject().get("Time")
                                                             .getAsInt() * 60);


            } catch (Exception e) {
                Reporter.report(context, e, MainActivity.PID);
            }

            repetitionData.setMap(map);
            for (RepetitionTask repetitionTask : map.values()) {
                repetitionTask.Description = repetitionTask.Description.replace("\\\"", "\"");
                repetitionTask.Description = repetitionTask.Description.replace("\\/", "");
                repetitionTask.Description = repetitionTask.Description.replace("\\\\", "\\");
            }

            dataToHtml(context, map);

            return repetitionData;
        }

        private static void dataToHtml(Context context,
                                       HashMap<Integer, RepetitionTask> map) {
            for (RepetitionTask repetitionTask : map.values()) {
                File file = new File(DataLoader.getRepetitionFolder(context) + repetitionTask
                        .ID + ".html");
                try {

                    file.createNewFile();

                    FileWriter writer = new FileWriter(file);

                    String s =
                            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
                            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\n"
                            + "<link rel=\"stylesheet\" href=\"style.css\">\n"
                            + "\n"
                            + "<script type=\"text/x-mathjax-config\">\n"
                            + "//\n"
                            + "//  Do NOT use this page as a template for your own pages.  It includes\n"
                            + "//  code that is needed for testing your site's installation of MathJax,\n"
                            + "//  and that should not be used in normal web pages.  Use sample.html as\n"
                            + "//  the example for how to call MathJax in your own pages.\n"
                            + "//\n"
                            + "  MathJax.HTML.Cookie.Set(\"menu\",{});\n"
                            + "  MathJax.Hub.Config({\n"
                            + "    extensions: [\"tex2jax.js\"],\n"
                            + "    jax: [\"input/TeX\",\"output/HTML-CSS\"],\n"
                            + "    messageStyle: \"none\",\n"
                            + "    \"HTML-CSS\": {\n"
                            + "      availableFonts:[], preferredFont: \"TeX\", webFont: \"TeX\",\n"
                            + "      styles: {\".MathJax_Preview\": {visibility: \"hidden\"}},\n"
                            + "    }\n"
                            + "  });\n"
                            + "\n"
                            + "(function (HUB) {\n"
                            + "\n"
                            + "  var MINVERSION = {\n"
                            + "    Firefox: 3.0,\n"
                            + "    Opera: 9.52,\n"
                            + "    MSIE: 6.0,\n"
                            + "    Chrome: 0.3,\n"
                            + "    Safari: 2.0,\n"
                            + "    Konqueror: 4.0,\n"
                            + "    Unknown: 10000.0 // always disable unknown browsers\n"
                            + "  };\n"
                            + "\n"
                            + "  if (!HUB.Browser.versionAtLeast(MINVERSION[HUB.Browser]||0.0)) {\n"
                            + "    HUB.Config({\n"
                            + "      jax: [],                   // don't load any Jax\n"
                            + "      extensions: [],            // don't load any extensions\n"
                            + "      \"v1.0-compatible\": false   // skip warning message due to no jax\n"
                            + "    });\n"
                            + "    setTimeout('document.getElementById(\"badBrowser\").style.display = \"\"',0);\n"
                            + "  }\n"
                            + "\n"
                            + "  if (HUB.Browser.isMSIE && !HUB.Browser.versionAtLeast(\"7.0\")) {\n"
                            + "    setTimeout('document.getElementById(\"MSIE6\").style.display = \"\"');\n"
                            + "  }\n"
                            + "\n"
                            + "})(MathJax.Hub);\n"
                            + "\n"
                            + "</script>\n"
                            + "<script type=\"text/javascript\" src=\"MathJax/MathJax"
                            + ".js\"></script>";

                    writer.write(s + repetitionTask.Description);
                    writer.flush();
                    writer.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public LinkedHashMap<Integer, RepetitionTask> getMap() {
            return map;
        }

//        private static int increment(int i) {
//            return i >= 4 ? 0 : ++i;
//        }

        public void setMap(
                LinkedHashMap<Integer, RepetitionTask> map) {
            this.map = map;
        }

        public String getHtmlFilePath(int number) {
            RepetitionTask t = map.get(number);
            if (t == null) {
                return "test.html";
            } else {
                return t.ID + ".html";
            }
        }

        public long getRepetitionDuration() {
            return repetitionDuration;
        }

        public void setRepetitionDuration(int repetitionDuration) {
            this.repetitionDuration = repetitionDuration;
        }

        public static class RepetitionTask {

            int    ID          = 0;
            String Description = "";

            public RepetitionTask() {

            }

            public RepetitionTask(int ID, String description) {
                this.ID = ID;
                Description = description;
            }

            public int getID() {
                return ID;
            }

            public String getDescription() {
                return Description;
            }
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

            final Button prevBtn   = new Button(context);
            Button       finishBtn = new Button(context);
            final Button nextBtn   = new Button(context);

            prevBtn.setLayoutParams(new LayoutParams(-1, -1, 5));
            finishBtn.setLayoutParams(new LayoutParams(-1, -1, 5));
            nextBtn.setLayoutParams(new LayoutParams(-1, -1, 5));

            prevBtn.setOnClickListener(this);
            finishBtn.setOnClickListener(this);
            nextBtn.setOnClickListener(this);

            prevBtn.setTag(0);
            finishBtn.setTag(1);
            nextBtn.setTag(2);

            RepetitionFragmentLeft.addOnTaskChangedList(new OnTaskChanged() {
                @Override
                public void onChanged(int taskNumber) {
                    if (taskNumber >= RepetitionFragmentLeft.MAX_TASK_NUMBER) {
                        nextBtn.setEnabled(false);
                        nextBtn.setBackgroundResource(R.drawable.repetition_right_disabled);
                    } else if (taskNumber <= RepetitionFragmentLeft
                            .MIN_TASK_NUMBER) {
                        prevBtn.setEnabled(false);
                        prevBtn.setBackgroundResource(R.drawable.repetition_left_disabled);
                    } else {
                        nextBtn.setEnabled(true);
                        prevBtn.setEnabled(true);
                        prevBtn.setBackgroundResource(R.drawable.repetition_left);
                        nextBtn.setBackgroundResource(R.drawable.repetition_right);
                    }
                }
            });

            prevBtn.setBackgroundResource(R.drawable.repetition_left_disabled);
            prevBtn.setEnabled(false);
            nextBtn.setBackgroundResource(R.drawable.repetition_right);
            finishBtn.setBackgroundResource(R.drawable.repetition_finish);

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
                    break;
                }
                case 1: {
                    onFinish();
                    break;
                }
                case 2: {
                    onNextTask();
                    break;
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

        public void setTaskNumber(int number) {
            textView.setText("Задание " + number);
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

        public Result(int scoreSecondary) {
            this.scoreSecondary = scoreSecondary;
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
