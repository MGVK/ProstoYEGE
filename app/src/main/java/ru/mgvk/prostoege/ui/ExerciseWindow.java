package ru.mgvk.prostoege.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.*;
import ru.mgvk.prostoege.*;
import ru.mgvk.util.Reporter;

/**
 * Created by mihail on 16.10.16.
 */
public class ExerciseWindow extends FrameLayout implements View.OnClickListener {

    public final static byte NOT_DECIDED = 0, WRONG_ANSWER = 1,
            PROMPTED = 2, DECIDED_FIRSTLY = 3,
            ANSWER_SHOWED = 4, DECIDED_SECONDLY = 5;
    public final static int[] indicators = {R.drawable.button_yellow, R.drawable.button_red,
            R.drawable.button_blue, R.drawable.button_green,
            R.drawable.button_red, R.drawable.button_blue};
    private static final boolean APPEAR = true, DISAPPEAR = false;

    int QUESTION_ID = 0;
    int Number = 0;
    LinearLayout mainLayout;
    Context context;
    private String answer = "", separator = "&";
    private LinearLayout answerLayout;
    private TextView answerTextView;
    private ImageButton answerClearButton;
    private int status = 0;
    private TitleLayout titleLayout;
    private DescriptionWebView description;
    private Task.Exercise currentExercise;
    private boolean opened = false;

    public ExerciseWindow(Context context) {
        super(context);
        this.context = context;
        initViews();
    }

    public void openExercise(Task.Exercise exercise) {
        if (exercise == null) {
            close();
            return;
        }
        initParams((currentExercise = exercise).getData());
        titleLayout.setNumber(Number);
        setStatus(exercise.getStatus());
        answerTextView.setText(exercise.getTmpText());
        description.reloadDescription();
        open();
        ((MainActivity) context).addToBackStack(new Runnable() {
            @Override
            public void run() {
                closeExercise();
            }
        });
    }

    protected void open() {
        opened = true;
        animateAlpha(APPEAR);
    }


    protected void animateAlpha(boolean type) {
        ObjectAnimator a = new ObjectAnimator();
        a.setTarget(this);
        a.setPropertyName("alpha");
        if (type) {
            setAlpha(0);
            setVisibility(VISIBLE);
            a.setFloatValues(0, 1);
        } else {
            a.setFloatValues(1, 0);
            a.addListener(new Animator.AnimatorListener() {


                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        a.setDuration(150);
        a.start();
    }

    private void initParams(Profile.TaskData.ExercizesData data) {
        this.QUESTION_ID = data.ID;
        answer = data.Answer;
        Number = data.Number;
    }

    public void close() {
        opened = false;
        animateAlpha(DISAPPEAR);

    }

    public void closeExercise() {
        close();
    }

    void openNextExercise() {
//        closeExercise();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((MainActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            openExercise(currentExercise.getNextExercise());
                        } catch (Exception e) {
                            Reporter.report(context, e, ((MainActivity) context).reportSubject);
                        }
                    }
                });
            }
        }).start();
    }

    private void initViews() {

        this.setBackgroundResource(R.drawable.white_background_shadow);
        this.setOnClickListener(this);// to prevent clicking underground views

        mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        closeExercise();

        LayoutParams lp = new LayoutParams(-1, -2);
        lp.setMargins(UI.calcSize(9), UI.calcSize(8),
                UI.calcSize(9), UI.calcSize(5));
        mainLayout.setLayoutParams(lp);
        mainLayout.addView(titleLayout = new TitleLayout());
        mainLayout.addView(description = new DescriptionWebView());
        mainLayout.addView(new ButtonsLayout());
        mainLayout.addView(new AnswerLayout());
        mainLayout.addView(new NumPad());

        this.addView(mainLayout);
    }

    public DescriptionWebView getDescription() {
        return description;
    }

    public void setStatus(int status) {
//        if (status == ANSWER_SHOWED && this.status == PROMPTED) {
//            // TODO: 05.02.17 исправить статусы!
//        }

        this.status = status;
        setIndicatorColor(ExerciseWindow.indicators[status]);
        currentExercise.setStatus(status);
    }

    private void showAnswer() {

        if (status != DECIDED_FIRSTLY && status != DECIDED_SECONDLY) {
            setStatus(ANSWER_SHOWED);
        }

//        if (status == NOT_DECIDED || status == PROMPTED) {
//            setStatus(ANSWER_SHOWED);
//        }

        ((MainActivity) context).ui.openExerciseAnswerShowWindow(answer);
    }

    private void checkAnswer() {


        if (status != DECIDED_FIRSTLY && status != DECIDED_SECONDLY
                && status != ANSWER_SHOWED) {
//                    if (status == PROMPTED || status == WRONG_ANSWER) {
//                        setStatus(PROMPTED);
//                    } else {
//                        setStatus(DECIDED_FIRSTLY);
//                    }
            if (answerTextView.getText() != null && answerTextView.getText()
                    .toString().replace("|", "").equals(answer)) {

                ((MainActivity) context).ui.openExerciseResultWindow(true);
                openNextExercise();

                if (status == NOT_DECIDED) {
                    setStatus(DECIDED_FIRSTLY);
                } else if (status == WRONG_ANSWER || status == PROMPTED) {
                    setStatus(DECIDED_SECONDLY);
                }

                } else {
                if (status == NOT_DECIDED) {
                    setStatus(WRONG_ANSWER);
                }
                ((MainActivity) context).ui.openExerciseResultWindow(false);
            }
        }
    }

    private void showHint() {
        ((MainActivity) context).ui.openHintWindow(currentExercise);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        return super.onTouchEvent(event);
        return false;
    }

    void clearAnswer() {
        answerTextView.setText("");
    }

    void togglePositive_Negative() {
        String text = (String) answerTextView.getText();
        if (text == null || text.equals("")) {
            answerTextView.setText("-");
        } else if (text.charAt(0) == '-') {
            answerTextView.setText(text.substring(1));
        } else {
            answerTextView.setText(String.format("-%s", text));
        }
    }

    void setComma() {
        if (answerTextView.getText() == null || answerTextView.getText().equals("")) {
            answerTextView.setText("0,");
        } else if (!((String) answerTextView.getText()).contains(",")) {
            answerTextView.setText(String.format("%s,", answerTextView.getText()));
        }
    }

    @Override
    public void onClick(View v) {

        if ((Integer) v.getTag() == R.drawable.btn_answer_show) {
            showAnswer();
        } else if ((Integer) v.getTag() == R.drawable.btn_answer_hint) {
            showHint();
        } else if ((Integer) v.getTag() == R.drawable.btn_answer_check) {
            checkAnswer();
        } else if ((Integer) v.getTag() == R.drawable.button_answer_clear) {
            clearAnswer();
        } else if ((Integer) v.getTag() == 10) {
            togglePositive_Negative();
        } else if ((Integer) v.getTag() == 12) {
            setComma();
        } else if ((Integer) v.getTag() == -1) {
//            closeExercise();
            ((MainActivity) context).onBackPressed();
        } else {
            currentExercise.setTmpText(String.format("%s%s", answerTextView.getText(),
                    String.valueOf(v.getTag())));
            answerTextView.setText(currentExercise.getTmpText());
        }
    }

    void setIndicatorColor(int resId) {
        titleLayout.setIndicatorColor(resId);
    }

    public boolean isOpened() {
        return opened;
    }

    class TitleLayout extends FrameLayout {

        ExerciseNumberImage numberView;
        TextView title;

        public TitleLayout() {
            super(context);
            LayoutParams lp = new LayoutParams(-1, -2);
            lp.setMargins(UI.calcSize(20), 0, 0, UI.calcSize(0));
            this.setLayoutParams(lp);
            setOnClickListener(ExerciseWindow.this);
            setTag(-1);
            setNumber();
            setTitle();
            setCircule();
        }

        /**
         * Изображение с номером задания
         *
         * @param number >=0
         */
        void setNumber(int number) {
            if (numberView != null && title != null) {
                numberView.setNumber(number);
                title.setText("Задача " + Number);
            }
        }

        void setNumber() {
            numberView = new ExerciseNumberImage(context);
            LayoutParams lp = new LayoutParams(UI.calcSize(50),
                    UI.calcSize(50));
            lp.gravity = Gravity.LEFT;
            numberView.setLayoutParams(lp);
            this.addView(numberView);
        }

        void setTitle() {
            title = new TextView(context);
            title.setTextSize(20);
            LayoutParams lp = new LayoutParams(-2, -2);
//            lp.gravity = Gravity.CENTER_HORIZONTAL|Gravity.TOP;
            lp.gravity = Gravity.CENTER;
            title.setLayoutParams(lp);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(context.getResources().getColor(R.color.task_text));
            try {
                title.setTypeface(DataLoader.getFont(context, "comic"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.addView(title);
        }

        void setCircule() {
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(indicators[NOT_DECIDED]);
            LayoutParams lp = new LayoutParams(-2, UI.calcSize(25));
            lp.setMargins(0, 0, UI.calcSize(10), 0);
            lp.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            imageView.setLayoutParams(lp);
            this.addView(imageView);
        }

        void setIndicatorColor(int resId) {
            ((ImageView) getChildAt(2)).setImageResource(resId);
        }

    }

    public class DescriptionWebView extends WebView {

        private boolean isLoaded = false;

        public DescriptionWebView() {
            super(context);
            getSettings().setJavaScriptEnabled(true);
            setDrawingCacheEnabled(true);
            getSettings().setAppCacheEnabled(true);

            this.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(context, context.getString(R.string.mess_text_copy_not_allowed), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }


        public void reloadDescription() {
            loadUrl(DataLoader.ExcerciseDescriptionRequest + QUESTION_ID);
        }

    }

    class ButtonsLayout extends LinearLayout {

        int[] resIds = {R.drawable.btn_answer_show,
                R.drawable.btn_answer_hint,
                R.drawable.btn_answer_check};
        String[] texts = {"СДАЮСЬ",
                "ПОДСКАЗКА",
                "ПРОВЕРИТЬ"};

        public ButtonsLayout() {
            super(context);
            setOrientation(HORIZONTAL);
            LayoutParams lp = new LayoutParams(-1, -2);
            this.setLayoutParams(lp);
            setButtons();
        }

        void setButtons() {
            for (int i = 0; i < 3; i++) {
                TextView button = new TextView(context);
                LinearLayout.LayoutParams lp = new LayoutParams(-1,
                        UI.calcSize(32), 1);
                lp.setMargins(UI.calcSize(2), 0, UI.calcSize(2), 0);
                button.setLayoutParams(lp);
                button.setBackgroundResource(resIds[i]);
                button.setOnClickListener(ExerciseWindow.this);
                button.setTextColor(Color.WHITE);
                button.setTextSize((float) 13.5);
                button.setGravity(Gravity.CENTER);
                button.setText(texts[i]);
                button.setTag(resIds[i]);
                this.addView(button);
            }
        }
    }

    class AnswerLayout extends LinearLayout {

        public AnswerLayout() {
            super(context);
            setAnswerLabel();
            setAnswerTextView();
            setClearButton();
        }

        void setAnswerLabel() {
            TextView text = new TextView(context);
            text.setLayoutParams(new LayoutParams(-1,
                    UI.calcSize(35), (float) 3.3));
            text.setGravity(Gravity.CENTER);
            text.setText(R.string.exercises_answer_label);
            text.setTextSize(20);
            text.setTextColor(
                    context.getResources().getColor(R.color.task_text));
            //empty listener to make unclicked this view. WORKS! DO NOT TOUCH!!!
            text.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            this.addView(text);
        }

        void setAnswerTextView() {
            answerTextView = new TextView(context);
            answerTextView.setLayoutParams(new LayoutParams(-1,
                    UI.calcSize(35), 2));
            answerTextView.setGravity(Gravity.LEFT);
            answerTextView.setTextSize(20);
            answerTextView.setTextColor(
                    context.getResources().getColor(R.color.task_text));
            //empty listener to make unclicked this view. WORKS! DO NOT TOUCH!!!
            answerTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            this.addView(answerTextView);

        }

        void setClearButton() {
            answerClearButton = new ImageButton(context);
            answerClearButton.setLayoutParams(new LayoutParams(-1,
                    UI.calcSize(35), 4));
            answerClearButton.setImageResource(R.drawable.button_answer_clear);
            answerClearButton.setTag(R.drawable.button_answer_clear);
            answerClearButton.setOnClickListener(ExerciseWindow.this);
            this.addView(answerClearButton);

        }

    }

    class NumPad extends LinearLayout {

        public NumPad() {
            super(context);
            this.setOrientation(VERTICAL);
            this.setLayoutParams(new LayoutParams(-1, -2));
            setButtons();
        }

        void setButtons() {

            for (int l = 0; l <= 9; l += 3) {
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(HORIZONTAL);
                layout.setLayoutParams(
                        new LinearLayout.LayoutParams(-1, UI.calcSize(45)));
                this.addView(layout);
                for (int i = 1; i <= 3; i++) {
                    Button b = new Button(context);
                    b.setGravity(Gravity.CENTER);
                    b.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
                    b.setText(String.valueOf(i + l));
                    b.setTag(i + l);
                    b.setOnClickListener(ExerciseWindow.this);
                    b.setTextColor(getResources().getColor(R.color.task_text));
                    layout.addView(b);
                }
            }

            ((Button) findViewWithTag(10)).setText("+/-");
            ((Button) findViewWithTag(11)).setText("0");
            (findViewWithTag(11)).setTag(0);
            ((Button) findViewWithTag(12)).setText(",");

        }
    }
}
