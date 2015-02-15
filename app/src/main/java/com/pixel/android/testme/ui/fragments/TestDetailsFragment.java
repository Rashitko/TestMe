package com.pixel.android.testme.ui.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.pixel.android.testme.R;
import com.pixel.android.testme.db.TestMeContract;
import com.pixel.android.testme.io.models.GeneratedTest;
import com.pixel.android.testme.ui.activities.CreateTestActivity;
import com.pixel.android.testme.ui.adapters.CheckableCursorAdapter;
import com.pixel.android.testme.ui.dialogs.ErrorDialog;
import com.pixel.android.testme.ui.dialogs.WarningDialog;
import com.pixel.android.testme.interfaces.CheckableCursorCallbacks;
import com.pixel.android.testme.interfaces.TestCreator;
import com.pixel.android.testme.utils.Logger;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnFocusChange;

public class TestDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, CheckableCursorCallbacks {
    private static final String SELECTED_CLASS = "SELECTED_CLASS";
    private static final String SELECTED_COUNT_TEXT = "SELECTED_COUNT_TEXT";
    private static final String LIMITED_CHECKED = "LIMITED_CHECKED";
    private static final String TIME_LIMIT_TEXT = "TIME_LIMIT_TEXT";
    private static final String CHECKED_INDICES = "CHECKED_INDICES";
    private static final String SELECTED_CATEGORIES = "SELECTED_CATEGORIES";
    private static final int QUESTIONS_COUNT_LOADER_ID = 0;
    private static final int CATEGORIES_LOADER_ID = 1;
    private int mCount = -1;
    private CheckableCursorAdapter mAdapter;

    @InjectView(R.id.questions_total_count)
    TextView mQuestionTotalCount;
    @InjectView(R.id.questions_selected_count)
    TextView mSelectedCount;
    @InjectView(R.id.limited)
    CheckBox mLimited;
    @InjectView(R.id.time_limit)
    EditText mTimeLimit;
    @InjectView(R.id.categories)
    ListView mCategories;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new CheckableCursorAdapter(getActivity(), this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCategories.setAdapter(mAdapter);
        getLoaderManager().initLoader(QUESTIONS_COUNT_LOADER_ID, getQuestionsCountAdapterBundle(mAdapter.getSelectedIds()), this);
        getLoaderManager().initLoader(CATEGORIES_LOADER_ID, null, this);
        if (savedInstanceState != null) {
            mSelectedCount.setText(savedInstanceState.getString(SELECTED_COUNT_TEXT, ""));
            mTimeLimit.setText(savedInstanceState.getString(TIME_LIMIT_TEXT, ""));
            mLimited.setChecked(savedInstanceState.getBoolean(LIMITED_CHECKED, false));
            mAdapter.setCheckedPositions((ArrayList<Integer>) savedInstanceState.getSerializable(CHECKED_INDICES));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test_details, container, false);
        ButterKnife.inject(this, rootView);
        mCategories.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SELECTED_COUNT_TEXT, mSelectedCount.getText().toString());
        outState.putString(TIME_LIMIT_TEXT, mTimeLimit.getText().toString());
        outState.putBoolean(LIMITED_CHECKED, mLimited.isChecked());
        outState.putSerializable(CHECKED_INDICES, mAdapter.getCheckedPositions());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.back)
    public void onBack(View view) {
        ((TestCreator) getActivity()).onBack();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.start_test)
    public void onStartTest(View view) {
        view.requestFocus();
        if (validate()) {
            final long classId = getArguments().getLong(SELECTED_CLASS);
            final int questionsCount = Integer.parseInt(mSelectedCount.getText().toString());
            final ArrayList<Long> selectedCategories = mAdapter.getSelectedIds();
            final int timeLimit = mLimited.isChecked() ? Integer.parseInt(mTimeLimit.getText().toString()) * 60 : -1;
            final GeneratedTest generatedTest = GeneratedTest.generateNew(classId, questionsCount, selectedCategories, timeLimit, getActivity());
            Intent intent = new Intent();
            intent.putExtra(CreateTestActivity.INTENT_KEY_GENERATED_TEST, generatedTest);
            getActivity().setResult(CreateTestActivity.GENERATE_NEW_TEST, intent);
            getActivity().finish();
        } else {
            Logger.d("Input errors, cant start test", this.getClass());
        }
    }

    public static TestDetailsFragment newInstance(long selectedClass) {
        Bundle args = new Bundle();
        args.putLong(SELECTED_CLASS, selectedClass);
        TestDetailsFragment fragment = new TestDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == QUESTIONS_COUNT_LOADER_ID) {
            Uri baseUri = TestMeContract.Questions.CONTENT_URI;
            final String[] projection = new String[]{TestMeContract.Questions._ID};
            final long classId = getArguments().getLong(SELECTED_CLASS);
            String selection;
            ArrayList<Long> selectedCategories = (ArrayList<Long>) args.getSerializable(SELECTED_CATEGORIES);
            selection = TestMeContract.Questions.findByClassIdAndCategories(classId, selectedCategories);
            return new CursorLoader(getActivity(), baseUri, projection, selection, null, null);
        } else if (id == CATEGORIES_LOADER_ID) {
            Uri baseUri = TestMeContract.Categories.CONTENT_URI;
            final String[] projection = new String[]{TestMeContract.Categories._ID, TestMeContract.Categories.CATEGORY_NAME};
            final long classId = getArguments().getLong(SELECTED_CLASS);
            final String order = TestMeContract.Categories.CATEGORY_NAME + " ASC";
            return new CursorLoader(getActivity(), baseUri, projection, TestMeContract.Categories.findByClassId(classId), null, order);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == QUESTIONS_COUNT_LOADER_ID) {
            mCount = data.getCount();
            setTotalCountLabel();
        } else if (loader.getId() == CATEGORIES_LOADER_ID) {
            mAdapter.swapCursor(data);
            while (data.moveToNext()) {
                Logger.d(data.getString(data.getColumnIndex(TestMeContract.Categories.CATEGORY_NAME)), this.getClass());
            }
        }
    }

    private void setTotalCountLabel() {
        mQuestionTotalCount.setText(getString(R.string.questions_total_count) + mCount);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == QUESTIONS_COUNT_LOADER_ID) {
            mCount = -1;
        } else if (loader.getId() == CATEGORIES_LOADER_ID) {
            mAdapter.swapCursor(null);
        }
    }

    @SuppressWarnings("unused")
    @OnEditorAction(R.id.questions_selected_count)
    public boolean onQuestionCountEdition(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            validateSelectedQuestionsCount();
        }
        return true;
    }

    @SuppressWarnings("unused")
    @OnEditorAction(R.id.time_limit)
    public boolean onTimeLimitEdition(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            validateTimeLimit();
        }
        return true;
    }

    private boolean validate() {
        return validateCategoriesSelection() && validateSelectedQuestionsCount() && validateTimeLimit();
    }

    private boolean validateCategoriesSelection() {
        if (mAdapter.getSelectedIds().size() == 0) {
            ErrorDialog.createDialog(getFragmentManager(), getString(R.string.too_few_categories_selected));
            return false;
        }
        return true;
    }

    private boolean validateTimeLimit() {
        if (mLimited == null || mTimeLimit == null || !mLimited.isChecked()) {
            return true;
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(mTimeLimit.getText().toString());
            return true;
        } catch (NumberFormatException e) {
            ErrorDialog.createDialog(getFragmentManager(), getString(R.string.wrong_time_limit_error));
            return false;
        }
    }

    private boolean validateSelectedQuestionsCount() {
        if (mSelectedCount == null) {
            return true;
        }
        try {
            final int selectedCount = Integer.parseInt(mSelectedCount.getText().toString());
            if (selectedCount > mCount) {
                mSelectedCount.setText("" + mCount);
                WarningDialog.createDialog(getFragmentManager(), getString(R.string.too_much_questions_selected));
                return false;
            } else if (selectedCount < 1) {
                mSelectedCount.setText("1");
                WarningDialog.createDialog(getFragmentManager(), getString(R.string.too_few_questions_selected));
                return false;
            }
        } catch (NumberFormatException e) {
            ErrorDialog.createDialog(getFragmentManager(), getString(R.string.wrong_question_count_error));
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    @OnFocusChange(R.id.questions_selected_count)
    public void onQuestionCountFocusChange(View view, boolean hasFocus) {
        if (!hasFocus && view != null) {
            validateSelectedQuestionsCount();
        }
    }

    @SuppressWarnings("unused")
    @OnCheckedChanged(R.id.limited)
    public void onTimeLimitChecked(CompoundButton buttonView, boolean isChecked) {
        final int visibility = isChecked ? View.VISIBLE : View.GONE;
        mTimeLimit.setVisibility(visibility);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.limited_label)
    public void onTimeLimitLabelClick(View view) {
        mTimeLimit.callOnClick();
    }

    @Override
    public void onSelectionChanged(ArrayList<Long> selectedIds) {
        Bundle args = getQuestionsCountAdapterBundle(selectedIds);
        getLoaderManager().restartLoader(QUESTIONS_COUNT_LOADER_ID, args, this);
    }

    private Bundle getQuestionsCountAdapterBundle(ArrayList<Long> selectedIds) {
        Bundle args = new Bundle();
        args.putSerializable(SELECTED_CATEGORIES, selectedIds);
        return args;
    }
}
