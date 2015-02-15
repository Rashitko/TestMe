package com.pixel.android.testme.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import com.pixel.android.testme.R;
import com.pixel.android.testme.io.models.GeneratedTest;
import com.pixel.android.testme.ui.fragments.TestPresentationFragment;

public class TestPresentationActivity extends ActionBarActivity {
    private static final String INTENT_KEY_EDITABLE = "EDITABLE";
    public static String INTENT_KEY_GENERATED_TEST = CreateTestActivity.INTENT_KEY_GENERATED_TEST;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_presentation);
        final boolean editable = getIntent().getExtras().getBoolean(INTENT_KEY_EDITABLE);
        if (!editable) {
            setTitle(getString(R.string.review_tests));
        }
        final GeneratedTest generatedTest = getIntent().getExtras().getParcelable(INTENT_KEY_GENERATED_TEST);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, TestPresentationFragment.newInstance(generatedTest, editable))
                    .commit();
        }
    }

    public static void startActivity(Context context, Parcelable generatedTest, boolean editableFlag) {
        Intent intent = new Intent(context, TestPresentationActivity.class);
        intent.putExtra(INTENT_KEY_GENERATED_TEST, generatedTest);
        intent.putExtra(INTENT_KEY_EDITABLE, editableFlag);
        context.startActivity(intent);
    }
}
