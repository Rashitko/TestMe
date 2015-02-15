package com.pixel.android.testme.ui.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.pixel.android.testme.R;
import com.pixel.android.testme.db.TestMeContract;
import com.pixel.android.testme.interfaces.TestCreator;

public class ClassesListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 1;
    private SimpleCursorAdapter mAdapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(R.string.no_classes));
        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                new String[]{TestMeContract.Classes.CLASS_NAME},
                new int[] {android.R.id.text1},
                0);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    public static ClassesListFragment newInstance() {
        return new ClassesListFragment();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri baseUri = TestMeContract.Classes.CONTENT_URI;
        final String[] projection = new String[] {TestMeContract.Classes._ID, TestMeContract.Classes.CLASS_NAME};
        final String order = TestMeContract.Classes.CLASS_NAME + " ASC";
        return new CursorLoader(getActivity(), baseUri, projection, null, null, order);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ((TestCreator) getActivity()).setClass(id);
    }
}
