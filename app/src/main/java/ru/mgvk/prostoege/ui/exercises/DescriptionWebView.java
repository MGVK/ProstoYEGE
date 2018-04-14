package ru.mgvk.prostoege.ui.exercises;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.webkit.WebView;
import ru.mgvk.prostoege.DataLoader;
import ru.mgvk.prostoege.MainActivity;

public class DescriptionWebView extends WebView {

    private Context context;
    private boolean isLoaded = false;

    public DescriptionWebView(Context context) {
        super(context);
        this.context = context;
    }

    public DescriptionWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public DescriptionWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public DescriptionWebView(Context context, AttributeSet attrs, int defStyleAttr,
                              int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    public DescriptionWebView(Context context, AttributeSet attrs, int defStyleAttr,
                              boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
        this.context = context;
    }

    public void init() {
        setTag("DescriptionWebView");
        getSettings().setJavaScriptEnabled(true);
        getSettings().setSupportZoom(true);
        setDrawingCacheEnabled(true);
        getSettings().setAppCacheEnabled(true);
    }

    public void loadHTMLFile(final String path) {
        loadUrl("file://" + path);
    }

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
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
