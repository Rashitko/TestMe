package com.pixel.android.testme.io.models;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pixel.android.testme.db.TestMeContract;
import com.pixel.android.testme.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class GeneratedTest implements Parcelable {

    private int testId;
    private final long classId;
    private boolean limited;
    private final int timeLimit;
    private int timeSpent;
    private int currentQuestion;
    private final ArrayList<Long> selectedQuestions;
    private HashMap<Integer, HashSet<Integer>> selectedAnswers;
    private boolean completed;

    public GeneratedTest(long classId, int timeLimit, ArrayList<Long> selectedQuestions) {
        this.classId = classId;
        this.timeLimit = timeLimit;
        this.selectedQuestions = selectedQuestions;
        limited = timeLimit > 0;
        selectedAnswers = new HashMap<>();
    }

    public static GeneratedTest generateNew(long classId, int questionsCount, ArrayList<Long> selectedCategories, int timeLimit, Context context) {
        ArrayList<Long> selectedQuestions = new ArrayList<>(questionsCount);
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = {TestMeContract.Questions._ID};
        final String selection = TestMeContract.Questions.findByClassIdAndCategories(classId, selectedCategories);
        final Cursor cursor = resolver.query(TestMeContract.Questions.CONTENT_URI, projection, selection, null, "RANDOM() LIMIT " + questionsCount);
        while (cursor.moveToNext()) {
            selectedQuestions.add(cursor.getLong(cursor.getColumnIndex(TestMeContract.Questions._ID)));
        }
        cursor.close();
        GeneratedTest test = new GeneratedTest(classId, timeLimit, selectedQuestions);
        Gson gson = new Gson();
        try {
            test.save(context);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
        Logger.d(gson.toJson(test), GeneratedTest.class);
        return test;
    }

    private void save(Context context) throws RemoteException, OperationApplicationException {
        ContentValues values = getContentValues();
        final ContentResolver resolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.add(ContentProviderOperation.newInsert(TestMeContract.Tests.CONTENT_URI).withValues(values).build());
        final ContentProviderResult[] contentProviderResults = resolver.applyBatch(TestMeContract.CONTENT_AUTHORITY, operations);
        if (contentProviderResults.length != 1) {
            throw new IllegalStateException("only one test should be created");
        }
        testId = TestMeContract.Tests.getId(contentProviderResults[0].uri);
    }

    private ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        Gson gson = new Gson();
        values.put(TestMeContract.Tests.CLASS_ID, classId);
        values.put(TestMeContract.Tests.LIMITED, limited);
        values.put(TestMeContract.Tests.TIME_LIMIT, timeLimit);
        values.put(TestMeContract.Tests.TIME_SPENT, timeSpent);
        values.put(TestMeContract.Tests.CURRENT_QUESTION, currentQuestion);
        values.put(TestMeContract.Tests.SELECTED_QUESTIONS, gson.toJson(selectedQuestions));
        values.put(TestMeContract.Tests.SELECTED_ANSWERS, gson.toJson(selectedAnswers));
        values.put(TestMeContract.Tests.COMPLETED, completed);
        return values;
    }

    public void update(Context context) throws RemoteException, OperationApplicationException {
        ContentValues values = getContentValues();
        final ContentResolver resolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.add(ContentProviderOperation
                .newUpdate(TestMeContract.Tests.CONTENT_URI)
                .withValues(values)
                .withSelection(TestMeContract.Tests.findById(getTestId()), null)
                .build());
        final ContentProviderResult[] contentProviderResults = resolver.applyBatch(TestMeContract.CONTENT_AUTHORITY, operations);
        Logger.d(Arrays.toString(contentProviderResults), getClass());
    }

    public int getTestId() {
        return testId;
    }

    public boolean isLimited() {
        return limited;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public int getTimeSpent() {
        return timeSpent;
    }

    public int getCurrentQuestion() {
        return currentQuestion;
    }

    public ArrayList<Long> getSelectedQuestions() {
        return selectedQuestions;
    }

    public HashMap<Integer, HashSet<Integer>> getSelectedAnswers() {
        return selectedAnswers;
    }


    public static Parcelable loadFromDb(Context context, int id) {
        final ContentResolver resolver = context.getContentResolver();
        final String order = TestMeContract.Tests._ID + " ASC LIMIT 1";
        final Cursor cursor = resolver.query(TestMeContract.Tests.CONTENT_URI, null, TestMeContract.Tests.findById(id), null, order);
        GeneratedTest generatedTest = null;
        if (cursor.moveToFirst()) {
            Gson gson = new Gson();
            final long loadedClassId = cursor.getLong(cursor.getColumnIndex(TestMeContract.Tests.CLASS_ID));
            final int loadedTimeLimit = cursor.getInt(cursor.getColumnIndex(TestMeContract.Tests.TIME_LIMIT));
            final String selQuestJson = cursor.getString(cursor.getColumnIndex(TestMeContract.Tests.SELECTED_QUESTIONS));
            final ArrayList<Long> loadedSelectedQuestions = gson.fromJson(selQuestJson, new TypeToken<ArrayList<Long>>() {
            }.getType());
            generatedTest = new GeneratedTest(loadedClassId, loadedTimeLimit, loadedSelectedQuestions);
            generatedTest.testId = id;
            generatedTest.limited = loadedTimeLimit > 0;
            generatedTest.timeSpent = cursor.getInt(cursor.getColumnIndex(TestMeContract.Tests.TIME_SPENT));
            generatedTest.currentQuestion = cursor.getInt(cursor.getColumnIndex(TestMeContract.Tests.CURRENT_QUESTION));
            generatedTest.completed = cursor.getInt(cursor.getColumnIndex(TestMeContract.Tests.COMPLETED)) == 1;
            final String selAnsJson = cursor.getString(cursor.getColumnIndex(TestMeContract.Tests.SELECTED_ANSWERS));
            try {
                final JSONObject object = new JSONObject(selAnsJson);
                int position = 0;
                while (object.has(Integer.toString(position))) {
                    final JSONArray jsonArray = object.getJSONArray(Integer.toString(position));
                    HashSet<Integer> listData = new HashSet<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        listData.add((Integer) jsonArray.get(i));
                    }
                    generatedTest.selectedAnswers.put(position, listData);
                    position++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Logger.e("cant find test with id: " + id, GeneratedTest.class);
        }
        cursor.close();
        return generatedTest;
    }

    public void onTimerTick() {
        timeSpent++;
    }

    public void setSelectedAnswers(HashSet<Integer> selectedPositions) {
        HashSet<Integer> positions = new HashSet<>(selectedPositions);
        selectedAnswers.put(currentQuestion, positions);
    }

    public boolean moveToNextQuestion() {
        if (currentQuestion + 1 < selectedQuestions.size()) {
            currentQuestion++;
            return true;
        }
        return false;
    }

    public boolean moveToPrevQuestion() {
        if (currentQuestion > 0) {
            currentQuestion--;
            return true;
        }
        return false;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.testId);
        dest.writeLong(this.classId);
        dest.writeByte(limited ? (byte) 1 : (byte) 0);
        dest.writeInt(this.timeLimit);
        dest.writeInt(this.timeSpent);
        dest.writeInt(this.currentQuestion);
        dest.writeSerializable(this.selectedQuestions);
        dest.writeSerializable(this.selectedAnswers);
        dest.writeByte(completed ? (byte) 1 : (byte) 0);
    }

    private GeneratedTest(Parcel in) {
        this.testId = in.readInt();
        this.classId = in.readLong();
        this.limited = in.readByte() != 0;
        this.timeLimit = in.readInt();
        this.timeSpent = in.readInt();
        this.currentQuestion = in.readInt();
        this.selectedQuestions = (ArrayList<Long>) in.readSerializable();
        this.selectedAnswers = (HashMap<Integer, HashSet<Integer>>) in.readSerializable();
        this.completed = in.readByte() != 0;
    }

    public static final Creator<GeneratedTest> CREATOR = new Creator<GeneratedTest>() {
        public GeneratedTest createFromParcel(Parcel source) {
            return new GeneratedTest(source);
        }

        public GeneratedTest[] newArray(int size) {
            return new GeneratedTest[size];
        }
    };

    public void moveToFirst() {
        currentQuestion = 0;
    }
}
