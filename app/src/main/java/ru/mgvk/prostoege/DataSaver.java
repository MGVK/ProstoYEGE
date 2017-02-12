package ru.mgvk.prostoege;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by mihail on 08.10.16.
 */
public class DataSaver {

    public String FILE_PATH = "";
    Context context;

    DataSaver(Context context){
        this.context = context;
        FILE_PATH = context.getFilesDir()+File.separator+"Profiles"+File.separator;
    }


    public void saveProfile(Profile p){
        try {
            FileOutputStream fos = new FileOutputStream(FILE_PATH+ ((MainActivity) context).PID);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(p);
            oos.close();
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    public Profile loadProfile(String id){
            try {

                FileInputStream fis = new FileInputStream(FILE_PATH+id);
                ObjectInputStream ois = new ObjectInputStream(fis);
                return (Profile) ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return null;
    }

}
