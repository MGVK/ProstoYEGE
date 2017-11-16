package ru.mgvk.prostoege.ui;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import ru.mgvk.prostoege.InstanceController;
import ru.mgvk.prostoege.R;

public class VideoActivity extends Activity {

    private FrameLayout     layout;
    private SimpleExoPlayer player;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }


        layout = (FrameLayout) findViewById(R.id.layout);

        player = (SimpleExoPlayer) InstanceController
                .getObject("VideoPlayer_" + getIntent().getStringExtra("ID"));

        SimpleExoPlayerView v = new SimpleExoPlayerView(this);

        v.setPlayer(player);

        player.setPlayWhenReady(true);

        layout.addView(v);

    }

    @Override
    protected void onStart() {
        super.onStart();
//        UI.enterFullScreen(this);

        try {
            InstanceController
                    .putObject("Orientation", getResources().getConfiguration().orientation);
        } catch (InstanceController.NotInitializedError notInitializedError) {
            notInitializedError.printStackTrace();
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        player.setPlayWhenReady(false);
        layout.removeAllViews();
        super.onDestroy();
    }
}
