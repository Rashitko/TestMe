package com.pixel.android.testme.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import com.pixel.android.testme.R;
import com.pixel.android.testme.ui.fragments.PausedTestsListFragment;

public class ResumeTestActivity extends ActionBarActivity {

    private static final String KEY_COMPLETED = "COMPLETED";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, PausedTestsListFragment.newInstance(getIntent().getExtras().getBoolean(KEY_COMPLETED, false)))
                    .commit();
        }
    }

    public static void startActivity(Context context, boolean completed) {
        Intent intent = new Intent(context, ResumeTestActivity.class);
        intent.putExtra(KEY_COMPLETED, completed);
        context.startActivity(intent);
    }

}
