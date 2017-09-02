package ru.mgvk.prostoege;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.gson.Gson;
import ru.mgvk.prostoege.ui.MainScrollView;
import ru.mgvk.prostoege.ui.UI;
import ru.mgvk.util.BackStack;
import ru.mgvk.util.MathJaxPreparer;
import ru.mgvk.util.Reporter;
import ru.mgvk.util.Stopwatch;

import java.io.File;
import java.io.FileOutputStream;
import java.net.ConnectException;
import java.util.ArrayList;


public class MainActivity extends Activity implements MainScrollView.OnScreenSwitchedListener {

    static final  String APP_SETTINGS = "SETTINGS_EGE";
    public static String PID          = "default";
    public          UI        ui;
    public volatile Profile   profile;
    public          Pays      pays;
    public          Stopwatch stopwatch;
    public          String    reportSubject;
    private         Context   context;
    private ArrayList<OnConfigurationUpdateListener> configurationUpdatesList = new ArrayList<>();
    private boolean                                  restoring                = false;
    private BackStack backStack;
    //    private Stack<Runnable> backStack = new Stack<>();
    private boolean                            profileIsLoading    = false;
    private ArrayList<Profile.OnLoadCompleted> onLoadCompletedList = new ArrayList<>();

    // TODO: 10.08.16 user-friendly ошибки
    private boolean pressAgain = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        backStack = new BackStack(this);

        try {

            setAccountInfo();

            setContentView(R.layout.activity_main);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }

            stopwatch = new Stopwatch(true);
            stopwatch.start("MainActivity_onCreate");

            final int p = UI.calcSize(5);
            findViewById(R.id.main_linear).setPadding(0, getStatusBarHeight(), 0, 0);
            context = this;
//        loadingBackground = new ImageView(this);
//        loadingImage = new ImageView(this);
//        loadingBackground.setImageResource(R.drawable.loading_b);
//        loadingImage.setImageResource(R.drawable.loading_i);


//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//
//                    } catch (Exception e) {
//                        Reporter.report(context, e, reportSubject);
//                    }
//                }
//            }).start();
//


            if (InstanceController.getObject("Profile") == null) {
                if (!prepare()) {
                    UI.makeErrorMessage(context, "Ошибка загрузка профиля");
                    return;
                }
                restoring = false;
            } else {
                restoring = true;
            }

//            getBackStack().addState(StateTags.MAIN_ACTIVITY);

            ui = new UI(context, restoring);


            ui.mainScroll.addOnScreenSwitchedListener(this);

            stopwatch.checkpoint("MainActivity_onCreate_finish");
            Log.d("ActivityState", "onCreate");

            MathJaxPreparer.prepare(this);

        } catch (Exception e) {
            Reporter.report(this, e, reportSubject);
        }

    }

    int getStatusBarHeight() {
        int result = 0;
        int resID  = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resID > 0) {
            result = getResources().getDimensionPixelSize(resID);
        }
        return result;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("ActivityState", "onStart");

//        setBootScreen();


    }

//    void setBootScreen() {
//        int h = getResources().getDisplayMetrics().heightPixels;
//        int w = getResources().getDisplayMetrics().widthPixels;
//        int h_i = (int) (0.8 * h);
//        int w_i = (int) (0.72 * h_i);
//
//        if (w / (double) h < 0.75) {
//            w = (int) (3 / 4.0 * h);
//        } else if (w / (double) h > 0.75) {
//            h = (int) (4 / 3.0 * w);
//        }
//        loadingBackground.setLayoutParams(new ViewGroup.LayoutParams(w, h));
//        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w_i, h_i);
//        lp.gravity = Gravity.CENTER;
//        loadingImage.setLayoutParams(lp);
//
//        try {
//            ((ViewGroup) getWindow().getDecorView().getRootView()).addView(loadingBackground);
//            ((ViewGroup) getWindow().getDecorView().getRootView()).addView(loadingImage);
//        } catch (Exception e) {
//            ((ViewGroup) getWindow().getDecorView().getRootView()).removeView(loadingBackground);
//            ((ViewGroup) getWindow().getDecorView().getRootView()).removeView(loadingImage);
//            ((ViewGroup) getWindow().getDecorView().getRootView()).addView(loadingBackground);
//            ((ViewGroup) getWindow().getDecorView().getRootView()).addView(loadingImage);
//        }
//
//    }

    private void setAccountInfo() {

        Account[] acc = AccountManager.get(this).getAccountsByType("com.google");
        if (acc.length == 0) {
            Toast.makeText(this, R.string.error_acc, Toast.LENGTH_SHORT).show();
        } else {
            PID = acc[0].name;
            Toast.makeText(this, "Используется аккаунт: " + PID, Toast.LENGTH_LONG).show();
        }
        reportSubject = "ErrorReport_" + PID;


    }

    public void clearBackStack() {
        backStack.clear();
    }

    boolean prepare() {

        stopwatch.checkpoint("prepare");
        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll()
                .build();
        StrictMode.setThreadPolicy(policy);


        new Thread(new Runnable() {
            @Override
            public void run() {
                profileIsLoading = true;

                try {

                    if (profile == null) {
                        profile = new Gson().fromJson(DataLoader.getProfile(PID), Profile.class);

                    }
                    stopwatch.checkpoint("prepare_3");

                    profile.ID = PID;
//                    profile.prepareData();
                    for (Profile.OnLoadCompleted onLoadCompleted : onLoadCompletedList) {
                        onLoadCompleted.onCompleted(restoring);
                    }


                } catch (ConnectException ce) {

                    profileIsLoading = false;

                    return;

                } catch (Exception e) {
                    // TODO: 09.10.16 вывод ошибки о неверных данных
                    e.printStackTrace();
                    profileIsLoading = false;

                    return;
                }
                profileIsLoading = false;
            }
        }).start();


        try {
            InstanceController.putObject("WIFI_only", false);
        } catch (InstanceController.NotInitializedError notInitializedError) {
            notInitializedError.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //prepare image for sharing
                    File sharefile = new File(context.getApplicationContext().getExternalCacheDir(),
                            "share_image.png");
                    if (!sharefile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.share_image);
                        FileOutputStream out = new FileOutputStream(sharefile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
        stopwatch.checkpoint("prepare_finish");
        return true;
    }

    public boolean isProfileIsLoading() {
        return profileIsLoading;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d("ActivityState", "onSaveInstanceState_1");
        try {
            InstanceController.putObject("Profile", profile);
        } catch (InstanceController.NotInitializedError notInitializedError) {
            notInitializedError.printStackTrace();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("ActivityState", "onRestoreInstanceState_1");
        restoring = true;
        profile = (Profile) InstanceController.getObject("Profile");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ui.updateSizes(newConfig.orientation);
        for (OnConfigurationUpdateListener onConfigurationUpdateListener : configurationUpdatesList) {
            onConfigurationUpdateListener.onConfigurationUpdate(newConfig);
        }
    }

    public void addOnConfigurationUpdateListener(OnConfigurationUpdateListener
                                                         onConfigurationUpdateListener) {
        if (onConfigurationUpdateListener != null) {
            configurationUpdatesList.add(onConfigurationUpdateListener);
        }
    }

    @Override
    protected void onDestroy() {

        ui = null;
        profile = null;
        InstanceController.clear();
        backStack = null;
//        System.exit(0);

        Log.d("ActivityState", "onDestroy");
        super.onDestroy();

    }

    public void addToBackStack(Runnable action) {
        backStack.addAction(action);
//        backStack.push(action);
    }

    public void removeLastBackStackAction() {
        backStack.removeLastAction();
    }

    public BackStack getBackStack() {
        return backStack;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if ((backStack.size() > 1)) {
            backStack.pop().run();
        } else {
            if (pressAgain) {
                Toast.makeText(context, "Для выхода нажмите еще раз!",
                        Toast.LENGTH_SHORT).show();
                pressAgain = false;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            pressAgain = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {
                super.onBackPressed();
                onDestroy();
            }
        }


        // TODO: 15.08.16 стэк возврата
    }

    public void updateCoins(Integer coins) {
        profile.Coins += coins;
        ui.updateCoins();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_HOME) {
            ui.videoListFragment.stopVideos();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("ActResult", "onActivityResult(" + requestCode + "," + resultCode + ","
                           + data);

        // Pass on the activity result to the helper for handling
        if (!pays.mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d("ActResult", "onActivityResult handled by IABUtil.");
        }
    }

    public void addOnProfileLoadingCompleted(Profile.OnLoadCompleted onLoadCompleted) {
        this.onLoadCompletedList.add(onLoadCompleted);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("ActivityState", "onPause");
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState,
                                       PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        Log.d("ActivityState", "onRestoreInstanceState_2");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.d("ActivityState", "onPostCreate");
        stopwatch.checkpoint("Pays_start");
        pays = new Pays(context);
        stopwatch.checkpoint("Pays_ready");
        stopwatch.finish("onPostCreate");
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        Log.d("ActivityState", "onPostCreate_2");
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d("ActivityState", "onRestart");
    }

    @Override
    public void onStateNotSaved() {
        super.onStateNotSaved();
        Log.d("ActivityState", "onStateNotSaved");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ActivityState", "onResume");

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d("ActivityState", "onPostResume");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d("ActivityState", "onStop");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d("ActivityState", "onLowMemory");
//        onDestroy();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d("ActivityState", "onTrimMemory");
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        Log.d("ActivityState", "onSaveInstanceState_2");
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        Log.d("ActivityState", "onAttachFragment");
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        Log.d("ActivityState", "onActivityReenter");
    }

    @Override
    public void finish() {
        Log.d("ActivityState", "finish");
        super.finish();
    }

    @Override
    public void finishAffinity() {
        Log.d("ActivityState", "finishAffinity");
        super.finishAffinity();
    }

    @Override
    public void switchedRight() {
        if (ui.taskListFragment.getCurrentTask() == null) {
            ui.taskListFragment.chooseTask(0);
        }

        addToBackStack(new Runnable() {
            @Override
            public void run() {
                ui.mainScroll.toLeft();
            }
        });
    }

    @Override
    public void switchedLeft() {

    }

    public interface OnConfigurationUpdateListener {
        void onConfigurationUpdate(Configuration configuration);
    }


}
