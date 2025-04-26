package com.example.smartpolchat;

import java.util.List;

public class RuleEntry {
    public String[] keywords;
    public Answer answer;
    public List<SlideEntry> slides;

    public static class Answer {
        public String text;
        public List<ButtonEntry> buttons;
    }

    public static class ButtonEntry {
        public String label;
        public String image;
    }

    public static class SlideEntry {
        public String text;
    }
}