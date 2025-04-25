package com.example.smartpolchat;

import java.util.List;

public class RuleEntry {
    public String[] keywords;  // 쉼표로 나뉜 키워드 배열
    public String answerText;  // 응답 텍스트
    public List<ButtonEntry> buttons;  // 버튼 목록

    // 🔹 버튼 정의용 내부 클래스
    public static class ButtonEntry {
        public String label;  // 버튼에 표시할 텍스트
        public String image;  // 버튼 클릭 시 출력할 이미지 파일명 (drawable 이름)
    }
}