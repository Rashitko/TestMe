package com.pixel.android.testme.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.pixel.android.testme.R;
import com.pixel.android.testme.interfaces.AnswersAdapterCallbacks;

import java.util.ArrayList;
import java.util.HashSet;

public class AnswersAdapter extends ArrayAdapter<String> {
    private final AnswersAdapterCallbacks mListener;
    private final HashSet<Integer> mSelectedPositions;
    private final HashSet<Integer> mRightAnswers;
    private final boolean mEditable;

    public AnswersAdapter(Context context, AnswersAdapterCallbacks listener, ArrayList<String> questions) {
        super(context, R.layout.list_item_saved_test, questions);
        mListener = listener;
        mSelectedPositions = new HashSet<>();
        mRightAnswers = null;
        mEditable = true;
    }

    public AnswersAdapter(Context context, AnswersAdapterCallbacks listener, ArrayList<String> questions, HashSet<Integer> rightAnswers) {
        super(context, R.layout.list_item_saved_test, questions);
        mListener = listener;
        mSelectedPositions = new HashSet<>();
        mRightAnswers = new HashSet<>();
        mEditable = false;
    }

    @Override
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
        if (!mEditable) {
            mRightAnswers.clear();
            mRightAnswers.addAll(mListener.getRightAnswers());
        }
    }

    public void setSelectedPositions(HashSet<Integer> newPositions) {
        mSelectedPositions.clear();
        if (newPositions != null) {
            mSelectedPositions.addAll(newPositions);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_category, parent, false);
        }
        TextView question = (TextView) convertView.findViewById(android.R.id.text1);
        question.setText(getItem(position));
        final int checkBoxId = mEditable ? R.id.checkbox2 : R.id.checkbox;
        final CheckBox checkBox = (CheckBox) convertView.findViewById(checkBoxId);
        checkBox.setChecked(mSelectedPositions.contains(Integer.valueOf(position)));
        checkBox.setClickable(mEditable);
        convertView.setClickable(mEditable);
        if (mEditable) {
            setListeners(position, convertView, checkBox);
        } else {
            checkBox.setVisibility(View.VISIBLE);
            setIndicator(position, convertView);
        }
        return convertView;
    }

    private void setIndicator(int position, View convertView) {
        CheckBox rightAnswer = (CheckBox) convertView.findViewById(R.id.checkbox2);
        rightAnswer.setChecked(true);
        rightAnswer.setClickable(false);
        if (markedOk(position)) {
            rightAnswer.setButtonDrawable(R.drawable.checkbox_tick);
        } else {
            rightAnswer.setButtonDrawable(R.drawable.checkbox_cross);
        }
        rightAnswer.setVisibility(View.VISIBLE);
    }

    private void setListeners(final int position, View convertView, final CheckBox checkBox) {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!mSelectedPositions.contains(Integer.valueOf(position))) {
                        mSelectedPositions.add(position);
                    }
                } else {
                    mSelectedPositions.remove(Integer.valueOf(position));
                }
                if (mListener != null) {
                    mListener.onAnswerSelected(mSelectedPositions);
                }
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.performClick();
            }
        });
    }

    private boolean markedOk(int position) {
        return (mRightAnswers.contains(Integer.valueOf(position)) && mSelectedPositions.contains(Integer.valueOf(position))) ||
                (!mRightAnswers.contains(Integer.valueOf(position)) && !mSelectedPositions.contains(Integer.valueOf(position)));
    }
}
