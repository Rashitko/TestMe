package com.pixel.android.testme.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.pixel.android.testme.R;
import com.pixel.android.testme.io.models.ImportedData;
import com.pixel.android.testme.utils.Logger;
import com.pixel.android.testme.utils.ToastGenerator;

import java.io.FileNotFoundException;
import java.io.FileReader;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ManageMainScreenFragment extends Fragment {

    private static final int REQUEST_CHOOSER = 1234;

    public ManageMainScreenFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manage_main_screen, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.add_questions)
    public void onAddQuestions(View view) {
        Intent getContentIntent = FileUtils.createGetContentIntent();
        Intent intent = Intent.createChooser(getContentIntent, "Select a file");
        startActivityForResult(intent, REQUEST_CHOOSER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHOOSER) {
            if (resultCode == Activity.RESULT_OK) {
                final Uri uri = data.getData();
                String path = FileUtils.getPath(getActivity(), uri);
                if (path != null && FileUtils.isLocal(path)) {
                    try {
                        FileReader reader = new FileReader(path);
                        Gson gson = new Gson();
                        ImportedData imported = gson.fromJson(reader, ImportedData.class);
                        final int savedCount = imported.save(getActivity());
                        ToastGenerator.showShort(getString(R.string.questions_saved) + savedCount, getActivity());
                        getActivity().finish();
                    } catch (FileNotFoundException | RemoteException | OperationApplicationException e) {
                        e.printStackTrace();
                    }
                } else {
                    Logger.d("Only local files are supported now", this.getClass());
                }
            }
        }
    }

    public static ManageMainScreenFragment newInstance() {
        return new ManageMainScreenFragment();
    }

}
