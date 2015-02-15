package com.pixel.android.testme.utils;

import android.util.Log;

public class Logger {

    private static final String PREFIX = "Raspilot.";

    public static void d(String message, Class c) {
        String tag = PREFIX + c.getName();
        Log.d(tag, message);
    }

    public static void e(String message, Class c) {
        String tag = PREFIX + c.getName();
        Log.e(tag, message);
    }

    public static void w(String message, Class c) {
        String tag = PREFIX + c.getName();
        Log.w(tag, message);
    }
}
