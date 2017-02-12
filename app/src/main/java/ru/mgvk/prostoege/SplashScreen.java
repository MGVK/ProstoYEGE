package ru.mgvk.prostoege;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.io.IOException;
import java.net.Socket;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);


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
        }).start();
    }
}
