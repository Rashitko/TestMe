package com.pixel.android.testme.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastGenerator {

    public static void showShort (String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLong (String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
