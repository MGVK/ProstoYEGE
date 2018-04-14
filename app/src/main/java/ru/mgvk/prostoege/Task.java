package ru.mgvk.prostoege;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ru.mgvk.prostoege.ui.SwipedLinearLayout;
import ru.mgvk.prostoege.ui.UI;
import ru.mgvk.prostoege.ui.exercises.ExerciseWindow;
import ru.mgvk.util.Reporter;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by mihail on 09.08.16.
 */
public class Task extends SwipedLinearLayout implements View.OnClickListener {

    private static final String TAG = Task.class.getSimpleName();
    private Context   context;
    private ImageView image;
    private TextView  title;
    private TextView  description;
    private TextView  points;

    private int index = 0;

    private ArrayList<Exercise> exercisesList = new ArrayList<>();

    private boolean choosed    = false;
    private byte    m          = 2;
    private int     imageSize  = 64;
    private int     taskHeight = 64;
    private int     taskWidth  = 0;
    private Profile.TaskData                     data;
    private ProgressBar                          progressBar;
    private int                                  firstTimeExercises;
    private OnFirstTimeExerciseIncrementListener onFirstTimeExerciseIncrementListener;

    /**
     * @param context
     */
    Task(Context context, Profile.TaskData taskData) {
        this(context);
        this.context = context;

        this.data = taskData;
//        taskWidth = ((MainActivity) context).ui.rootView1.getWidth();
        setOrientation(LinearLayout.HORIZONTAL);
        setLayoutParams(new LayoutParams(-1, UI.calcSize(taskHeight)));
//        setBackgroundDrawable(context.getResources().getDrawable((R.drawable.task_back_1)));
//        getChildLayout().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.white_background));
//        getChildLayout().setBackgroundColor(Color.parseColor("#f7f7f7"));
        getChildLayout().setBackgroundColor(Color.WHITE);
        this.setOnClickListener(this);

        // TODO: 10.08.16 установка параметров
        try {

            setIndex(taskData.Number - 1);
//            setTaskNumberView(taskData.Number);

            setImage();

            setDescription(taskData.Number, taskData.Description);
            setPoints(taskData.Points);
            setProgressBar(taskData.Progress);

//            setVideos(taskData.Videos.Video);
            setExcersize(taskData.Questions.Questions);


            setViews();


        } catch (Exception e) {
//            LogHelper.d(e.getMessage());
            e.printStackTrace();
        }
    }

    public Task(Context context) {
        super(context);
    }

    private void setProgressBar(int progress) {
        if (progressBar == null) {
            progressBar = new ProgressBar(context);
        }
        progressBar.setMax(100);
        Log.d(TAG, "setProgressBar: " + progress);
        progressBar.setProgress(progress);
    }

    @Deprecated
    private void setVideos(Profile.TaskData.VideoData data[]) {

        int number = 1;
        for (Profile.TaskData.VideoData aData : data) {
            try {

//                videoList.add(
//                        new Video(aData.ID, aData.YouTube, aData.Description,number++,aData.Price));

            } catch (Exception e) {
                Log.e("TaskLoading", "VideoLoadingError: " + e.getLocalizedMessage());
            }
        }

    }

    public void restore() {
        onReturn();
    }

    private void setExcersize(Profile.TaskData.ExercizesData data[]) {

        for (Profile.TaskData.ExercizesData aData : data) {
            try {
                exercisesList.add(new Exercise(aData, context));
            } catch (Exception e) {
                Log.e("TaskLoading", "ExcerciseLoadingError: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public ArrayList<Exercise> getExercisesList() {
        return exercisesList == null ? new ArrayList<Exercise>() : exercisesList;
    }

    private void setImage() {
        if (image == null) {
            image = new ImageView(context);

//            image.setImageURI(Uri.parse(DataLoader.getTaskPirctureRequest(index)));
//            image.setImageResource(resId);

            setImageDrawable();

            LayoutParams lp = new LayoutParams(
                    UI.calcSize(imageSize),
                    UI.calcSize(imageSize));

//            lp.setMargins(UI.calcSize(m), UI.calcSize(m),
//                    UI.calcSize(m), UI.calcSize(m));
            lp.gravity = Gravity.CENTER;
            image.setLayoutParams(lp);
        }

    }

//    @Deprecated
//    public ArrayList<VideoLayout.VideoCard> getVideoList() {
//        return videoList == null ? new ArrayList<VideoLayout.VideoCard>() : videoList;
////        return videoList2 == null ? new ArrayList<Video>() : (ArrayList<Video>) videoList2.values();
//    }

    private void setImageDrawable() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    final Bitmap b = BitmapFactory.decodeStream(
                            new URL(DataLoader.getTaskPirctureRequest(index + 1)).openStream());

                    ((MainActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                image.setImageBitmap(b);
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

    private void setTaskNumberView(int number) {
        setTitle(context.getResources().getString(R.string.task_title) + number);
    }

    void setTitle(String text) {
        if (title == null) {
            title = new TextView(context);
            LayoutParams lp = new LayoutParams(-2, -2);
            lp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
            title.setLayoutParams(lp);
            title.setTextSize(20);
            title.setText(text);
            try {
                title.setTypeface(DataLoader.getFont(context, "comic"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            title.setTextColor(context.getResources().getColor(R.color.task_text));
        }
    }

//    Video loadVideo(String id) {
//        return videoList2.returnTo(id);
//    }

    @SuppressLint("SetTextI18n")
    private void setPoints(int p) {
        if (points == null) {
            points = new TextView(context);
        }
        LayoutParams lp = new LayoutParams(-1, UI.calcSize(23));
        lp.gravity = Gravity.BOTTOM | Gravity.END;
        lp.setMargins(UI.calcSize(5), 0, 0, UI.calcSize(2));
        points.setLayoutParams(lp);
        points.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        points.setTextSize(16);
        points.setText(p + getPointsCaption(p));
        points.setTextColor(context.getResources().getColor(R.color.task_points));
    }

    public void incrementFirstTimeExercise() {
        firstTimeExercises++;
        if (onFirstTimeExerciseIncrementListener != null) {
            onFirstTimeExerciseIncrementListener.onIncrement(getFirstTimeExercises());
        }
    }

    public void setOnFirstTimeExerciseIncrementListener(
            OnFirstTimeExerciseIncrementListener onFirstTimeExerciseIncrementListener) {
        this.onFirstTimeExerciseIncrementListener = onFirstTimeExerciseIncrementListener;
    }

    private String getPointsCaption(int p) {
        if (p == 1) {
            return " балл";
        }
        if (p <= 4 && p > 0) {
            return " балла";
        }
        return " баллов";
    }

    private void setViews() {

//        FrameLayout l = new FrameLayout(context);
//        l.setLayoutParams(new LayoutParams(UI.calcSize(taskHeight), UI.calcSize(taskHeight)));
//        l.addView(image);
        this.addView(image);

        FrameLayout layout = new FrameLayout(context);
        layout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        layout.addView(description);
        layout.addView(points);
        layout.addView(progressBar);

        this.addView(layout);
    }

    @Override
    public void onSwipe() {
        super.onSwipe();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        progressBar.setParentWidth(View.MeasureSpec.getSize(widthMeasureSpec));
    }

    @Override
    public void onClick(View v) {
        try {
            if (isChoosed()) {
                ((MainActivity) context).ui.mainScroll.toRight();
                ((MainActivity) context).addToBackStack(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) context).ui.mainScroll.toLeft();
//                        ((MainActivity) context).getBackStack().returnToState(
//                                StateTags.TASK_LIST_FRAGMENT);
//                        ((MainActivity)context).ui.openTaskOrVideoFragment(true);

                    }
                });
            } else {
                ((MainActivity) context).ui.openVideoListFragment(Task.this);
                ((MainActivity) context).ui.taskListFragment.chooseTask(index);
            }
        } catch (Exception e) {
            Reporter.report(context, e, ((MainActivity) context).reportSubject);
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
    }

    private boolean isChoosed() {
        return choosed;
    }

    public void setChoosed(boolean choosed) {
        this.choosed = choosed;
//        if (choosed) {
//            setBackgroundResource(R.drawable.task_back_2);
//        } else {
//            setBackgroundResource(R.drawable.task_back_1);
//        }
    }

    public int getNumber() {
        return getIndex() + 1;
    }

    public int getIndex() {
        return index;
    }

    private void setIndex(int id) {
        this.index = id;
    }

    public String getDescription() {
        return data.Description;
    }

    private void setDescription(int number, String text) {
        if (description == null) {
            description = new TextView(context);
        }
        LayoutParams lp = new LayoutParams(-1, UI.calcSize(42));
        lp.gravity = Gravity.TOP | Gravity.END;
        lp.setMargins(UI.calcSize(5), 0, 0, 0);
        description.setLayoutParams(lp);
        description.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        description.setText(getResources().getString(R.string.task_title) + number + " " + text);
        description.setTextSize(18);
        description.setLineSpacing(UI.calcSize(5), 0.5f);
        try {
            description.setTypeface(DataLoader.getFont(context, "comic"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        description.setTextColor(context.getResources().getColor(R.color.task_text));
    }

    private Exercise getNextExercise(Exercise exercise) {
        try {
            return exercisesList.get(exercisesList.indexOf(exercise) + 1);
        } catch (Exception e) {
            return null;
        }
    }

    public Profile.TaskData.VideoData[] getVideoData() {
        return data.Videos.Video;
    }

    public int getFirstTimeExercises() {
        return firstTimeExercises;
    }

    public void setFirstTimeExercises(int firstTimeExercises) {
        this.firstTimeExercises = firstTimeExercises;
    }

    public int getMinExercises() {
        return data.MinQuestion;
    }

    public int getCompleteExercises() {
        return data.CompletQuestion;
    }

    public interface OnFirstTimeExerciseIncrementListener {
        void onIncrement(int currentValue);
    }

//    public class Video {
//
//        int videoID = 0;
//        byte m = (byte) UI.calcSize(5);
//        LinearLayout mainLayout;
//        FrameLayout numberLayout;
//        VideoLayout videoLayout;
//        ImageView buyingIndicator;
//        private String youtubeID = "";
//        private String Description = "";
//        private VideoPlayer player;
//        private boolean buyed = false;
//        private int number=1;
//        private int price=0;
//
//        /**
//         *
//         */
//
//        public Video(int id, String youtubeID, String description,int number,int price) {
//
//            super(context);
//            this.videoID = id;
//            this.number = number;
//            this.price = price;
//            try {
//
//                if (youtubeID == null||youtubeID.equals("")) {
//                    setBuyed(false);
//                } else {
//                    this.youtubeID = youtubeID;
//                    setBuyed(true);
//                }
//                Description = description;
//
//
//            } catch (Exception e) {
//                Log.e("VideoData", "Incorrect data! " + e.getLocalizedMessage());
//            }
//
//
//            setPlayer();
//
//
//            initViews();
//
//
//            setVideoDescrition();
//
//        }
//
//        public int getNumber() {
//            return number;
//        }
//
//        public int getPrice(){
//            return price;
//        }
//
//        public int getVideoID() {
//            return videoID;
//        }
//
//        public void setOnVideoStateChangeListener() {
//
//        }
//
//        public boolean isPlaying() {
//            return player != null && player.isPlaying();
//        }
//
//        public void stop() {
//            if (player != null) {
//                player.stop();
//            }
//        }
//
//        public void pause() {
//            if (player != null && isPlaying()) {
//                player.pause();
//            }
//        }
//
//        public void start() {
//            if (player != null) {
//                player.start();
//            }
//        }
//
//        public void updateSizes(int w, int h) {
//            if (player != null) {
//
//                player.getSmallDisplay().setLayoutParams(new LinearLayout.LayoutParams(
//                        (int) (0.75 * w), h = (int) ((9 / 16.0) * 0.75 * w)));
//            }
//
////            numberLayout.setLayoutParams(new LinearLayout.LayoutParams((int) (0.15 * w), h));
////            videoLayout.setLayoutParams(new LinearLayout.LayoutParams(w - UI.calcSize(2 * m), h));
//
//        }
//
//        public String getDescription() {
//            return Description;
//        }
//
//        public Task getTask(){
//            return Task.this;
//        }
//
//        void setPlayer() {
//
//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
//
//            player = new VideoPlayer(context);
//            player.setVideoID(youtubeID);
//            player.getSmallDisplay().setLayoutParams(lp);
//            if(Task.this.getNumber()==1||Task.this.getNumber()==2){
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            final Bitmap b = BitmapFactory.decodeStream(
//                                    new URL(DataLoader.getVideoBackRequest(videoID)).openStream());
//
//                            ((MainActivity) context).runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    try {
//                                        player.setPicture(new BitmapDrawable(getResources(),b));
//                                    } catch (Exception e) {
//                                        Reporter.report(context, e, ((MainActivity) context).reportSubject);
//                                    }
//                                }
//                            });
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//
//            }else{
//                player.setPicture(context.getResources().getDrawable(R.drawable.video_back));
//            }
//            if(!buyed&&(Task.this.getNumber()!=1&&Task.this.getNumber()!=2)) {
//                player.getSmallDisplay().setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onKeyClicked(View v) {
//                        ((MainActivity) context).ui.openVideoPurchaseDialog(Video.this);
//                    }
//                });
//            }
//        }
//
//        private void initViews() {
//            mainLayout = new LinearLayout(context);
//            mainLayout.setLayoutParams(new LayoutParams(-1, -1));
//            ((LayoutParams) mainLayout.getLayoutParams()).setMargins(m, m, m, m);
//            mainLayout.setPadding(4 * m, 3 * m, 0, 2 * m);
//            mainLayout.setOrientation(LinearLayout.VERTICAL);
//
//            videoLayout = new VideoLayout(context);
//
//            ViewGroup.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
//            numberLayout = new FrameLayout(context);
//            numberLayout.setLayoutParams(lp);
//
//            buyingIndicator = new ImageView(context);
//            lp = new LayoutParams(UI.calcSize(20), UI.calcSize(20));
//            ((LayoutParams) lp).gravity = Gravity.CENTER;
//            buyingIndicator.setLayoutParams(lp);
//            buyingIndicator.setBackgroundDrawable(context.getResources().getDrawable(
//                    isBuyed() ? R.drawable.button_green : R.drawable.button_red));
//
//            TextView number = new TextView(context);
//            lp = new LayoutParams(-2, -2);
//            ((LayoutParams) lp).gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
//            ((LayoutParams) lp).setMargins(0, 0, 0, UI.calcSize(15));
//            number.setLayoutParams(lp);
//            number.setText("#" + (this.number) + "\n" + this.videoID);
//            number.setTextSize(18);
//            number.setTextColor(Color.parseColor("#05025d"));
//            number.setGravity(Gravity.CENTER);
//
//            numberLayout.addView(buyingIndicator);
//            numberLayout.addView(number);
//
//            videoLayout.addView(player.getSmallDisplay());
//            videoLayout.addView(numberLayout);
//
//            mainLayout.addView(videoLayout);
//            player.updateParent(videoLayout);
//
//            this.setBackgroundResource(R.drawable.white_background_shadow);
//            this.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
//            this.addView(mainLayout);
//
//        }
//
//        public boolean isBuyed() {
//            return buyed;
//        }
//
//        public void setBuyed(boolean buyed) {
//            this.buyed = buyed;
//            if (buyed) {
//                buyedVideos.add(this);
//                setPlayerBack();
//                if (buyingIndicator != null) {
//                    buyingIndicator.setBackgroundResource(R.drawable.button_green);
//                }
//            }
//        }
//
//        void setPlayerBack() {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//
//                        final Bitmap b = BitmapFactory.decodeStream(
//                                new URL(DataLoader.getVideoBackRequest(videoID)).openStream());
//
//                        ((MainActivity) context).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    player.setBuyed(new BitmapDrawable(getResources(), b));
//                                } catch (Exception e) {
//                                    Reporter.report(context, e, ((MainActivity) context).reportSubject);
//                                }
//                            }
//                        });
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//        }
//
//        void setVideoDescrition() {
//            TextView descr = new TextView(context);
//            descr.setText(Description);
//            descr.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
//            ((LinearLayout.LayoutParams) descr.getLayoutParams()).setMargins(0, 0, UI.calcSize(10), 0);
//            descr.setTextColor(Color.parseColor("#05025d"));
//            descr.setTextSize(15);
//            descr.setGravity(Gravity.TOP);
//            try {
//                descr.setTypeface(DataLoader.getFont(context, "comic"));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            mainLayout.addView(descr);
//        }
//
//        public void setYoutubeID(String youtubeID) {
//            this.youtubeID = youtubeID;
//            player.setVideoID(youtubeID);
//            if (youtubeID != null && player == null) {
//                setPlayer();
//            }
//        }
//
//        class VideoLayout extends LinearLayout {
//
//
//            public VideoLayout(Context context) {
//                super(context);
//                setOrientation(LinearLayout.HORIZONTAL);
//                setGravity(Gravity.LEFT);
//                setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
//            }
//        }
//    }

    static class ProgressBar extends View {

        boolean confChanged = true;
        private LayoutParams lp;
        private double max              = 0;
        private double oneDivisionWidth = 0;
        private double progress         = 0;
        private int    parentWidth      = 0;

        public ProgressBar(Context context) {
            super(context);
            lp = new LayoutParams(-1, UI.calcSize(2));
            lp.gravity = Gravity.BOTTOM | Gravity.START;
            setLayoutParams(lp);
            setBackgroundResource(R.color.task_progress);
        }

        public void setMax(double max) {
            this.max = max;
            updateSizes();
        }

        public void setOneDivisionWidth(double dw) {
            this.oneDivisionWidth = dw;
        }

        public void setProgress(double progress) {
            this.progress = progress;
            Log.d("ProgressBar", "setProgress: " + progress);
            updateSizes();
        }

        private void updateSizes() {
            if (oneDivisionWidth != 0) {
                //КОСТЫЛЬ МАТЬ ЕГО. НЕ ТРОГАТЬ!!!
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lp = new LayoutParams((int) (oneDivisionWidth * progress),
                                        UI.calcSize(2));

                                Log.d("ProgressBar", "run: " + lp.width);

                                lp.gravity = Gravity.BOTTOM | Gravity.START;
                                setLayoutParams(lp);
                            }
                        });
                    }
                }).start();
            }
        }

        @Override
        protected void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            confChanged = true;
        }

        public void setParentWidth(int width) {
            if (confChanged) {
                parentWidth = width;
                setOneDivisionWidth(width / max);
                updateSizes();
                confChanged = false;
            }
        }
    }

    public static class Exercise extends FrameLayout implements OnClickListener {


        private final View indicator;
        int Status = ExerciseWindow.NOT_DECIDED;
        TextView title;
        private String tmpText = "";
        private Profile.TaskData.ExercizesData data;


        Exercise(Profile.TaskData.ExercizesData data, Context context) {
            super(context);
            this.data = data;
            Status = data.Status;
            inflate(context, R.layout.exercise_item, this);
            this.title = (TextView) findViewById(R.id.title);
            this.indicator = findViewById(R.id.indicator);
            setOnClickListener(this);
            setTag(-1);
            setTitle();
            setIndicator();
        }


        public Task getTask() {
            return ((MainActivity) getContext()).ui.taskListFragment.getCurrentTask();
        }

        void setTitle() {
            title.setText("Задача №" + data.Number);
        }

        void setIndicator() {
            indicator.setBackgroundResource(ExerciseWindow.indicators[getStatus()]);
        }

        public String getTmpText() {
            return tmpText;
        }

        public void setTmpText(String tmpText) {
            this.tmpText = tmpText;
        }

        public int getHintID() {
            return data.ID;
        }

        public int getHintPrice() {
            return data.PriceHint;
        }

        void setIndicatorColor(int resId) {
            indicator.setBackgroundResource(resId);
        }

        public Profile.TaskData.ExercizesData getData() {
            return data;
        }

        public boolean hintIsBought() {
            return data.hintIsBought;
        }

        @Override
        public void onClick(View v) {
            ((MainActivity) getContext()).ui.exercisesListFragment
                    .getExerciseWindow().openExercise(this);
//            ((MainActivity) context).ui.exercisesListFragment.scrollListUp();
        }

        public int getStatus() {
            return Status;
        }

        public void setStatus(int status) {
            this.Status = status;
            setIndicatorColor(ExerciseWindow.indicators[status]);
            try {
                if (data.hintIsBought) {
                    status += 10;
                }
                DataLoader.putQuestion(data.ID, status);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean isPromted() {
            return (Status == ExerciseWindow.PROMPTED);
        }

        public Exercise getNextExercise() {
            return getTask().getNextExercise(this);
        }

        public boolean isSolved() {
            return (getStatus() == ExerciseWindow.DECIDED_FIRSTLY
                    || getStatus() == ExerciseWindow.DECIDED_SECONDLY);
        }

        public void setHintIsBought() {
            data.hintIsBought = true;
        }

        public int getBonus1() {
            return data.Bonus1;
        }

        public int getBonus2() {
            return data.Bonus2;
        }

    }
}

