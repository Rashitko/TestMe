package com.pixel.android.testme.ui.fragments;

import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pixel.android.testme.R;
import com.pixel.android.testme.db.TestMeContract;
import com.pixel.android.testme.interfaces.AnswersAdapterCallbacks;
import com.pixel.android.testme.io.models.GeneratedTest;
import com.pixel.android.testme.ui.activities.CreateTestActivity;
import com.pixel.android.testme.ui.activities.TestPresentationActivity;
import com.pixel.android.testme.ui.adapters.AnswersAdapter;
import com.pixel.android.testme.ui.dialogs.CloseOnOkDialog;
import com.pixel.android.testme.ui.dialogs.ErrorDialog;
import com.pixel.android.testme.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class TestPresentationFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AnswersAdapterCallbacks {

    private static final String KEY_GENERATED_TEST = TestPresentationActivity.INTENT_KEY_GENERATED_TEST;
    private static final String KEY_EDITABLE = "EDITABLE";
    private static final int LOADER_ID = 1;
    private static final long TIMER_TICK_INTERVAL = 1000;
    private static final String KEY_RIGHT_ANSWERS = "RIGHT_ANSWERS";
    private static final String KEY_RIGHT_ANSWERED_NUMBERS = "RIGHT_ANSWERED_NUMBERS";

    private GeneratedTest mGeneratedTest;
    private AnswersAdapter mAdapter;
    private ArrayList<String> mAnswers = new ArrayList<>();
    private CountDownTimer mTimer;
    private HashSet<Integer> mRightAnswers;
    private HashSet<Integer> mRightAnsweredNumbers;
    private boolean mEditable;

    @InjectView(R.id.question)
    TextView mQuestion;
    @InjectView(R.id.current_question)
    TextView mCurrentQuestion;
    @InjectView(R.id.right_count)
    TextView mRight;
    @InjectView(R.id.time_left)
    TextView mTimeLeft;
    @InjectView(R.id.answers)
    ListView mAnswersList;
    @InjectView(R.id.prev)
    Button mPrev;
    @InjectView(R.id.next)
    Button mNext;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEditable = getArguments().getBoolean(KEY_EDITABLE);

        if (mEditable) {
            mAdapter = new AnswersAdapter(getActivity(), this, mAnswers);
        } else {
            mAdapter = new AnswersAdapter(getActivity(), this, mAnswers, mRightAnswers);
        }
        if (savedInstanceState != null) {
            if (mGeneratedTest == null) {
                mGeneratedTest = savedInstanceState.getParcelable(KEY_GENERATED_TEST);
            }
            if (mRightAnswers == null) {
                mRightAnswers = (HashSet<Integer>) savedInstanceState.getSerializable(KEY_RIGHT_ANSWERS);
            }
            if (mRightAnsweredNumbers == null) {
                mRightAnsweredNumbers = (HashSet<Integer>) savedInstanceState.getSerializable(KEY_RIGHT_ANSWERED_NUMBERS);
            }
        } else {
            mRightAnsweredNumbers = new HashSet<>();

            mGeneratedTest = getArguments().getParcelable(KEY_GENERATED_TEST);
            countRightAnsweredQuestions();
            if (!mEditable) {
                mGeneratedTest.moveToFirst();
            }
        }
    }

    private void countRightAnsweredQuestions() {
        Cursor cursor = getActivity().getContentResolver().query(TestMeContract.Questions.CONTENT_URI,
                new String[]{TestMeContract.Questions._ID, TestMeContract.Questions.RIGHT_ANSWER},
                TestMeContract.Questions.findByIds(mGeneratedTest.getSelectedQuestions()),
                null,
                TestMeContract.Questions._ID + " ASC");
        ArrayList<Integer> rightAnswers;
        int position;
        Gson gson = new Gson();
        final HashMap<Integer, HashSet<Integer>> selectedAnswers = mGeneratedTest.getSelectedAnswers();
        while (cursor.moveToNext()) {
            final String json = cursor.getString(cursor.getColumnIndex(TestMeContract.Questions.RIGHT_ANSWER));
            Logger.d(json, getClass());
            rightAnswers = gson.fromJson(
                    json, new TypeToken<ArrayList<Integer>>() {
                    }.getType());
            position = mGeneratedTest.getSelectedQuestions().indexOf(Long.valueOf(cursor.getInt(cursor.getColumnIndex(TestMeContract.Questions._ID))));
            if (position > mGeneratedTest.getCurrentQuestion()) {
                continue;
            }
            if (selectedAnswers.containsKey(Integer.valueOf(position)) && selectedAnswers.get(Integer.valueOf(position)) != null) {
                HashSet<Integer> ansAtPosition = selectedAnswers.get(Integer.valueOf(position));
                if (rightAnswers.containsAll(ansAtPosition) && ansAtPosition.containsAll(rightAnswers)) {
                    mRightAnsweredNumbers.add(position);
                }
            }
        }
        cursor.close();
    }

    private long computeTimerLimit() {
        return (mGeneratedTest.getTimeLimit() - mGeneratedTest.getTimeSpent()) * 1000;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test_presentation, container, false);
        ButterKnife.inject(this, rootView);
        setButtonVisibility();
        mAnswersList.setAdapter(mAdapter);
        return rootView;
    }

    private void setButtonVisibility() {
        if (mGeneratedTest.getCurrentQuestion() > 0) {
            mPrev.setVisibility(View.VISIBLE);
        }
        if (mGeneratedTest.getCurrentQuestion() == mGeneratedTest.getSelectedQuestions().size() - 1) {
            if (mEditable) {
                mNext.setText(getString(R.string.complete));
            } else {
                mNext.setVisibility(View.INVISIBLE);
            }
        }
    }

    private String getTimeFromMillis(long millisUntilFinished) {
        final int hours = (int) TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
        final int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - (int) TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished));
        final int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - (int) TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished));
        final String minutesString = minutes >= 10 ? Integer.toString(minutes) : "0" + minutes;
        final String secondsString = seconds >= 10 ? Integer.toString(seconds) : "0" + seconds;
        return hours + ":" + minutesString + ":" + secondsString;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mEditable) {
            try {
                mGeneratedTest.update(getActivity());
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }
            final Intent intent = getActivity().getIntent();
            intent.putExtra(CreateTestActivity.INTENT_KEY_GENERATED_TEST, mGeneratedTest);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGeneratedTest.isLimited() && mEditable) {
            final long timerLimit = computeTimerLimit();
            mTimer = new CountDownTimer(timerLimit, TIMER_TICK_INTERVAL) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mTimeLeft.setText(getTimeFromMillis(millisUntilFinished));
                    mGeneratedTest.onTimerTick();
                }

                @Override
                public void onFinish() {
                    mGeneratedTest.onTimerTick();
                    mGeneratedTest.setCompleted(true);
                    CloseOnOkDialog.createDialog(getFragmentManager(), getString(R.string.time_up));
                }
            };
            mTimer.start();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_GENERATED_TEST, mGeneratedTest);
        outState.putSerializable(KEY_RIGHT_ANSWERS, mRightAnswers);
        outState.putSerializable(KEY_RIGHT_ANSWERED_NUMBERS, mRightAnsweredNumbers);
    }

    public static TestPresentationFragment newInstance(GeneratedTest generatedTest, boolean editable) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_GENERATED_TEST, generatedTest);
        args.putBoolean(KEY_EDITABLE, editable);
        TestPresentationFragment testPresentationFragment = new TestPresentationFragment();
        testPresentationFragment.setArguments(args);
        return testPresentationFragment;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = TestMeContract.Questions.CONTENT_URI;
        final String[] projection = {TestMeContract.Questions.QUESTION,
                TestMeContract.Questions.ANSWERS,
                TestMeContract.Questions.RIGHT_ANSWER,
                TestMeContract.Questions._ID};
        final Long questionId = mGeneratedTest.getSelectedQuestions().get(mGeneratedTest.getCurrentQuestion());
        final String selection = TestMeContract.Questions.findById(questionId);
        final String order = TestMeContract.Questions._ID + " ASC LIMIT 1";
        return new CursorLoader(getActivity(), uri, projection, selection, null, order);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Gson gson = new Gson();
        if (data.moveToFirst()) {
            mQuestion.setText(data.getString(data.getColumnIndex(TestMeContract.Questions.QUESTION)));
            HashSet<String> answers = gson.fromJson(
                    data.getString(data.getColumnIndex(TestMeContract.Questions.ANSWERS)),
                    new TypeToken<HashSet<String>>() {
                    }.getType());
            mAnswers.clear();
            mAnswers.addAll(answers);
            mCurrentQuestion.setText((mGeneratedTest.getCurrentQuestion() + 1) + "/" + mGeneratedTest.getSelectedQuestions().size());
            setRightCount();
            mRightAnswers = gson.fromJson(
                    data.getString(data.getColumnIndex(TestMeContract.Questions.RIGHT_ANSWER)),
                    new TypeToken<HashSet<Integer>>() {
                    }.getType());
            mAdapter.setSelectedPositions(mGeneratedTest.getSelectedAnswers().get(mGeneratedTest.getCurrentQuestion()));
            mAdapter.notifyDataSetChanged();
            mAdapter.notifyDataSetInvalidated();
        }
    }

    private void setRightCount() {
        mRight.setText(getString(R.string.right) + ": " + mRightAnsweredNumbers.size());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onAnswerSelected(HashSet<Integer> selectedPositions) {
        mGeneratedTest.setSelectedAnswers(selectedPositions);
    }

    @Override
    public HashSet<Integer> getRightAnswers() {
        return mRightAnswers;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.next)
    public void onNext(View view) {
        if (!valid()) {
            return;
        }
        final HashSet<Integer> savedAnswers = mGeneratedTest.getSelectedAnswers().get(mGeneratedTest.getCurrentQuestion());
        if (mRightAnswers.containsAll(savedAnswers) && savedAnswers.containsAll(mRightAnswers)) {
            mRightAnsweredNumbers.add(mGeneratedTest.getCurrentQuestion());
        }
        if (mGeneratedTest.moveToNextQuestion()) {
            getLoaderManager().restartLoader(LOADER_ID, null, this);
            setButtonVisibility();
        } else {
            if (mTimer != null) {
                mTimer.cancel();
            }
            setRightCount();
            mGeneratedTest.setCompleted(true);
            CloseOnOkDialog.createDialog(getFragmentManager(), getString(R.string.test_complete));
        }
    }

    private boolean valid() {
        final HashMap<Integer, HashSet<Integer>> selectedAnswers = mGeneratedTest.getSelectedAnswers();
        final boolean valid = selectedAnswers.containsKey(mGeneratedTest.getCurrentQuestion())
                && selectedAnswers.get(mGeneratedTest.getCurrentQuestion()).size() > 0;
        if (!valid) {
            ErrorDialog.createDialog(getFragmentManager(), getString(R.string.error_select_option));
        }
        return valid;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.prev)
    public void onPrev(View view) {
        if (mGeneratedTest.moveToPrevQuestion()) {
            if (mEditable && mRightAnsweredNumbers.contains(Integer.valueOf(mGeneratedTest.getCurrentQuestion()))) {
                mRightAnsweredNumbers.remove(Integer.valueOf(mGeneratedTest.getCurrentQuestion()));
            }
            if (mGeneratedTest.getCurrentQuestion() == 0) {
                view.setVisibility(View.INVISIBLE);
            }
            mNext.setText(getString(R.string.button_text_next));
            mNext.setVisibility(View.VISIBLE);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        } else {
            ErrorDialog.createDialog(getFragmentManager(), getString(R.string.error_first_question));
        }
    }
}
