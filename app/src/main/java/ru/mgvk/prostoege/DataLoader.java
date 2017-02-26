package ru.mgvk.prostoege;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * Created by mihail on 10.08.16.
 */
public class DataLoader {

    public final static String ExcerciseDescriptionRequest = "http://213.159.214.5/script/mobile/1/question_load.php?ID=";
    public final static String ExcerciseHintRequest = "http://213.159.214.5/script/mobile/1/hint_load.php?ID=";
    public final static String PolicyURL = "http://213.159.214.5/policy.html";
    private final static String POLICY_SETTINGS = "POLICY";
    private final static Object b = new Object();
    static ArrayList<Task> taskList = new ArrayList<>();
    static int tasks_ready = 0;
    static Context context;
    static boolean loadingInThread = true;
    private static String url = null;

    DataLoader(Context context) {

    }

    public static ArrayList<Task> loadTasks(Context pcontext) {
        context = pcontext;
        taskList = new ArrayList<>();
        tasks_ready = 0;

        final Profile p = ((MainActivity) context).profile;
        Log.d("taskLoading", "Profile " + p);

        long T = System.currentTimeMillis();
        for (int i = 0; i < p.Tasks.length; i++) {
            taskList.add(null);
        }


        for (int i = 0; i < p.Tasks.length; i++) {
            final int task_i = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long t = System.currentTimeMillis();

                    taskList.set(task_i, new Task(context, p.Tasks[task_i]));

                    Log.d("time_loadTask_" + task_i, (System.currentTimeMillis() - t) + "");
                    tasks_ready++;
                }
            }).start();
        }

        while (tasks_ready < taskList.size()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Log.d("time_loadAllTask", System.currentTimeMillis() - T + "");


        return taskList;
    }

    public static ArrayList<Task> _loadTasks(Context pcontext) {
        context = pcontext;
        taskList = new ArrayList<>();

        final Profile p = ((MainActivity) context).profile;
        Log.d("taskLoading", "Profile " + p);

        long T = System.currentTimeMillis();

        for (int i = 0; i < p.Tasks.length; i++) {
            taskList.add(null);
        }

        loadingInThread = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int task_i = p.Tasks.length/2; task_i < p.Tasks.length; task_i++) {
                    long t = System.currentTimeMillis();

                    taskList.set(task_i,new Task(context, p.Tasks[task_i]));

                    Log.d("time_loadTask_" + task_i, (System.currentTimeMillis() - t) + "");
                }
                loadingInThread = false;
            }
        }).start();

        for (int task_i = 0; task_i < p.Tasks.length/2; task_i++) {
            long t = System.currentTimeMillis();

            taskList.set(task_i,new Task(context, p.Tasks[task_i]));

            Log.d("time_loadTask_" + task_i, (System.currentTimeMillis() - t) + "");
        }

        while(loadingInThread){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Log.d("time_loadAllTask", System.currentTimeMillis() - T + "");

        return taskList;
    }

    public static ArrayList<Task> __loadTasks(Context pcontext) {
        context = pcontext;
        taskList = new ArrayList<>();

        MainActivity mainActivity = (MainActivity) context;

        final Profile p = ((MainActivity) context).profile;
        Log.d("taskLoading", "Profile " + p);

        mainActivity.stopwatch.checkpoint("Loading Tasks start");

        for (int task_i = 0; task_i < p.Tasks.length; task_i++) {
            long t = System.currentTimeMillis();

            taskList.add(new Task(context, p.Tasks[task_i]));

            mainActivity.stopwatch.checkpoint("Loading Task: " + task_i);
        }

        mainActivity.stopwatch.checkpoint("Loading Tasks finish");

        return taskList;
    }

    public static String getProfile(String profileId) throws Exception{
        //        Log.d("Profile",s);
        return getResponse("http://213.159.214.5/script/mobile/2/profile.php", "ProfileID=" + profileId);
    }

    public static String getVideo(int number) throws Exception{
        return "{\"Video\":" +
                getResponse("http://213.159.214.5/script/mobile/2/video/array.php",
                        "ProfileID=" + MainActivity.PID + "&TaskNumber=" + number) + "}";
    }

    public static String getQuestion(int number) throws Exception{
        return "{\"Questions\":" +
                getResponse("http://213.159.214.5/script/mobile/2/question/array.php",
                        "ProfileID=" + MainActivity.PID + "&TaskNumber=" + number) + "}";
    }


    public static String buyVideo(int id) throws Exception{
        return getResponse("http://213.159.214.5/script/mobile/2/video/buy.php", "ProfileID=" + MainActivity.PID + "+&VideoID=" + id);
    }

    public static String buyHint(int id) throws Exception{
        return getResponse("http://213.159.214.5/script/mobile/2/question/hint/buy.php", "ProfileID=" + MainActivity.PID + "&QuestionID=" + id);
    }

    public static String buyCoins(int count) throws Exception{
        return getResponse("http://213.159.214.5/script/mobile/2/buy.php", "ProfileID=" + MainActivity.PID + "&Coins=" + count);
    }

    public static String sendQuestions() throws Exception{
        return getResponse("http://213.159.214.5/script/mobile/2/ProfileID.php", "ProfileID=" + MainActivity.PID);
    }

    public static String putRepost() throws Exception {
        return getResponse("http://213.159.214.5/script/mobile/2/repost.php", "ProfileID=" + MainActivity.PID + "&Check=1");
    }

    public static String putQuestion(int id, int status) throws Exception {
        return getResponse("http://213.159.214.5/script/mobile/2/question/save.php", "ProfileID=" + MainActivity.PID + "&Question=" + id + ":" + status + "|");
    }


    static String getResponse(String url, String params) throws Exception {

        String line = "", result = "";

            HttpURLConnection connection = (HttpURLConnection)
                    new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.getOutputStream().write(params.getBytes());
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = br.readLine()) != null) {
                result = line;
            }
            br.close();

        return result;
    }

    public static String getVideoURI(final String ID) {

        Log.d("DataLoader", "Parsing video: " + ID);

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (b) {
                    url = getVideoURL(ID);
                    b.notifyAll();
                }
            }
        }).start();

        synchronized (b) {
            try {
                b.wait(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    static String getVideoURL(String id) {
        String urlToRead = "http://www.youtube.com/get_video_info?video_id=" + id;

        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        String result = "";
        try {
            url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();

            for (String s : result.split("&")) {
                if (s.contains("url_encoded_fmt_stream_map")) {
                    return (decode(s.substring("url_encoded_fmt_stream_map=".length())));
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return "0_0";

    }

    public static boolean acceptLicense(Context context) {
//        String filePath = context.getPackageResourcePath()+"license";
//        File file;
//        boolean res=false;
//        try {
//            file = new File(filePath);
//            if(!file.exists()&&!file.createNewFile()){
//                return false;
//            }
//            BufferedWriter writer= new BufferedWriter(new FileWriter(file));
//            writer.write(new char[]{'1'});
//            writer.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        context.getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE)
                .edit().putInt(POLICY_SETTINGS, 1).apply();

        return isLicenseAccepted(context);
    }

    public static boolean isLicenseAccepted(Context context) {

//        String filePath = context.getPackageResourcePath()+"license";
//        boolean res=false;
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
//            char[] c = new char[1];
//            if(reader.read(c)!=-1){
//                res = (c==new char[]{'1'});
//            }
//
//            reader.close();
//
//        } catch (IOException e) {
//            e.printStackTrace(
// );
//        }
        try {
            return 1 == context.getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE)
                    .getInt(POLICY_SETTINGS, 0);
        } catch (Exception e) {
            throw e;
        }
    }


    private static String decode(String s) {
        s = URLDecoder.decode(s);
        String p[] = s.split(",");
        for (String s1 : p) {
            if (s1.contains("itag=22")) {
//                return extractURL(URLDecoder.decode(URLDecoder.decode(s1)));
                return URLDecoder.decode(extractURL(s1));
            }
        }
        return "0_0";
    }

    private static String extractURL(String s) {

//        return s;

        for (String s1 : s.split("&")) {
            if (s1.contains("url")) {
                return s1.substring("url=".length());
            }
        }
        return "0_0";
    }


    public static String getTaskPirctureRequest(int id) {
        return "http://213.159.214.5/images/tasks/" + id + ".png";
    }

    public static String getVideoBackRequest(int id) {
        return "http://213.159.214.5/images/video/" + id + ".png";
    }

    public static Typeface getFont(Context context, String name) {
        try {
            return Typeface.createFromAsset(context.getAssets(), "fonts/" + name + ".ttf");
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    public static String getHintRequest(int id) {
        return "http://213.159.214.5/script/mobile/1/hint_load.php?ID=" + id;
    }
}
