package ru.mgvk.prostoege.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;

import ru.mgvk.prostoege.R;

/**
 * Created by mike on 23.01.17.
 */
public class ExerciseNumberImage extends TextView{
    private int number=0;

    public ExerciseNumberImage(Context context) {
        super(context);
        setBackgroundResource(R.drawable.exercise_number_image);
        setGravity(Gravity.CENTER);
        setTextSize(17);
        setTextColor(Color.BLACK);
    }

    public void setNumber(int number) {
        this.number = number;
        setText(String.valueOf(number));
    }
}
