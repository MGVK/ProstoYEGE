package ru.mgvk.prostoege;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ru.mgvk.prostoege.ui.*;
import ru.mgvk.util.Reporter;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mihail on 09.08.16.
 */
public class Task extends SwipedLinearLayout implements View.OnClickListener {

    Context context;
    ImageView image;
    TextView title;
    TextView description;
    TextView points;

    int index = 0;


    ArrayList<Video> videoList = new ArrayList<>();
    ConcurrentHashMap<String, Video> videoList2 = new ConcurrentHashMap<>();
    ArrayList<Video> buyedVideos = new ArrayList<>();
    ArrayList<Exercise> exercisesList = new ArrayList<>();

    boolean choosed = false;
    private byte m = 2;
    private int imageSize = 65;
    private int taskHeight = 65 + 2 * (m + 5);
    private int taskWidth = 0;

    private Profile.TaskData data;

    /**
     * @param context
     */
    public Task(Context context, Profile.TaskData taskData) {
        this(context);
        this.context = context;

        this.data = taskData;
//        taskWidth = ((MainActivity) context).ui.rootView1.getWidth();


        setOrientation(LinearLayout.HORIZONTAL);
        setLayoutParams(new LayoutParams(-1, UI.calcSize(taskHeight + 2 * m)));
        setBackgroundDrawable(context.getResources().getDrawable((R.drawable.task_back_1)));
//        getChildLayout().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.white_background));
        getChildLayout().setBackgroundColor(Color.parseColor("#f7f7f7"));
        this.setOnClickListener(this);


        // TODO: 10.08.16 установка параметров
        try {

            setIndex(taskData.Number - 1);
            setTaskNumber(taskData.Number);
            setImage();


            setDescription(taskData.Description);
            setPoints(taskData.Points);
            setVideos(taskData.Videos.Video);
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

    private void setVideos(Profile.TaskData.VideoData data[]) {

        int number=1;
        for (Profile.TaskData.VideoData aData : data) {
            try {

                videoList.add(
                        new Video(aData.ID, aData.YouTube, aData.Description,number++,aData.Price));
            } catch (Exception e) {
                Log.e("TaskLoading", "VideoLoadingError: " + e.getLocalizedMessage());
            }
        }

    }

    public void restore() {
        onReturn();
    }

    void setExcersize(Profile.TaskData.ExercizesData data[]) {

        for (Profile.TaskData.ExercizesData aData : data) {
            try {
                exercisesList.add(new Exercise(aData, context));
            } catch (Exception e) {
                Log.e("TaskLoading", "ExcerciseLoadingError: " + e.getMessage());
            }
        }
    }

    public ArrayList<Exercise> getExercisesList() {
        return exercisesList == null ? new ArrayList<Exercise>() : exercisesList;
    }

    public ArrayList<Video> getVideoList() {
        return videoList == null ? new ArrayList<Video>() : videoList;
//        return videoList2 == null ? new ArrayList<Video>() : (ArrayList<Video>) videoList2.values();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int id) {
        this.index = id;
    }

    void setImage() {
        if (image == null) {
            image = new ImageView(context);
//            image.setImageURI(Uri.parse(DataLoader.getTaskPirctureRequest(index)));
//            image.setImageResource(resId);

            setImageDrawable();

            LayoutParams lp = new LayoutParams(
                    UI.calcSize(imageSize),
                    UI.calcSize(imageSize));

            lp.setMargins(UI.calcSize(m), UI.calcSize(m),
                    UI.calcSize(m), UI.calcSize(m));
            lp.gravity = Gravity.CENTER;
            image.setLayoutParams(lp);
        }

    }

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

    Video getVideo(String id) {
        return videoList2.get(id);
    }

    void setTaskNumber(int number) {
        setTitle(context.getResources().getString(R.string.task_title) + " " + number);
    }

    void setTitle(String text) {
        if (title == null) {
            title = new TextView(context);
            LayoutParams lp = new LayoutParams(-1, -2);
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

    @SuppressLint("SetTextI18n")
    void setPoints(int p) {
        if (points == null) {
            points = new TextView(context);
            LayoutParams lp = new LayoutParams(-2, -2);
            lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            points.setGravity(Gravity.BOTTOM | Gravity.END);
            points.setLayoutParams(lp);
            points.setTextSize(15);
            points.setText(p + getPointsCaption(p));
            points.setTextColor(context.getResources().getColor(R.color.task_points));
        }
    }

    String getPointsCaption(int p) {
        if (p == 1) {
            return " балл";
        }
        if (p <= 4 && p > 0) {
            return " балла";
        }
        return " баллов";
    }

    void setViews() {

        FrameLayout l = new FrameLayout(context);
        l.setLayoutParams(new LayoutParams(UI.calcSize(taskHeight), UI.calcSize(taskHeight)));

        FrameLayout l2 = new FrameLayout(context);
        l2.setLayoutParams(new LayoutParams(UI.calcSize(imageSize + 2 * m), UI.calcSize(imageSize + 2 * m)));
        ((LayoutParams) l2.getLayoutParams()).setMargins(UI.calcSize(m), UI.calcSize(m),
                UI.calcSize(m), UI.calcSize(m));
        l2.addView(image);
        l2.setBackgroundResource(R.color.task_imag_backgroung);
        l.addView(l2);
        this.addView(l);

        LinearLayout layout = new LinearLayout(context);

        layout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams lp = new LayoutParams(-1, -2);
        lp.setMargins(5, 5, 0, 5);
        lp.gravity = Gravity.TOP;
        layout.setGravity(Gravity.CENTER);
        layout.setLayoutParams(lp);
        layout.addView(title);
        layout.addView(description);
//        layout.addView(points);
        this.addView(layout);

        this.addView(points);
    }

    @Override
    public void onSwipe() {
        super.onSwipe();

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

    public boolean isChoosed() {
        return choosed;
    }

    public void setChoosed(boolean choosed) {
        this.choosed = choosed;
        if (choosed) {
            setBackgroundResource(R.drawable.task_back_2);
//            for (Exercise exercise : exercisesList) {
//                try {
//                    exercise.loadDescription();
//                } catch (Exception ignored) {
//                }
//            }
        } else {
            setBackgroundResource(R.drawable.task_back_1);
        }
    }

    public int getNumber() {
        return index + 1;
    }

    public String getDescription() {
        return data.Description;
    }

    void setDescription(String text) {
        if (description == null) {
            description = new TextView(context);
        }
        LayoutParams lp = new LayoutParams(-1, -2, 1);
        lp.setMargins(15, 0, 0, 0);
        description.setLayoutParams(lp);
        description.setText(text);
        description.setTextSize(17);
        try {
            description.setTypeface(DataLoader.getFont(context, "comic"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        description.setTextColor(context.getResources().getColor(R.color.task_text));
    }

    public Exercise getNextExercise(Exercise exercise) {
        try {
            return exercisesList.get(exercisesList.indexOf(exercise) + 1);
        } catch (Exception e) {
            return null;
        }
    }

    public class Video extends FrameLayout {

        int videoID = 0;
        byte m = (byte) UI.calcSize(5);
        LinearLayout mainLayout;
        FrameLayout numberLayout;
        VideoLayout videoLayout;
        ImageView buyingIndicator;
        private String youtubeID = "";
        private String Description = "";
        private VideoPlayer player;
        private boolean buyed = false;
        private int number=1;
        private int price=0;

        /**
         *
         */

        public Video(int id, String youtubeID, String description,int number,int price) {

            super(context);
            this.videoID = id;
            this.number = number;
            this.price = price;
            try {

                if (youtubeID == null||youtubeID.equals("")) {
                    setBuyed(false);
                } else {
                    this.youtubeID = youtubeID;
                    setBuyed(true);
                }
                Description = description;


            } catch (Exception e) {
                Log.e("VideoData", "Incorrect data! " + e.getLocalizedMessage());
            }


            setPlayer();


            initViews();


            setVideoDescrition();

        }

        public int getNumber() {
            return number;
        }

        public int getPrice(){
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
            return Description;
        }

        public Task getTask(){
            return Task.this;
        }

        void setPlayer() {

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);

            player = new VideoPlayer(context);
            player.setVideoID(youtubeID);
            player.getSmallDisplay().setLayoutParams(lp);
            if(Task.this.getNumber()==1||Task.this.getNumber()==2){

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
                                        player.setPicture(new BitmapDrawable(getResources(),b));
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

            }else{
                player.setPicture(context.getResources().getDrawable(R.drawable.video_back));
            }
            if(!buyed&&(Task.this.getNumber()!=1&&Task.this.getNumber()!=2)) {
                player.getSmallDisplay().setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            ((MainActivity) context).ui.openVideoPurchaseDialog(Video.this);
                        } catch (Exception e) {
                            Reporter.report(context, e, ((MainActivity) context).reportSubject);
                        }
                    }

                });
            }
        }

        private void initViews() {
            mainLayout = new LinearLayout(context);
            mainLayout.setLayoutParams(new LayoutParams(-1, -1));
            ((LayoutParams) mainLayout.getLayoutParams()).setMargins(m, m, m, m);
            mainLayout.setPadding(4 * m, 3 * m, 0, 2 * m);
            mainLayout.setOrientation(LinearLayout.VERTICAL);

            videoLayout = new VideoLayout(context);

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
                buyedVideos.add(this);
                setPlayerBack();
                if (buyingIndicator != null) {
                    buyingIndicator.setBackgroundResource(R.drawable.button_green);
                }
            }
        }

        void setPlayerBack() {
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
                                    player.setBuyed(new BitmapDrawable(getResources(), b));
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

        void setVideoDescrition() {
            TextView descr = new TextView(context);
            descr.setText(Description);
            descr.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            ((LinearLayout.LayoutParams) descr.getLayoutParams()).setMargins(0, 0, UI.calcSize(10), 0);
            descr.setTextColor(Color.parseColor("#05025d"));
            descr.setTextSize(15);
            descr.setGravity(Gravity.TOP);
            try {
                descr.setTypeface(DataLoader.getFont(context, "comic"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            mainLayout.addView(descr);
        }

        public void setYoutubeID(String youtubeID) {
            this.youtubeID = youtubeID;
            player.setVideoID(youtubeID);
            if (youtubeID != null && player == null) {
                setPlayer();
            }
        }

        class VideoLayout extends LinearLayout {


            public VideoLayout(Context context) {
                super(context);
                setOrientation(LinearLayout.HORIZONTAL);
                setGravity(Gravity.LEFT);
                setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            }
        }
    }

    public class Exercise extends FrameLayout implements OnClickListener {


        int Status = ExerciseWindow.NOT_DECIDED;

        private String tmpText="";

        private int Number = 0;
        private Profile.TaskData.ExercizesData data;


        public Exercise(Profile.TaskData.ExercizesData data, Context context) {
            super(context);
            this.data = data;
            Status = data.Status;
            LayoutParams lp = new LayoutParams(-1, UI.calcSize(60));
            lp.setMargins(UI.calcSize(20), 0, 0, 0);
            this.setLayoutParams(lp);
            setBackgroundResource((R.drawable.exercises_back));
            setOnClickListener(this);
            setTag(-1);
            setNumber(data.Number);
            setTitle();
            setCircule();
        }

        /**
         * Изображение с номером задания
         *
         * @param number >=0
         */

        void setNumber(int number) {
            this.Number = number;
            ExerciseNumberImage v = new ExerciseNumberImage(context);
            v.setNumber(number);
            LayoutParams lp = new LayoutParams(UI.calcSize(50),
                    UI.calcSize(50));
            lp.setMargins(UI.calcSize(5), 0, 0, 0);
            lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            v.setLayoutParams(lp);
            this.addView(v);
        }

        void setTitle() {
            TextView title = new TextView(context);
            title.setTextSize(20);

            LayoutParams lp = new LayoutParams(-2, -2);
//            lp.gravity = Gravity.CENTER_HORIZONTAL|Gravity.TOP;
            lp.gravity = Gravity.CENTER;
            title.setLayoutParams(lp);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(context.getResources().getColor(R.color.task_text));
            title.setText("Задача " + this.Number);

            try {
                title.setTypeface(DataLoader.getFont(context, "comic"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.addView(title);
        }

        void setCircule() {
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(ExerciseWindow.indicators[Status]);
            LayoutParams lp = new LayoutParams(-2, UI.calcSize(25));
            lp.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            lp.setMargins(0, 0, UI.calcSize(10), 0);
            imageView.setLayoutParams(lp);
            this.addView(imageView);
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
            ((ImageView) getChildAt(2)).setImageResource(resId);
        }

        public Profile.TaskData.ExercizesData getData() {
            return data;
        }

        @Override
        public void onClick(View v) {
            try {
                ((MainActivity) context).ui.exercisesListFragment
                        .getExerciseWindow().openExercise(this);
//            ((MainActivity) context).ui.exercisesListFragment.scrollListUp();
            } catch (Exception e) {
                Reporter.report(context, e, ((MainActivity) context).reportSubject);
            }
        }

        public int getStatus() {
            return Status;
        }

        public void setStatus(int status) {
            this.Status = status;
            try {
                DataLoader.putQuestion(data.ID,status);
            } catch (Exception e) {
                e.printStackTrace();
            }
            setIndicatorColor(ExerciseWindow.indicators[status]);
        }

        public boolean isPromted() {
            return (Status == ExerciseWindow.PROMPTED);
        }

        public Exercise getNextExercise() {
            return Task.this.getNextExercise(this);
        }

        public boolean isSolved() {
            return (getStatus() == ExerciseWindow.DECIDED);
        }
    }


}

