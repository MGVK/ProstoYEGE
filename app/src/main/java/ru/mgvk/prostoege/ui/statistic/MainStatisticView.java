package ru.mgvk.prostoege.ui.statistic;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import ru.mgvk.prostoege.DataLoader;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;
import ru.mgvk.prostoege.fragments.RepetitionFragmentLeft;
import ru.mgvk.prostoege.ui.UI;

/**
 * Created by mike on 24.07.17.
 */
public class MainStatisticView extends LinearLayout {

    private static OnScaleButtonClickListener onScaleButtonClickListener;
    private final int titleTextSize = 22;
    StatisticPlot plot;
    private int desiredPoints = 0;
    private int currentPoints = 0;
    private TextView statisticTitle, desiredPointsText, currentPointsText, taskLabel;
    private Context context;
    private int pointsTextSize = 18;
    private Button   repetitionButton;
    private EditText editText;
    private Button   scaleButton;

    public MainStatisticView(Context context) {
        super(context);
        init();
    }

    public MainStatisticView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MainStatisticView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MainStatisticView(Context context, AttributeSet attrs, int defStyleAttr,
                             int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public static void setOnScaleButtonClickListener(
            OnScaleButtonClickListener onScaleButtonClickListener) {
        MainStatisticView.onScaleButtonClickListener = onScaleButtonClickListener;
    }

    void init() {

        context = getContext();
        initViews();

//        initTestData();
        initData();

        setCurrentPoints(0);
        setDesiredPoints(0);

    }

    private void initData() {
        DataLoader.setOnStatisticLoadingCompleteListener(
                new DataLoader.OnStatisticLoadingCompleteListener() {
                    @Override
                    public void onLoadCompleted(StatisticData[] statisticData) {
                        try {
                            Thread.currentThread().sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        for (int i = statisticData.length - 1; i >= 0; i--) {
                            addResult(new RepetitionFragmentLeft.Result(statisticData[i]));
                        }

//                        for (StatisticData statisticDatum : statisticData) {
//                            addResult(new RepetitionFragmentLeft.Result(statisticDatum));
//                        }
                    }
                });
    }

    private void initTestData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((MainActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        plot.addColumn(new RepetitionFragmentLeft.Result(0, 0));
                        plot.addColumn(new RepetitionFragmentLeft.Result(25, 50));
                        plot.addColumn(new RepetitionFragmentLeft.Result(50, 100));
                    }
                });

            }
        }).start();
    }

    private void initViews() {

        setTitle();
        setPoints();
        setPlot();
//        setTestButton();
        setChangeScaleButton();
        setRepetitionBtn();
        setTaskLabel();
    }

    private void setChangeScaleButton() {
        scaleButton = new Button(context);
        scaleButton.setText("Показать все");
        scaleButton.setLayoutParams(new LayoutParams(-1, UI.calcSize(50)));
        scaleButton.setTag(false); // commonView=false;
        addView(scaleButton);
        scaleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                changeScaleButtonLabel();

                if (onScaleButtonClickListener != null) {
                    onScaleButtonClickListener.onClick(scaleButton);
                }
            }
        });

    }

    public void addResult(RepetitionFragmentLeft.Result result) {
        plot.addColumn(result);
    }

    private void changeScaleButtonLabel() {
        if (scaleButton.getTag() instanceof Boolean) {
            if ((Boolean) scaleButton.getTag()) {
                //commonView == true => making it false
                scaleButton.setText("Показать последние");
            } else {
                scaleButton.setText("Показать все");
            }
            scaleButton.setTag(!(Boolean) scaleButton.getTag());
        }
    }

    private void setTestButton() {
        Button button = new Button(context);
        button.setText("Добавить результат");
        button.setLayoutParams(new LayoutParams(-1, UI.calcSize(50)));
        addView(button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                editText = new EditText(context);
                editText.setText("" + (int) (Math.random() * 100));

                new AlertDialog.Builder(context)
                        .setTitle("Введите результат (из 100)")
                        .setView(editText)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    int r = Integer.valueOf(String.valueOf
                                            (editText.getText()));
                                    plot.addColumn(new RepetitionFragmentLeft.Result(0, r));

                                    Toast.makeText(context, "Добавлено: " + r, Toast.LENGTH_SHORT)
                                            .show();
                                } catch (Exception e) {

                                    Toast.makeText(context, "Неверное значение :(: ", Toast
                                            .LENGTH_SHORT)
                                            .show();
                                }
                            }
                        })
                        .create().show();

            }
        });
    }

    private void setRepetitionBtn() {
        repetitionButton = new Button(context);
        repetitionButton.setText(R.string.statistic_main_repbtn);
        int p = UI.calcSize(10);
//        repetitionButton.setPadding(p, 0, p, 0);
        repetitionButton.setGravity(Gravity.CENTER);
        repetitionButton.setBackgroundResource(R.drawable.task_back_1);
        repetitionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).ui.openRepetitionFragment();

            }
        });
        addView(repetitionButton);

//        LayoutParams lp = (LayoutParams) repetitionButton.getLayoutParams();
        LinearLayout.LayoutParams lp = new LayoutParams(-2, -2);
        lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        lp.setMargins(p, 0, p, 0);
//        lp.width = -2;
//        lp.height = -2;
        repetitionButton.setLayoutParams(lp);
    }

    private void setPoints() {
        desiredPointsText = new TextView(context);
        desiredPointsText.setText(R.string.statistic_main_desired);
        desiredPointsText.setTextSize(pointsTextSize);
        desiredPointsText.setGravity(Gravity.CENTER);

        currentPointsText = new TextView(context);
        currentPointsText.setText(R.string.statistic_main_current);
        currentPointsText.setTextSize(pointsTextSize);
        currentPointsText.setGravity(Gravity.CENTER);

        addView(desiredPointsText);
        addView(currentPointsText);
    }

    private void setTitle() {
        statisticTitle = new TextView(context);
        statisticTitle.setText(R.string.statistic_main_title);
        statisticTitle.setTextColor(Color.BLACK);
        statisticTitle.setTextSize(titleTextSize);
        addView(statisticTitle);
        LayoutParams p = (LayoutParams) statisticTitle.getLayoutParams();
        p.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        statisticTitle.setLayoutParams(p);
    }

    private void setTaskLabel() {
        taskLabel = new TextView(context);
        taskLabel.setText(R.string.statistic_main_tasklabel);
        taskLabel.setTextColor(Color.BLACK);
        taskLabel.setTextSize(titleTextSize);
        addView(taskLabel);
        LayoutParams p = (LayoutParams) taskLabel.getLayoutParams();
        p.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        taskLabel.setLayoutParams(p);

    }

    private void setPlot() {
        plot = new StatisticPlot(context);
        addView(plot);

    }

    public int getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints(int currentPoints) {
        this.currentPoints = currentPoints;
        currentPointsText
                .setText(context.getText(R.string.statistic_main_current) + " " + currentPoints);
    }

    public int getDesiredPoints() {
        return desiredPoints;
    }

    public void setDesiredPoints(int desiredPoints) {
        this.desiredPoints = desiredPoints;
        desiredPointsText
                .setText(context.getText(R.string.statistic_main_desired) + " " + desiredPoints);
    }


    public interface OnScaleButtonClickListener {
        void onClick(Button scaleButton);
    }
}
