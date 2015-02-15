package com.pixel.android.testme.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.pixel.android.testme.R;
import com.pixel.android.testme.ui.fragments.MainScreenFragment;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainScreenFragment.newInstance())
                    .commit();
        }
    }

}
