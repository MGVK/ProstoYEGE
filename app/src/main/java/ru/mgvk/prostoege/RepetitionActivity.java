package ru.mgvk.prostoege;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.mgvk.prostoege.fragments.RepetitionFragment;
import ru.mgvk.prostoege.fragments.RepetitionFragmentLeft;
import ru.mgvk.prostoege.ui.TimeButton;
import ru.mgvk.prostoege.ui.UI;
import ru.mgvk.prostoege.ui.exercises.AnswerLayout;
import ru.mgvk.prostoege.ui.exercises.DescriptionWebView;
import ru.mgvk.prostoege.ui.exercises.NumPad;
import ru.mgvk.prostoege.ui.statistic.StatisticData;
import ru.mgvk.util.RepetitionTimer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RepetitionActivity extends AppCompatActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private static final int                           MAX_TASK_NUMBER    = 19;
    private static final int                           MIN_TASK_NUMBER    = 1;
    private static       ArrayList<OnTaskChanged>      onTaskChangedList
                                                                          = new ArrayList<>();
    private static       OnStartedListener             onStartedListener;
    private final        int                           checkRepetition    = 100;
    private              SectionsPagerAdapter          mSectionsPagerAdapter;
    private              ViewPager                     mViewPager;
    private              Context                       context;
    private              MainActivity                  mainActivity;
    private              TimeButton                    timeButton;
    private              ViewGroup                     container;
    private              ImageButton                   rightButton;
    private              LinearLayout                  mainLayout;
    private              NumPad                        numPad;
    private              AnswerLayout                  answerLayout;
    private              RepetitionTimer               repetitionTimer;
    private              boolean                       stopped            = false;
    private              boolean                       inited             = false;
    // duration in  seconds
    private              int                           repetitionDuration = (3/*hour*/ * 60
                                                                             + 55)/*minutes*/ * 60;
    private              RepetitionData                data;
    private              RepetitionFragmentLeft.Result currentResult;
    private              DescriptionWebView            descriptionWebView;
    private              int                           currentTaskNumber  = 1;
    private              ArrayList<String>             answers            = new ArrayList<>();
    private              NavigationView                navigationView;
    private              int                           taskCount          = 19;
    private              DrawerLayout                  mDrawerLayout;

    public static void setOnTaskChangedList(
            ArrayList<OnTaskChanged> onTaskChangedList) {
        RepetitionActivity.onTaskChangedList = onTaskChangedList;
    }

    public static void setOnStartedListener(
            OnStartedListener onStartedListener) {
        RepetitionActivity.onStartedListener = onStartedListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_repetition);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        initMenu(navigationView.getMenu());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);

//        mDrawerLayout.setPadding(0, UI.getStatusBarHeight(), 0, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);


        prepareData();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(1);

//        findViewById(R.id.btn_menu).setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        } else {
            if (item.getItemId() == checkRepetition) {
                onClose();
                return true;
            } else {
                mViewPager.setCurrentItem(item.getItemId());
                return true;
            }
        }
    }


    private void initMenu(Menu menu) {
        int i;
        for (i = 1; i <= taskCount; i++) {
            menu.add(0, i - 1, i - 1, "Задача №" + i);
        }
        menu.add(0, 100, 100, "Проверить");
    }

    private void onRepetitionStarted() {

    }


    @Override
    protected void onStart() {
        super.onStart();
        initTimer();
        if (onStartedListener != null) {
            onStartedListener.onStart();
        }
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


    private String completeAnswers() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            result.append(data.getMap().get(i + 1).ID).append(":")
                    .append(((RepetitionFragment) mSectionsPagerAdapter.getItem(i)).getAnswer())
                    .append("|");
        }

        return result.toString();
    }


    public void onClick(View view) {

    }

    void onClose() {
        UI.openRequestDialog("Вы уверены?",
                "Вы действительно хотите закончить ",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        repetitionTimer.stop();
                    }
                }, null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onClose();
        }

        return super.onKeyDown(keyCode, event);
    }

    public RepetitionData getData() {
        return data;
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

    public void setInited(boolean inited) {
        this.inited = inited;
    }

    private void onRepetitionFinished() {


        new AsyncTask<Void, Void, RepetitionFragmentLeft.Result>() {

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
            protected RepetitionFragmentLeft.Result doInBackground(Void... voids) {

                String s = "";
                try {
                    s = DataLoader
                            .sendRepetitionAnswers(completeAnswers(), repetitionTimer.getTime());

                    StatisticData data = new Gson().fromJson(s, StatisticData.class);

                    JsonElement element = new JsonParser().parse(s);
                    JsonObject object = element.getAsJsonObject().get("Answers")
                            .getAsJsonObject();
                    data.marks = new int[19];

                    data.TimeSpent = repetitionTimer.getTime();
                    data.Time = new SimpleDateFormat("HH:mm").format(new Date());

                    for (int i = 0; i < 19; i++) {

                        data.marks[i] = object.get((i + 1) + "").getAsInt();
                    }

                    return new RepetitionFragmentLeft.Result(data);
                } catch (Exception e) {
                    e.printStackTrace();
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Произошла ошибка при загрузке данных!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                return new RepetitionFragmentLeft.Result(0);

            }

            @Override
            protected void onPostExecute(final RepetitionFragmentLeft.Result result) {

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (dialog.isShowing()) dialog.dismiss();

                        mainActivity.ui.openRepetitionResultWindow(result);

                        mainActivity.ui.closeRepetitionFragment();


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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int i = item.getItemId();
        if (i >= 0 && i <= taskCount) {
            mViewPager.setCurrentItem(i);
            navigationView.setCheckedItem(i);
            mDrawerLayout.closeDrawers();
            return true;
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        for (int i = 0; i < mViewPager.getChildCount(); i++) {
            ((RepetitionFragment) mSectionsPagerAdapter.getItem(i)).reload();
        }

        super.onResume();
    }

    private interface OnTaskChanged {

        void onChanged(int taskNumber);

    }

    public interface OnStartedListener {

        void onStart();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            return RepetitionFragment.newInstance(position + 1,
                    DataLoader.getRepetitionFolder(context) + data.getHtmlFilePath(position + 1));

        }

        @Override
        public int getCount() {

            return taskCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Задача " + position;
        }

    }


}
