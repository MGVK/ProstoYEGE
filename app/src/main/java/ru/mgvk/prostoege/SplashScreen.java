package ru.mgvk.prostoege;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import ru.mgvk.prostoege.ui.AnimatedCounter;

import java.io.IOException;
import java.net.Socket;

public class SplashScreen extends Activity {


    AnimatedCounter counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        new InstanceController();

        counter = (AnimatedCounter) findViewById(R.id.counter);


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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            startActivity(new Intent(SplashScreen.this, MainActivity.class));

                            finish();

                        }
                    });
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
}
