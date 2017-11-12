package ru.mgvk.prostoege.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import ru.mgvk.prostoege.*;
import ru.mgvk.prostoege.fragments.*;
import ru.mgvk.util.Reporter;
import ru.mgvk.util.StateTags;

import java.text.SimpleDateFormat;

/**
 * Created by Michael_Admin on 08.08.2016.
 */
public class UI {

    private static int realDPI;

    public int deviceWidth = 0, deviceHeight = 0;
    public View rootView0, rootView1, rootView2;

    public MainScrollView          mainScroll;
    public MainMenu                mainMenu;
    public TaskListFragment        taskListFragment;
    public VideoListFragment       videoListFragment;
    public Fragment                currentFragment;
    public ExercisesListFragment   exercisesListFragment;
    public ToolsFragment           toolsFragment;
    public RepetitionFragmentLeft  repetitionFragmentLeft;
    public RepetitionFragmentRight repetitionFragmentRight;
    private boolean added = true;
    private Context             context;
    private MainActivity        mainActivity;
    private FragmentTransaction tr;
    private FragmentManager     manager;
    private BalanceWindow       balanceWindow;

    public UI(Context context, boolean restoring) {


        mainActivity = (MainActivity) (this.context = context);

        mainActivity.stopwatch.checkpoint("UI_start");

        realDPI = mainActivity.getResources().getDisplayMetrics().densityDpi;

        if (!DataLoader.isLicenseAccepted(context)) {
            openPolicyWindow();
        }

        initFolders();

        initSizes();


        initFragments(restoring);

//        addFragments();

        initViews();
        updateSizes(context.getResources().getConfiguration().orientation);

        openTaskListFragment();
        mainActivity.stopwatch.checkpoint("UI_finish");

    }

    public static float calcSize(float size) {
        return (float) (size * (realDPI / (double) 160));
    }

    public static int calcSize(int size) {
        return (int) (size * (realDPI / (double) 160));
    }

    public static int calcFontSize(int size) {
        return (int) (size * (160 / (double) realDPI));
    }

    public static void enterFullScreen(Context context) {

        ((MainActivity) context).findViewById(R.id.root_0)
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                       | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                       | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                       | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                       | View.SYSTEM_UI_FLAG_FULLSCREEN
                                       | View.SYSTEM_UI_FLAG_LOW_PROFILE
                                       | View.SYSTEM_UI_FLAG_IMMERSIVE);
        try {
            ((MainActivity) context).getActionBar().hide();
        } catch (NullPointerException ignored) {
        }
    }

    public static void exitFullScreen(Context context) {

        ((MainActivity) context).findViewById(R.id.root_0)
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                       | View.SYSTEM_UI_FLAG_VISIBLE);
        try {
            ((MainActivity) context).getActionBar().show();
        } catch (NullPointerException ignored) {
        }
    }

    public static String getPriceLabel(int price) {
        String s;
        int    p = price % 10;
        if (p == 1) {
            s = " Ёж";
        } else if (p > 1 && p < 5) {
            s = " Ежа";
        } else {
            s = " Ежей";
        }

        return price + s;

    }

    public static void makeErrorMessage(Context context, String s) {

        HintWindow window = new HintWindow(context);
        TextView   text   = new TextView(context);
        text.setText(s);
        window.layout.setBackgroundResource(R.drawable.answer_incorrect);
        text.setTextColor(Color.WHITE);
        window.addView(text);

        window.open();
    }

    public static void openQuickTestResultDialog(Context context, String testResult,
                                                 int tasksCount, long testDuration) {
        new AlertDialog.Builder(context)
                .setTitle("Результаты теста")
                .setMessage("Вы ответили правильно " + testResult + " из " + tasksCount + "\nза "
                            + testDuration + " секунд")
                .setPositiveButton("Ok", null)
                .create().show();
    }

    public static ProgressDialog openProgressDialog(Context context, String s) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(s);
        dialog.show();
        return dialog;
    }

    private void initFolders() {
        DataLoader.getRepetitionFolder(context);
        DataLoader.getQuickTestFolder(context);
    }

    public void openRepetitionResultWindow(RepetitionFragmentLeft.Result result) {

        final AlertDialog.Builder b = new AlertDialog.Builder(context)
                .setTitle("Результаты тестирования:")
                .setMessage("Дата: " + (new SimpleDateFormat("dd MMMM")
                        .format(result.getDate(context))) + " в "
                            + result.getTime() + "\n"
                            + "Длительность экзамена:" + result.getDuration() + "\n"
                            + "Первичный балл: " + result.getScorePrimary() + "\n"
                            + "Вторичный балл: " + result.getScoreSecondary() + "\n"
                )
                .setPositiveButton("Закрыть", null);

        if (result.getStatisticDatum().marks != null
            && result.getStatisticDatum().marks.length != 0) {
            b.setNeutralButton("Баллы по задачам", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    WebView v = new WebView(context);
                    v.setVerticalScrollBarEnabled(false);
                    v.getSettings().setSupportZoom(false);
                    v.setLayoutParams(new ViewGroup.LayoutParams(-1, UI.calcSize(200)));
                    v.loadUrl("file://" + DataLoader.getRepetitionFolder(null) + "stat_tmp.html");

                    new AlertDialog.Builder(context)
                            .setTitle("Баллы по задачам")
                            .setView(v)
                            .setPositiveButton("Ok", null)
                            .show();

                }
            });
        }

        b.create().show();

    }

    private void openPolicyWindow() {

        WebView webView = new WebView(context);
        webView.loadUrl(DataLoader.PolicyURL);
        webView.clearCache(true);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
//        webView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {}
//        });

        new AlertDialog.Builder(context)
                .setMessage("Пользовательское соглашение")
                .setCancelable(false)
                .setView(webView)
                .setPositiveButton("ПРИНИМАЮ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataLoader.acceptLicense(context);
                    }
                })
                .setNegativeButton("НЕ ПРИНИМАЮ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mainActivity.finish();
                    }
                }).create().show();


//        LicenseWindow window = new LicenseWindow(context);
//        window.open();

    }

    void initSizes() {
        deviceWidth = mainActivity.getResources().getDisplayMetrics().widthPixels;
        deviceHeight = mainActivity.getResources().getDisplayMetrics().heightPixels;
    }

    void initViews() {
        rootView0 = mainActivity.findViewById(R.id.root_0);
        rootView1 = mainActivity.findViewById(R.id.root_1);
        rootView2 = mainActivity.findViewById(R.id.root_2);
        mainScroll = (MainScrollView) mainActivity.findViewById(R.id.mainScroll);
        mainScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);


        initMainMenu();

    }

    void initFragments(final boolean restoring) {
        if (restoring) {
//            taskListFragment = (TaskListFragment) mainActivity.getFragmentManager()
//                    .findFragmentByTag("TaskListFragment");
//            videoListFragment = (VideoListFragment) mainActivity.getFragmentManager()
//                    .findFragmentByTag("VideoListFragment");
//            exercisesListFragment = (ExercisesListFragment) mainActivity.getFragmentManager()
//                    .findFragmentByTag("ExercisesListFragment");
//            toolsFragment = (ToolsFragment) mainActivity.getFragmentManager()
//                    .findFragmentByTag("ToolsFragment");
            mainActivity.stopwatch.checkpoint("initFragments_1");

            taskListFragment = (TaskListFragment) InstanceController
                    .getObject("TasksFragment");
            videoListFragment = (VideoListFragment) InstanceController
                    .getObject("VideosFragment");
            exercisesListFragment = (ExercisesListFragment) InstanceController
                    .getObject("ExercisesFragment");
            toolsFragment = (ToolsFragment) InstanceController
                    .getObject("ToolsFragment");
            repetitionFragmentLeft = (RepetitionFragmentLeft) InstanceController
                    .getObject("RepetitionFragmentLeft");
            repetitionFragmentRight = (RepetitionFragmentRight) InstanceController
                    .getObject("RepetitionFragmentRight");

        } else {

            taskListFragment = new TaskListFragment(context);

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    videoListFragment = new VideoListFragment(context);
                    exercisesListFragment = new ExercisesListFragment(context);
                    toolsFragment = new ToolsFragment(context);
                    repetitionFragmentRight = new RepetitionFragmentRight();
                    repetitionFragmentLeft = new RepetitionFragmentLeft(context);
                }
            });

            mainActivity.stopwatch.checkpoint("initFragments_3");
            try {
                InstanceController.putObject("TasksFragment", taskListFragment);
            } catch (InstanceController.NotInitializedError notInitializedError) {
                notInitializedError.printStackTrace();
            }
            try {
                InstanceController.putObject("VideosFragment", videoListFragment);
            } catch (InstanceController.NotInitializedError notInitializedError) {
                notInitializedError.printStackTrace();
            }
            try {
                InstanceController.putObject("ExercisesFragment", exercisesListFragment);
            } catch (InstanceController.NotInitializedError notInitializedError) {
                notInitializedError.printStackTrace();
            }
            try {
                InstanceController.putObject("ToolsFragment", toolsFragment);
            } catch (InstanceController.NotInitializedError notInitializedError) {
                notInitializedError.printStackTrace();
            }
            try {
                InstanceController.putObject("RepetitionFragmentLeft", repetitionFragmentLeft);
            } catch (InstanceController.NotInitializedError notInitializedError) {
                notInitializedError.printStackTrace();
            }
            try {
                InstanceController.putObject("RepetitionFragmentRight", repetitionFragmentRight);
            } catch (InstanceController.NotInitializedError notInitializedError) {
                notInitializedError.printStackTrace();
            }
        }
    }

    void initMainMenu() {
        mainMenu = new MainMenu(context, (ViewGroup) rootView0);

    }

    public void openMenu(MenuPanel menu) {
        if (menu != null) {
            doMenuAppearAnimation(menu);
//            menu.setX(0);
//            menu.setVisibility(View.VISIBLE);
        }
    }

    public void closeMenu(MenuPanel menu) {
        if (menu != null) {
            doMenuDisappearAnimation(menu);
        }
    }

    void doMenuAppearAnimation(final MenuPanel menu) {
        menu.setX(-1 * menu.getWidth());
        menu.setVisibility(View.VISIBLE);
        ObjectAnimator a = ObjectAnimator.ofFloat(menu, "x", menu.getX(), 0);
        a.setDuration(300);
        a.start();

        mainActivity.addToBackStack(new Runnable() {
            @Override
            public void run() {
                closeMenu(menu);
            }
        });

    }

    void doMenuDisappearAnimation(final MenuPanel menu) {
        ObjectAnimator a = ObjectAnimator.ofFloat(menu, "x", 0, -1 * menu.getWidth());
        a.setDuration(300);
        a.start();
        a.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                menu.setVisibility(View.INVISIBLE);
                menu.setX(-1 * menu.getWidth());
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

//    private void addFragments() {
//        FragmentManager     manager = mainActivity.getFragmentManager();
//        FragmentTransaction tr      = manager.beginTransaction();
//        tr.add(R.id.root_1, exercisesListFragment, "ExercisesListFragment");
//        tr.add(R.id.root_2, toolsFragment, "ToolsFragment");
//        tr.add(R.id.root_2, videoListFragment, "VideoListFragment");
//        tr.add(R.id.root_1, taskListFragment, "TaskListFragment");
//
//        tr.hide(taskListFragment);
//        tr.hide(exercisesListFragment);
//        tr.hide(videoListFragment);
//        tr.hide(toolsFragment);
//        tr.commit();
//
//
//    }

    public void updateSizes(int orientation) {
        initSizes();
        double k = orientation == Configuration.ORIENTATION_PORTRAIT ? 1 : 0.5;
        rootView1.setLayoutParams(new LinearLayout.LayoutParams((int) (k * deviceWidth), -1));
        rootView2.setLayoutParams(new LinearLayout.LayoutParams((int) (k * deviceWidth), -1));

        mainMenu.setLayoutParams(new FrameLayout.LayoutParams((deviceWidth), -1));
        mainMenu.updateSizes((int) (k * deviceWidth), deviceHeight);


    }

    public void openTaskOrVideoFragment(final boolean task) {

        manager = mainActivity.getFragmentManager();
        tr = manager.beginTransaction();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (manager.findFragmentById(videoListFragment.getId()) == null) {
                    added = false;
                    tr.add(R.id.root_2, videoListFragment, "VideoListFragment");
                    added = true;
                }
            }
        }).start();

        if (manager.findFragmentById(taskListFragment.getId()) == null) {
            tr.add(R.id.root_1, taskListFragment, "TaskListFragment");
        }

        while (!added) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        if (currentFragment != null) {
            hideCurrentFragments(tr, false);
        }

        tr.show(taskListFragment);

        tr.show(videoListFragment);

        tr.commit();

        currentFragment = taskListFragment;

        if (task) {
            mainScroll.toLeft();
        } else {
            mainScroll.toRight();
        }

    }

    public void openExercisesOrToolsFragment(final boolean exercises) {
        FragmentManager     manager = mainActivity.getFragmentManager();
        FragmentTransaction tr      = manager.beginTransaction();

        if (manager.findFragmentById(exercisesListFragment.getId()) == null) {
            tr.add(R.id.root_1, exercisesListFragment, "ExercisesListFragment");
        }
        if (manager.findFragmentById(toolsFragment.getId()) == null) {
            tr.add(R.id.root_2, toolsFragment, "ToolsFragment");
        }

        if (currentFragment != null) {
            hideCurrentFragments(tr, true);
        }

        tr.show(exercisesListFragment);
        tr.show(toolsFragment);
        tr.commit();
        currentFragment = exercisesListFragment;
        if (exercises) {
            mainScroll.toLeft();
        } else {
            mainScroll.toRight();
        }


    }

    public void openTaskListFragment() {

        mainActivity.getBackStack().addState(StateTags.TASK_LIST_FRAGMENT);
        openTaskOrVideoFragment(true);
    }

    public void hideCurrentFragments(FragmentTransaction tr, boolean currIsTasks) {
        if (currIsTasks) {
            tr.hide(taskListFragment);
            tr.hide(videoListFragment);
        } else {
            tr.hide(toolsFragment);
            tr.hide(exercisesListFragment);
        }
    }

    public void setCurrentTask(Task task) throws Exception {
        videoListFragment.setCurrentTask(task);
        if (exercisesListFragment == null) {
            exercisesListFragment = new ExercisesListFragment(context);
        }
        exercisesListFragment.setCurrentTask(task);
        if (toolsFragment == null) {
            toolsFragment = new ToolsFragment(context);
        }
        toolsFragment.setTask(task);
    }

    public void openVideoListFragment(Task task) throws Exception {


        mainActivity.addToBackStack(new Runnable() {
            @Override
            public void run() {
                openTaskOrVideoFragment(true);
            }
        });

        if (task != null) {
            setCurrentTask(task);
        }

        openTaskOrVideoFragment(false);

        mainActivity.getBackStack().addState(StateTags.VIDEO_LIST_FRAGMENT);


    }

    public void openExercisesListFragment() {

        mainActivity.addToBackStack(new Runnable() {
            @Override
            public void run() {
                openTaskOrVideoFragment(false);
            }
        });

        openExercisesOrToolsFragment(true);


        mainActivity.getBackStack().addState(StateTags.EXERCISE_LIST_FRAGMENT);

    }

    public void openToolsFragment() {
        mainActivity.addToBackStack(new Runnable() {
            @Override
            public void run() {
                openExercisesOrToolsFragment(true);
            }
        });
        openExercisesOrToolsFragment(false);

        mainActivity.getBackStack().addState(StateTags.TOOLS_FRAGMENT);

    }

    public void closeRepetitionFragment() {

//        new AlertDialog.Builder(context)
//                .setTitle("Вы действительно хотите закончить репетицию ЕГЭ?")
//                .setPositiveButton("Да!", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        if(result!=null) {
//                            openRepetitionResultWindow(result);
//                        }
//
//                    }
//                })
//                .setNegativeButton("Отмена",null)
//                .create().show();

        repetitionFragmentLeft.onClose();

        FragmentManager     manager = mainActivity.getFragmentManager();
        FragmentTransaction tr      = manager.beginTransaction();
        tr.hide(repetitionFragmentLeft);
        tr.hide(repetitionFragmentRight);
        currentFragment = taskListFragment;

        tr.commit();


    }

    public void openRightRepetitionComponent() {
        this.mainScroll.toRight();
    }

//    public void openPreviousFragment() {
//        FragmentTransaction tr = mainActivity.getFragmentManager().beginTransaction();
//        tr.hide(currentFragment);
//        tr.commit();
//    }

    public void openLeftRepetitionComponent() {
        this.mainScroll.toLeft();
    }

    public void openRepetitionFragment() {
        mainActivity.addToBackStack(new Runnable() {
            @Override
            public void run() {
                closeRepetitionFragment();
            }
        });

        FragmentManager     manager = mainActivity.getFragmentManager();
        FragmentTransaction tr      = manager.beginTransaction();

        MainActivity.stopwatch.checkpoint("repetition fragment asked");

        if (manager.findFragmentById(repetitionFragmentLeft.getId()) == null) {
            tr.add(R.id.root_1, repetitionFragmentLeft, "RepetitionFragmentLeft");
        }
        if (manager.findFragmentById(repetitionFragmentRight.getId()) == null) {
            tr.add(R.id.root_2, repetitionFragmentRight, "RepetitionFragmentRight");
        }

//        if (currentFragment != null) {
//            hideCurrentFragments(tr, true);
//            hideCurrentFragments(tr, false);
//        }

        tr.show(repetitionFragmentLeft);
        tr.show(repetitionFragmentRight);

        tr.commit();

        MainActivity.stopwatch.checkpoint("repetition fragment showed");

        currentFragment = repetitionFragmentLeft;

    }

    public void openBalanceDialog() {
        balanceWindow = new BalanceWindow(context);
        balanceWindow.open();
    }

    public void openHintWindow(Task.Exercise currentExercise) {

        if (currentExercise.hintIsBought()) {
            HintWindow hintWindow = new HintWindow(context);
            hintWindow.addView(new HintWebView(context, currentExercise.getHintID()));
            hintWindow.open();
        } else {
            openHintPurchaseWindow(currentExercise);
        }
    }

    public void openHintPurchaseWindow(final Task.Exercise exercise) {
        final HintWindow window = new HintWindow(context);
        LinearLayout     layout = new LinearLayout(context);
        layout.setLayoutParams(new ViewGroup.LayoutParams(-2, calcSize(80)));

        TextView txt = new TextView(context);
        txt.setText("Подсказка стоит "
                    + getPriceLabel(exercise.getHintPrice()) +
                    "\nПродолжить?\n" +
                    "У Вас " + getPriceLabel(mainActivity.profile.Coins));
        txt.setTextSize(20);
        txt.setTextColor(Color.WHITE);
        txt.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        layout.addView(txt);

        ImageButton okBtn = new ImageButton(context);
        okBtn.setBackgroundResource(R.drawable.ok);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(UI.calcSize(40),
                UI.calcSize(40));
        lp.gravity = Gravity.RIGHT;
        okBtn.setLayoutParams(lp);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    mainActivity.onBackPressed();

                    if (mainActivity.profile.Coins < exercise.getHintPrice()) {
                        openLowCoinsWindow();
                    } else {
                        if (mainActivity.pays.buyHint(exercise.getHintID())) {
                            mainActivity.updateCoins(-1 * exercise.getHintPrice());
                            updateCoins();
                            exercise.setHintIsBought();
                            exercisesListFragment.getExerciseWindow()
                                    .setStatus(ExerciseWindow.PROMPTED);
                            openHintWindow(exercise);
                        } else {
                            makeErrorMessage(context, "Произошла ошибка:(\nПопробуйте еще раз.");
                        }
                    }
                } catch (Exception e) {
                    Reporter.report(context, e, mainActivity.reportSubject);
                }
            }
        });
        layout.addView(okBtn);
        mainActivity.pays.setOnPurchaseListener(new Pays.OnPurchaseListener() {
            @Override
            public void OnPurchase() {
                window.close();
            }
        });
        window.addView(layout);
//        window.layout.getLayoutParams().height=-2;
//        window.layout.setLayoutParams(window.layout.getLayoutParams());
        window.open();


    }

    public void openVideoPurchaseDialog(VideoLayout.VideoCard video) {
        final VideoPurchaseWindow videoPurchaseWindow = new VideoPurchaseWindow(context, video);
        videoPurchaseWindow.open();
    }

    public void openLowCoinsWindow() {
        final HintWindow hintWindow = new HintWindow(context);
        hintWindow.layout.setBackgroundResource(R.drawable.answer_incorrect);

        LinearLayout balanceLayout = new LinearLayout(context);
        balanceLayout.setOrientation(LinearLayout.VERTICAL);
        balanceLayout.setLayoutParams(new FrameLayout.LayoutParams(-2, -2));

        TextView text = new TextView(context);
        text.setText("Не хватает ежей!");
        text.setTextSize(15);
        text.setTextColor(Color.WHITE);
        text.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

        TextView balanceBtn = new TextView(context);
        balanceBtn.setBackgroundResource(R.drawable.btn_videodonate_balance);
        balanceBtn.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        balanceBtn.setText("ПОПОЛНИТЬ СЧЕТ");
        balanceBtn.setGravity(Gravity.CENTER);
        balanceBtn.setTextSize(15);
        balanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).ui.openBalanceDialog();
            }
        });

        balanceLayout.addView(text);
        balanceLayout.addView(balanceBtn);

        hintWindow.addView(balanceLayout);
//        ((ViewGroup.LayoutParams) hintWindow.layout.getLayoutParams()).gravity = Gravity.CENTER;

        hintWindow.open();

    }

    public void openExerciseAnswerShowWindow(String answer) {
        final HintWindow window = new HintWindow(context);
        TextView         txt    = new TextView(context);
        txt.setTextColor(Color.WHITE);
        txt.setText("Верный ответ: " + answer);
        txt.setTextSize(20);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2);
        lp.setMargins(UI.calcSize(5), UI.calcSize(5), UI.calcSize(5), UI.calcSize(5));
        txt.setLayoutParams(lp);
        window.addView(txt);
        window.open();

        window.closeWithDelay(2000);
    }

    public void openExerciseResultWindow(boolean correct, int bonus) {
        final HintWindow window   = new HintWindow(context);
        TextView         textView = new TextView(context);
        textView.setTextColor(Color.WHITE);
        if (correct) {
            String text = "Верный ответ!";
            if (bonus != 0) {
                text += "\nВы получаете бонус: "
                        + getPriceLabel(bonus);
                mainActivity.updateCoins(bonus);
            }
            textView.setText(text);
            window.layout.setBackgroundResource(R.drawable.answer_correct);
        } else {
            textView.setText("Ответ неверен!");
            window.layout.setBackgroundResource(R.drawable.answer_incorrect);
        }
        textView.setTextSize(20);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2);
        lp.setMargins(UI.calcSize(5), UI.calcSize(5), UI.calcSize(5), UI.calcSize(5));
        textView.setLayoutParams(lp);
        window.addView(textView);
        window.open();
        window.closeWithDelay(2500);

    }

    public void updateCoins() {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    taskListFragment.updateCoins();
                } catch (Exception e) {
                    Reporter.report(context, e, ((MainActivity) context).reportSubject);
                }
            }
        });

    }

    public void openWifiOnlyDialog() {
        makeErrorMessage(context, "Включен режим \"Только WiFi\"!");
    }

    public void makeShareHelpMessage() {


        final HintWindow window = new HintWindow(context);
        window.layout.setBackgroundResource(R.drawable.hint_back);

        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        layout.setMinimumWidth(UI.calcSize(160));
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView text = new TextView(context);
        text.setText(R.string.help_share);
        text.setTextColor(Color.WHITE);
        text.setTextSize(17);
        layout.addView(text);

        TextView okBtn = new TextView(context);
        okBtn.setBackgroundResource(R.drawable.btn_answer_check);
        okBtn.setText("ЗАЙТИ!");
        okBtn.setTextSize(20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        okBtn.setLayoutParams(lp);

        View.OnClickListener listener;
        okBtn.setOnClickListener(listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mainActivity.onBackPressed();
                window.close();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    openTaskOrVideoFragment(true);
                                } catch (Exception e) {
                                    Reporter.report(context, e,
                                            ((MainActivity) context).reportSubject);
                                }
                            }
                        });
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    openMenu(mainMenu);
                                } catch (Exception e) {
                                    Reporter.report(context, e,
                                            ((MainActivity) context).reportSubject);
                                }
                            }
                        });
                    }
                }).start();

            }
        });
        layout.setOnClickListener(listener);
        layout.addView(okBtn);
        window.addView(layout);
//        window.layout.getLayoutParams().height=-2;
//        window.layout.setLayoutParams(window.layout.getLayoutParams());
        window.open();

    }
}
