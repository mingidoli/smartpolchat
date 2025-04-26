package com.example.smartpolchat;

import java.util.List;

public class ChatMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_BUTTON = 3;
    public static final int TYPE_SLIDE = 4; // 추가됨


    public int type;
    public String message;
    public String time;
    public int imageResId;  // 이미지용
    public List<RuleEntry.ButtonEntry> buttons;  // 버튼 목록
    public List<RuleEntry.SlideEntry> slides; // 추가됨


    // 기본 텍스트 메시지 (사용자, GPT)
    public ChatMessage(int type, String message, String time) {
        this.type = type;
        this.message = message;
        this.time = time;
    }

    // 이미지 메시지
    public ChatMessage(int type, String message, String time, int imageResId) {
        this.type = type;
        this.message = message;
        this.time = time;
        this.imageResId = imageResId;
    }

    // 버튼 목록 메시지 (여러 버튼)
    public ChatMessage(int type, String message, String time, List<RuleEntry.ButtonEntry> buttons) {
        this.type = type;
        this.message = message;
        this.time = time;
        this.buttons = buttons;
    }
    public ChatMessage(int type, String message, String time, List<RuleEntry.SlideEntry> slides, boolean isSlide) {
        this.type = type;
        this.message = message;
        this.time = time;
        this.slides = slides;
    }
}
