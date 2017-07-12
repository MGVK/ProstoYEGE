package ru.mgvk.prostoege.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ru.mgvk.prostoege.DataLoader;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;
import ru.mgvk.util.Reporter;

import java.net.URL;

/**
 * Created by mihail on 27.10.16.
 */
public class VideoPurchaseWindow extends DialogWindow {

    private Context context;
    private int width = 0, height = 0;
    private int max_video_w, max_video_h;
    private AttachedLayout layout;
    private VideoLayout.VideoCard video;
    private FrameLayout mainLayout;

    public VideoPurchaseWindow(Context context, VideoLayout.VideoCard video) {
        super(context);
        this.context = context;
        this.video = video;

        initViews();
        initSizes();

    }

    void initViews() {
        layout = new AttachedLayout(context);
        mainLayout = new FrameLayout(context);
        mainLayout.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        setBackground();
        mainLayout.addView(layout);
        super.addView(mainLayout);
    }

    void setBackground() {

        mainLayout.setBackgroundResource(R.drawable.beige_window_back);

        int[] gravity = {Gravity.TOP | Gravity.LEFT, Gravity.TOP | Gravity.RIGHT,
                Gravity.BOTTOM | Gravity.LEFT, Gravity.BOTTOM | Gravity.RIGHT};
        int[] resIDs = {R.drawable.top_left, R.drawable.top_right,
                R.drawable.bottom_left, R.drawable.bottom_right};

        for (int i = 0; i <= 3; i++) {
            ImageView view = new ImageView(context);
            view.setLayoutParams(new FrameLayout.LayoutParams(UI.calcSize(25), UI.calcSize(30)));
            ((FrameLayout.LayoutParams) view.getLayoutParams()).gravity = gravity[i];
            view.setImageResource(resIDs[i]);
            mainLayout.addView(view);
        }

    }


    void initSizes() {
        width = ((MainActivity) context).ui.deviceWidth;
        height = ((MainActivity) context).ui.deviceHeight;


        FrameLayout.LayoutParams lp;
        if (getContext().getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT) {

            max_video_w = (int) (0.87 * width);
            max_video_h = (int) (max_video_w / 1.7);

            lp = new FrameLayout.LayoutParams((int) (0.9 * width), -2);
        }
        else {
            max_video_h = (int) (0.45 * height);
            max_video_w = (int) (max_video_h * 1.7);

            lp = new FrameLayout.LayoutParams(-2, (int) (0.9 * height));
        }

        if (layout != null) {
            layout.setLayoutParams(lp);
        }

    }


    class AttachedLayout extends LinearLayout {

        LinearLayout balanceLayout;
        ImageView imageView;
        Bitmap b;
        private TextView donateBtn;
        private ScrollView scrollView;
        private LinearLayout scrolllayout;


        public AttachedLayout(Context context) {
            super(context);
            setPadding(0, UI.calcSize(20), 0, UI.calcSize(20));
            setOrientation(VERTICAL);
            setGravity(CENTER_HORIZONTAL);
            initSizes();
            initViews();
        }

        void initViews() {
            setBalanceLayout();
            this.addView(addSpace(5));
            setVideoTitle();
            this.addView(addSpace(5));
            setScrollView();
            setVideoPicture();
            scrolllayout.addView(addSpace(5));
            setPrice();
            this.addView(addSpace(5));
            setDonateBtn();
        }

        private void setScrollView() {
            scrollView = new ScrollView(context);
            scrollView.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
            scrolllayout = new LinearLayout(context);
            scrolllayout.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
            scrolllayout.setOrientation(VERTICAL);
            scrollView.addView(scrolllayout);
            this.addView(scrollView);
        }

        void setBalanceLayout() {
            balanceLayout = new LinearLayout(context);
            balanceLayout.setPadding(UI.calcSize(10), 0, UI.calcSize(20), 0);
            balanceLayout.setOrientation(LinearLayout.HORIZONTAL);
            balanceLayout.setLayoutParams(new FrameLayout.LayoutParams(-2, UI.calcSize(50)));

            TextView text = new TextView(context);
            text.setText("Ваш баланс:\n" + UI.getPriceLabel(((MainActivity) context).profile.Coins));
            text.setTextSize(17);
            text.setTextColor(Color.BLACK);
            text.setPadding(UI.calcSize(5), 0, UI.calcSize(5), 0);
            text.setGravity(Gravity.CENTER);
            text.setTypeface(DataLoader.getFont(context, "comic"));
            text.setLayoutParams(new LayoutParams(-2, -1));

            TextView balanceBtn = new TextView(context);
            balanceBtn.setBackgroundResource(R.drawable.btn_videodonate_balance);
            balanceBtn.setLayoutParams(new LayoutParams(-2, -1));
            balanceBtn.setText("ПОПОЛНИТЬ СЧЁТ");
            balanceBtn.setTextColor(Color.WHITE);
            balanceBtn.setTypeface(DataLoader.getFont(context, "comic"));
            balanceBtn.setGravity(Gravity.CENTER);
            balanceBtn.setPadding(UI.calcSize(5), 0, UI.calcSize(5), 0);
            balanceBtn.setTextSize(13);
            balanceBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) context).ui.openBalanceDialog();
                }
            });

            balanceLayout.addView(text);
            balanceLayout.addView(balanceBtn);

            this.addView(balanceLayout);

        }

        void setVideoTitle() {
            ScrollView scrollView = new ScrollView(context);
            scrollView.setPadding(UI.calcSize(10), 0, UI.calcSize(20), 0);
            scrollView.setLayoutParams(new FrameLayout.LayoutParams(-1, UI.calcSize(100)));

            TextView description = new TextView(context);
            description.setText(
                    video.getNumber() + "." + video.getNumber()
                            + " \n" + video.getDescription());
            description.setLayoutParams(new LayoutParams(-1, -2));
            description.setTextSize(18);
            description.setTextColor(context.getResources().getColor(R.color.task_text));
            try {
                description.setTypeface(DataLoader.getFont(context, "comic"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            scrollView.addView(description);
            this.addView(scrollView);
        }

        void initVideoSize() {
            LinearLayout.LayoutParams lp = new LayoutParams(max_video_w, max_video_h);
            lp.gravity = Gravity.CENTER;
            imageView.setLayoutParams(lp);
        }

        void setVideoPicture() {
            imageView = new ImageView(context);
            imageView.setBackgroundResource(R.drawable.video_back_loading);
            imageView.setAdjustViewBounds(true);
//            imageView.setMaxHeight(max_video_h);
//            imageView.setMaxWidth(max_video_w);
            initVideoSize();
//            imageView.setLayoutParams(new LayoutParams(-1,-1));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        b = BitmapFactory.decodeStream(
                                new URL(DataLoader.getVideoBackRequest(video.getVideoID()))
                                        .openStream());
                        ((MainActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
//                                imageView.setImageDrawable(new BitmapDrawable(getResources(), b));
                                    animateVideoPicture(false,
                                            new BitmapDrawable(getResources(),
                                                    b));
                                } catch (Exception e) {
                                    Reporter.report(context,
                                            e,
                                            ((MainActivity) context).reportSubject);
                                }
                            }
                        });
                    } catch (Exception e) {
                        Reporter.report(context, e, ((MainActivity) context).reportSubject);
                    }
                }
            }).start();
            scrolllayout.addView(imageView);
        }

        void animateVideoPicture(boolean in, final Drawable picture) {
            ObjectAnimator a = new ObjectAnimator();
            a.setTarget(imageView);
            a.setPropertyName("alpha");
            a.setDuration(200);
            if (in) {
                imageView.setImageDrawable(picture);
                imageView.setBackgroundColor(Color.TRANSPARENT);
                a.setFloatValues(0, 1);
            } else {
                a.setFloatValues(1, 0);
                a.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        try {
                            animateVideoPicture(true, picture);
                        } catch (Exception e) {
                            Reporter.report(context, e, ((MainActivity) context).reportSubject);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
            a.start();
        }

        void setPrice() {
            TextView text = new TextView(context);
            LinearLayout.LayoutParams lp = new LayoutParams(-1, -2);
            lp.setMargins(UI.calcSize(10), 0, UI.calcSize(20), 0);
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            text.setLayoutParams(lp);
            text.setTextSize(17);
            text.setTextColor(Color.BLACK);
            text.setTypeface(DataLoader.getFont(context, "comic"));
            text.setText("Цена: " + UI.getPriceLabel((video.getPrice())));
            scrolllayout.addView(text);
        }


        @Override
        protected void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                try {
                    this.removeView(donateBtn);
                } catch (Exception ignored) {
                }
                balanceLayout.addView(donateBtn);
            }
            else {
                try {
                    balanceLayout.removeView(donateBtn);
                } catch (Exception ignored) {
                }
                this.addView(donateBtn);
            }

            initSizes();
            initVideoSize();

        }

        void setDonateBtn() {
            donateBtn = new TextView(context);
            donateBtn.setText("РАЗБЛОКИРОВАТЬ");
            donateBtn.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams lp = new LayoutParams(-2, /*UI.calcSize(26)*/-1);
            lp.gravity = Gravity.CENTER;
            lp.setMargins(UI.calcSize(10), 0, UI.calcFontSize(10), 0);
            donateBtn.setTextSize(17);
            donateBtn.setGravity(Gravity.CENTER);
            donateBtn.setPadding(UI.calcSize(5), 0, UI.calcSize(5), 0);
            donateBtn.setLayoutParams(lp);
            donateBtn.setBackgroundResource(R.drawable.btn_videodonate_donate);
            donateBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (((MainActivity) context).profile.Coins >= video.getPrice()) {
                            try {
                                video.setYoutubeID((((MainActivity) context).pays.buyVideo(video.getVideoID())));
                            } catch (Exception e) {
                                UI.makeErrorMessage(context, "Ошибка соединения с сервером!");
                            }
                            video.setBuyed(true);

                            ((MainActivity) context).updateCoins(-1 * video.getPrice());
                            close();
                        } else {
                            ((MainActivity) context).ui.openLowCoinsWindow();
                        }

                        if (((MainActivity) context).profile.Repost != 1) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    ((MainActivity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                ((MainActivity) context).ui.makeShareHelpMessage();
                                            } catch (Exception e) {
                                                Reporter.report(context, e, ((MainActivity) context).reportSubject);
                                            }
                                        }
                                    });
                                }
                            }).start();
                        }
                    } catch (Exception e) {
                        Reporter.report(context, e, ((MainActivity) context).reportSubject);
                    }
                }
            });
            if (getContext().getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_PORTRAIT) {
                this.addView(donateBtn);
            }
            else {
                balanceLayout.addView(donateBtn);
            }
        }

        Space addSpace(int h) {
            Space space = new Space(context);
            space.setLayoutParams(new LayoutParams(-1, UI.calcSize(h)));
            return space;
        }

    }

}
