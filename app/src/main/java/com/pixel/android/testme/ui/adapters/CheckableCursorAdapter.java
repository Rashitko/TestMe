package com.pixel.android.testme.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.pixel.android.testme.R;
import com.pixel.android.testme.db.TestMeContract;
import com.pixel.android.testme.interfaces.CheckableCursorCallbacks;
import com.pixel.android.testme.utils.Logger;

import java.util.ArrayList;

public class CheckableCursorAdapter extends CursorAdapter {

    private ArrayList<Integer> mCheckedPositions;
    private Context mContext;
    private CheckableCursorCallbacks mListener;

    public CheckableCursorAdapter(Context context, CheckableCursorCallbacks listener) {
        super(context, null, 0);
        initialize(context, listener);
    }

    private void initialize(Context context, CheckableCursorCallbacks listener) {
        mContext = context;
        mCheckedPositions = new ArrayList<>();
        mListener = listener;
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        final Cursor cursor = super.swapCursor(newCursor);
        if (mCheckedPositions.size() == 0) {
            for (int i = 0; i < newCursor.getCount(); i++) {
                mCheckedPositions.add(i);
            }
        }
        return cursor;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.list_item_category, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Logger.d("Bind view", getClass());
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_category, parent, false);
        }
        TextView mTextView = (TextView) convertView.findViewById(android.R.id.text1);
        getCursor().moveToPosition(position);
        mTextView.setText(getCursor().getString(getCursor().getColumnIndex(TestMeContract.Categories.CATEGORY_NAME)));
        CheckBox mCheckBox = (CheckBox) convertView.findViewById(R.id.checkbox2);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.findViewById(R.id.checkbox2).performClick();
            }
        });
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!mCheckedPositions.contains(Integer.valueOf(position))) {
                        mCheckedPositions.add(position);

                    }
                } else {
                    mCheckedPositions.remove(Integer.valueOf(position));
                }
                if (mListener != null) {
                    mListener.onSelectionChanged(getSelectedIds());
                }
            }
        });
        mCheckBox.setChecked(mCheckedPositions.contains(Integer.valueOf(position)));
        return convertView;
    }

    public ArrayList<Integer> getCheckedPositions() {
        return mCheckedPositions;
    }

    public void setCheckedPositions(ArrayList<Integer> checkedPositions) {
        this.mCheckedPositions = checkedPositions;
    }

    public ArrayList<Long> getSelectedIds() {
        ArrayList<Long> result = new ArrayList<>(mCheckedPositions.size());
        for (int i = 0; i < mCheckedPositions.size(); i++) {
            result.add(getItemId(mCheckedPositions.get(i)));
        }
        return result;
    }
}
