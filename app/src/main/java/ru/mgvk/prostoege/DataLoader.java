package ru.mgvk.prostoege;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Log;
import ru.mgvk.prostoege.ui.statistic.StatisticData;
import ru.mgvk.util.Reporter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mihail on 10.08.16.
 */
public class DataLoader {

    public final static  String          ExcerciseDescriptionRequest
                                                         =
            "http://213.159.214.5/script/mobile/2/question/load.php?ID=";
    public final static  String          ExcerciseHintRequest
                                                         =
            "http://213.159.214.5/script/mobile/2/hint/load.php?ID=";
    public final static  String          PolicyURL       = "http://213.159.214.5/policy.html";
    private final static String          POLICY_SETTINGS = "POLICY";
    private final static Object          b               = new Object();
    public static        String          DefaultHTMLFile = "test.html";
    static               ArrayList<Task> taskList        = new ArrayList<>();
    static               int             tasks_ready     = 0;
    static Context context;
    static boolean loadingInThread = true;
    static Profile p;
    private static String url = null;
    private static onTaskLoadCompleted                onTaskLoadCompleted;
    private static OnStatisticLoadingCompleteListener onStatisticLoadingCompleteListener;
    private static HashMap<String, String> folders   = new HashMap<>();
    private static boolean                 firstTime = true;

    DataLoader(Context context) {

    }

    public static void setOnStatisticLoadingCompleteListener(
            OnStatisticLoadingCompleteListener onStatisticLoadingCompleteListener) {
        DataLoader.onStatisticLoadingCompleteListener = onStatisticLoadingCompleteListener;
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

        while (tasks_ready <= taskList.size()) {
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
                for (int task_i = p.Tasks.length / 2; task_i < p.Tasks.length; task_i++) {
                    long t = System.currentTimeMillis();

                    taskList.set(task_i, new Task(context, p.Tasks[task_i]));

                    Log.d("time_loadTask_" + task_i, (System.currentTimeMillis() - t) + "");
                }
                loadingInThread = false;
            }
        }).start();

        for (int task_i = 0; task_i < p.Tasks.length / 2; task_i++) {
            long t = System.currentTimeMillis();

            taskList.set(task_i, new Task(context, p.Tasks[task_i]));

            Log.d("time_loadTask_" + task_i, (System.currentTimeMillis() - t) + "");
        }

        while (loadingInThread) {
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

        p = ((MainActivity) context).profile;
        Log.d("taskLoading", "Profile " + p);

        ((MainActivity) context).stopwatch.checkpoint("Loading Tasks start");

        ((MainActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onTaskLoadCompleted.onTaskLoadStarted();
            }
        });

        statisticDataLoad();

        for (int task_i = 0; task_i < p.Tasks.length; task_i++) {

            p.loadVideo(p.Tasks[task_i]);
            p.loadQuestion(p.Tasks[task_i]);
            p.prepareData(p.Tasks[task_i]);

            taskList.add(new Task(context, p.Tasks[task_i]));
            if (onTaskLoadCompleted != null) {
                final int finalTask_i1 = task_i;
                ((MainActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onTaskLoadCompleted.onCompleted(taskList.get(finalTask_i1));
                    }
                });
            }
            ((MainActivity) context).stopwatch.checkpoint("Loading Task: " + task_i);

        }

        if (onTaskLoadCompleted != null) {
            onTaskLoadCompleted.onAllTaskLoadCompleted();
        }

        ((MainActivity) context).stopwatch.checkpoint("Loading Tasks finish");

        return taskList;
    }

    private static void statisticDataLoad() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                p.loadRepetitionStatistic();
                onStatisticLoadingCompleteListener.onLoadCompleted(p.getStatistic());
            }
        }).start();
    }

    public static void clearFile(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException("Cannot delete file!!!");
        }

        if (!file.createNewFile()) {
            throw new IOException("Cannot create file!!!");
        }
    }

    public static void loadTaskPicture(int taskID, Constants type) {
        try {

            File f = new File(getFolder(type) + taskID + ".png");

            clearFile(f);

            BitmapFactory.decodeStream(
                    new URL("http://213.159.214.5/images/" +
                            (type == Constants.QUICK_TEST ? "video/quick" : "rehearsal")
                            + "/" + taskID + ".png")
                            .openStream())
                    .compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(f));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getProfile(String profileId) throws Exception {
        //        Log.d("Profile",s);
        return getResponse("http://213.159.214.5/script/mobile/2_3/profile.php",
                "ProfileID=" + profileId);
    }

    public static String getVideo(int number) throws Exception {
        return "{\"Video\":" +
               getResponse("http://213.159.214.5/script/mobile/2/video/array.php",
                       "ProfileID=" + MainActivity.PID + "&TaskNumber=" + number) + "}";
    }

    public static String getQuestion(int number) throws Exception {
        return "{\"Questions\":" +
               getResponse("http://213.159.214.5/script/mobile/2/question/array.php",
                       "ProfileID=" + MainActivity.PID + "&TaskNumber=" + number) + "}";
    }

    public static String buyVideo(int id) throws Exception {
        return getResponse("http://213.159.214.5/script/mobile/2/video/buy.php",
                "ProfileID=" + MainActivity.PID + "+&VideoID=" + id);
    }

    public static String buyHint(int id) throws Exception {
        return getResponse("http://213.159.214.5/script/mobile/2/question/hint/buy.php",
                "ProfileID=" + MainActivity.PID + "&QuestionID=" + id);
    }

    public static String buyCoins(int count) throws Exception {
        return getResponse("http://213.159.214.5/script/mobile/2/buy.php",
                "ProfileID=" + MainActivity.PID + "&Coins=" + count);
    }

    public static String sendQuestions() throws Exception {
        return getResponse("http://213.159.214.5/script/mobile/2/ProfileID.php",
                "ProfileID=" + MainActivity.PID);
    }

    public static String putRepost() throws Exception {
        return getResponse("http://213.159.214.5/script/mobile/2/repost.php",
                "ProfileID=" + MainActivity.PID + "&Check=1");
    }

    public static String putQuestion(int id, int status) throws Exception {
        return getResponse("http://213.159.214.5/script/mobile/2/question/save.php",
                "ProfileID=" + MainActivity.PID + "&Question=" + id + ":" + status + "|");
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

    public static boolean acceptLicense(Context context) {

        context.getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE)
                .edit().putInt(POLICY_SETTINGS, 1).apply();

        return isLicenseAccepted(context);
    }

//    public static String getVideoURI(final String ID) {
//
//        Log.d("DataLoader", "Parsing video: " + ID);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                synchronized (b) {
//                    url = getVideoURL(ID);
//                    b.notifyAll();
//                }
//            }
//        }).start();
//
//        synchronized (b) {
//            try {
//                b.wait(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        return url;
//    }

//    static String getVideoURL(String id) {
//        String urlToRead = "http://www.youtube.com/get_video_info?video_id=" + id;
//
//        URL url;
//        HttpURLConnection conn;
//        BufferedReader rd;
//        String line;
//        String result = "";
//        try {
//            url = new URL(urlToRead);
//            conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            while ((line = rd.readLine()) != null) {
//                result += line;
//            }
//            rd.close();
//
//            for (String s : result.split("&")) {
//                if (s.contains("url_encoded_fmt_stream_map")) {
//                    return (decode(s.substring("url_encoded_fmt_stream_map=".length())));
//                }
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return "0_0";
//
//    }

    public static boolean isLicenseAccepted(Context context) {

        try {
            return 1 == context
                    .getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE)
                    .getInt(POLICY_SETTINGS, 0);
        } catch (Exception e) {
            Reporter.report(context, e, ((MainActivity) context).reportSubject);
            return false;
        }
    }

    public static String getTaskPirctureRequest(int id) {
        return "http://213.159.214.5/images/tasks/" + id + ".png";
    }


//    private static String decode(String s) {
//        s = URLDecoder.decode(s);
//        String p[] = s.split(",");
//        for (String s1 : p) {
//            if (s1.contains("itag=22")) {
////                return extractURL(URLDecoder.decode(URLDecoder.decode(s1)));
//                return URLDecoder.decode(extractURL(s1));
//            }
//        }
//        return "0_0";
//    }

//    private static String extractURL(String s) {
//
////        return s;
//
//        for (String s1 : s.split("&")) {
//            if (s1.contains("url")) {
//                return s1.substring("url=".length());
//            }
//        }
//        return "0_0";
//    }

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
        return "http://213.159.214.5/script/mobile/2/question/hint/load.php?ID=" + id;
    }

    public static void setOnTaskLoadCompleted(DataLoader.onTaskLoadCompleted onTaskLoadCompleted) {
        DataLoader.onTaskLoadCompleted = onTaskLoadCompleted;
    }

    public static boolean sendReport(String report) {

        try {
            return !"-1".equals(getResponse("http://mgvk.esy.es/logs.php",
                    "pid=" + MainActivity.PID + "&report=" + report));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return names of files to load during QuickTest in the right order
     */
    public static String getQuickTestTasks(int videoID) {

        String result = "";

        try {
            result = getResponse("http://213.159.214.5/script/mobile/3/video/test/array.php",
                    "ProfileID=" + MainActivity.PID + "&VideoID=" + videoID);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }

    public static String getRepetitionTasksJson() {

        String result = "";

        try {
            result = getResponse("http://213.159.214.5/script/mobile/3/rehearsal/array.php",
                    "ProfileID=" + MainActivity.PID);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String sendRepetitionAnswers(String repetitionData, String time) throws
            Exception {
        return getResponse("http://213.159.214.5/script/mobile/3/rehearsal/save.php?",
                "ProfileID=" + MainActivity.PID + "&Question=" + repetitionData + "&Time=" + time);
    }

    public static String getRepetitionTask(String id) {
        return "";
    }

    public static String getMathJaxFolder(Context context) {
        return getFolder(context, "MathJax");
    }

    public static String getRepetitionFolder(Context context) {

        return getFolder(context, "Repetition");
    }

    private static String getFolder(Constants type) {

        switch (type) {
            case REPETITION: {
                return getRepetitionFolder(context);
            }
            case QUICK_TEST: {
                return getQuickTestFolder(context);
            }
            case MATHJAX: {
                return getMathJaxFolder(context);
            }
        }
        return "";

    }

    private static String getFolder(Context context, String name) {
        if (folders.get(name) == null) {
            firstTime = true;
        }
        if (context == null && folders.get(name) == null) {
            throw new NullPointerException(name + " Folder must be initialized with not null "
                                           + "context");
        }

        File dir;
        if (context != null && !(dir = new File(context.getApplicationContext().getFilesDir() +
                                                "/" + name + "/")).exists()) {
            if (dir.mkdirs()) {
                folders.put(name, context.getApplicationContext().getFilesDir() + "/" + name + "/");
            }
        } else if (firstTime) {
            folders.put(name, context.getApplicationContext().getFilesDir() + "/" + name + "/");
            firstTime = false;
        }

        return folders.get(name);

    }

    public static void dataToHtml(String folder,
                                  HashMap<Integer, HTMLTask> map) {

        for (HTMLTask task : map.values()) {
            File file = new File(folder + task
                    .ID + ".html");
            try {
                DataLoader.clearFile(file);

                FileWriter writer = new FileWriter(file);

                String s =
                        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
                        + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\n"
                        + "<link rel=\"stylesheet\" href=\"style.css\">\n"
                        + "\n"
                        + "<script type=\"text/x-mathjax-config\">\n"
                        + "//\n"
                        + "//  Do NOT use this page as a template for your own pages.  It includes\n"
                        + "//  code that is needed for testing your site's installation of MathJax,\n"
                        + "//  and that should not be used in normal web pages.  Use sample.html as\n"
                        + "//  the example for how to call MathJax in your own pages.\n"
                        + "//\n"
                        + "  MathJax.HTML.Cookie.Set(\"menu\",{});\n"
                        + "  MathJax.Hub.Config({\n"
                        + "    extensions: [\"tex2jax.js\"],\n"
                        + "    jax: [\"input/TeX\",\"output/HTML-CSS\"],\n"
                        + "    messageStyle: \"none\",\n"
                        + "    \"HTML-CSS\": {\n"
                        + "      availableFonts:[], preferredFont: \"TeX\", webFont: \"TeX\",\n"
                        + "      styles: {\".MathJax_Preview\": {visibility: \"hidden\"}},\n"
                        + "    }\n"
                        + "  });\n"
                        + "\n"
                        + "(function (HUB) {\n"
                        + "\n"
                        + "  var MINVERSION = {\n"
                        + "    Firefox: 3.0,\n"
                        + "    Opera: 9.52,\n"
                        + "    MSIE: 6.0,\n"
                        + "    Chrome: 0.3,\n"
                        + "    Safari: 2.0,\n"
                        + "    Konqueror: 4.0,\n"
                        + "    Unknown: 10000.0 // always disable unknown browsers\n"
                        + "  };\n"
                        + "\n"
                        + "  if (!HUB.Browser.versionAtLeast(MINVERSION[HUB.Browser]||0.0)) {\n"
                        + "    HUB.Config({\n"
                        + "      jax: [],                   // don't load any Jax\n"
                        + "      extensions: [],            // don't load any extensions\n"
                        + "      \"v1.0-compatible\": false   // skip warning message due to no jax\n"
                        + "    });\n"
                        + "    setTimeout('document.getElementById(\"badBrowser\").style.display = \"\"',0);\n"
                        + "  }\n"
                        + "\n"
                        + "  if (HUB.Browser.isMSIE && !HUB.Browser.versionAtLeast(\"7.0\")) {\n"
                        + "    setTimeout('document.getElementById(\"MSIE6\").style.display = \"\"');\n"
                        + "  }\n"
                        + "\n"
                        + "})(MathJax.Hub);\n"
                        + "\n"
                        + "</script>\n"
                        + "<script type=\"text/javascript\" src=\"../MathJax/MathJax/MathJax"
                        + ".js\"></script>";

                writer.write(s + task.Description);
                if (task.hasImage) {
                    writer.write("<image src=\"" + task.ID + ".png\">");
                }
                writer.flush();
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static String getRepetitionStatistic() throws Exception {
        return getResponse("http://213.159.214.5/script/mobile/3/rehearsal/all.php",
                "ProfileID=" + MainActivity.PID);

    }

    public static String getQuickTestFolder(Context context) {

        return getFolder(context, "QuickTest");

    }

    public static String getQuickTestResults(int videoID, String s, long time) {
        String res = "";
        try {
            res = getResponse("http://213.159.214.5/script/mobile/3/video/test/save.php",
                    "ProfileID=" + MainActivity.PID
                    + "&Question=" + s
                    + "&VideoID=" + videoID
                    + "&Time=" + time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public interface OnStatisticLoadingCompleteListener {
        void onLoadCompleted(StatisticData[] statisticData);
    }


    public interface onTaskLoadCompleted {
        void onTaskLoadStarted();

        void onCompleted(Task task);

        void onAllTaskLoadCompleted();

    }
}
