package ycuration.model;

import ycuration.controller.NoticeDashboard;
import ycuration.model.Notice;
import ycuration.model.CrawlEngine;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.File;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AlertMonitor implements Runnable {
    private boolean running = true;
    private final List<String> targetUrls = new ArrayList<>();

    private String openAiApiKey = "";

    public AlertMonitor() {
        targetUrls.add("https://www.yu.ac.kr/main/intro/yu-news.do?mode=list");
        targetUrls.add("https://www.yu.ac.kr/computer/notice/notice.do?mode=list&boardConfigId=14654");
        targetUrls.add("https://www.yu.ac.kr/scholar/notice/notice.do?mode=list&boardConfigId=14850");

        // 💡 생성자 시점에 외부 파일로부터 API Key를 동적 로드
        loadApiKeyFromFile();
    }

    private void loadApiKeyFromFile() {
        File configFile = new File("config.txt");
        if (configFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
                String line = br.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    this.openAiApiKey = line.trim();
                    System.out.println("[AlertMonitor] 외부 config.txt 파일로부터 OpenAI API Key 로드 완료.");
                }
            } catch (Exception e) {
                System.err.println("[AlertMonitor] config.txt 파싱 오류: " + e.getMessage());
            }
        } else {
            System.err.println("[AlertMonitor 경고] 프로젝트 루트에 config.txt 파일이 없습니다.");
        }
    }

    @Override
    public void run() {
        System.out.println("[AlertMonitor] 1~5페이지 전수 조사형 고정밀 알림 엔진 감시 시작.");
        CrawlEngine crawler = new CrawlEngine();
        TelegramNotifier bot = new TelegramNotifier();
        SentNoticeStore store = new SentNoticeStore();

        while (running) {
            try {
                List<String> keywords = NoticeDashboard.getInstance().getUserKeywords();

                if (keywords != null && !keywords.isEmpty()) {
                    for (String baseUrl : targetUrls) {

                        for (int page = 1; page <= 5; page++) {
                            int offset = (page - 1) * 10;
                            String pageTargetUrl = baseUrl;
                            if (pageTargetUrl.contains("?")) {
                                pageTargetUrl += "&article.offset=" + offset + "&articleLimit=10";
                            } else {
                                pageTargetUrl += "?article.offset=" + offset + "&articleLimit=10";
                            }

                            List<Notice> notices = crawler.scrapeNoticeList(pageTargetUrl, 1);

                            if (notices != null) {
                                for (Notice notice : notices) {
                                    if (store.hasSent(notice)) continue;

                                    for (String keyword : keywords) {
                                        if (notice.getTitle().contains(keyword)) {
                                            System.out.println("[AlertMonitor] " + page + "페이지에서 키워드 검출 성공: " + notice.getTitle());

                                            String realContent = fetchDetailContent(notice.getOriginalUrl(), notice.getTitle());
                                            String aiSummary = requestGptSummary(realContent);

                                            String alertMessage = "[Y-Curation 키워드 알림]\n"
                                                    + "검출 키워드: " + keyword + "\n"
                                                    + "발견 위치: " + page + "페이지\n\n"
                                                    + "공지 제목: " + notice.getTitle() + "\n"
                                                    + "작성일자: " + notice.getDate() + "\n\n"
                                                    + "AI 핵심 3줄 요약:\n" + aiSummary + "\n\n"
                                                    + "원문 보러가기:\n" + notice.getOriginalUrl();

                                            boolean isSent = bot.sendMessage(null, alertMessage);
                                            if (isSent) {
                                                store.markSent(notice);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                            Thread.sleep(300);
                        }
                    }
                }
                Thread.sleep(15000);

            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                System.err.println("[AlertMonitor 루프 예외] " + e.getMessage());
            }
        }
    }

    private String fetchDetailContent(String originalUrl, String defaultTitle) {
        if (originalUrl == null || originalUrl.isEmpty()) return defaultTitle;
        try {
            org.jsoup.nodes.Document detailDoc = org.jsoup.Jsoup.connect(originalUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(6000)
                    .get();

            org.jsoup.nodes.Element contentBox = detailDoc.select("div.view_con, div.v_area, div.b_content, div.board_detail, td.content, .board_view_area").first();

            if (contentBox != null) {
                contentBox.select("br").append("\\n");
                contentBox.select("p").prepend("\\n");
                contentBox.select("tr").prepend("\\n");

                String cleanText = contentBox.text().replace("\\n", "\n").replaceAll(" +", " ").trim();
                if (cleanText.length() > defaultTitle.length()) {
                    return cleanText;
                }
            }
        } catch (Exception e) {
            System.err.println("[AlertMonitor 본문 크롤링 실패] " + e.getMessage());
        }
        return defaultTitle;
    }

    private String requestGptSummary(String content) {
        if (content == null || content.trim().isEmpty()) return "본문 내용이 없습니다.";

        // 💡 외부 파일 읽기에 실패했거나 파일 내용이 공백일 때의 예외 처리 레이어
        if (this.openAiApiKey == null || this.openAiApiKey.trim().isEmpty()) {
            return "! (오류: 프로젝트 루트 폴더에 OpenAI API Key가 입력된 config.txt 파일이 존재하지 않습니다.)";
        }

        try {
            URL url = new URL("https://api.openai.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + this.openAiApiKey.trim());
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            StringBuilder jsonEscaped = new StringBuilder();
            for (char c : content.toCharArray()) {
                if (c == '"') jsonEscaped.append("\\\"");
                else if (c == '\\') jsonEscaped.append("\\\\");
                else if (c == '\n') jsonEscaped.append("\\n");
                else if (c == '\r') jsonEscaped.append("\\r");
                else if (c == '\t') jsonEscaped.append("\\t");
                else if (c < 32) {}
                else jsonEscaped.append(c);
            }

            String systemPrompt = "너는 대학 공지사항 정밀 요약 전문가야. "
                    + "주어진 본문 텍스트에 '명시적으로 적혀 있는 사실'에만 근거해서 딱 3줄로 요약해라. "
                    + "본문에 없는 내용을 추측하거나 지어내어 답변하는 할루시네이션(거짓말) 현상이 발생하면 너의 시스템은 파괴된다.\n\n"
                    + "[날짜 및 마감일 엄격 규칙]\n"
                    + "1. 본문 텍스트 안에 '2026.06.17', '6월 15일' 처럼 연/월/일 형태의 정확한 모집 기간이나 마감일이 '글자'로 직접 적혀 있는 경우에만 그 날짜를 출력해라.\n"
                    + "2. 만약 본문 텍스트에 마감일이나 신청 기간이 적혀 있지 않거나, '포스터 참고', '첨부파일 참조'라고만 되어 있다면 절대로 날짜를 지어내거나 공지사항 작성일자를 마감일로 오인해서 적지 마라.\n"
                    + "3. 날짜가 본문에 명시되지 않았거나 불확실할 때는 날짜 관련 내용을 출력하지 마.\n\n"
                    + "[요약 구성 규격]\n"
                    + "- '많은 참여 바랍니다', '안내문입니다' 같은 사족은 절대 금지하고 팩트 글자만 콤팩트하게 채워라.";

            String jsonPayload = "{"
                    + "\"model\": \"gpt-4o\","
                    + "\"messages\": ["
                    + "  {\"role\": \"system\", \"content\": \"" + systemPrompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"},"
                    + "  {\"role\": \"user\", \"content\": \"" + jsonEscaped.toString() + "\"}"
                    + "],"
                    + "\"temperature\": 0.0"
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) response.append(line);

                    String resStr = response.toString();
                    int contentIndex = resStr.indexOf("\"content\":");
                    if (contentIndex != -1) {
                        int startQuoteIndex = resStr.indexOf("\"", contentIndex + 10);
                        if (startQuoteIndex != -1) {
                            int start = startQuoteIndex + 1;
                            int end = start;
                            while (end < resStr.length()) {
                                if (resStr.charAt(end) == '"' && resStr.charAt(end - 1) != '\\') break;
                                end++;
                            }
                            return resStr.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
                        }
                    }
                }
            }
        } catch (Exception e) {
            return "AI 통신 예외 발생: " + e.getMessage();
        }
        return "요약본 가공 실패.";
    }

    public void stop() {
        this.running = false;
    }
}