package com.pixel.android.testme.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import com.pixel.android.testme.R;
import com.pixel.android.testme.interfaces.TestCreator;
import com.pixel.android.testme.ui.fragments.ClassesListFragment;
import com.pixel.android.testme.ui.fragments.TestDetailsFragment;

public class CreateTestActivity extends ActionBarActivity implements TestCreator {

    private static final String SELECTED_CLASS = "SELECTED_CLASS";
    public static final String INTENT_KEY_GENERATED_TEST = "INTENT_KEY_GENERATED_TEST";
    private long mSelectedClass = -1;
    public static final int GENERATE_NEW_TEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELECTED_CLASS)) {
                mSelectedClass = savedInstanceState.getLong(SELECTED_CLASS);
            }
        } else {
            setSelectClass();
        }
    }

    private void setSelectClass() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ClassesListFragment.newInstance())
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSelectedClass >= 0) {
            outState.putLong(SELECTED_CLASS, mSelectedClass);
        }
    }

    public static void startActivityForResult(Fragment context, int requestCode) {
        context.startActivityForResult(new Intent(context.getActivity(), CreateTestActivity.class), requestCode);
    }

    @Override
    public void setClass(long id) {
        mSelectedClass = id;
        setTestDetailsFragment();
    }

    @Override
    public void onBack() {
        mSelectedClass = -1;
        getFragmentManager().popBackStack();
//        setSelectClass();
    }

    private void setTestDetailsFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, TestDetailsFragment.newInstance(mSelectedClass))
                .addToBackStack(null)
                .commit();
    }


}
