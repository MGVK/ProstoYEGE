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

    void getVideos() {
        for (final TaskData task : Tasks) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        task.Videos = new Gson().fromJson(DataLoader.getVideo(task.Number), Videos.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).run();
        }
    }

    void getQuestions() {
        for (TaskData task : Tasks) {
            try {
                task.Questions = new Gson().fromJson(DataLoader.getQuestion(task.Number), Questions.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void prepareData() {
        getVideos();
        getQuestions();
        try {
            if (Tasks.length > 0) {
                for (TaskData task : Tasks) {
                    TaskData.ExercizesData newQ[] = new TaskData.ExercizesData[task.Questions.Questions.length];
                    for (TaskData.ExercizesData question : task.Questions.Questions) {
                        newQ[question.Number - 1] = question;
                    }
                    task.Questions.Questions = newQ;
                    newQ = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class TaskData {
        public Videos Videos;
        public Questions Questions;
        String Description = "Описание";
        int Points = 2;
        int Number;

        public int getPictureID() {
            return R.drawable.ti_1;
        }

        class VideoData {
            int ID = 0;
            int Number = 0;
            String Description = "description";
            int Price=0;
            String YouTube = "";
        }

        public class ExercizesData {
            public int ID = 0;
            public int Number = 0;
            public int PriceHint = 0;
            public String Answer = "_0_o__...";
            public int Status = 0;
            public int Bonus1 = 0;
            public int Bonus2 = 0;
        }
    }

    public class Videos {
        TaskData.VideoData Video[];
    }

    public class Questions {
        TaskData.ExercizesData Questions[];
    }
}
