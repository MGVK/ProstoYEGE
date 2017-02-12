package ru.mgvk.prostoege.ui;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import ru.mgvk.prostoege.R;

/**
 * Created by mihail on 08.10.16.
 */
public class WhiteWindow extends DialogWindow {

    Context context;
    private AttachedLayout layout;
    private Button closeBtn;


    public WhiteWindow(Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init(){
        layout = new AttachedLayout(context);
        layout.setBackgroundResource(R.drawable.white_background);
        super.addView(layout);

        closeBtn = new Button(context);
        closeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

    }

    @Override
    public void addView(View child) {
//        super.addView(child);
        layout.addView(child);
    }



    class AttachedLayout extends FrameLayout {

        public AttachedLayout(Context context) {
            super(context);
            setLayoutParams(new LayoutParams(-2,-2));
        }
    }

}
