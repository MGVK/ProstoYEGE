package ru.mgvk.prostoege;

import android.content.Context;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.mgvk.util.Reporter;

import java.util.LinkedHashMap;

import static ru.mgvk.prostoege.DataLoader.dataToHtml;

public class RepetitionData {

    private static LinkedHashMap<Integer, HTMLTask> map = new LinkedHashMap<>();
    private        int                              repetitionDuration;


    public static RepetitionData fromFuckingJSON(Context context, String json) {

        if (json.length() < 3) {
            return null;
        }

        RepetitionData repetitionData = new RepetitionData();

        try {

            JsonElement element = new JsonParser().parse(json);


            for (int j = 1; j <= 19; j++) {

                JsonObject o           = element.getAsJsonObject().getAsJsonObject(j + "");
                int        id          = o.get("ID").getAsInt();
                String     description = o.get("Description").getAsString();
                boolean    hasImage;
                try {
                    hasImage = o.get("Image").getAsBoolean();
                } catch (Exception e) {
                    hasImage = false;
                }

                map.put(j, new HTMLTask(id, description, hasImage, Constants.REPETITION));

            }

            repetitionData.setRepetitionDuration(element.getAsJsonObject().get("Time")
                                                         .getAsInt() * 60);

        } catch (Exception e) {
            Reporter.report(context, e, MainActivity.PID);
        }

        repetitionData.setMap(map);
        for (HTMLTask HTMLTask : map.values()) {
            HTMLTask.Description = HTMLTask.Description.replace("\\\"", "\"");
            HTMLTask.Description = HTMLTask.Description.replace("\\/", "");
            HTMLTask.Description = HTMLTask.Description.replace("\\\\", "\\");
        }

        dataToHtml(DataLoader.getRepetitionFolder(context), map);

        return repetitionData;
    }


    public LinkedHashMap<Integer, HTMLTask> getMap() {
        return map;
    }

//        private static int increment(int i) {
//            return i >= 4 ? 0 : ++i;
//        }

    public void setMap(
            LinkedHashMap<Integer, HTMLTask> map) {
        this.map = map;
    }

    public String getHtmlFilePath(int number) {
        HTMLTask t = map.get(number);
        if (t == null) {
            return "test.html";
        } else {
            return t.ID + ".html";
        }
    }

    public long getRepetitionDuration() {
        return repetitionDuration;
    }

    public void setRepetitionDuration(int repetitionDuration) {
        this.repetitionDuration = repetitionDuration;
    }


}