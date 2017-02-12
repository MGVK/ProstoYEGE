package ru.mgvk.prostoege.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.mgvk.prostoege.DataLoader;
import ru.mgvk.prostoege.InstanceController;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;

/**
 * Created by mihail on 08.10.16.
 */
public class MainMenu extends MenuPanel implements OnClickListener {

    private ViewGroup parent;
    Context context;
    MainActivity mainActivity;
    private MenuItem balanceBtn;
    private MenuItem greetingBtn;
    private MenuItem shareBtn;
    private MenuItem ratingBtn;
    private MenuItem wifiBtn;
    private MenuItem orientationBtn;
    private MenuItem restoreBtn;


    public MainMenu(Context context,ViewGroup parent) {
        super(context);
        mainActivity = (MainActivity) (this.context = context);
        this.parent = parent;
        init();
    }

    public void updateSizes(int w, int h){
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int)(0.4*w),(int)(0.4*w));
        shareBtn.setLayoutParams(lp);
        ratingBtn.setLayoutParams(lp);
        wifiBtn.setLayoutParams(lp);
        orientationBtn.setLayoutParams(lp);
        restoreBtn.setLayoutParams(lp);
        balanceBtn.setLayoutParams(lp);
    }

    void init() {

        setVisibility(View.INVISIBLE);

        setOnBackClickListener(new MenuPanel.OnBackClickListener() {
            @Override
            public void onClick(MenuPanel menu) {
                mainActivity.ui.closeMenu(menu);
            }
        });

        balanceBtn = new MenuItem(context);
        balanceBtn.setImage(mainActivity.getResources().getDrawable(R.drawable.item_balance));
//        balanceBtn.setText(R.string.btn_balance);
        balanceBtn.setOnClickListener(this);
        addItem(balanceBtn);

        restoreBtn = new MenuItem(context);
        restoreBtn.setImage(mainActivity.getResources().getDrawable(R.drawable.item_restore));
        restoreBtn.setText(R.string.btn_restore);
        restoreBtn.setOnClickListener(this);
        addItem(restoreBtn);

//        greetingBtn = new MenuItem(context);
//        greetingBtn.setImage(mainActivity.getResources().getDrawable(R.drawable.ti_1));
//        greetingBtn.setText(R.string.btn_greeting);
//        greetingBtn.setOnClickListener(this);
//        addItem(greetingBtn);

        shareBtn = new MenuItem(context);
        shareBtn.setImage(mainActivity.getResources().getDrawable(R.drawable.item_share));
        shareBtn.setText(R.string.btn_share);
        shareBtn.setOnClickListener(this);
        addItem(shareBtn);

        ratingBtn = new MenuItem(context);
        ratingBtn.setImage(mainActivity.getResources().getDrawable(R.drawable.item_rate));
        ratingBtn.setText(R.string.btn_rate);
        ratingBtn.setOnClickListener(this);
        addItem(ratingBtn);


        wifiBtn = new MenuItem(context);
        wifiBtn.setImage(mainActivity.getResources().getDrawable(R.drawable.item_wifi_off));
        wifiBtn.setText(R.string.btn_wifi);
        wifiBtn.setOnClickListener(this);
        addItem(wifiBtn);

        orientationBtn = new MenuItem(context);
        orientationBtn.setImage(mainActivity.getResources().getDrawable(R.drawable.item_orientation));
        orientationBtn.setText(R.string.btn_orientation);
        orientationBtn.setOnClickListener(this);
        addItem(orientationBtn);

        parent.addView(this);
    }

    @Override
    public void onClick(View v) {

        if (v == balanceBtn) {
            mainActivity.ui.openBalanceDialog();
        }
//        if (v == greetingBtn) {
//
//        }
        if (v == shareBtn) {
            try {
                //Рабочий вариант
//                Intent intent = new Intent(Intent.ACTION_SEND);
//                intent.setType("text/plain");
////                intent.setPackage("com.vkontakte.android");
//                intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_description));
//                context.startActivity(intent);

                String text = context.getString(R.string.share_description);
                File sharefile = new File(context.getApplicationContext().getExternalCacheDir(),
                        "share_image.png");
                Intent share = new Intent(android.content.Intent.ACTION_SEND)
                        .setType("image/*")
                        .putExtra(Intent.EXTRA_STREAM,
                                Uri.parse("file://" + sharefile))
                        .putExtra(Intent.EXTRA_TEXT, text);
                context.startActivity(Intent.createChooser(share,"Поделитесь с друзьями!"));

            }catch (Exception e){
                e.printStackTrace();
            }

            if(mainActivity.profile.Repost != 1) {
                mainActivity.updateCoins(55);
                try {
                    DataLoader.putRepost();
                } catch (Exception e) {
                   mainActivity.ui.makeErrorMessage("Ошибка сервера: putRepost");
                }
                mainActivity.profile.Repost = 1;
            }
        }
        if (v == ratingBtn) {
            try {
                context.startActivity(
                        new Intent(Intent.ACTION_VIEW)
                                .setData(Uri.parse("market://details?id=ru.mgvk.prostoege"))
                                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                                        |Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if (v == wifiBtn) {
            try {
                if((boolean)InstanceController.getObject("WIFI_only")) {
                    InstanceController.putObject("WIFI_only", false);
                    wifiBtn.setImage(mainActivity.getResources().getDrawable(R.drawable.item_wifi_off));
                }else{
                    InstanceController.putObject("WIFI_only", true);
                    wifiBtn.setImage(mainActivity.getResources().getDrawable(R.drawable.item_wifi_on));
                }

            } catch (InstanceController.NotInitializedError notInitializedError) {
                notInitializedError.printStackTrace();
            }
        }
        if (v == orientationBtn) {
            if(context.getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_PORTRAIT){
            mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }else{
                mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

        }
        if (v == restoreBtn) {
            mainActivity.ui.taskListFragment.restoreTasks();
        }

    }
}
