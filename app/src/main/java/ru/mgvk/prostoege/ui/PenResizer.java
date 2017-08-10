package ru.mgvk.prostoege.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;
import ru.mgvk.prostoege.MainActivity;

/**
 * Created by mihail on 26.08.16.
 */
public class PenResizer extends SeekBar implements SeekBar.OnSeekBarChangeListener{
    PaintView paintView;
    public PenResizer(Context context) {
        super(context);
        setParams();
    }

    public PenResizer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setParams();
    }

    public PenResizer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setParams();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PenResizer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setParams();
    }



    void setParams(){
        paintView = ((MainActivity)getContext()).ui.toolsFragment.paintView;
        setVisibility(GONE);
        this.setOnSeekBarChangeListener(this);
        this.setMax(30);
    }

    public void setPaintView(PaintView paintView) {
        this.paintView = paintView;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        ((MainActivity) getContext()).ui.mainScroll.setScrollEnabled(true);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        ((MainActivity) getContext()).ui.mainScroll.setScrollEnabled(false);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        paintView.setLineWidth(seekBar.getProgress());
        ((MainActivity) getContext()).ui.mainScroll.setScrollEnabled(true);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            ((MainActivity) getContext()).ui.mainScroll.setScrollEnabled(false);
        }
        if(event.getAction()==MotionEvent.ACTION_UP){
            ((MainActivity) getContext()).ui.mainScroll.setScrollEnabled(true);
        }

        return super.onTouchEvent(event);
    }
}
