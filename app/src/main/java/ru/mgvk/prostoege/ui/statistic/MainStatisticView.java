package ru.mgvk.prostoege.ui.statistic;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
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

    private static final int COLOR_TEXT = Color.BLACK;
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
        setPointsAndScaleBtn();
        setPlot();
//        setTestButton();
        setRepetitionBtn();
//        setChangeScaleButton();
        setTaskLabel();
    }

    private void setChangeScaleButton(LinearLayout ll) {
        scaleButton = new Button(context);
        scaleButton.setText(R.string.scale_show_all);
//        scaleButton.setTextSize(18);
        scaleButton.setTransformationMethod(null);
        scaleButton.setBackgroundResource(R.drawable.bg_repetition_btn);
        scaleButton.setTextColor(Color.BLACK);
        scaleButton.setPadding(0, 0, 0, 0);

        int                       p  = 4;
        LinearLayout.LayoutParams lp = new LayoutParams(-1, UI.calcSize(40), 2);
        lp.gravity = Gravity.CENTER;
//        lp.setMargins(0, p, 0, 0);
        scaleButton.setLayoutParams(lp);

        scaleButton.setTag(false); // showAll=false;
        ll.addView(scaleButton);
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
                //showLast == true => making it false
                scaleButton.setText(R.string.scale_show_all);
            } else {
                scaleButton.setText(R.string.scale_show_last);
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
//        repetitionButton.setPadding(p, 0, p, 0);
//        repetitionButton.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        repetitionButton.setTextSize(20);
        repetitionButton.setPadding(0, 0, 0, 0);
        repetitionButton.setBackgroundResource(R.drawable.bg_repetition_btn);
        repetitionButton.setTransformationMethod(null);
        repetitionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UI.openRequestDialog(context.getString(R.string.repetition_start_request_title),
                        context.getString(R.string.repetition_start_request_text),
                        new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                UI.openRepetitionActivity();
                            }
                        }, null);
            }
        });
        LinearLayout.LayoutParams lp = new LayoutParams(-1, UI.calcSize(40));
//        lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
//        lp.setMargins(0, p, 0, 0);
        repetitionButton.setLayoutParams(lp);

        addView(repetitionButton);
    }

    private void setPointsAndScaleBtn() {
        desiredPointsText = new TextView(context);
        desiredPointsText.setText(R.string.statistic_main_desired);
        desiredPointsText.setTextSize(pointsTextSize);
        desiredPointsText.setGravity(Gravity.LEFT);
        desiredPointsText.setTextColor(COLOR_TEXT);

        currentPointsText = new TextView(context);
        currentPointsText.setText(R.string.statistic_main_current);
        currentPointsText.setTextSize(pointsTextSize);
        currentPointsText.setGravity(Gravity.LEFT);
        currentPointsText.setTextColor(COLOR_TEXT);


        LinearLayout l = new LinearLayout(context);
        l.setOrientation(VERTICAL);
        l.addView(desiredPointsText);
        l.addView(currentPointsText);
        l.setLayoutParams(new LayoutParams(-1, -2, 1));

        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(HORIZONTAL);
        ll.setLayoutParams(new LayoutParams(-1, -2));
        ll.addView(l);

        setChangeScaleButton(ll);

        addView(ll);
    }

    private void setTitle() {
        statisticTitle = new TextView(context);
        statisticTitle.setText(R.string.statistic_main_title);
        statisticTitle.setTextColor(COLOR_TEXT);
        statisticTitle.setTextSize(titleTextSize);
        statisticTitle.setTypeface(statisticTitle.getTypeface(), Typeface.BOLD);
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
        taskLabel.setTypeface(taskLabel.getTypeface(), Typeface.BOLD);

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
