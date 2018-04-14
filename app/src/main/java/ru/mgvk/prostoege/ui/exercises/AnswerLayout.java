package ru.mgvk.prostoege.ui.exercises;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;
import ru.mgvk.prostoege.ui.UI;

public class AnswerLayout extends FrameLayout implements View.OnClickListener {

    private OnClickListener listener;
    private TextIndicator   textIndicator;
    private TextView        answerTextView;
    private ImageButton     answerClearButton;
    private Context         context;
    private LinearLayout    l;


    public AnswerLayout(Context context) {
        super(context);
        init();
    }

    public AnswerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnswerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public AnswerLayout(Context context, AttributeSet attrs, int defStyleAttr,
                        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public AnswerLayout(Context context, OnClickListener listener) {
        super(context);
        this.listener = listener;
        init();
    }

    private void init() {
        this.context = getContext();
        setLayoutParams(new LayoutParams(-1, UI.calcSize(40)));
        l = new LinearLayout(context);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setLayoutParams(new LinearLayout.LayoutParams(-2, -1));
        l.setGravity(Gravity.CENTER | Gravity.LEFT);
        addView(l);
        setAnswerLabel();
        setAnswerTextView();

        setClearButton();
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        listener = l;
    }

    private void setAnswerLabel() {
        TextView text = new TextView(context);
        text.setLayoutParams(new LayoutParams(-2, -2));
        text.setGravity(Gravity.CENTER);
        text.setText(R.string.exercises_answer_label);
        text.setTextSize(20);
        text.setTextColor(
                context.getResources().getColor(R.color.task_text));
        text.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startIndicator();
            }
        });
        l.addView(text);
    }

    public TextView getAnswerTextView() {
        return answerTextView;
    }

    public ImageButton getAnswerClearButton() {
        return answerClearButton;
    }

    private void clearAnswer() {
        if (answerTextView.length() > 0) {
            answerTextView.setText(answerTextView.getText().toString().replace("|", "")
                    .subSequence(0, answerTextView.length() - 1));
        }
    }

    public String getAnswer() {
        if (answerTextView != null) {
            return answerTextView.getText().toString();
        }
        return null;
    }

    public void togglePositive_Negative() {
        String text = answerTextView.getText().toString();
        if (text == null || text.equals("")) {
            answerTextView.setText("-");
        } else if (text.charAt(0) == '-') {
            answerTextView.setText(text.substring(1));
        } else {
            answerTextView.setText(String.format("-%s", text));
        }
    }

    public void setComma() {
        if (answerTextView.getText() == null || answerTextView.getText().equals("")) {
            answerTextView.setText("0,");
        } else if (!(answerTextView.getText().toString()).contains(",")) {
            answerTextView.setText(String.format("%s,", answerTextView.getText()));
        }
    }

    private void setAnswerTextView() {
        answerTextView = new TextView(context);
        answerTextView.setLayoutParams(new LayoutParams(-2, -1));
        answerTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        answerTextView.setTextSize(20);
        answerTextView.setTextColor(
                context.getResources().getColor(R.color.task_text));
        //empty listener to make unclickable this view. WORKS! DO NOT TOUCH!!!
        answerTextView.setOnClickListener(this);

        l.addView(answerTextView);
    }


    private void startIndicator() {

        //ВЫКЛЮЧЕН! см класс

        if (textIndicator == null) {
            (textIndicator = new TextIndicator()).start();
        } else if (textIndicator.isInterrupted()) {
            textIndicator.start();
        }
    }

    void stopIndicator() {
        if (textIndicator != null) {
            textIndicator.interrupt();
        }
    }

    private void setClearButton() {
        answerClearButton = new ImageButton(context);
        LayoutParams lp = new LayoutParams(UI.calcSize(24), -UI.calcSize(18),
                Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        int a = UI.calcSize(UI.calcSize(10));
        lp.setMargins(a, 0, a, 0);
        answerClearButton.setLayoutParams(lp);
        answerClearButton.setBackgroundResource(R.drawable.delete);
        answerClearButton.setOnClickListener(AnswerLayout.this);
        this.addView(answerClearButton);
    }

    @Override
    public void onClick(View v) {
        if (v == answerTextView) {
            startIndicator();
        } else if (v == answerClearButton) {
            clearAnswer();
        } else if (listener != null) {
            listener.onClick(v);
        }
    }

    class TextIndicator extends Thread {

        @Override
        public void run() {
            while (!!isInterrupted()) { // убрать ! чтобы работало!
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        answerTextView.setText(answerTextView.getText() + "|");

                    }
                });
                pause();
                ((MainActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        answerTextView
                                .setText(answerTextView.getText().toString().replace("|", ""));

                    }
                });
                pause();
            }
        }

        private void pause() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
