package com.example.smartpolchat;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.*;
import android.view.View;
import org.json.*;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView editTextInput;
    private Button buttonSend;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private final List<ChatMessage> chatList = new ArrayList<>();
    private final HashMap<String, RuleEntry> ruleMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        GPTService.init(this); // ✅ 이 줄을 여기 추가!

        editTextInput = findViewById(R.id.editTextInput);
        buttonSend = findViewById(R.id.buttonSend);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        loadRulesFromJson();

        // 🔥 자동완성 어댑터 세팅
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(ruleMap.keySet())
        );
        editTextInput.setAdapter(adapter);
        editTextInput.setThreshold(1);

        chatAdapter = new ChatAdapter(chatList, this, chatRecyclerView);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        buttonSend.setOnClickListener(v -> {
            String input = editTextInput.getText().toString().trim();
            if (input.isEmpty()) return;

            chatList.add(new ChatMessage(ChatMessage.TYPE_USER, input, getCurrentTime()));
            chatAdapter.notifyItemInserted(chatList.size() - 1);
            chatRecyclerView.smoothScrollToPosition(chatList.size() - 1);
            editTextInput.setText("");

            if (ruleMap.containsKey(input)) {
                RuleEntry entry = ruleMap.get(input);

                int startIndex = chatList.size(); // ✅ 추가 전 위치 기록

                if (entry.answer != null && entry.answer.text != null && !entry.answer.text.trim().isEmpty()) {
                    chatList.add(new ChatMessage(ChatMessage.TYPE_BOT, entry.answer.text, getCurrentTime(), entry.answer.buttons));
                }

                int addedCount = chatList.size() - startIndex; // ✅ 정확한 개수
                if (addedCount > 0) {
                    chatAdapter.notifyItemRangeInserted(startIndex, addedCount);
                }

                chatRecyclerView.smoothScrollToPosition(chatList.size() - 1);
                return;
            }

            chatList.add(new ChatMessage(ChatMessage.TYPE_BOT, "🤖 답변 준비 중...", getCurrentTime()));
            chatAdapter.notifyItemInserted(chatList.size() - 1);
            chatRecyclerView.smoothScrollToPosition(chatList.size() - 1);
        });
    }

    private void loadRulesFromJson() {
        try {
            InputStream is = getAssets().open("rules.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            JSONArray arr = new JSONArray(new String(buffer, "UTF-8"));

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                RuleEntry entry = new RuleEntry();

                String keywordField = obj.optString("keyword", "");
                if (!keywordField.isEmpty()) {
                    entry.keywords = keywordField.split("\\s*,\\s*");
                    for (String key : entry.keywords) {
                        ruleMap.put(key.trim(), entry);
                    }
                }

                JSONObject answerObj = obj.optJSONObject("answer");
                if (answerObj != null) {
                    RuleEntry.Answer answer = new RuleEntry.Answer();
                    answer.text = answerObj.optString("text", "");

                    JSONArray buttonArray = answerObj.optJSONArray("buttons");
                    if (buttonArray != null) {
                        List<RuleEntry.ButtonEntry> buttons = new ArrayList<>();
                        for (int j = 0; j < buttonArray.length(); j++) {
                            JSONObject btnObj = buttonArray.getJSONObject(j);
                            RuleEntry.ButtonEntry btn = new RuleEntry.ButtonEntry();
                            btn.label = btnObj.getString("label");
                            btn.image = btnObj.getString("image");
                            buttons.add(btn);
                        }
                        answer.buttons = buttons;
                    }
                    entry.answer = answer;
                }

                JSONArray slidesArray = obj.optJSONArray("slides");
                if (slidesArray != null) {
                    List<RuleEntry.SlideEntry> slides = new ArrayList<>();
                    for (int j = 0; j < slidesArray.length(); j++) {
                        JSONObject slideObj = slidesArray.getJSONObject(j);
                        RuleEntry.SlideEntry slide = new RuleEntry.SlideEntry();
                        slide.text = slideObj.getString("text");
                        slides.add(slide);
                    }
                    entry.slides = slides;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "⚠️ 규정 파일 로딩 오류 발생", Toast.LENGTH_LONG).show();
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
}
