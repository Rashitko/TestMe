package com.pixel.android.testme.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class TestMeDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "lionexpo.db";
    private static final int DATABASE_VERSION = 1;

    public TestMeDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    interface Tables {
        String QUESTIONS = "questions";
        String CLASSES = "classes";
        String CATEGORIES = "categories";
        String TESTS = "tests";
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.CLASSES + " ('" +
                        BaseColumns._ID + "' INTEGER PRIMARY KEY AUTOINCREMENT,'" +
                        TestMeContract.ClassesColumns.CLASS_NAME + "' TEXT NOT NULL," +
                        "UNIQUE (" + TestMeContract.ClassesColumns.CLASS_NAME + ") ON CONFLICT IGNORE" +
                        ")"
        );
        db.execSQL("CREATE TABLE " + Tables.QUESTIONS + " ('" +
                        BaseColumns._ID + "' INTEGER PRIMARY KEY AUTOINCREMENT,'" +
                        TestMeContract.QuestionColumns.CLASS_ID + "' INTEGER NOT NULL,'" +
                        TestMeContract.QuestionColumns.ANSWERS + "' TEXT NOT NULL,'" +
                        TestMeContract.QuestionColumns.QUESTION + "' TEXT NOT NULL,'" +
                        TestMeContract.QuestionColumns.RIGHT_ANSWER + "' TEXT NOT NULL,'" +
                        TestMeContract.QuestionColumns.CATEGORY_ID + "' INTEGER NOT NULL," +
                        "UNIQUE (" + TestMeContract.QuestionColumns.QUESTION + ") ON CONFLICT IGNORE," +
                        "UNIQUE (" + TestMeContract.QuestionColumns.QUESTION + "," + TestMeContract.QuestionColumns.QUESTION + ") ON CONFLICT IGNORE" +
                        ")"
        );
        db.execSQL("CREATE TABLE " + Tables.CATEGORIES + " ('" +
                        BaseColumns._ID + "' INTEGER PRIMARY KEY AUTOINCREMENT,'" +
                        TestMeContract.CategoriesColumns.CLASS_ID + "' INTEGER NOT NULL,'" +
                        TestMeContract.CategoriesColumns.CATEGORY_NAME + "' TEXT NOT NULL," +
                        "UNIQUE (" + TestMeContract.CategoriesColumns.CATEGORY_NAME + ") ON CONFLICT IGNORE" +
                        ")"
        );
        db.execSQL("CREATE TABLE " + Tables.TESTS + " ('" +
                        BaseColumns._ID + "' INTEGER PRIMARY KEY AUTOINCREMENT,'" +
                        TestMeContract.TestColumns.CLASS_ID + "' INTEGER NOT NULL,'" +
                        TestMeContract.TestColumns.LIMITED + "' BOOLEAN NOT NULL,'" +
                        TestMeContract.TestColumns.TIME_LIMIT + "' INTEGER NOT NULL,'" +
                        TestMeContract.TestColumns.TIME_SPENT + "' INTEGER NOT NULL,'" +
                        TestMeContract.TestColumns.CURRENT_QUESTION + "' INTEGER NOT NULL,'" +
                        TestMeContract.TestColumns.SELECTED_QUESTIONS + "' INTEGER NOT NULL,'" +
                        TestMeContract.TestColumns.SELECTED_ANSWERS + "' STRING NOT NULL, '" +
                        TestMeContract.TestColumns.COMPLETED + "' BOOLEAN NOT NULL, '" +
                        TestMeContract.TestColumns.CREATED_AT + "' DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
