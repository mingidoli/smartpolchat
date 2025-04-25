package com.example.smartpolchat;

import android.content.Context;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class GPTService {

    private static String API_KEY = null;
    private static final String ASSISTANT_ID = "asst_LCaqNb5WHacrfzIr822Sspx1";
    private static final String BASE_URL = "https://api.openai.com/v1";

    // 🔑 키를 외부 파일에서 불러오는 메서드 (앱 시작 시 1회 호출)
    public static void init(Context context) {
        try {
            Properties props = new Properties();
            InputStream inputStream = context.getAssets().open("apikey.properties");
            props.load(inputStream);
            API_KEY = props.getProperty("OPENAI_API_KEY");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String askGPT(String userInput) {
        if (API_KEY == null) return "❌ API Key가 로딩되지 않았습니다.";
        try {
            // 1. 새 Thread 생성
            JSONObject threadRes = post(BASE_URL + "/threads", new JSONObject());
            String threadId = threadRes.getString("id");

            // 2. 사용자 메시지 추가
            JSONObject messageData = new JSONObject();
            messageData.put("role", "user");
            messageData.put("content", userInput);
            post(BASE_URL + "/threads/" + threadId + "/messages", messageData);

            // 3. Run 실행
            JSONObject runData = new JSONObject();
            runData.put("assistant_id", ASSISTANT_ID);
            JSONObject runRes = post(BASE_URL + "/threads/" + threadId + "/runs", runData);
            String runId = runRes.getString("id");

            // 4. 응답 상태 대기
            String status;
            do {
                Thread.sleep(1000);
                JSONObject runCheck = get(BASE_URL + "/threads/" + threadId + "/runs/" + runId);
                status = runCheck.getString("status");
            } while (!status.equals("completed"));

            // 5. 메시지 가져오기
            JSONObject messageRes = get(BASE_URL + "/threads/" + threadId + "/messages");
            JSONArray data = messageRes.getJSONArray("data");

            if (data.length() > 0) {
                JSONObject latest = data.getJSONObject(0);
                JSONArray parts = latest.getJSONObject("content").getJSONArray("parts");
                return parts.getString(0);
            }

            return "❌ 응답 없음. 데이터를 가져올 수 없습니다.";

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Assistant 응답 실패: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private static JSONObject post(String url, JSONObject body) throws IOException, JSONException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("OpenAI-Beta", "assistants=v1");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        if (body != null) {
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes());
                os.flush();
            }
        }

        try (InputStream is = conn.getInputStream()) {
            String result = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
            return new JSONObject(result);
        }
    }

    private static JSONObject get(String url) throws IOException, JSONException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("OpenAI-Beta", "assistants=v1");

        try (InputStream is = conn.getInputStream()) {
            String result = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
            return new JSONObject(result);
        }
    }
}