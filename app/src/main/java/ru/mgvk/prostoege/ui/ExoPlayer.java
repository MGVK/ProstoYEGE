package ru.mgvk.prostoege.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import ru.mgvk.prostoege.InstanceController;
import ru.mgvk.prostoege.MainActivity;

public class ExoPlayer {

    private final SimpleExoPlayer            player;
    private       Context                    context;
    private       SimpleExoPlayerView        view;
    private       String                     ID;
    private       ImageView                  pictureView;
    private       boolean                    buyed;
    private       OnVideoStateChangeListener onVideoStateChangeListener;
    private       boolean                    playing;

    public ExoPlayer(Context context) {

        this.context = context;

        player =
                ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector(
                        new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter())));


        ((MainActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view = new SimpleExoPlayerView(ExoPlayer.this.context);

                view.setPlayer(player);

            }
        });

        initPictureView();

    }

    public static SimpleExoPlayer newInstance(Context context, String id) {

        return null;
    }

    void initPictureView() {
        pictureView = new ImageView(context);
        pictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ID != null && ID.length() > 0) {
                    prepare();
                    try {
                        InstanceController.putObject("VideoPlayer_" + ID, player);
                    } catch (InstanceController.NotInitializedError notInitializedError) {
                        notInitializedError.printStackTrace();
                    }

                    start();
                    context.startActivity(
                            new Intent(context, VideoActivity.class).putExtra("ID", ID));
                }
            }
        });

    }

    SimpleExoPlayerView getPlayerView() {
        return view;
    }

    void setVideoID(String ID) {
        this.ID = ID;
//        prepare();
    }

    void prepare() {
        MediaSource videoSource = new ExtractorMediaSource(
                VideoPlayer.VideoInfoGetter.getVideoURI(ID),
                new DefaultDataSourceFactory(
                        context, Util.getUserAgent(context, "ProstoEGE"))
                , new DefaultExtractorsFactory(), null, null);

        player.prepare(videoSource);
//        player.addListener();

    }

    public void stop() {
//        player.stop();
//        playing = false;
    }

    public View getPictureView() {
        return pictureView;
    }

    public void setBuyed(Drawable drawable) {
        buyed = true;
        setPicture(drawable);
    }

    public void setPicture(Drawable drawable) {
        pictureView.setImageDrawable(drawable);
    }

    public boolean isPlaying() {
        return playing;
    }

    public void pause() {
        player.setPlayWhenReady(false);
        playing = false;
    }

    public void start() {
        player.setPlayWhenReady(true);
        playing = true;
    }

    public interface OnVideoStateChangeListener {
        void onPlay(ExoPlayer v);

        void onPause(ExoPlayer v);
        // TODO: 12.11.17

    }

}
