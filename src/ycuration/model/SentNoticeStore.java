package ycuration.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SentNoticeStore {
    private static final String SENT_FILE = "sent_notices.txt";
    private final Set<String> sentNoticeKeys = new HashSet<>();

    public SentNoticeStore() {
        load();
    }

    public boolean hasSent(Notice notice) {
        return sentNoticeKeys.contains(createKey(notice));
    }

    public void markSent(Notice notice) {
        String key = createKey(notice);
        if (key.isEmpty() || sentNoticeKeys.contains(key)) return;

        sentNoticeKeys.add(key);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SENT_FILE, true))) {
            bw.write(key);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("[SentNoticeStore Error] 발송 기록 저장 실패: " + e.getMessage());
        }
    }

    private void load() {
        File file = new File(SENT_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    sentNoticeKeys.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("[SentNoticeStore Error] 발송 기록 로드 실패: " + e.getMessage());
        }
    }

    private String createKey(Notice notice) {
        if (notice == null) return "";
        if (notice.getOriginalUrl() != null && !notice.getOriginalUrl().trim().isEmpty()) {
            return notice.getOriginalUrl().trim();
        }
        return nullToEmpty(notice.getDate()) + "|" + nullToEmpty(notice.getTitle());
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
