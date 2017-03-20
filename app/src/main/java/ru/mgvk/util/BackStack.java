package ru.mgvk.util;

import java.util.LinkedHashMap;

/**
 * Created by mike on 19.03.17.
 */
public class BackStack extends LinkedHashMap<String, Runnable> {

    private int lastKeyID = 0;
    private String lastKey = "";
    private Runnable state = new Runnable() {
        @Override
        public void run() {

        }
    };

    public Runnable returnTo(String key) {
        if (containsKey(key)) {
            Object[] set = keySet().toArray();
            for (int i = size() - 1; i > 0; i--) {
                if (set[i].equals(key)) {
                    Runnable tmp = super.get(set[i]);
                    remove(tmp);
                    return tmp;
                }
                super.get(set[i]).run();
                remove(set[i]);
            }
        }
        return null;
    }

    public void returnToState(String tag) {
        if (containsKey(tag)) {
            Object[] set = keySet().toArray();
            for (int i = size() - 1; i > 0; i--) {
                if (set[i].equals(tag)) {
                    return;
                }
                super.get(set[i]).run();
                remove(set[i]);
            }
        }
    }

    public void addState(String tag) {
        addAction(tag, state);
    }

    public void addAction(String key, Runnable value) {
        super.put(lastKey = key, value);
    }

    public void addAction(Runnable value) {
        super.put(String.valueOf(++lastKeyID), value);
    }

    public void removeLastAction() {
        if (size() > 0) {
            remove(keySet().toArray()[size() - 1]);
        }
    }

    public Runnable pop() {

        if (size() > 0) {
            Runnable tmp = (Runnable) values().toArray()[size() - 1];
            remove(keySet().toArray()[size() - 1]);
            if (tmp == state) {
                return pop();
            }
            return tmp;
        }
        return null;

    }


}
