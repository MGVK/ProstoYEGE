package ru.mgvk.prostoege.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ru.mgvk.prostoege.*;
import ru.mgvk.util.Reporter;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mike on 27.02.17.
 */
public class VideoLayout extends LinearLayout {

    private Context context;
    private Task currentTask;
    private Profile.TaskData.VideoData[] videoData;
    private int maxCardsCount = 0;
    private ArrayList<VideoCard> currentVideosList = new ArrayList<>();
    private HashMap<Integer, Drawable> videoBackgrounds = new HashMap<>();

    public VideoLayout(Context context, int maxCardsCount) {
        super(context);
        this.context = context;
        this.maxCardsCount = maxCardsCount;

        setOrientation(VERTICAL);
        setLayoutParams(new LayoutParams(-1, -1));

        initCards();
    }

    private void initCards() {
        for (int i = 1; i <= maxCardsCount; i++) {
            addView(new VideoCard(i));
        }
    }

    public void openVideosFromTask(Task task) {
        this.currentTask = task;
        videoData = task.getVideoData();
        loadVideos();
    }

    private void loadVideos() {
        try {
            hideUnusefullCards();
            currentVideosList.clear();
            for (int i = 0; i < videoData.length; i++) {
                ((VideoCard) getChildAt(i)).init(
                        videoData[i].ID, videoData[i].YouTube, videoData[i].Description, videoData[i].Price);
                currentVideosList.add((VideoCard) getChildAt(i));
            }
        } catch (Exception e) {
            Reporter.report(context, e, ((MainActivity) context).reportSubject);
        }
    }

    private void hideUnusefullCards() {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setVisibility(GONE);
        }
    }

    public ArrayList<VideoCard> getCurrentVideosList() {
        return currentVideosList != null ?
                currentVideosList : new ArrayList<VideoCard>();
    }

    public class VideoCard extends FrameLayout {

        int videoID = 0;
        byte m = (byte) UI.calcSize(5);
        LinearLayout mainLayout;
        FrameLayout numberLayout;
        VideoCardLayout videoLayout;
        ImageView buyingIndicator;
        private String youtubeID = "";
        private String description = "";
        private VideoPlayer player;
        private boolean buyed = false;
        private int number = 1;
        private int price = 0;
        private TextView descriptionView;

        /**
         *
         */

        public VideoCard(int number) {
            this(0, "", "", number, 0);
        }

        public VideoCard(int id, String youtubeID, String description, int number, int price) {

            super(context);
            this.number = number;
            this.videoID = id;
            this.price = price;
            this.description = description;
            this.youtubeID = youtubeID;

//            setPlayer();
            initPlayer();
            initViews();
            setVideoDescrition();

        }

        void init(int id, String youtubeID, String description, int price) {
            this.videoID = id;
            this.price = price;
            setDescription(description);

            try {

                if (youtubeID == null || youtubeID.equals("")) {
                    setBuyed(false);
                } else {
                    setYoutubeID(this.youtubeID = youtubeID);
                    setBuyed(true);
                }


            } catch (Exception e) {
                Log.e("VideoData", "Incorrect data! " + e.getLocalizedMessage());
            }

            setVisibility(VISIBLE);

        }

        public int getNumber() {
            return number;
        }

        public int getPrice() {
            return price;
        }

        public int getVideoID() {
            return videoID;
        }

        public void setOnVideoStateChangeListener() {

        }

        public boolean isPlaying() {
            return player != null && player.isPlaying();
        }

        public void stop() {
            if (player != null) {
                player.stop();
            }
        }

        public void pause() {
            if (player != null && isPlaying()) {
                player.pause();
            }
        }

        public void start() {
            if (player != null) {
                player.start();
            }
        }

        public void updateSizes(int w, int h) {
            if (player != null) {

                player.getSmallDisplay().setLayoutParams(new LinearLayout.LayoutParams(
                        (int) (0.75 * w), h = (int) ((9 / 16.0) * 0.75 * w)));
            }

//            numberLayout.setLayoutParams(new LinearLayout.LayoutParams((int) (0.15 * w), h));
//            videoLayout.setLayoutParams(new LinearLayout.LayoutParams(w - UI.calcSize(2 * m), h));

        }

        public String getDescription() {
            return description;
        }

        private void setDescription(String description) {
            this.description = description;
            if (descriptionView == null) {
                setVideoDescrition();
            }
            descriptionView.setText(description);
        }

        public Task getTask() {
            return currentTask;
        }

        void initPlayer() {

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);

            player = new VideoPlayer(context);
            player.getSmallDisplay().setLayoutParams(lp);

            player.setPicture(context.getResources().getDrawable(R.drawable.video_back));

        }

        void setPlayer() {

//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
//
//            player = new VideoPlayer(context);
//            player.getSmallDisplay().setLayoutParams(lp);
            player.setVideoID(youtubeID);

            if (currentTask != null
                    && (currentTask.getNumber() == 1 || currentTask.getNumber() == 2)) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final Bitmap b = BitmapFactory.decodeStream(
                                    new URL(DataLoader.getVideoBackRequest(videoID)).openStream());

                            ((MainActivity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        player.setPicture(new BitmapDrawable(getResources(), b));
                                    } catch (Exception e) {
                                        Reporter.report(context, e, ((MainActivity) context).reportSubject);
                                    }
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            } else {
                player.setPicture(context.getResources().getDrawable(R.drawable.video_back));
            }
            if (!buyed && (currentTask.getNumber() != 1 && currentTask.getNumber() != 2)) {
                player.getSmallDisplay().setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) context).ui.openVideoPurchaseDialog(VideoCard.this);
                    }
                });
            }
        }

        private void initViews() {
            setVisibility(GONE);

            mainLayout = new LinearLayout(context);
            mainLayout.setLayoutParams(new LayoutParams(-1, -1));
            ((LayoutParams) mainLayout.getLayoutParams()).setMargins(m, m, m, m);
            mainLayout.setPadding(4 * m, 3 * m, 0, 2 * m);
            mainLayout.setOrientation(LinearLayout.VERTICAL);

            videoLayout = new VideoCardLayout(context);

            ViewGroup.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
            numberLayout = new FrameLayout(context);
            numberLayout.setLayoutParams(lp);

            buyingIndicator = new ImageView(context);
            lp = new LayoutParams(UI.calcSize(20), UI.calcSize(20));
            ((LayoutParams) lp).gravity = Gravity.CENTER;
            buyingIndicator.setLayoutParams(lp);
            buyingIndicator.setBackgroundDrawable(context.getResources().getDrawable(
                    isBuyed() ? R.drawable.button_green : R.drawable.button_red));

            TextView number = new TextView(context);
            lp = new LayoutParams(-2, -2);
            ((LayoutParams) lp).gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            ((LayoutParams) lp).setMargins(0, 0, 0, UI.calcSize(15));
            number.setLayoutParams(lp);
            number.setText("#" + (this.number) /*+ "\n" + this.videoID*/);
            number.setTextSize(18);
            number.setTextColor(Color.parseColor("#05025d"));
            number.setGravity(Gravity.CENTER);

            numberLayout.addView(buyingIndicator);
            numberLayout.addView(number);

            videoLayout.addView(player.getSmallDisplay());
            videoLayout.addView(numberLayout);

            mainLayout.addView(videoLayout);
            player.updateParent(videoLayout);

            this.setBackgroundResource(R.drawable.white_background_shadow);
            this.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
            this.addView(mainLayout);

        }

        public boolean isBuyed() {
            return buyed;
        }

        public void setBuyed(boolean buyed) {
            this.buyed = buyed;
            if (buyed) {
                setPlayerBack();
                if (buyingIndicator != null) {
                    buyingIndicator.setBackgroundResource(R.drawable.button_green);
                }
            }
//            else {
//                setYoutubeID("");
//            }
        }

        void setPlayerBack() {

            if (videoBackgrounds.get(getVideoID()) != null) {
                player.setBuyed(videoBackgrounds.get(getVideoID()));
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            final Bitmap b = BitmapFactory.decodeStream(
                                    new URL(DataLoader.getVideoBackRequest(videoID)).openStream());

                            ((MainActivity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        videoBackgrounds.put(getVideoID(), new BitmapDrawable(getResources(), b));
                                        player.setBuyed(videoBackgrounds.get(getVideoID()));
                                    } catch (Exception e) {
                                        Reporter.report(context, e, ((MainActivity) context).reportSubject);
                                    }
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }

        private void setVideoDescrition() {
            descriptionView = new TextView(context);
            descriptionView.setText(description);
            descriptionView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            ((LinearLayout.LayoutParams) descriptionView.getLayoutParams())
                    .setMargins(0, 0, UI.calcSize(10), 0);
            descriptionView.setTextColor(Color.parseColor("#05025d"));
            descriptionView.setTextSize(15);
            descriptionView.setGravity(Gravity.TOP);
            try {
                descriptionView.setTypeface(DataLoader.getFont(context, "comic"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            mainLayout.addView(descriptionView);
        }

        public void setYoutubeID(String youtubeID) {
            this.youtubeID = youtubeID;
            videoData[number - 1].YouTube = youtubeID;
            player.setVideoID(youtubeID);
            if (youtubeID != null && player == null) {
                setPlayer();
            }
        }


        private class VideoCardLayout extends LinearLayout {


            public VideoCardLayout(Context context) {
                super(context);
                setOrientation(LinearLayout.HORIZONTAL);
                setGravity(Gravity.LEFT);
                setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            }
        }
    }
}
