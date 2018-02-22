package ru.mgvk.prostoege.ui.exercises;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;
import ru.mgvk.prostoege.ui.UI;

public class AnswerLayout extends LinearLayout implements View.OnClickListener {

    private OnClickListener listener;
    private TextIndicator   textIndicator;
    private TextView        answerTextView;
    private ImageButton     answerClearButton;
    private Context         context;


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

    void init() {
        this.context = getContext();
        setAnswerLabel();
        setAnswerTextView();
        setClearButton();
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        listener = l;
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
        text.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startIndicator();
            }
        });
        this.addView(text);
    }

    public TextView getAnswerTextView() {
        return answerTextView;
    }

    public ImageButton getAnswerClearButton() {
        return answerClearButton;
    }

    void clearAnswer() {
        if (answerTextView.length() > 0) {
            answerTextView.setText(answerTextView.getText().toString().replace("|", "")
                    .subSequence(0, answerTextView.length() - 1));
        }
    }

    public void togglePositive_Negative() {
        String text = (String) answerTextView.getText();
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
        } else if (!((String) answerTextView.getText()).contains(",")) {
            answerTextView.setText(String.format("%s,", answerTextView.getText()));
        }
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
        answerTextView.setOnClickListener(this);

        this.addView(answerTextView);
    }

    void startIndicator() {
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

    void setClearButton() {
        answerClearButton = new ImageButton(context);
        answerClearButton.setLayoutParams(new LayoutParams(-1,
                UI.calcSize(35), 4));
        answerClearButton.setImageResource(R.drawable.button_answer_clear);
        answerClearButton.setTag(R.drawable.button_answer_clear);
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
            while (!isInterrupted()) {
                ((MainActivity) context).runOnUiThread(new Runnable() {
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
