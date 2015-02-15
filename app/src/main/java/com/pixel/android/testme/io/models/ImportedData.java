package com.pixel.android.testme.io.models;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.pixel.android.testme.db.TestMeContract;
import com.pixel.android.testme.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class ImportedData {
    @SerializedName("class")
    private String lessonName;
    private List<Question> questions;

    public List<Question> getQuestions() {
        return questions;
    }

    public String getLessonName() {
        return lessonName;
    }

    public int save(Context context) throws RemoteException, OperationApplicationException {
        final ContentResolver contentResolver = context.getContentResolver();
        final long classId = getClassId(getLessonName(), contentResolver);
        ContentValues values = new ContentValues();
        final ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        Gson gson = new Gson();
        final ArrayList<Long> categoryIds = new ArrayList<>();
        long id;
        for (int i = 0; i < getQuestions().size(); i++) {
            id = getCategoryId(getQuestions().get(i).getCategory(), classId, contentResolver);
            categoryIds.add(id);
        }
        for (int i = 0; i < getQuestions().size(); i++) {
            values.put(TestMeContract.Questions.QUESTION, getQuestions().get(i).getQuestion());
            values.put(TestMeContract.Questions.RIGHT_ANSWER, gson.toJson(getQuestions().get(i).getRightAnswer()));
            values.put(TestMeContract.Questions.ANSWERS, gson.toJson(getQuestions().get(i).getAnswers()));
            values.put(TestMeContract.Questions.CLASS_ID, classId);
            values.put(TestMeContract.Questions.CATEGORY_ID, categoryIds.get(i));
            operations.add(ContentProviderOperation.newInsert(TestMeContract.Questions.CONTENT_URI).withValues(values).build());
        }
        final ContentProviderResult[] contentProviderResults = contentResolver.applyBatch(TestMeContract.CONTENT_AUTHORITY, operations);
        Logger.d(contentProviderResults.length + " questions added", this.getClass());
        return contentProviderResults.length;
    }

    private long getClassId(String className, ContentResolver contentResolver) throws RemoteException, OperationApplicationException {
        Uri uri = TestMeContract.Classes.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri,
                new String[] {TestMeContract.Classes._ID},
                TestMeContract.Classes.findByName(className),
                null,
                null);
        long id;
        if (cursor.getCount() < 1) {
            final ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            operations.add(ContentProviderOperation.newInsert(TestMeContract.Classes.CONTENT_URI).withValue(TestMeContract.Classes.CLASS_NAME, className).build());
            final ContentProviderResult[] contentProviderResults = contentResolver.applyBatch(TestMeContract.CONTENT_AUTHORITY, operations);
            id = TestMeContract.Classes.getId(contentProviderResults[0].uri);
        } else {
            cursor.moveToFirst();
            id = cursor.getLong(cursor.getColumnIndex(TestMeContract.Classes._ID));
        }
        cursor.close();
        return id;
    }

    private long getCategoryId(String categoryName, long classId, ContentResolver contentResolver) throws RemoteException, OperationApplicationException {
        Uri uri = TestMeContract.Categories.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri,
                new String[] {TestMeContract.Categories._ID},
                TestMeContract.Categories.findByName(categoryName, classId),
                null,
                null);
        long id;
        if (cursor.getCount() < 1) {
            final ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            final ContentValues values = new ContentValues();
            values.put(TestMeContract.Categories.CATEGORY_NAME, categoryName);
            values.put(TestMeContract.Categories.CLASS_ID, classId);
            operations.add(ContentProviderOperation.newInsert(TestMeContract.Categories.CONTENT_URI).withValues(values).build());
            final ContentProviderResult[] contentProviderResults = contentResolver.applyBatch(TestMeContract.CONTENT_AUTHORITY, operations);
            id = TestMeContract.Categories.getId(contentProviderResults[0].uri);
        } else {
            cursor.moveToFirst();
            id = cursor.getLong(cursor.getColumnIndex(TestMeContract.Categories._ID));
        }
        cursor.close();
        return id;
    }
}
