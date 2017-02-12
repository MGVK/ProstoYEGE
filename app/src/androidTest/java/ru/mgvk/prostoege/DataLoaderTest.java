package ru.mgvk.prostoege;

import android.util.Log;

import junit.framework.TestCase;

/**
 * Created by mihail on 18.09.16.
 */
public class DataLoaderTest extends TestCase {

    @Override
    protected void runTest() throws Throwable {
        super.runTest();

        Log.d("test",DataLoader.getProfile("test"));

    }
}