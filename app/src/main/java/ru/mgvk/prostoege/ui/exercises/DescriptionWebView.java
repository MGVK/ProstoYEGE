package ru.mgvk.prostoege.ui.exercises;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import ru.mgvk.prostoege.DataLoader;
import ru.mgvk.prostoege.MainActivity;

public class DescriptionWebView extends WebView {

    private Context context;
    private boolean isLoaded = false;

    public DescriptionWebView(Context context) {
        super(context);
    }

    public DescriptionWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DescriptionWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DescriptionWebView(Context context, AttributeSet attrs, int defStyleAttr,
                              int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public DescriptionWebView(Context context, AttributeSet attrs, int defStyleAttr,
                              boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
    }

    void init() {
        setTag("DescriptionWebView");
        context = getContext();
        getSettings().setJavaScriptEnabled(true);
        getSettings().setSupportZoom(true);
        setDrawingCacheEnabled(true);
        getSettings().setAppCacheEnabled(true);
    }

    public void loadHTMLFile(String path) {
        loadUrl("file://" + path);
    }

    public void reloadDescription() {
        reloadDescription(((MainActivity) context).ui
                .exercisesListFragment.getExerciseWindow().QUESTION_ID);
    }

    public void reloadDescription(int id) {
        loadUrl(DataLoader.ExcerciseDescriptionRequest + id);
    }

    public void reloadrepetitionTask(String id) {
        loadUrl(DataLoader.getRepetitionTask(id));
    }

}
