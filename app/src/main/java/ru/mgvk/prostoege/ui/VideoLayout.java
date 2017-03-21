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

    private static final String TAG = "VideoLayout";
    private Context context;
    private Task currentTask;
    private Profile.TaskData.VideoData[] videoData;
    private int maxCardsCount = 0;
    private ArrayList<VideoCard> currentVideosList = new ArrayList<>();
    private HashMap<Integer, Drawable> videoBackgrounds = new HashMap<>();
    private ArrayList<VideoCard> cardsList = new ArrayList<>();
    private VideoPlayer playingVideo;

    public VideoLayout(Context context) {
        this(context, 0);
    }

    public VideoLayout(Context context, int maxCardsCount) {
        super(context);
        this.context = context;
        if (InstanceController.getObject("VideoLayout_maxCardsCount") == null) {
            this.maxCardsCount = maxCardsCount;
        }
        else {
            this.maxCardsCount = (int) InstanceController.getObject("VideoLayout_maxCardsCount");
        }

        Log.d("VideoLayout_maxC", "" + maxCardsCount);

        setOrientation(VERTICAL);
        setLayoutParams(new LayoutParams(-1, -2));

        ((MainActivity) context).profile
                .setOnMaxVideosCountIncreased(new Profile.OnMaxVideosCountIncreased() {
                    @Override
                    public void onIncrease(int newCount) {
                        increaseCardsCount(newCount);
                    }
                });

        initCards();
        saveMaxCount();
    }

    public void increaseCardsCount(int newCount) {


        final int currSize = cardsList.size();
        if (newCount > currSize) {
            maxCardsCount = newCount;
            for (int i = currSize; i < newCount; i++) {
//                final int finalI = i;
//                ((MainActivity) context).runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
                cardsList.add(new VideoCard(i + 1));
//                    }
//                });
            }
        }

        saveMaxCount();
    }

    private void saveMaxCount() {
        try {
            InstanceController.putObject("VideoLayout_maxCardsCount", maxCardsCount);
        } catch (InstanceController.NotInitializedError notInitializedError) {
            notInitializedError.printStackTrace();
        }
    }

    private void initCards() {
        for (int i = 0; i < maxCardsCount; i++) {
            cardsList.add(new VideoCard(i + 1));
        }
    }

    private void addCard(VideoCard child) {
        super.addView(child);
        cardsList.add(child);
    }

    public void openVideosFromTask(Task task) {
        this.currentTask = task;
        videoData = task.getVideoData();
        loadVideos();
    }

    private void loadVideos() {
        try {
//            hideUnusefullCards(videoData.length);
            try {
                removeAllViews();
            } catch (Exception ignored) {
            }
            currentVideosList.clear();
            for (int i = 0; i < videoData.length; i++) {
                (cardsList.get(i)).init(
                        videoData[i].ID, videoData[i].YouTube, videoData[i].Description,
                        videoData[i].Price);
                currentVideosList.add(cardsList.get(i));
            }
        } catch (Exception e) {
            Reporter.report(context, e, ((MainActivity) context).reportSubject);
        }
    }

    private void hideUnusefullCards(int usefullCardsCount) {
        for (int i = usefullCardsCount; i < getChildCount(); i++) {
            try {
                removeViewAt(i);
            } catch (Exception ignored) {
            }
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
            this.youtubeID = youtubeID;
            setDescription(description);

            try {

                if (youtubeID == null || youtubeID.equals("")) {
                    setBuyed(false);
                    setOnClickListener();
                }
                else {
                    setBuyed(true);
                    setYoutubeID(youtubeID);
                }

            } catch (Exception e) {
                Log.e("VideoData", "Incorrect data! " + e.getLocalizedMessage());
            }
            try {
                VideoLayout.this.addView(this);
            } catch (Exception ignored) {
            }

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

        private void setDescription(final String description) {
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

            player.setOnVideoStateChangeListener(new VideoPlayer.OnVideoStateChangeListener() {
                @Override
                public void onPlay(VideoPlayer v) {
                    if (playingVideo != null && playingVideo != v) {
                        Log.d("onPlay", "stopVideo");
                        playingVideo.stop();
                    }
                    playingVideo = v;
                }

                @Override
                public void onPause(VideoPlayer v) {

                }

                @Override
                public void onStop(VideoPlayer v) {

                }

                @Override
                public void onFullScreen(VideoPlayer v) {

                }
            });

        }

        void setPlayer() {

//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
//
//            player = new VideoPlayer(context);
//            player.getSmallDisplay().setLayoutParams(lp);
            player.stop();

            player.setVideoID(youtubeID);

            if (currentTask == null) {
                return;
            }


        }

        private void initViews() {

            mainLayout = new LinearLayout(context);
            mainLayout.setLayoutParams(new LayoutParams(-1, -2));
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
            setPlayerBack();
            if (buyingIndicator != null) {
                buyingIndicator.setBackgroundResource(buyed ?
                        R.drawable.button_green
                        : R.drawable.button_red);
            }

        }

        void setOnClickListener() {
            if ((currentTask.getNumber() != 1 && currentTask.getNumber() != 2)) {
                player.getSmallDisplay().setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) context).ui.openVideoPurchaseDialog(VideoCard.this);
                    }
                });
            }

        }

        void setPlayerBack() {

            if (!buyed) {
                player.setPicture(context.getResources().getDrawable(R.drawable.video_back));
            }
            else {
                if (videoBackgrounds.get(getVideoID()) != null) {
                    player.setBuyed(videoBackgrounds.get(getVideoID()));
                }
                else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                final Bitmap b = BitmapFactory.decodeStream(
                                        new URL(DataLoader.getVideoBackRequest(videoID))
                                                .openStream());

                                ((MainActivity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            videoBackgrounds.put(getVideoID(),
                                                    new BitmapDrawable(getResources(), b));
                                            player.setBuyed(videoBackgrounds.get(getVideoID()));
                                        } catch (Exception e) {
                                            Reporter.report(context, e,
                                                    ((MainActivity) context).reportSubject);
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
//            videoData[number - 1].YouTube = youtubeID;
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
