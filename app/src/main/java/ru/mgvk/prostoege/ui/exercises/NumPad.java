package ru.mgvk.prostoege.ui.exercises;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import ru.mgvk.prostoege.R;
import ru.mgvk.prostoege.ui.UI;

public class NumPad extends LinearLayout implements View.OnClickListener {

    private Context      context;
    private AnswerLayout answerLayout;
    private OnKeyClicked onKeyClicked;


    public NumPad(Context context) {
        super(context);

    }

    public NumPad(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumPad(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NumPad(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public NumPad(Context context, OnClickListener listener) {
        super(context);
    }


    public void init(NumPad.OnKeyClicked listener, AnswerLayout answerLayout) {
        init(listener);
        setAnswerLayout(answerLayout);
    }

    public void init(NumPad.OnKeyClicked listener) {
        this.context = getContext();
        setOnKeyClicked(listener);
        this.setOrientation(VERTICAL);
        this.setLayoutParams(new LayoutParams(-1, -2));
        setButtons();
        isInEditMode();
    }

    void setButtons() {

        for (int l = 0; l <= 9; l += 3) {

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(HORIZONTAL);
            layout.setLayoutParams(
                    new LayoutParams(-1, UI.calcSize(65)));
            this.addView(layout);

            for (int i = 1; i <= 3; i++) {

                Button b = new Button(context);
                b.setGravity(Gravity.CENTER);
                b.setLayoutParams(new LayoutParams(-1, -1, 1));
                b.setText(String.valueOf(i + l));
                b.setTag(i + l);
                b.setOnClickListener(this);
                b.setTextColor(Color.WHITE);
                b.setBackgroundResource(R.drawable.exercise_numpad_button_background);
                layout.addView(b);

            }
        }

        ((Button) findViewWithTag(10)).setText("+/-");
        ((Button) findViewWithTag(11)).setText("0");
        (findViewWithTag(11)).setTag(0);
        ((Button) findViewWithTag(12)).setText(",");

    }

    public void setAnswerLayout(AnswerLayout answerLayout) {
        this.answerLayout = answerLayout;
    }

    public void setOnKeyClicked(OnKeyClicked onKeyClicked) {
        this.onKeyClicked = onKeyClicked;
    }

    @Override
    public void onClick(View v) {
        try {
            if ((Integer) v.getTag() == 10) {
                answerLayout.togglePositive_Negative();
            } else if ((Integer) v.getTag() == 12) {
                answerLayout.setComma();
            } else {
                answerLayout.getAnswerTextView().setText(String.format("%s%s",
                        answerLayout.getAnswerTextView().getText().toString()
                                .replace("|", ""),
                        String.valueOf(v.getTag())));
            }
            onKeyClicked.onKeyClicked(((Button) v),
                    answerLayout.getAnswerTextView().getText()
                            .toString().replace("|", ""));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnKeyClicked {

        void onKeyClicked(Button b, String s);

    }
}
