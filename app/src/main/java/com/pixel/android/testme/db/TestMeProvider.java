package com.pixel.android.testme.db;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.pixel.android.testme.utils.SelectionBuilder;

import java.util.ArrayList;

public class TestMeProvider extends ContentProvider {

    private static final UriMatcher URI_MATCHER = buildUriMatcher();
    private TestMeDatabase dbHelper;

    private static final int CLASSES = 100;
    private static final int CLASSES_ID = 101;

    private static final int QUESTIONS = 200;
    private static final int QUESTIONS_ID = 201;
    private static final int CLASSES_QUESTIONS = 202;

    private static final int CATEGORIES = 300;

    private static final int TESTS = 400;
    private static final int COMPLETE_TESTS = 401;
    private static final int INCOMPLETE_TESTS = 402;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TestMeContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, "classes", CLASSES);
        uriMatcher.addURI(authority, "classes/*", CLASSES_ID);

        uriMatcher.addURI(authority, "questions", QUESTIONS);
        uriMatcher.addURI(authority, "questions/*", QUESTIONS_ID);
        uriMatcher.addURI(authority, "classes/questions/*", CLASSES_QUESTIONS);

        uriMatcher.addURI(authority, "categories", CATEGORIES);

        uriMatcher.addURI(authority, "tests", TESTS);
        uriMatcher.addURI(authority, "tests/complete", COMPLETE_TESTS);
        uriMatcher.addURI(authority, "tests/incomplete", INCOMPLETE_TESTS);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new TestMeDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        final int match = URI_MATCHER.match(uri);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (match == INCOMPLETE_TESTS || match == COMPLETE_TESTS) {
            final String sql = "SELECT " + TestMeContract.Classes.getQualified(TestMeContract.Classes.CLASS_NAME) + ","
                    + TestMeContract.Tests.getQualified(TestMeContract.Tests.CURRENT_QUESTION) + ","
                    + TestMeContract.Tests.getQualified(TestMeContract.Tests.SELECTED_QUESTIONS) + ","
                    + TestMeContract.Tests.getQualified(TestMeContract.Tests.CREATED_AT) + ","
                    + TestMeContract.Tests.getQualified(TestMeContract.Tests.LIMITED) + ","
                    + TestMeContract.Tests.getQualified(TestMeContract.Tests.TIME_LIMIT) + ","
                    + TestMeContract.Tests.getQualified(TestMeContract.Tests.TIME_SPENT) + ","
                    + TestMeContract.Tests.getQualified(TestMeContract.Tests._ID) + " AS _id"
                    + " FROM " + TestMeDatabase.Tables.TESTS
                    + " LEFT OUTER JOIN "
                        + TestMeDatabase.Tables.CLASSES + " ON "
                        + TestMeContract.Classes.getQualified(TestMeContract.Classes._ID)
                        + " = " + TestMeContract.Tests.getQualified(TestMeContract.Tests.CLASS_ID)
                    + " WHERE " + TestMeContract.Tests.findByCompleted(match == COMPLETE_TESTS)
                    + " ORDER BY " + TestMeContract.Tests.getQualified(TestMeContract.Tests.CREATED_AT) + " DESC";
            cursor = db.rawQuery(sql, null);
        } else {
            SelectionBuilder builder = buildUpSelection(uri);
            cursor = builder.where(selection, selectionArgs).query(db, projection, sortOrder);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        long id;
        if (match == CLASSES) {
            id = db.insertOrThrow(TestMeDatabase.Tables.CLASSES, null, values);
            notifyChange(uri, false);
            return TestMeContract.Classes.buildUri(Integer.toString((int) id));
        } else if (match == QUESTIONS) {
            db.insertOrThrow(TestMeDatabase.Tables.QUESTIONS, null, values);
            notifyChange(uri, false);
            return TestMeContract.Questions.CONTENT_URI;
        } else if (match == CATEGORIES) {
            id = db.insertOrThrow(TestMeDatabase.Tables.CATEGORIES, null, values);
            notifyChange(uri, false);
            return TestMeContract.Categories.buildUri(Integer.toString((int) id));
        } else if (match == TESTS) {
            id = db.insertOrThrow(TestMeDatabase.Tables.TESTS, null, values);
            notifyChange(uri, false);
            return TestMeContract.Tests.buildUri(Integer.toString((int) id));
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        if (match == TESTS) {
            final int update = db.update(TestMeDatabase.Tables.TESTS, values, selection, selectionArgs);
            notifyChange(uri, false);
            return update;
        }
        return 0;
    }

    private void notifyChange(Uri uri, boolean sync) {
        Context context = getContext();
        context.getContentResolver().notifyChange(uri, null, sync);
    }

    @Override
    public ContentProviderResult[] applyBatch(@NonNull

                                                  ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    private SelectionBuilder buildUpSelection(Uri uri) {
        SelectionBuilder builder = new SelectionBuilder();
        final int match = URI_MATCHER.match(uri);
        if (match == CLASSES) {
            builder.table(TestMeDatabase.Tables.CLASSES);
        } else if (match == QUESTIONS) {
            builder.table(TestMeDatabase.Tables.QUESTIONS);
        } else if (match == CATEGORIES) {
            builder.table(TestMeDatabase.Tables.CATEGORIES);
        } else if (match == TESTS) {
            builder.table(TestMeDatabase.Tables.TESTS);
        }
        return builder;
    }
}
