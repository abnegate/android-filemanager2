package com.jakebarnby.filemanager;

import android.os.AsyncTask;

/**
 * Created by Jake on 10/30/2017.
 */

public class Util {

    public static void waitMillis(long millis) {
        AsyncTask.execute(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
