package ru.mgvk.prostoege.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import ru.mgvk.prostoege.InstanceController;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;
import ru.mgvk.util.Reporter;

/**
 * Created by mihail on 08.10.16.
 */
public class SettingsMenu extends MenuPanel implements View.OnClickListener {

    Context context;
    MainActivity mainActivity;
    private MenuItem restoreBtn;
    private MenuItem wifiBtn;
    private MenuItem orientationBtn;
    private ViewGroup parent;

    public SettingsMenu(Context context) {
        this(context, null);
    }

    public SettingsMenu(Context context,ViewGroup parent) {
        super(context);
        if(parent==null){
            throw new NullPointerException("Menu's parent is not initialized!!!");
        }
        mainActivity = (MainActivity) (this.context = context);
        this.parent = parent;
        init();
    }


    void init() {

        setVisibility(View.INVISIBLE);

        setOnBackClickListener(new OnBackClickListener() {
            @Override
            public void onClick(MenuPanel menu) {
                try {
                    mainActivity.ui.closeMenu(menu);
                } catch (Exception e) {
                    Reporter.report(context, e, ((MainActivity) context).reportSubject);
                }
            }
        });

        restoreBtn = new MenuItem(context);
        restoreBtn.setImage(mainActivity.getResources().getDrawable(R.drawable.ti_1));
        restoreBtn.setText(R.string.btn_restore);
        restoreBtn.setOnClickListener(this);
        addItem(restoreBtn);

        wifiBtn = new MenuItem(context);
        wifiBtn.setImage(mainActivity.getResources().getDrawable(R.drawable.ti_1));
        wifiBtn.setText(R.string.btn_wifi);
        wifiBtn.setOnClickListener(this);
        addItem(wifiBtn);

        orientationBtn = new MenuItem(context);
        orientationBtn.setImage(mainActivity.getResources().getDrawable(R.drawable.ti_1));
        orientationBtn.setText(R.string.btn_orientation);
        orientationBtn.setOnClickListener(this);
        addItem(orientationBtn);

        parent.addView(this);
    }


    @Override
    public void onClick(View v) {
        try {

            if (v == restoreBtn) {
                mainActivity.ui.taskListFragment.restoreTasks();
            }
            if (v == wifiBtn) {
                try {
                    InstanceController.putObject("WIFI_only",!(boolean)InstanceController.getObject("WIFI_only"));
                } catch (InstanceController.NotInitializedError notInitializedError) {
                    notInitializedError.printStackTrace();
                }
            }
            if (v == orientationBtn) {
            }

        } catch (Exception e) {
            Reporter.report(context, e, ((MainActivity) context).reportSubject);
        }
    }


}
