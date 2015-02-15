package com.pixel.android.testme.io.models;

import java.util.List;

@SuppressWarnings("unused")
public class Question {

    private String question;
    private String category;
    private List<String> answers;
    private List<Integer> rightAnswers;

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public List<Integer> getRightAnswer() {
        return rightAnswers;
    }

    public String getCategory() {
        return category;
    }
}
