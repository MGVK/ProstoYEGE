package ru.mgvk.prostoege;

import com.google.gson.Gson;

/**
 * Created by mihail on 14.09.16.
 */
public class Profile {
    public int Coins = 0;
    public String ID;
    public int Repost = 0;
    public TaskData Tasks[];
    public int maxVideoCount = 0;
    private OnMaxVideosCountIncreased onMaxVideosCountIncreased;

    void getVideos() {
        for (final TaskData task : Tasks) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadVideo(task);
                }
            }).run();
        }
    }

    void getQuestions() {
        for (TaskData task : Tasks) {
            loadQuestion(task);
        }
    }

    Videos loadVideo(TaskData taskData) {
        try {
            return taskData.Videos = new Gson()
                    .fromJson(DataLoader.getVideo(taskData.Number), Videos.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    Questions loadQuestion(TaskData taskData) {
        try {
            return taskData.Questions = new Gson().fromJson(DataLoader.getQuestion(taskData.Number), Questions.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Deprecated
    void _prepareData() {
        getVideos();
        getQuestions();
        try {
            if (Tasks.length > 0) {
                for (TaskData task : Tasks) {
                    prepareData(task);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void prepareData(TaskData task) {
        TaskData.ExercizesData newQ[] =
                new TaskData.ExercizesData[task.Questions.Questions.length];
        for (TaskData.ExercizesData question : task.Questions.Questions) {
            newQ[question.Number - 1] = question;


            //костыль. Страшный костыль.
            // TODO: 29.06.17 ИСправить страшный костыль #3_9
            if (question.Status > 10) {
                question.hintIsBought = true;
                question.Status -= 10;
            }
        }
        task.Questions.Questions = newQ;
        newQ = null;

        if (task.Videos.Video.length > maxVideoCount) {
            maxVideoCount = task.Videos.Video.length;
            if (onMaxVideosCountIncreased != null) {
                onMaxVideosCountIncreased.onIncrease(maxVideoCount);
            }
        }
    }

    public void setOnMaxVideosCountIncreased(OnMaxVideosCountIncreased onMaxVideosCountIncreased) {
        this.onMaxVideosCountIncreased = onMaxVideosCountIncreased;
    }

    public interface OnLoadCompleted {
        void onCompleted(boolean restoring);
    }

    public interface OnMaxVideosCountIncreased {

        void onIncrease(int newCount);

    }

    public static class Questions {
        TaskData.ExercizesData Questions[];
    }

    public class TaskData {
        public Videos    Videos;
        public Questions Questions;
        String Description = "Описание";
        int    Points      = 2;
        int Number;

        public int getPictureID() {
            return R.drawable.ti_1;
        }

        public class VideoData {
            public int    ID          = 0;
            public int    Number      = 0;
            public String Description = "description";
            public int    Price       = 0;
            public String YouTube     = "";
        }

        public class ExercizesData {
            public int     ID           = 0;
            public int     Number       = 0;
            public int     PriceHint    = 0;
            public String  Answer       = "_0_o__...";
            public int     Status       = 0;
            public boolean hintIsBought = false;
            public int     Bonus1       = 0;
            public int     Bonus2       = 0;
        }
    }

    public class Videos {
        TaskData.VideoData Video[];
    }

}
