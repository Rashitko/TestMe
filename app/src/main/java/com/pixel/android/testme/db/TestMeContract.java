package com.pixel.android.testme.db;

import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;

public class TestMeContract {

    interface ClassesColumns {
        String CLASS_NAME = "class_name";
    }

    interface CategoriesColumns {
        String CATEGORY_NAME = "category_name";
        String CLASS_ID = "class_id";
    }

    interface QuestionColumns {
        String CLASS_ID = "class_id";
        String CATEGORY_ID = "category_id";
        String QUESTION = "question";
        String ANSWERS = "answers";
        String RIGHT_ANSWER = "right_answer";
    }

    interface TestColumns {
        String CLASS_ID = "class_id";
        String LIMITED = "limited";
        String TIME_LIMIT = "time_limit";
        String TIME_SPENT = "time_spent";
        String CURRENT_QUESTION = "current_question";
        String SELECTED_QUESTIONS = "selected_questions";
        String SELECTED_ANSWERS = "selected_answers";
        String COMPLETED = "completed";
        String CREATED_AT = "created_at";
    }

    public static final String CONTENT_AUTHORITY = "com.pixel.android.testme";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_QUESTIONS = "questions";
    private static final String PATH_CLASSES = "classes";
    private static final String PATH_CATEGORIES = "categories";
    private static final String PATH_TESTS = "tests";

    public static class Classes implements ClassesColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CLASSES).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.testme.class";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.testme.class";

        public static Uri buildUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static long getId(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        public static String findByName(String className) {
            return CLASS_NAME + " = '" + className + "' COLLATE NOCASE";
        }

        public static String getQualified(String name) {
            return TestMeDatabase.Tables.CLASSES + "." + name;
        }
    }

    public static class Questions implements QuestionColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_QUESTIONS).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.testme.question";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.testme.question";

        public static String findByClassId(long classId) {
            return CLASS_ID + " = " + classId;
        }

        public static String findByClassIdAndCategories(long classId, ArrayList<Long> selectedCategories) {
            StringBuilder builder = new StringBuilder();
            builder.append(CLASS_ID + " = ").append(classId);
            if (selectedCategories.size() == 0) {
                builder.append(" AND " + CATEGORY_ID + " IS NULL");
            } else {
                for (int i = 0; i < selectedCategories.size(); i++) {
                    if (i == 0) {
                        builder.append(" AND ");
                    } else {
                        builder.append(" OR ");
                    }
                    builder.append("(" + CATEGORY_ID + " = ").append(selectedCategories.get(i)).append(")");
                }
            }
            return builder.toString();
        }

        public static String findById(long id) {
            return BaseColumns._ID + " = " + id;
        }

        public static String findByIds(ArrayList<Long> selectedQuestions) {
            if (selectedQuestions.size() == 0) {
                return null;
            }
            String ids = selectedQuestions.toString();
            ids = ids.replaceAll("\\[", "(");
            ids = ids.replaceAll("\\]", ")");
            return _ID + " IN " + ids;
        }
    }

    public static class Categories implements CategoriesColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORIES).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.testme.category";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.testme.category";

        public static String findByClassId(long classId) {
            return CLASS_ID + " = " + classId;
        }

        public static String findByName(String className, long classId) {
            return CATEGORY_NAME + " = '" + className + "' COLLATE NOCASE AND " + CLASS_ID + " = " + classId;
        }

        public static Uri buildUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static long getId(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

    public static class Tests implements TestColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TESTS).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.testme.test";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.testme.test";

        public static Uri buildUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static String findById(int id) {
            return _ID + " = " + id;
        }

        public static String findByCompleted(boolean completed) {
            final int completedFlag = completed ? 1 : 0;
            return COMPLETED + " = " + completedFlag;
        }

        public static int getId(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }

        public static Uri findByCompletedUri(boolean completed) {
            String completedPath = completed ? "complete" : "incomplete";
            return CONTENT_URI.buildUpon().appendPath(completedPath).build();
        }

        public static String getQualified(String name) {
            return TestMeDatabase.Tables.TESTS + "." + name;
        }
    }

}
