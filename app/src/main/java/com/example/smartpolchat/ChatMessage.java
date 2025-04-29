package com.example.smartpolchat;

import java.util.List;

public class ChatMessage {

    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_SLIDE = 3; // 🔹 슬라이드 전용 타입 추가

    private int type;
    private String message;
    private String time;
    private List<ButtonEntry> buttons;
    private String imageName;
    private List<SlideEntry> slides;

    // 기본 텍스트 메시지
    public ChatMessage(int type, String message, String time) {
        this.type = type;
        this.message = message;
        this.time = time;
    }

    // 텍스트 + 버튼
    public ChatMessage(int type, String message, String time, List<ButtonEntry> buttons) {
        this.type = type;
        this.message = message;
        this.time = time;
        this.buttons = buttons;
    }

    // 텍스트 + 버튼 + 슬라이드 (기본 구조)
    public ChatMessage(int type, String message, String time, List<ButtonEntry> buttons, List<SlideEntry> slides) {
        this.type = type;
        this.message = message;
        this.time = time;
        this.buttons = buttons;
        this.slides = slides;
    }

    // 슬라이드 전용 메시지
    public ChatMessage(int type, String time, List<SlideEntry> slides) {
        this.type = type;
        this.time = time;
        this.slides = slides;
    }

    // getter/setter
    public int getType() { return type; }
    public String getMessage() { return message; }
    public String getTime() { return time; }

    public List<ButtonEntry> getButtons() { return buttons; }

    public List<SlideEntry> getSlides() { return slides; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }
}