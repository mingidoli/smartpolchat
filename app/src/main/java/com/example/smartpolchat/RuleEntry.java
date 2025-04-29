package com.example.smartpolchat;

import java.util.List;


public class RuleEntry {
    public String keyword;
    public String answerText;
    public List<ButtonEntry> buttons;
    public List<SlideEntry> slides;    // ✅ 슬라이드 추가


    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }


}