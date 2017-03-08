package ru.mgvk.prostoege;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ProgressBar;
import ru.mgvk.prostoege.ui.AnimatedCounter;

import java.io.IOException;
import java.net.Socket;

public class SplashScreen extends Activity {


    AnimatedCounter counter;
    LoadingIndicator loadingIndicator;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        new InstanceController();

//        counter = (AnimatedCounter) findViewById(R.id.counter);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


    }

    @Override
    protected void onStart() {
        super.onStart();
//        counter.startCounting();


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket("213.159.214.5", 80);
                    socket.isConnected();
                    socket.close();

//                    if(!DataLoader.sendReport("[test;fdestestes]")){
//                        throw new IOException("Cannot connect:(");
//                    }

//                    startLoadingIndicator();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            startActivity(new Intent(SplashScreen.this, MainActivity.class));
                            finish();

                        }
                    });

                    /*new Thread(new Runnable() {
                        @Override
                        public void run() {
                            long time = System.currentTimeMillis();
                            while(System.currentTimeMillis()-time>15000) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (InstanceController.getObject("LoadingCompleted") != null) {
                                            stopLoadingIndicator();
                                        }
                                    }
                                });
                                try {
                                    Thread.sleep(150);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();*/


                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(SplashScreen.this)
                                    .setTitle("Ошибка!")
                                    .setMessage("Не могу запуститься без интернета :(")
                                    .setCancelable(false)
                                    .setPositiveButton("Выход", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            System.exit(1);
                                        }
                                    })
                                    .create().show();
                        }
                    });

                    e.printStackTrace();
                }


            }
        })
                .start()
        ;

    }

    void startLoadingIndicator() {
        (loadingIndicator = new LoadingIndicator()).start();
    }

    void stopLoadingIndicator() {
        loadingIndicator.finish();
    }

    class LoadingIndicator extends Thread {
        private Integer progress = 0;
        private boolean works = false;

        @Override
        public void run() {
            works = true;
            while (works) {

                Log.d("Indicator", "works");
                try {

                    if ((progress = (Integer) InstanceController.getObject(DataLoader.TAG_TaskLoadingProgress)) != null) {
//                        progress = (int) InstanceController.getObject(DataLoader.TAG_TaskLoadingProgress);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(progress);
                            }

                        });
                        Log.d("Indicator", "progress: " + progress);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void finish() {
            works = false;
        }
    }
}
