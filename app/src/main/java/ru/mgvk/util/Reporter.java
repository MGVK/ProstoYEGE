package ru.mgvk.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import ru.mgvk.prostoege.InstanceController;

/**
 * Created by mike on 24.02.17.
 */
public class Reporter {

    private static String email = "support.prostoyege@yandex.ru";

    public static void report(final Context context, Exception e, final String subject) {

        if (InstanceController.isInitialized()) {
            InstanceController.getObject("report");
        }

        String s = "";
        s += e.toString() + "\n";
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            s += stackTraceElement.toString() + "\n";
        }

        final String finalS = s;
        new AlertDialog.Builder(context)
                .setTitle("Упс... ошибка!")
                .setMessage("В приложении произошла ошибка. Пожалуйста, отправьте отчет разработчикам.")
                .setCancelable(false)
                .setPositiveButton("Отправить отчет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email))
//                                .setType("text/plain")
                                .putExtra(Intent.EXTRA_SUBJECT, subject)
                                .putExtra(Intent.EXTRA_TEXT, finalS);
                        context.startActivity(intent);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }
}
