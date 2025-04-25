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
                chatList.add(new ChatMessage(ChatMessage.TYPE_BOT, entry.answerText, getCurrentTime()));
                if (entry.buttons != null && !entry.buttons.isEmpty()) {
                    chatList.add(new ChatMessage(ChatMessage.TYPE_BUTTON, null, getCurrentTime(), entry.buttons));
                }
                chatAdapter.notifyItemRangeInserted(chatList.size() - 2, 2);
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
                entry.answerText = obj.getString("answerText");

                String keywordField = obj.optString("keyword", "");
                if (!keywordField.isEmpty()) {
                    entry.keywords = keywordField.split("\\s*,\\s*");
                    for (String key : entry.keywords) {
                        ruleMap.put(key.trim(), entry);
                    }
                }

                JSONArray buttonsArray = obj.optJSONArray("buttons");
                if (buttonsArray != null) {
                    List<RuleEntry.ButtonEntry> buttonList = new ArrayList<>();
                    for (int j = 0; j < buttonsArray.length(); j++) {
                        JSONObject btnObj = buttonsArray.getJSONObject(j);
                        RuleEntry.ButtonEntry btn = new RuleEntry.ButtonEntry();
                        btn.label = btnObj.getString("label");
                        btn.image = btnObj.getString("image");
                        buttonList.add(btn);
                    }
                    entry.buttons = buttonList;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "⚠️ 규정 파일 로딩 중 오류 발생", Toast.LENGTH_LONG).show();
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
}
