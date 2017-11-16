package ru.mgvk.prostoege.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import ru.mgvk.prostoege.InstanceController;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;
import ru.mgvk.util.Reporter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * Created by mihail on 21.08.16.
 */
public class VideoPlayer {


    Context context;
    MediaPlayer mediaPlayer;
    Activity activity;
    Display smallDisplay, fullScreenDisplay;
    Display currDisplay;
    ImageView picture;
    private String videoID = "";
    private boolean fullScreen = false;
    private OnExceptionCorruptedListener listener;
    private OnVideoStateChangeListener onVideoStateChangeListener;
    private boolean touched = false;


    //    public void start(){
//        if(mediaPlayer!=null) {
//            mediaPlayer.start();
//        }
//    }
    private boolean buyed = false;
    private boolean wasStoped = false;
    private String TAG = "VideoPlayer";


    public VideoPlayer(Context context) throws ClassCastException {
        this.context = context;
        activity = ((Activity) context);
        initDisplays();
    }

    public Display getSmallDisplay() {
        return smallDisplay;
    }

    public Display getFullScreenDisplay() {
        return fullScreenDisplay;
    }

    void initDisplays() {
        smallDisplay = new Display(context);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    fullScreenDisplay = new Display(context);
                    fullScreenDisplay.setFullScreenDisplay(true);
                    fullScreenDisplay.deactivate();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ((ViewGroup) activity.getWindow().getDecorView()
                                        .getRootView()).addView(fullScreenDisplay);
                            } catch (Exception e) {
                                Reporter.report(context, e, ((MainActivity) context).reportSubject);
                            }
                        }
                    });
                } catch (Exception e) {
                    Reporter.report(context, e, ((MainActivity) context).reportSubject);
                }
            }
        }).start();
    }

    private void toggleOrientation() {
        if (isFullScreen()) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            fullScreen = false;
            currDisplay = smallDisplay;
            UI.exitFullScreen(context);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            fullScreen = true;
            currDisplay = fullScreenDisplay;
//            UI.enterFullScreen(context);
        }
    }

    private void changeDisplay(final boolean continuePlaying) {

//        if (isPlaying()) {
//            mediaPlayer.pause();
        pause();
//        }

        if (isFullScreen()) {
            fullScreenDisplay.deactivate();
            smallDisplay.activate();
            ((MainActivity) activity).getBackStack().removeLastAction();
        } else {
            ((MainActivity) activity).addToBackStack(new Runnable() {
                @Override
                public void run() {
                    changeDisplay(!continuePlaying);
                    callOnFullScreen();
                }
            });
            smallDisplay.deactivate();
            fullScreenDisplay.activate();
        }

        toggleOrientation();


//        new Thread(new Runnable() {
//            @Override
//            public void run() {

        try {
            if (mediaPlayer != null && currDisplay != null) {
                currDisplay.initHolder(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {
                        Log.d(TAG, "surfaceCreated: changeDisplay");
//                        holder.removeCallback(this);
                        mediaPlayer.setDisplay(holder);
//                            mediaPlayer.start();
                        if (continuePlaying) {
                            start(currDisplay);
                        } else {
                            pause();
                        }
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder holder,
                                               int format, int width, int height) {
                        Log.d(TAG, "surfaceChanged: changeDisplay: "
                                + format + " " + width + " " + height);
//                        holder.removeCallback(this);
                        mediaPlayer.setDisplay(holder);
                        if (continuePlaying) {
                            start(currDisplay);
                        } else {
                            pause();
                        }
                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder) {
                        Log.d(TAG, "surfaceDestroyed: changeDisplay");
                    }
                });

//                            activateSeekBar();
            }

        } catch (Exception e) {
            Reporter.report(context, e, ((MainActivity) context).reportSubject);
        }


//            }
//        }).start();
    }

    public void updateParent(ViewGroup p) {
        smallDisplay.parent = p;
    }

    public String getVideoID() {
        return videoID;
    }

    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public boolean isPlaying() {
        try {
            boolean res = mediaPlayer != null && mediaPlayer.isPlaying();
            Log.d(TAG, "isPlaying: " + res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "isPlaying: false");
            return false;
        }

    }

    private void doException(Exception e) {
        if (listener != null) {
            listener.onExceptionCatch(e);
        }
    }

    public void setPicture(Drawable drawable) {
        if (picture == null) {
            picture = new ImageView(context);
            picture.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
            smallDisplay.setPicture(picture);
        } else {
            try {
                smallDisplay.removeView(picture);
                smallDisplay.addView(picture);
//                smallDisplay.setPicture(picture);
            } catch (Exception ignored) {
            }
        }
        picture.setImageDrawable(drawable);
    }

    void initPlayer(Display display) {

        if ((videoID == null || videoID.equals(""))) {
            doException(new NullPointerException("VideoID is null!"));
            return;
        }
        try {
            if (display.getSurface() == null) {
                display.initSurface();

            }
            try {
                smallDisplay.removePicture(picture);
                picture = null;
            } catch (Exception ignored) {
            }

            display.reInitViews();

            activity.getWindow().setFormat(PixelFormat.UNKNOWN);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, VideoInfoGetter.getVideoURI(videoID));


            display.initHolder(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {

                    Log.d(TAG, "surfaceCreated: initPlayer");

                    holder.removeCallback(this);

                    mediaPlayer.setDisplay(holder);
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                            smallDisplay.seekBar.setMax(mediaPlayer.getDuration());
                            fullScreenDisplay.seekBar.setMax(mediaPlayer.getDuration());
                            wasStoped = false;
                            activateSeekBar();
                        }
                    });

                    try {
                        mediaPlayer.prepareAsync();
//                        mediaPlayer.prepare();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    Log.d(TAG, "surfaceChanged: initPlayer");
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    Log.d(TAG, "surfaceDestroyed: initPlayer");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            UI.makeErrorMessage(context, "Ошибка загрузки видео!");
        }
    }


    private void activateSeekBar() {
        new SeekBarProgressController().start();
    }

    public void stop() {
        if (mediaPlayer != null && isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (fullScreen) {
            changeDisplay(false);
        }
        callOnStop();
        wasStoped = true;
        smallDisplay.setStoped();
        fullScreenDisplay.setStoped();
        smallDisplay.setPicture(picture);

    }

    public void pause() {
        if (mediaPlayer != null && isPlaying()) {
//            wasStoped = true;
            smallDisplay.setStoped();
            fullScreenDisplay.setStoped();
            mediaPlayer.pause();
            callOnPause();
        }
    }

    public void start() {
        start(smallDisplay);
    }

    public void start(Display display) {

        if (mediaPlayer == null || wasStoped) {
            initPlayer(display);
        } else {
            mediaPlayer.start();
        }

        display.setPlaying();

        wasStoped = false;

        callOnPlay();

    }

    public void setBuyed() {
        setBuyed(null);
    }

    public void setBuyed(Drawable back) {
        buyed = true;
        smallDisplay.initSurface();
        fullScreenDisplay.initSurface();
        if (back != null) {
            setPicture(back);
        }
        smallDisplay.setBuyed();
        fullScreenDisplay.setBuyed();

    }

    public void setOnVideoStateChangeListener(OnVideoStateChangeListener onVideoStateChangeListener) {
        this.onVideoStateChangeListener = onVideoStateChangeListener;
    }

    private void callOnPlay() {
        Log.d(TAG, "callOnPlay");
        activateSeekBar();
        if (onVideoStateChangeListener != null) {
            onVideoStateChangeListener.onPlay(this);
        }
    }

    private void callOnStop() {
        Log.d(TAG, "callOnStop");
        if (onVideoStateChangeListener != null) {
            onVideoStateChangeListener.onStop(this);
        }
    }

    private void callOnPause() {
        Log.d(TAG, "callOnPause");
        if (onVideoStateChangeListener != null) {
            onVideoStateChangeListener.onPause(this);
        }
    }

    private void callOnFullScreen() {
        Log.d(TAG, "callOnFullScreen");
        if (onVideoStateChangeListener != null) {
            onVideoStateChangeListener.onFullScreen(this);
        }
    }


    public interface OnExceptionCorruptedListener {

        void onExceptionCatch(Exception e);

    }

    public interface OnVideoStateChangeListener {

        void onPlay(VideoPlayer v);

        void onPause(VideoPlayer v);

        void onStop(VideoPlayer v);

        void onFullScreen(VideoPlayer v);

    }

    public static class VideoInfoGetter {

        private static String url = null;

        static Uri getVideoURI(final String ID) {

            try {
                url = Executors.newSingleThreadExecutor().submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return getVideoURL(ID);
                    }
                }).get();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return url == null ? null : Uri.parse(url);
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

        private static String decode(String s) {
            s = URLDecoder.decode(s);
            String p[] = s.split(",");
            for (String s1 : p) {
                if (s1.contains("itag=22")) {
                    return URLDecoder.decode(extractURL(s1));
                }
            }
            return "0_0";
        }

        private static String extractURL(String s) {

            for (String s1 : s.split("&")) {
                if (s1.contains("url")) {
                    return s1.substring("url=".length());
                }
            }
            return "0_0";
        }

    }

    public class Display extends FrameLayout implements View.OnClickListener {

        DisplaySurface display;
        VideoSeek seekBar;
        ImageButton playPauseButton, fullScreenButton;
        ViewGroup parent;
        int minWidth = UI.calcSize(55);
        int minHeight = UI.calcSize(31);
        private boolean fullScreenDisplay = false;

        public Display(Context context) {
            super(context);

            initViews();

            setMinimumWidth(minWidth);
            setMinimumHeight(minHeight);

        }

        @Override
        public void setOnClickListener(OnClickListener l) {
            if (picture != null && indexOfChild(picture) != -1) {
                picture.setOnClickListener(l);
            }
            else {
                super.setOnClickListener(l);
            }
        }

        public SurfaceView getSurface() {
            return display;
        }

        public void setFullScreenDisplay(boolean fullScreenDisplay) {
            this.fullScreenDisplay = fullScreenDisplay;
        }

        void updateParent() {
            if (getParent() != null) {
                parent = (ViewGroup) getParent();
            }
        }

        public void activate() {
//            onWindowFocusChanged(true);
            if (getParent() == null && parent != null) {
//                parent.addView(this);
            }
            setVisibility(VISIBLE);
            if (display != null) {
                display.setVisibility(VISIBLE);
            }

            if (isPlaying()) {
                playPauseButton.setBackgroundDrawable(
                        context.getResources().getDrawable(R.drawable.icon_pause));
            }

        }

        void reInitViews() {
            this.removeView(fullScreenButton);
            this.removeView(playPauseButton);
            this.removeView(seekBar);
            this.addView(fullScreenButton);
            this.addView(playPauseButton);
            this.addView(seekBar);
        }

        public void deactivate() {
            if (parent != null) {
//                parent.removeView(this);
            }
            setVisibility(GONE);
            if (display != null) {
                display.setVisibility(GONE);
            }
        }

        void setStoped() {
            playPauseButton.setBackgroundDrawable(
                    context.getResources().getDrawable(R.drawable.icon_play));
            seekBar.setProgress(0);

        }

        void setPlaying() {
            playPauseButton.setBackgroundDrawable(
                    context.getResources().getDrawable(R.drawable.icon_pause));
        }

        SurfaceHolder initHolder(SurfaceHolder.Callback callback) {

            SurfaceHolder holder = display.getHolder();
            holder.addCallback(callback);
            holder.setFixedSize(display.getWidth(), display.getHeight());
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            return holder;
        }

        void setBuyed() {
            reInitViews();
            fullScreenButton.setVisibility(VISIBLE);
            playPauseButton.setVisibility(VISIBLE);
            seekBar.setVisibility(VISIBLE);
        }

        public void initSurface() {
            try {
                removeView(display);
            } catch (Exception e) {
                e.printStackTrace();
            }
            display = new DisplaySurface(context);
            display.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            display.setOnClickListener(this);
            display.setMinimumWidth(UI.calcSize(110));
            display.setMinimumHeight(UI.calcSize(62));
            if (fullScreenDisplay) {
                this.addView(display);
            }
        }


        void setPicture(ImageView picture) {
            removeView(display);
            try {
                picture.setOnClickListener(this);
                addView(picture);
            } catch (Exception ignored) {
            }
            setBackgroundColor(Color.TRANSPARENT);
        }

        private void initViews() {
            setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
            setBackgroundColor(Color.BLACK);

            seekBar = new VideoSeek(context);
            seekBar.setThumb(context.getResources().getDrawable(R.drawable.thumb_video));

            this.addView(seekBar);

            fullScreenButton = new ImageButton(context);
            fullScreenButton.setVisibility(GONE);
            LayoutParams lp = new LayoutParams(-2, -2);
            lp.gravity = Gravity.END | Gravity.BOTTOM;
            fullScreenButton.setBackgroundDrawable(
                    context.getResources().getDrawable(R.drawable.icon_full_screen));
            fullScreenButton.setLayoutParams(lp);
            fullScreenButton.setOnClickListener(this);
            this.addView(fullScreenButton);

            playPauseButton = new ImageButton(context);
            lp = new LayoutParams(-2, -2);
            lp.gravity = Gravity.CENTER;
            playPauseButton.setBackgroundDrawable(
                    context.getResources().getDrawable(R.drawable.icon_play));
            playPauseButton.setOnClickListener(this);
            playPauseButton.setLayoutParams(lp);
            this.addView(playPauseButton);
            playPauseButton.setVisibility(GONE);

        }

        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);

            updateParent();
        }

        @Override
        public void setLayoutParams(ViewGroup.LayoutParams params) {
            if (fullScreenDisplay) {
                params.width = params.height = -1;
            }
            super.setLayoutParams(params);
        }

        @Override
        protected void onMeasure(int widthSpec, int heightSpec) {
            int w = MeasureSpec.getSize(widthSpec);
            int h = MeasureSpec.getSize(heightSpec);
            updateSizes(w, h);
            super.onMeasure(widthSpec, heightSpec);

        }


        void updateSizes(int width, int height) {
            try {
                int h09 = (int) (isFullScreen() ? 0.1 * height : 0.15 * height); //размер кнопки полного экрана
                int h2 = (int) (0.2 * height);
                int h04 = (int) (0.04 * height);
//            seekBar.setY(height - (h15 = (int) (0.15 * height)));
//                seekBar.getLayoutParams().width = width - h09;
//                seekBar.getLayoutParams().height = h04;
                ((LayoutParams) seekBar.getLayoutParams()).setMargins(h04, h09 / 2, h09, h04);
                seekBar.setLayoutParams(seekBar.getLayoutParams());

                fullScreenButton.getLayoutParams().height = h09;
                fullScreenButton.getLayoutParams().width = h09;
                ((LayoutParams) fullScreenButton.getLayoutParams()).setMargins(
                        0, 0, (int) (h04 / 2.0), (int) (h04 / 2.0));
                fullScreenButton.setLayoutParams(fullScreenButton.getLayoutParams());

                playPauseButton.getLayoutParams().width
                        = (playPauseButton.getLayoutParams().height = h2);
                playPauseButton.setLayoutParams(playPauseButton.getLayoutParams());
            } catch (Exception ignored) {
            }
        }


        @Override
        public void onClick(View v) {
            try {

                if (v == fullScreenButton) {
//                    changeDisplay((!isFullScreen()));
                    changeDisplay(false);
                    callOnFullScreen();
                }

                if (v == playPauseButton) {

                    if (isPlaying()) {
                        pause();
                    } else {
                        if (((boolean) InstanceController.getObject("WIFI_only"))) {

                            WifiManager manager =
                                    (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                            if (!manager.isWifiEnabled()) {

                                ((MainActivity) activity).ui.openWifiOnlyDialog();
                                return;
                            }
                        }
                        start(this);
                    }


                }
                if (v == display || (v == picture)) {
                    toggleButtonsVisibility();
                }
            } catch (Exception e) {
                Reporter.report(context, e, ((MainActivity) context).reportSubject);
            }
        }


        private void toggleButtonsVisibility() {
            if (playPauseButton.getVisibility() == VISIBLE) {
                playPauseButton.setVisibility(INVISIBLE);
                seekBar.setVisibility(INVISIBLE);
                fullScreenButton.setVisibility(INVISIBLE);
            } else {
                playPauseButton.setVisibility(VISIBLE);
                seekBar.setVisibility(VISIBLE);
                fullScreenButton.setVisibility(VISIBLE);
            }
        }

        private void removeDisplay() {

        }

        private void addDisplay() {

        }

        public void removePicture(ImageView picture) {
            try {
                removeView(picture);
            } catch (Exception ignored) {
            }
            try {
                addView(display);
            } catch (Exception e) {
                e.printStackTrace();
            }
            setBackgroundColor(Color.BLACK);
        }


        class DisplaySurface extends SurfaceView {

            public DisplaySurface(Context context) {
                super(context);
            }

            public DisplaySurface(Context context, AttributeSet attrs) {
                super(context, attrs);
            }

            public DisplaySurface(Context context, AttributeSet attrs, int defStyleAttr) {
                super(context, attrs, defStyleAttr);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public DisplaySurface(Context context, AttributeSet attrs,
                                  int defStyleAttr, int defStyleRes) {
                super(context, attrs, defStyleAttr, defStyleRes);
            }

            @Override
            protected void onMeasure(int w, int h) {
//                super.onMeasure(w, h);
//                w = getMeasuredWidth();
//                h = getMeasuredHeight();
                w = MeasureSpec.getSize(w);
                h = MeasureSpec.getSize(h);
//                Log.d("surfMeasure", w + " " + h);

                int oldH = h, oldW = w;

                if (w >= 0 && h >= 0) {

                    if (h == 0 || !fullScreenDisplay || w / ((double) h) <= 16 / 9.0) {
                        h = (int) (w * 9 / 16.0);
                    } else {
                        w = (int) (h * 16 / 9.0);
                    }
                    if (fullScreen) {
                        setX((oldW - w) / 2);
                        setY((oldH - h) / 2);
                    }
//                    Display.this.setLayoutParams(new LinearLayout.LayoutParams(w, h));
                }

                updateSizes(w, h);

//                Log.d("si", w + " " + h);


                setMeasuredDimension(w, h);
                Display.this.setMeasuredDimension(w, h);

//                    super.onMeasure(w, h);
//                ViewGroup.LayoutParams lp = getLayoutParams();
//                lp.width = w;
//                lp.height = h;
//                this.setLayoutParams(lp);
//                setMinimumWidth(w);
//                setMinimumHeight(h);

            }
        }
    }


    class VideoSeek extends android.support.v7.widget.AppCompatSeekBar
            implements OnSeekBarChangeListener {

        public VideoSeek(Context context) {
            super(context);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -2);
            lp.gravity = Gravity.BOTTOM;
            setLayoutParams(lp);
            setVisibility(GONE);
            setOnSeekBarChangeListener(this);
        }


        @Override
        public boolean onTouchEvent(MotionEvent event) {
            try {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ((MainActivity) activity).ui.videoListFragment
                            .setScrollViewEnabled(touched = false);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((MainActivity) activity).ui.videoListFragment
                            .setScrollViewEnabled(touched = true);
                }
            } catch (Exception e) {
                Reporter.report(context, e, ((MainActivity) context).reportSubject);
            }
            return super.onTouchEvent(event);

        }


        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            try {
                if (mediaPlayer != null && touched && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(progress);
                }
            } catch (Exception e) {
                Reporter.report(context, e, ((MainActivity) context).reportSubject);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            try {
                ((MainActivity) activity).ui.mainScroll.setScrollEnabled(false);
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
                touched = true;
            } catch (Exception e) {
                Reporter.report(context, e, ((MainActivity) context).reportSubject);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            try {
                ((MainActivity) activity).ui.mainScroll.setScrollEnabled(true);
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                    mediaPlayer.start();
                }
                touched = false;
            } catch (Exception e) {
                Reporter.report(context, e, ((MainActivity) context).reportSubject);
            }
        }
    }

    class SeekBarProgressController extends Thread {

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        Thread.sleep(500);
                        if (!touched) {
                            smallDisplay.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            fullScreenDisplay.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        }
                    } else {
                        interrupt();
                    }
                } catch (Exception ignored) {
//                e.printStackTrace();
//                    Reporter.report(context, e, ((MainActivity) context).reportSubject);
                }

            }

        }
    }

}
