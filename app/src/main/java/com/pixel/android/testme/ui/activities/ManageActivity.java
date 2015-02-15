package com.pixel.android.testme.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.pixel.android.testme.R;
import com.pixel.android.testme.ui.fragments.ManageMainScreenFragment;

public class ManageActivity extends ActionBarActivity {

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, ManageActivity.class));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ManageMainScreenFragment.newInstance())
                .commit();
    }

}
