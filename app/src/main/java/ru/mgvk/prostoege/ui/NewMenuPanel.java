package ru.mgvk.prostoege.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ru.mgvk.prostoege.DataLoader;
import ru.mgvk.prostoege.InstanceController;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;

import java.io.File;

public class NewMenuPanel extends DialogWindow {

    private final FrameLayout mainLayout;
    OnClickListener listener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.balance: {
                    ((MainActivity) getContext()).ui.openBalanceDialog();
                    break;
                }
                case R.id.statistic: {
                    break;
                }
                case R.id.share: {
                    try {
                        //Рабочий вариант
                        String text = getContext().getString(R.string.share_description);
                        File sharefile = new File(
                                getContext().getApplicationContext().getExternalCacheDir(),
                                "share_image.png");
                        Intent share = new Intent(android.content.Intent.ACTION_SEND)
                                .setType("image/*")
                                .putExtra(Intent.EXTRA_STREAM,
                                        Uri.parse("file://" + sharefile))
                                .putExtra(Intent.EXTRA_TEXT, text);
                        getContext()
                                .startActivity(
                                        Intent.createChooser(share, "Поделитесь с друзьями!"));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    MainActivity mainActivity = ((MainActivity) getContext());
                    if (mainActivity.profile.Repost != 1) {
                        mainActivity.updateCoins(55);
                        try {
                            DataLoader.putRepost();
                        } catch (Exception e) {
                            UI.makeErrorMessage(getContext(), "Ошибка сервера: putRepost");
                        }
                        mainActivity.profile.Repost = 1;
                    }
                    break;
                }
                case R.id.rate: {
                    try {
                        getContext().startActivity(
                                new Intent(Intent.ACTION_VIEW)
                                        .setData(Uri.parse("market://details?id=ru.mgvk.prostoege"))
                                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                                                  | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                }
                case R.id.restore: {
                    ((MainActivity) getContext()).ui.taskListFragment.restoreTasks();
                    break;
                }
                case R.id.wifi: {
                    try {
                        if ((boolean) InstanceController.getObject("WIFI_only")) {
                            InstanceController.putObject("WIFI_only", false);
                            ((TextView) v).setTypeface(Typeface.DEFAULT_BOLD);
                        } else {
                            InstanceController.putObject("WIFI_only", true);
                            ((TextView) v).setTypeface(Typeface.DEFAULT);
                        }

                    } catch (InstanceController.NotInitializedError notInitializedError) {
                        notInitializedError.printStackTrace();
                    }
                    break;
                }
                case R.id.btn_close: {
                    close();
                    break;
                }
                default: {
                    break;
                }
            }

        }
    };

    public NewMenuPanel(Context context) {
        super(context);
        mainLayout = (FrameLayout) LayoutInflater.from(context).inflate(R.layout
                .activity_menu, null);
        mainLayout.setLayoutParams(new RelativeLayout.LayoutParams(-1, UI.calcSize(385)));
        mainLayout.findViewById(R.id.balance).setOnClickListener(listener);
        mainLayout.findViewById(R.id.statistic).setOnClickListener(listener);
        mainLayout.findViewById(R.id.share).setOnClickListener(listener);
        mainLayout.findViewById(R.id.rate).setOnClickListener(listener);
        mainLayout.findViewById(R.id.wifi).setOnClickListener(listener);
        mainLayout.findViewById(R.id.restore).setOnClickListener(listener);
        mainLayout.findViewById(R.id.btn_close).setOnClickListener(listener);
        mainLayout.setOnClickListener(listener);

        setGravity(Gravity.BOTTOM);
        addView(mainLayout);
    }
}
