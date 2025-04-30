package com.example.smartpolchat;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private AutoCompleteTextView editTextInput;
    private Button buttonSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatList;
    private final HashMap<String, RuleEntry> ruleMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        editTextInput = findViewById(R.id.editTextInput);
        buttonSend = findViewById(R.id.buttonSend);

        chatList = new ArrayList<>();

        // ✅ 공지 추가
        ChatMessage notice = new ChatMessage();
        notice.setType(ChatMessage.TYPE_NOTICE);
        notice.setMessage("📢 SmartPolChat에 오신 걸 환영합니다!");
        notice.setTime("공지");

        chatList.add(0, notice);

        chatAdapter = new ChatAdapter(this, chatList, chatRecyclerView, imageName -> {
            ChatMessage imageMessage = new ChatMessage(ChatMessage.TYPE_IMAGE, null, getCurrentTime());
            imageMessage.setImageName(imageName);
            chatList.add(imageMessage);
            chatAdapter.notifyItemInserted(chatList.size() - 1);
            chatRecyclerView.scrollToPosition(chatList.size() - 1);
        });

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        loadRulesFromJson();
        setupAutoComplete();

        buttonSend.setOnClickListener(v -> {
            String userInput = editTextInput.getText().toString().trim();
            if (!userInput.isEmpty()) {
                addMessage(userInput, ChatMessage.TYPE_USER);
                respondToUser(userInput);
                editTextInput.setText("");
            }
        });

        editTextInput.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            addMessage(selected, ChatMessage.TYPE_USER);
            respondToUser(selected);
            editTextInput.setText("");
        });
    }

    private void loadRulesFromJson() {
        try {
            InputStream is = getAssets().open("rules.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                RuleEntry entry = new RuleEntry();
                entry.keyword = obj.getString("keyword").trim().toLowerCase();
                entry.answerText = obj.getString("answerText");

                // buttons 파싱
                JSONArray buttonArray = obj.optJSONArray("buttons");
                if (buttonArray != null) {
                    List<ButtonEntry> buttons = new ArrayList<>();
                    for (int j = 0; j < buttonArray.length(); j++) {
                        JSONObject btn = buttonArray.getJSONObject(j);
                        ButtonEntry b = new ButtonEntry();
                        b.label = btn.getString("label");
                        b.image = btn.getString("image");
                        buttons.add(b);
                    }
                    entry.buttons = buttons;
                }

                // slides 파싱
                JSONArray slideArray = obj.optJSONArray("slides");
                if (slideArray != null) {
                    List<SlideEntry> slides = new ArrayList<>();
                    for (int j = 0; j < slideArray.length(); j++) {
                        JSONObject sObj = slideArray.getJSONObject(j);
                        SlideEntry slide = new SlideEntry();
                        slide.text = sObj.getString("text");

                        JSONArray sButtons = sObj.optJSONArray("buttons");
                        if (sButtons != null) {
                            List<ButtonEntry> sBtnList = new ArrayList<>();
                            for (int k = 0; k < sButtons.length(); k++) {
                                JSONObject sb = sButtons.getJSONObject(k);
                                ButtonEntry b = new ButtonEntry();
                                b.label = sb.getString("label");
                                b.image = sb.getString("image");
                                sBtnList.add(b);
                            }
                            slide.buttons = sBtnList;
                        }

                        slides.add(slide);
                    }
                    entry.slides = slides;
                }

                ruleMap.put(entry.keyword, entry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupAutoComplete() {
        List<String> keywordList = new ArrayList<>(ruleMap.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                keywordList
        );
        editTextInput.setAdapter(adapter);
        editTextInput.setThreshold(1);
    }

    private void respondToUser(String userInput) {
        String normalizedInput = userInput.trim().toLowerCase();

        if (ruleMap.containsKey(normalizedInput)) {
            RuleEntry entry = ruleMap.get(normalizedInput);

            // 메인 텍스트 + 버튼 출력
            if (entry.answerText != null && !entry.answerText.trim().isEmpty()) {
                ChatMessage msg = new ChatMessage(
                        ChatMessage.TYPE_BOT,
                        entry.answerText,
                        getCurrentTime(),
                        entry.buttons
                );
                chatList.add(msg);
            }

            // 슬라이드가 있다면 별도 메시지 추가
            if (entry.slides != null && !entry.slides.isEmpty()) {
                ChatMessage slideMsg = new ChatMessage(
                        ChatMessage.TYPE_SLIDE,
                        getCurrentTime(),
                        entry.slides
                );
                chatList.add(slideMsg);
            }

            chatAdapter.notifyItemInserted(chatList.size() - 1);
            chatRecyclerView.scrollToPosition(chatList.size() - 1);

        } else {
            addMessage("해당 키워드에 대한 정보가 없습니다.", ChatMessage.TYPE_BOT);
        }
    }

    private void addMessage(String message, int type) {
        String time = getCurrentTime();
        ChatMessage chatMessage = new ChatMessage(type, message, time);
        chatList.add(chatMessage);
        chatAdapter.notifyItemInserted(chatList.size() - 1);
        chatRecyclerView.scrollToPosition(chatList.size() - 1);
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }
}