package ru.mgvk.prostoege.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


import ru.mgvk.prostoege.MainActivity;

/**
 * Created by mihail on 11.09.16.
 */
public class SwipedLinearLayout extends FrameLayout {
    private Context context;
    private ChildLayout childLayout;
    private OnClickListener onClick;
    private OnSwipingListener onSwipingListener;
    private ReturnButton returnButton;
    private boolean canceled=false;
    private boolean first = true;
//    private Drawable bg=null;

    public SwipedLinearLayout(Context context) {
        super(context);
        this.context = context;
        initParams();
        initChild();
    }

    void initChild(){
        childLayout = new ChildLayout(context);
        super.addView(childLayout);
        super.addView(returnButton = new ReturnButton(context));
    }

    void initParams(){
        setClickable(true);
        setForegroundGravity(Gravity.CENTER);
    }

    void setWrapperPadding(int l,int t,int r,int b){
        setPadding(l, t, r, b);
    }

    public LinearLayout getChildLayout() {
        return childLayout;
    }

    public void setOrientation(int orientation){
        childLayout.setOrientation(orientation);
    }

    private boolean onSuperTouchEvent(MotionEvent event){
        return super.onTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (first&&childLayout!=null) {
            childLayout.setX(childLayout.m);
            first = false;
        }
    }

    //
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
////        return childLayout.onTouchEvent(event);
////        return onSuperTouchEvent(event);
//        return super.onTouchEvent(event);
//    }


    public boolean isSwiping() {
        return childLayout.isMoving();
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
//        super.setOnClickListener(l);
        this.onClick = l;
    }

    @Override
    public void addView(View child) {
//        super.addView(child);
        childLayout.addView(child);
    }

    public void setOnSwipingListener(OnSwipingListener onSwipingListener) {
        this.onSwipingListener = onSwipingListener;
    }
    public void onSwipe(){
        childLayout.animateRemoving();
        ((MainActivity) context).ui.taskListFragment.taskScroll.setScrollEnabled(true);
        ((MainActivity) context).ui.mainScroll.setScrollEnabled(true);
    }

    public void onReturn(){
        childLayout.setVisibility(VISIBLE);
        setVisibility(VISIBLE);
        childLayout.animateReturning();
        returnButton.deactivate();
        canceled = true;
    }


    public interface OnSwipingListener{

        void onSwipe();

    }





    public class ChildLayout extends LinearLayout{


        float oldx=0,oldy=0;
        boolean moving = false;
        int m = UI.calcSize(5);

        public ChildLayout(Context context) {
            super(context);
            initParams();
        }


        public void initParams(){
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1,-1);
            lp.gravity = Gravity.CENTER;
            lp.setMargins(m,m,m,m);
            setLayoutParams(lp);
        }


        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                setAlpha((float) 0.7);
                ((MainActivity) context).ui.taskListFragment.taskScroll.setScrollEnabled(false);
                ((MainActivity) context).ui.mainScroll.setScrollEnabled(false);
                oldx = event.getX();
                oldy = event.getY();
            }else
            if(event.getAction()==MotionEvent.ACTION_MOVE){
                if(Math.abs(event.getY()-oldy)<Math.abs(event.getX()-oldx)|| moving){
                    ((MainActivity) context).ui.mainScroll.setScrollEnabled(false);
                    move(event.getX() - oldx);

                    return true;
                }else{
                    ((MainActivity) context).ui.taskListFragment.taskScroll.setScrollEnabled(true);




                }
//                else {
//                    return onSuperTouchEvent(event);
//                }

//                if(moving){
//                    move(event.getX()-oldx);
//                }
            }else

            if (event.getAction()==MotionEvent.ACTION_UP){
                setAlpha(1);
                if(!isMoving()){
//                    callOnClick();
                    onClick.onClick(this);
                }

                if(Math.abs(getX())>=getWidth()/4){
                    onSwipe();
                }else{
                    animateReturning();
                }

                moving =false;
                ((MainActivity) context).ui.taskListFragment.taskScroll.setScrollEnabled(true);
                ((MainActivity) context).ui.mainScroll.setScrollEnabled(true);


            }

//            return true;

            return onSuperTouchEvent(event);
//            return super.onTouchEvent(event);
        }

        public boolean isMoving() {
            return moving;
        }

        void move(float dx){
            moving = true;
//            if(Math.abs(getX())>=getWidth()/2){
//                onSwipe();
//            }else
            {
                setX((getX() + dx));
            }

        }



        void animateRemoving(){
            ObjectAnimator animator =
                    ObjectAnimator.ofFloat(ChildLayout.this,"x",Math.signum(getX())*getWidth());
            animator.setDuration(150);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    returnButton.activate();
                    childLayout.setVisibility(GONE);
//                    setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            animator.start();
        }

        public void animateReturning(){
            ObjectAnimator animator =
                    ObjectAnimator.ofFloat(ChildLayout.this,"x",m);
            animator.setDuration(200);
            animator.start();
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }

    }

    public class ReturnButton extends Button {

        public ReturnButton(Context context) {
            super(context);
            setVisibility(INVISIBLE);
            FrameLayout.LayoutParams lp = new LayoutParams(-1,-2);
            lp.gravity = Gravity.CENTER;
            lp.setMargins((int)UI.calcSize(20),0,(int)UI.calcSize(20),0);
            setLayoutParams(lp);
            setText("Отменить");
            setOnClickListener(null);
        }

        public void activate(){
            setVisibility(VISIBLE);
            canceled = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ((MainActivity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            returnButton.setVisibility(GONE);
                            if(canceled) {
                                SwipedLinearLayout.this.setVisibility(VISIBLE);
                            }else{
                                SwipedLinearLayout.this.setVisibility(GONE);
                            }
                        }
                    });
                }
            }).start();
        }


        public void deactivate(){
            setVisibility(GONE);
        }

        @Override
        public void setOnClickListener(final OnClickListener l) {
            super.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onReturn();

                    if(l!=null) l.onClick(v);
                }
            });
        }
    }




}
