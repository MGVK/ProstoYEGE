package ru.mgvk.prostoege;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentCallbacks2;
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
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.gson.Gson;
import ru.mgvk.prostoege.ui.UI;
import ru.mgvk.util.Reporter;
import ru.mgvk.util.Stopwatch;

import java.io.File;
import java.io.FileOutputStream;
import java.net.ConnectException;
import java.util.Stack;

public class MainActivity extends Activity {

    static final String APP_SETTINGS = "SETTINGS_EGE";
    public static String PID = "default";
    private final String TAG = "ActivityState";
    public UI ui;
    public volatile Profile profile;
    public long TIME = 0;
    public Pays pays;
    public Stopwatch stopwatch;
    public String reportSubject;
    Context context;
    OnConfigurationUpdate onConfigurationUpdate;
    private boolean restoring = false;
    private Stack<Runnable> backStack = new Stack<>();

    // TODO: 10.08.16 user-friendly ошибки

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
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


            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        stopwatch.checkpoint("Pays_start");
                        pays = new Pays(context);
                        stopwatch.checkpoint("Pays_ready");
                    } catch (Exception e) {
                        Reporter.report(context, e, reportSubject);
                    }
                }
            }).start();


            if (InstanceController.getObject("Profile") == null) {
                if (!prepare()) {
                    UI.makeErrorMessage(context, "Ошибка загрузка профиля");
                    return;
                }
                restoring = false;
            } else {
                restoring = true;
            }


            ui = new UI(context, restoring);

            stopwatch.checkpoint("MainActivity_onCreate_finish");
            Log.d(TAG, "onCreate");

        } catch (Exception e) {
            Reporter.report(this, e, reportSubject);
        }

    }

    int getStatusBarHeight() {
        int result = 0;
        int resID = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resID > 0) {
            result = getResources().getDimensionPixelSize(resID);
        }
        return result;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

//        setBootScreen();

    }


    private void setAccountInfo() {

        Account[] acc = AccountManager.get(this).getAccountsByType("com.google");
        if (acc.length == 0) {
            Toast.makeText(this, R.string.error_acc, Toast.LENGTH_SHORT).show();
        } else {
            PID = acc[0].name;
            Toast.makeText(this, "Используется аккаунт: " + PID, Toast.LENGTH_LONG).show();
        }
        reportSubject = PID;


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

    public void clearBackStack() {
        backStack.clear();
    }


    boolean prepare() {
        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        long t = System.currentTimeMillis();

        try {

            profile = new Gson().fromJson(DataLoader.getProfile(PID), Profile.class);

            profile.ID = PID;
            profile.prepareData();


        } catch (ConnectException ce) {

            return false;

        } catch (Exception e) {
            // TODO: 09.10.16 вывод ошибки о неверных данных
            e.printStackTrace();
            return false;
        }

        try {
            InstanceController.putObject("WIFI_only", false);
        } catch (InstanceController.NotInitializedError notInitializedError) {
            notInitializedError.printStackTrace();
            new InstanceController();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //prepare image for sharing

                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.share_image);
                    File sharefile = new File(context.getApplicationContext().getExternalCacheDir(), "share_image.png");
                    FileOutputStream out = new FileOutputStream(sharefile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

        Log.d("time", (TIME = System.currentTimeMillis()) - t + "");

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "onSaveInstanceState_1");
        try {
            InstanceController.putObject("Profile", profile);
        } catch (InstanceController.NotInitializedError notInitializedError) {
            notInitializedError.printStackTrace();
        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState_1");
        restoring = true;
        profile = (Profile) InstanceController.getObject("Profile");
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ui.updateSizes(newConfig.orientation);
        if (onConfigurationUpdate != null) {
            onConfigurationUpdate.onUpdate();
        }
    }

    @Override
    protected void onDestroy() {

        ui = null;
        profile = null;
        InstanceController.clear();
        backStack = null;
//        System.exit(0);

        Log.d(TAG, "onDestroy");
        super.onDestroy();

    }

    public void addToBackStack(Runnable action) {
        backStack.push(action);
    }

    public void removeLastBackStackAction() {
        backStack.removeElement(backStack.lastElement());
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (!backStack.empty()) {
            backStack.pop().run();
        } else {
            super.onBackPressed();
            onDestroy();
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

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "onPause");
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        Log.d(TAG, "onRestoreInstanceState_2");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.d(TAG, "onPostCreate");
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        Log.d(TAG, "onPostCreate_2");
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d(TAG, "onRestart");
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyLongPress: ");
        return super.onKeyLongPress(keyCode, event);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp: ");
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        Log.d(TAG, "onKeyMultiple: ");
        return super.onKeyMultiple(keyCode, repeatCount, event);
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyShortcut: ");
        return super.onKeyShortcut(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent: ");
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        Log.d(TAG, "onTrackballEvent: ");
        return super.onTrackballEvent(event);
    }

    @Override
    public void onUserInteraction() {
        Log.d(TAG, "onUserInteraction: ");
        super.onUserInteraction();
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        Log.d(TAG, "onWindowAttributesChanged: ");
        super.onWindowAttributesChanged(params);
    }

    @Override
    public void onContentChanged() {
        Log.d(TAG, "onContentChanged: ");
        super.onContentChanged();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (ui != null) {
            ui.onLeave();
        }
        Log.d(TAG, "onWindowFocusChanged: ");
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onAttachedToWindow() {
        Log.d(TAG, "onAttachedToWindow: ");
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow: ");
        super.onDetachedFromWindow();
    }

    @Override
    public void onStateNotSaved() {
        super.onStateNotSaved();
        Log.d(TAG, "onStateNotSaved");
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (ui != null) {
            ui.onLeave();
        }
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {

        Log.d(TAG, "onGenericMotionEvent: " + event.getActionIndex());

        return super.onGenericMotionEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d(TAG, "onPostResume");
        try {
            InstanceController.putObject("LoadingCompleted", "");
        } catch (InstanceController.NotInitializedError notInitializedError) {
            notInitializedError.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory");
//        onDestroy();
    }


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d(TAG, "onTrimMemory " + level);
        if (/*level== ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL
                ||*/level == ComponentCallbacks2.TRIM_MEMORY_COMPLETE
                || level == ComponentCallbacks2.TRIM_MEMORY_MODERATE
                || level == ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            finish();
            System.exit(0);
        }
//        if(level>=80) {
//            System.exit(0);
//        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        Log.d(TAG, "onSaveInstanceState_2");
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        Log.d(TAG, "onAttachFragment");
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        Log.d(TAG, "onActivityReenter");
    }

    @Override
    public void finish() {
        Log.d(TAG, "finish");
        super.finish();
    }

    @Override
    public void finishAffinity() {
        Log.d(TAG, "finishAffinity");
        super.finishAffinity();
    }

    public interface OnConfigurationUpdate {
        void onUpdate();
    }


}
