package com.pixel.android.testme.interfaces;

import java.util.HashSet;

public interface AnswersAdapterCallbacks {

    public void onAnswerSelected(HashSet<Integer> selected);

    HashSet<Integer> getRightAnswers();
}
