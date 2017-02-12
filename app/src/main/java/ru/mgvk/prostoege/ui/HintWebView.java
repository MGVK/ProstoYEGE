package ru.mgvk.prostoege.ui;

import android.content.Context;
import android.graphics.Color;
import android.webkit.WebView;

import ru.mgvk.prostoege.DataLoader;

/**
 * Created by mihail on 19.10.16.
 */
public class HintWebView extends WebView {

    public HintWebView(Context context, final int id) {
        super(context);
        getSettings().setJavaScriptEnabled(true);
        loadUrl(DataLoader.getHintRequest(id));
        setBackgroundColor(Color.TRANSPARENT);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }
}
