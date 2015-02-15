package com.pixel.android.testme.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pixel.android.testme.R;
import com.pixel.android.testme.db.TestMeContract;
import com.pixel.android.testme.utils.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class SavedTestsAdapter extends CursorAdapter {
    private final Context mContext;

    public SavedTestsAdapter(Context context) {
        super(context, null, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.list_item_saved_test, parent, false);
    }

    private String getTimeLimitText(Cursor cursor) {
        String result = mContext.getString(R.string.time_limit_row_text);
        result += " ";
        final int timeLimit = cursor.getInt(cursor.getColumnIndex(TestMeContract.Tests.getQualified(TestMeContract.Tests.TIME_LIMIT))) / 60;
        result += timeLimit;
        result += mContext.getString(R.string.minute);
        return result;
    }

    private String parseDateTime(String dateTime) {
        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = iso8601Format.parse(dateTime);
        } catch (ParseException e) {
            Logger.e("Parsing ISO8601 datetime failed", getClass());
        }
        if (date != null) {
            long when = date.getTime();
            int flags = 0;
            flags |= android.text.format.DateUtils.FORMAT_SHOW_TIME;
            flags |= android.text.format.DateUtils.FORMAT_SHOW_DATE;
            flags |= android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
            flags |= android.text.format.DateUtils.FORMAT_SHOW_YEAR;

            return android.text.format.DateUtils.formatDateTime(mContext,
                    when + TimeZone.getDefault().getOffset(when), flags);
        }
        return "";
    }

    private String getDetails(Cursor cursor) {
        String result = "";
        result += mContext.getString(R.string.questions_count);
        result += ": ";
        Gson gson = new Gson();
        final List<Integer> selectedQuestions =
                gson.fromJson(cursor.getString(cursor.getColumnIndex(
                        TestMeContract.Tests.getQualified(TestMeContract.Tests.SELECTED_QUESTIONS)
                )), new TypeToken<List<Integer>>() {
                }.getType());
        result += selectedQuestions.size();
        return result;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(cursor.getString(cursor.getColumnIndex(TestMeContract.Classes.getQualified(TestMeContract.Classes.CLASS_NAME))));
        TextView details = (TextView) view.findViewById(R.id.details);
        details.setText(getDetails(cursor));
        TextView createdAt = (TextView) view.findViewById(R.id.created_at);
        final String dateTime = cursor.getString(cursor.getColumnIndex(TestMeContract.Tests.getQualified(TestMeContract.Tests.CREATED_AT)));
        createdAt.setText(parseDateTime(dateTime));
        final boolean limited = cursor.getInt(cursor.getColumnIndex(TestMeContract.Tests.getQualified(TestMeContract.Tests.LIMITED))) == 1;
        TextView timeLimit = (TextView) view.findViewById(R.id.time_limit);
        if (limited) {
            timeLimit.setText(getTimeLimitText(cursor));
        } else {
            timeLimit.setVisibility(View.GONE);
        }
    }
}
