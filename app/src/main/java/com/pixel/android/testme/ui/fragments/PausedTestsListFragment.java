package com.pixel.android.testme.ui.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;

import com.pixel.android.testme.db.TestMeContract;
import com.pixel.android.testme.io.models.GeneratedTest;
import com.pixel.android.testme.ui.activities.ResumeTestActivity;
import com.pixel.android.testme.ui.activities.TestPresentationActivity;
import com.pixel.android.testme.ui.adapters.SavedTestsAdapter;

import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class PausedTestsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 1;
    private static final String KEY_COMPLETED = "COMPLETED";
    private SavedTestsAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        mAdapter = new SavedTestsAdapter(getActivity());
        setListAdapter(mAdapter);
        final boolean editableFlag = !getArguments().getBoolean(KEY_COMPLETED, false);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Cursor cursor = mAdapter.getCursor();
                cursor.moveToPosition(position);
                final int testId = cursor.getInt(cursor.getColumnIndex(TestMeContract.Tests._ID));
                TestPresentationActivity.startActivity(getActivity(),
                        GeneratedTest.loadFromDb(getActivity(), testId),
                        editableFlag);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                TestMeContract.Tests.findByCompletedUri(getArguments().getBoolean(KEY_COMPLETED)),
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public static PausedTestsListFragment newInstance(boolean completed) {
        Bundle args = new Bundle();
        args.putBoolean(KEY_COMPLETED, completed);
        final PausedTestsListFragment fragment = new PausedTestsListFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
