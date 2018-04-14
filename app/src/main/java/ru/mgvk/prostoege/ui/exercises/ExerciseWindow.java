package ru.mgvk.prostoege.ui.exercises;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.Profile;
import ru.mgvk.prostoege.R;
import ru.mgvk.prostoege.Task;
import ru.mgvk.util.Reporter;
import ru.mgvk.util.StateTags;

/**
 * Created by mihail on 16.10.16.
 */
public class ExerciseWindow extends FrameLayout
        implements View.OnClickListener, NumPad.OnKeyClicked {

    public final static byte NOT_DECIDED = 0, WRONG_ANSWER = 1,
            PROMPTED                     = 2, DECIDED_FIRSTLY = 3,
            ANSWER_SHOWED                = 4, DECIDED_SECONDLY = 5,
            DECIDED                      = 6, WRONG_ANSWER_ONCE = 7;

    public final static  int[]   indicators = {R.drawable.exercise_indicator_yellow,
                                               R.drawable.exercise_indicator_red,
                                               R.drawable.exercise_indicator_blue,
                                               R.drawable.exercise_indicator_green,
                                               R.drawable.exercise_indicator_red,
                                               R.drawable.exercise_indicator_blue,
                                               R.drawable.exercise_indicator_blue,
                                               R.drawable.exercise_indicator_red};
    private static final boolean APPEAR     = true, DISAPPEAR = false;

    int QUESTION_ID = 0;
    int Number      = 0;
    Context context;
    private String answer = "", separator = "&";
    private AnswerLayout answerLayout;

    private int status = 0;
    private DescriptionWebView description;
    private Task.Exercise      currentExercise;
    private boolean opened = false;
    private TextView title;

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
        setStatus(exercise.getStatus());
        answerLayout.getAnswerTextView().setText(exercise.getTmpText());
        description.reloadDescription(QUESTION_ID);
        open();
        ((MainActivity) context).getBackStack()
                .addAction("ExerciseWindow.openExercise", new Runnable() {
                    @Override
                    public void run() {
                        closeExercise();
                    }
                });
    }

    protected void open() {
        opened = true;
        animateAlpha(APPEAR);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((MainActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scrollUp();
                    }
                });
            }
        }).start();
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
        setNumber(data.Number);
    }

    private void setNumber(int number) {
        title.setText("Задача №" + number);
        Number = number;
    }

    public void close() {
        opened = false;
        animateAlpha(DISAPPEAR);
        if (answerLayout != null) {
            answerLayout.stopIndicator();
        }

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

        inflate(context, R.layout.exercise_window_layout, this);

        this.setOnClickListener(this);// to prevent clicking underground views
        closeExercise();

        findViewById(R.id.btn_answer_check).setOnClickListener(this);
        findViewById(R.id.btn_answer_hint).setOnClickListener(this);
        findViewById(R.id.btn_answer_show).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);

        answerLayout = (AnswerLayout) findViewById(R.id.answerlayout);
        answerLayout.getAnswerTextView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollDown();
            }
        });
        answerLayout.setOnClickListener(this);
        ((NumPad) findViewById(R.id.numpad)).init(this, answerLayout);
        title = (TextView) findViewById(R.id.title);
        title.setText("Задача №" + Number);

        description = (DescriptionWebView) findViewById(R.id.webview);
    }

    public DescriptionWebView getDescription() {
        return description;
    }

    public void setStatus(int status) {
//        if (status == ANSWER_SHOWED && this.status == PROMPTED) {
//            // TODO: 05.02.17 исправить статусы!
//        }

        if (status == DECIDED_FIRSTLY) {
            currentExercise.getTask().incrementFirstTimeExercise();
        }

        this.status = status;
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

        boolean result = answerLayout.getAnswerTextView().getText()
                .toString().replace("  |", "").equals(answer);

        ((MainActivity) context).ui.openExerciseResultWindow(result,
                status == WRONG_ANSWER ?
                        currentExercise.getBonus1() : (
                        status == WRONG_ANSWER_ONCE ?
                                currentExercise.getBonus2() : 0
                )
        );

        if (answerLayout.getAnswerTextView().getText() != null && status != DECIDED_FIRSTLY
            && status != DECIDED_SECONDLY && status != ANSWER_SHOWED) {
//                    if (status == PROMPTED || status == WRONG_ANSWER) {
//                        setStatus(PROMPTED);
//                    } else {
//                        setStatus(DECIDED_FIRSTLY);
//                    }
            if (result) {

                openNextExercise();

                if (status == NOT_DECIDED) {
                    setStatus(DECIDED_FIRSTLY);
                } else if (status == WRONG_ANSWER_ONCE) {
                    setStatus(DECIDED_SECONDLY);
                } else if (status == WRONG_ANSWER_ONCE || status == PROMPTED) {
                    setStatus(DECIDED_SECONDLY);
                }

            } else {
                if (status == NOT_DECIDED) {
                    setStatus(WRONG_ANSWER_ONCE);
                } else if (status == WRONG_ANSWER_ONCE) {
                    setStatus(WRONG_ANSWER);
                }
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

    @Override
    public void onClick(View v) {

        // TODO: 07.08.17 refactor this
        try {
            if (v.getId() == R.id.btn_answer_show) {
                showAnswer();
            } else if (v.getId() == R.id.btn_answer_hint) {
                showHint();
            } else if (v.getId() == R.id.btn_answer_check) {
                checkAnswer();
            } else if (v.getId() == R.id.btn_close) {
                closeExercise();
            } else if ((Integer) v.getTag() == 10) {
                answerLayout.togglePositive_Negative();
            } else if ((Integer) v.getTag() == 12) {
                answerLayout.setComma();
            } else if ((Integer) v.getTag() == -1) {
//            closeExercise();
//            ((MainActivity) context).onBackPressed();
                ((MainActivity) context).getBackStack().returnToState(
                        StateTags.EXERCISE_LIST_FRAGMENT);
            } else {
                currentExercise.setTmpText(
                        String.format("%s%s", answerLayout.getAnswerTextView().getText().toString()
                                        .replace("|",
                                                ""),
                                String.valueOf(v.getTag())));
                answerLayout.getAnswerTextView().setText(currentExercise.getTmpText());
            }

        } catch (Exception e) {
            Reporter.report(context, e, ((MainActivity) context).reportSubject);
        }
    }


    public boolean isOpened() {
        return opened;
    }

    private void scrollUp() {
        try {
            if (getParent() != null) {
                ((ScrollView) getParent()).fullScroll(View.FOCUS_UP);
            }
        } catch (Exception ignored) {
        }
    }

    private void scrollDown() {
        try {
            if (getParent() != null) {
                ((ScrollView) getParent()).fullScroll(View.FOCUS_DOWN);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onKeyClicked(Button b, String s) {

    }
}
