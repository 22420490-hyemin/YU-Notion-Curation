package ycuration.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TelegramNotifier {

    public TelegramNotifier() {
    }

    public boolean sendMessage(Object config, String message) {
        System.out.println("[Telegram API] 스마트폰 텔레그램 푸시 전송 시도 중...");

        String botToken = "";
        String chatId = "";

        // 💡 [수술 지점] 고정되어 있던 네 개인 정보를 지우고, UI 설정창에서 교수님이 저장한 파일을 읽어옴
        File configFile = new File("telegram_config.txt");
        if (configFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
                String line = br.readLine();
                if (line != null && line.contains(",")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        botToken = parts[0].trim();
                        chatId = parts[1].trim();
                    }
                }
            } catch (Exception e) {
                System.err.println("[Telegram API] 설정 파일 읽기 실패: " + e.getMessage());
            }
        }

        // 비전공자 맞춤 설명: 파일이 없거나 정보를 입력 안 했으면 전송을 중단하여 에러를 막음
        if (botToken == null || chatId == null || botToken.isEmpty() || chatId.isEmpty()) {
            System.err.println("[Telegram API] 토큰 또는 채팅 ID가 설정되지 않았습니다. UI 설정창을 확인하세요.");
            return false;
        }

        try {
            String encodedMessage = URLEncoder.encode(message, "UTF-8");

            String urlString = "https://api.telegram.org/bot" + botToken
                    + "/sendMessage?chat_id=" + chatId
                    + "&text=" + encodedMessage;

            URL urlObj = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    System.out.println("[Telegram API] 푸시 전송 성공! 스마트폰 상단바를 확인하세요.");
                    return true;
                }
            } else {
                System.err.println("[Telegram API] 전송 실패. 응답 코드: " + responseCode);
                return false;
            }
        } catch (Exception e) {
            System.err.println("[Telegram API Error] 통신 예외 발생: " + e.getMessage());
            return false;
        }
    }
}