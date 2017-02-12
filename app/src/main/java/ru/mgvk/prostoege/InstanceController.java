package ru.mgvk.prostoege;

import java.util.HashMap;

/**
 * Created by mihail on 19.08.16.
 */
public class InstanceController {

    /**
     * "Хранитель объектов" на все время жизни приложения.
     * Перед использованием необходимо однажды вызвать конструктор,
     * после этого обращаться к методам можно и анонимно, без экземпляра класса.
     *
     *
     */

    public InstanceController() {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if(thread.getName().equals(Saver.name)){
               return;
            }
        }
        new Saver().start();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Object getObject(String key){
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if(thread.getName().equals(Saver.name)){
                return ((Saver)thread).getObject(key);
            }
        }
        return null;
    }

    public static void putObject(String key,Object o) throws NotInitializedError {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if(thread.getName().equals(Saver.name)){
                ((Saver)thread).putObject(key,o);
                return;
            }
        }
        throw new NotInitializedError("Controller not initialized. " +
                "You need anonimly call constructor once before using static methods");
    }

    public static void clear(){
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if(thread.getName().equals(Saver.name)){
                ((Saver)thread).clear();
                thread.interrupt();
            }
        }
    }

    private static class Saver extends Thread{

        public static String name = "saverThread";

        private HashMap<String,Object> map = new HashMap<>();

        public Object getObject(String key) {
            return map.get(key);
        }

        public void putObject(String key,Object o){
            map.put(key,o);
        }


        @Override
        public void run() {
            setName(name);

            while(!isInterrupted()){
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        public void clear() {
            map.clear();
        }
    }


    public static class NotInitializedError extends Exception{
        public NotInitializedError() {
            super();
        }

        public NotInitializedError(String detailMessage) {
            super(detailMessage);
        }

        public NotInitializedError(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public NotInitializedError(Throwable throwable) {
            super(throwable);
        }
    }

}
