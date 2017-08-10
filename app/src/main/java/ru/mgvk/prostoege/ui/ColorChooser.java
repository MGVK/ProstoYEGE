package ru.mgvk.prostoege.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * Created by mihail on 26.08.16.
 */
public class ColorChooser extends LinearLayout implements View.OnClickListener {


    PaintView paintView;
    int[] colors = {Color.BLACK, Color.BLUE, Color.RED, Color.GREEN,
                    Color.parseColor("#ff9c00"), Color.parseColor("#9100d1"),
                    Color.parseColor("#ff118b"), Color.YELLOW};

    public ColorChooser(Context context) {
        super(context);
        setParams();
    }

    public ColorChooser(Context context, AttributeSet attrs) {
        super(context, attrs);
        setParams();
    }

    public ColorChooser(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setParams();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ColorChooser(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setParams();
    }

    public void setPaintView(PaintView paintView) {
        this.paintView = paintView;
    }

    void setParams(){
        setVisibility(GONE);
        setOrientation(HORIZONTAL);
        for(int i=0;i<8;i++){
            ImageButton b = new ImageButton(getContext());
            b.setOnClickListener(this);
            b.setBackgroundColor(colors[i]);
            b.setTag(colors[i]);
            LayoutParams lp =new LayoutParams(UI.calcSize(30), UI.calcSize(30));
            lp.setMargins(UI.calcSize(3), UI.calcSize(5),
                    UI.calcSize(3), UI.calcSize(5));
            b.setLayoutParams(lp);
            this.addView(b);
        }
    }


    @Override
    public void onClick(View v) {
        try{
            paintView.setLineColor((Integer) v.getTag());
        }catch (Exception e){}
    }
}
