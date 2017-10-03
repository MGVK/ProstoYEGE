package ru.mgvk.util;


import android.content.Context;
import org.apache.commons.io.IOUtils;
import ru.mgvk.prostoege.DataLoader;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by mike on 11.08.17.
 */
public class MathJaxPreparer {


    public static void copy(Context context, String from, File to) throws IOException {

        OutputStream myOutput = new FileOutputStream(to);
        byte[]       buffer   = new byte[1024];
        int          length;
        InputStream  myInput  = context.getAssets().open(from);
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myInput.close();
        myOutput.flush();
        myOutput.close();

    }

    public static void prepare(Context context) throws Exception {

        String outputDir = DataLoader.getRepetitionFolder(context);
        File   file      = new File(outputDir + "MathJax.zip");

        /*TESTTESTTEST*/
        File html = new File(DataLoader.DefaultHTMLFile = outputDir + "test.html");
        File png  = new File(outputDir + "smile.png");
        copy(context, "test.html", html);
        copy(context, "smile.png", png);
        /**/


        try {
            new File(outputDir).mkdir();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!file.exists()) {


            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            copy(context, "MathJax.zip", file);

        }


        java.util.zip.ZipFile zipFile = new ZipFile(file);
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry            = entries.nextElement();
                File     entryDestination = new File(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream  in  = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    out.close();
                }
            }

            try {
                new File(outputDir + "MathJax.zip").delete();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } finally {
            zipFile.close();
        }

    }

}
