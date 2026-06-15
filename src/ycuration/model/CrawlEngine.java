package ycuration.model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlEngine {

    public CrawlEngine() {
    }

    public java.util.List<Notice> scrapeNoticeList(String baseDeptUrl, int page) {
        java.util.List<Notice> list = new java.util.ArrayList<>();

        // 💡 [버그 격추 1: 주소 정밀 조립]
        // NoticeDashboard가 이미 페이지에 맞게 article.offset을 조립해서 주소를 보내주므로
        // 매개변수 baseDeptUrl을 다이렉트로 targetUrl에 바인딩하여 주소가 중복 가공되어 깨지는 현상을 원천 차단함
        String targetUrl = baseDeptUrl;

        if (baseDeptUrl == null || baseDeptUrl.isEmpty()) {
            targetUrl = "https://www.yu.ac.kr/main/intro/yu-news.do?mode=list";
        }

        System.out.println("[CrawlEngine] 실제 서버 타격 URL: " + targetUrl);

        try {
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(targetUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(8000)
                    .get();

            org.jsoup.select.Elements rows = doc.select("table.board_list tbody tr, table.table tbody tr, table tbody tr, table.board-table tbody tr");

            int virtualId = 1;

            for (org.jsoup.nodes.Element row : rows) {
                if (row.select("td").isEmpty()) continue;
                if (!row.select("td.b_file, td.file, td.download").isEmpty()) continue;

                // 💡 [버그 격추 2: 타 페이지 중요 고정 공지 도배 차단 필터]
                // html 태그에 고정 공지 클래스가 박혀있거나 번호 칸에 '공지' 혹은 별(★)이 박혀있다면 고정 공지임
                boolean isNoticePin = row.hasClass("b-notice") || row.hasClass("notice") || row.hasClass("is-notice");
                org.jsoup.nodes.Element numElement = row.select("td.b_num, td.num, td.b-td-num, td:first-child").first();
                if (numElement != null) {
                    String numText = numElement.text().trim();
                    if (numText.contains("공지") || numText.isEmpty() || numText.contains("★")) {
                        isNoticePin = true;
                    }
                }

                // 1페이지(offset=0 혹은 offset이 안 보일 때)가 아닌 2, 3, 4, 5페이지를 순회하는 중인데
                // 고정 공지 행을 만나면 리스트에 넣지 않고 스킵하여 중복 도배 현상을 완전히 박멸
                if (isNoticePin && (targetUrl.contains("offset=") && !targetUrl.contains("offset=0"))) {
                    continue;
                }

                // --- 원본 파싱 및 필터링 로직 100% 보존 구역 ---
                String title = row.select("td.b_tit, td.subject, td.title, a").first() != null ?
                        row.select("td.b_tit, td.subject, td.title, a").first().text().trim() : "";

                String date = row.select("td.b_date, td.date, td.b-td-date").first() != null ?
                        row.select("td.b_date, td.date, td.b-td-date").first().text().trim() : "";

                if (title.isEmpty() && row.select("td").size() > 2) {
                    title = row.select("td").get(1).text().trim();
                }
                if (date.isEmpty() && row.select("td").size() > 4) {
                    date = row.select("td").get(3).text().trim();
                }

                if (title.isEmpty() || title.equals("다운로드") || title.contains("첨부파일")) continue;

                if (title.matches("\\d+")) {
                    System.out.println("[CrawlEngine] 순수 숫자 쓰레기 행 발견 및 폐기 조치: " + title);
                    continue;
                }

                if (title.length() < 5) {
                    System.out.println("[CrawlEngine] 비정상 단축 유령 행 발견 및 폐기 조치: " + title);
                    continue;
                }
                // --- 원본 파싱 및 필터링 로직 보존 끝 ---

                org.jsoup.nodes.Element linkElement = row.select("a").first();
                String originalUrl = "";
                if (linkElement != null) {
                    String href = linkElement.attr("href").trim();
                    if (href.startsWith("http")) {
                        originalUrl = href;
                    } else {
                        String pureBaseUrl = baseDeptUrl;
                        if (baseDeptUrl.contains("?")) {
                            pureBaseUrl = baseDeptUrl.split("\\?")[0];
                        }
                        if (href.startsWith("/")) {
                            originalUrl = "https://www.yu.ac.kr" + href;
                        } else if (href.startsWith("?")) {
                            originalUrl = pureBaseUrl + href;
                        } else {
                            originalUrl = pureBaseUrl + (pureBaseUrl.contains(".do") ? "" : "/") + href;
                        }
                    }
                }

                // 💡 [인수 매칭 수술] 너의 Notice 생성자 포맷(int, String, String, String, String) 순서 일치화
                Notice notice = new Notice(virtualId++, title, title, date, originalUrl);
                list.add(notice);
            }


        } catch (Exception e) {
            System.err.println("[CrawlEngine 예외] " + e.getMessage());
        }

        return list;
    }
}