package com.pixel.android.testme.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pixel.android.testme.R;
import com.pixel.android.testme.ui.activities.CreateTestActivity;
import com.pixel.android.testme.ui.activities.ManageActivity;
import com.pixel.android.testme.ui.activities.ResumeTestActivity;
import com.pixel.android.testme.ui.activities.TestPresentationActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainScreenFragment extends Fragment {

    public static MainScreenFragment newInstance() {
        return new MainScreenFragment();
    }

    public MainScreenFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.manage_tests)
    public void onManageTests(View view) {
        ManageActivity.startActivity(getActivity());
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.create_test)
    public void onCreateTest(View view) {
        CreateTestActivity.startActivityForResult(this, CreateTestActivity.GENERATE_NEW_TEST);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.review_tests)
    public void onReviewTest(View view) {
        ResumeTestActivity.startActivity(getActivity(), true);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.resume_test)
    public void onResumeTest(View view) {
        ResumeTestActivity.startActivity(getActivity(), false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CreateTestActivity.GENERATE_NEW_TEST && data != null && data.hasExtra(CreateTestActivity.INTENT_KEY_GENERATED_TEST)) {
            TestPresentationActivity.startActivity(getActivity(), data.getExtras().getParcelable(CreateTestActivity.INTENT_KEY_GENERATED_TEST), true);
        }
    }
}
