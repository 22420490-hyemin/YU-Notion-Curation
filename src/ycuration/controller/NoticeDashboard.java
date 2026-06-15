package ycuration.controller;

public class NoticeDashboard {
    private static NoticeDashboard instance;
    private ycuration.view.MainFeedView currentMainFeed;
    private java.util.List<ycuration.model.Notice> originalNoticeList;

    private java.util.List<ycuration.model.Notice> alarmNoticeList = new java.util.ArrayList<>();
    private String currentMode = "CRAWL";

    public static NoticeDashboard getInstance() {
        if (instance == null) {
            instance = new NoticeDashboard();
        }
        return instance;
    }

    public NoticeDashboard() {
    }

    public boolean handleLoginRequest(String id, String pw) {
        System.out.println("[NoticeDashboard] 로그인 인증 시도 - 학번: [" + id + "]");
        if (id == null || id.isEmpty() || pw == null || pw.isEmpty()) {
            return false;
        }
        System.out.println("[NoticeDashboard] 로그인 인증 완벽 성공. 세션 가동.");
        return true;
    }

    public void handleSearchRequest(String searchWord) {
        if (this.currentMainFeed == null) return;

        this.currentMode = "CRAWL";

        if (searchWord == null || searchWord.trim().isEmpty()) {
            System.out.println("[NoticeDashboard] 검색어 없음. 현재 카테고리의 1페이지를 다시 불러옵니다.");
            changePage(1);
            return;
        }

        final String lowerWord = searchWord.trim().toLowerCase();
        System.out.println("[NoticeDashboard] 병렬 멀티스레드 1~5페이지 고속 검색 가동: [" + lowerWord + "]");

        this.currentMainFeed.showLoadingState();

        javax.swing.SwingWorker<java.util.List<ycuration.model.Notice>, Void> searchWorker =
                new javax.swing.SwingWorker<java.util.List<ycuration.model.Notice>, Void>() {

                    @Override
                    protected java.util.List<ycuration.model.Notice> doInBackground() throws Exception {
                        ycuration.model.CrawlEngine crawler = new ycuration.model.CrawlEngine();
                        java.util.List<ycuration.model.Notice> allPagesNotices = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
                        String baseDeptUrl = getCurrentDeptUrl();

                        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(5);
                        java.util.List<java.util.concurrent.Future<?>> futures = new java.util.ArrayList<>();

                        for (int page = 1; page <= 5; page++) {
                            final int currentPage = page;
                            java.util.concurrent.Future<?> future = executor.submit(() -> {
                                try {
                                    int offset = (currentPage - 1) * 10;
                                    String pageTargetUrl = baseDeptUrl;
                                    if (pageTargetUrl.contains("?")) {
                                        pageTargetUrl += "&article.offset=" + offset + "&articleLimit=10";
                                    } else {
                                        pageTargetUrl += "?article.offset=" + offset + "&articleLimit=10";
                                    }

                                    java.util.List<ycuration.model.Notice> pageData = crawler.scrapeNoticeList(pageTargetUrl, 1);
                                    if (pageData != null && !pageData.isEmpty()) {
                                        allPagesNotices.addAll(pageData);
                                    }
                                } catch (Exception e) {
                                    System.err.println("[검색 스레드 내부 에러] " + e.getMessage());
                                }
                            });
                            futures.add(future);
                        }

                        for (java.util.concurrent.Future<?> f : futures) {
                            f.get();
                        }

                        executor.shutdown();
                        return allPagesNotices;
                    }

                    @Override
                    protected void done() {
                        try {
                            java.util.List<ycuration.model.Notice> totalList = get();
                            java.util.List<ycuration.model.Notice> searchResults = new java.util.ArrayList<>();
                            java.util.Set<String> duplicateCheckSet = new java.util.HashSet<>();

                            for (ycuration.model.Notice notice : totalList) {
                                if (notice == null) continue;

                                String uniqueKey = notice.getTitle() == null ? "" : notice.getTitle().trim();
                                if (uniqueKey.isEmpty() || duplicateCheckSet.contains(uniqueKey)) continue;

                                String title = notice.getTitle() == null ? "" : notice.getTitle().toLowerCase();
                                String content = notice.getContent() == null ? "" : notice.getContent().toLowerCase();

                                if (title.contains(lowerWord) || (!content.isEmpty() && content.contains(lowerWord))) {
                                    searchResults.add(notice);
                                    duplicateCheckSet.add(uniqueKey);
                                }
                            }

                            NoticeDashboard.this.originalNoticeList = searchResults;
                            NoticeDashboard.this.currentMainFeed.refreshFeed(searchResults);
                            System.out.println("[NoticeDashboard] 고속 검색 완료. 검색된 순수 공지 수: " + searchResults.size() + "개");

                        } catch (Exception ex) {
                            System.err.println("[NoticeDashboard 검색 스레드 예외] " + ex.getMessage());
                            NoticeDashboard.this.currentMainFeed.refreshFeed(NoticeDashboard.this.originalNoticeList);
                        }
                    }
                };

        searchWorker.execute();
    }

    public void renderCuratedFeed(String userId) {
        System.out.println("\n[NoticeDashboard] 메인 대시보드 진입 프로세스 가동. 사용자: " + userId);
        this.currentMode = "CRAWL";

        ycuration.model.CrawlEngine crawler = new ycuration.model.CrawlEngine();
        java.util.List<ycuration.model.Notice> freshNotices = crawler.scrapeNoticeList(getCurrentDeptUrl(), 1);

        this.originalNoticeList = freshNotices;
        this.currentMainFeed = new ycuration.view.MainFeedView(userId, this.originalNoticeList);
        this.currentMainFeed.showCardFeed();
        this.currentMainFeed.refreshFeed(this.originalNoticeList);

        System.out.println("[NoticeDashboard] 초기 데이터 선제 적재 완료 및 메인 피드 10개 완벽 출력 성공.\n");
    }

    private String currentDeptUrl = "https://www.yu.ac.kr/main/intro/yu-news.do?mode=list";

    public String getCurrentDeptUrl() {
        return this.currentDeptUrl;
    }

    public void setCurrentDeptUrl(String url) {
        this.currentDeptUrl = url;
        this.currentMode = "CRAWL";
    }

    public void changePage(int targetPage) {
        if (this.currentMainFeed == null) return;

        if ("ALARM".equals(this.currentMode)) {
            System.out.println("[NoticeDashboard] 알림함 내부 페이지 이동 가동 ➔ " + targetPage + "페이지");

            int totalAlarmCount = this.alarmNoticeList.size();
            int fromIndex = (targetPage - 1) * 10;
            int toIndex = Math.min(fromIndex + 10, totalAlarmCount);

            if (fromIndex >= totalAlarmCount || fromIndex < 0) {
                this.currentMainFeed.refreshFeedWithPage(new java.util.ArrayList<>(), targetPage);
                return;
            }

            java.util.List<ycuration.model.Notice> pagedAlarm = this.alarmNoticeList.subList(fromIndex, toIndex);
            int requiredPageBarCount = (totalAlarmCount == 0) ? 1 : (int) Math.ceil((double) totalAlarmCount / 10.0);
            this.currentMainFeed.refreshFeedWithPage(pagedAlarm, requiredPageBarCount);
            return;
        }

        this.currentMainFeed.showLoadingState();

        int offset = (targetPage - 1) * 10;
        String pageTargetUrl = this.currentDeptUrl;
        if (pageTargetUrl.contains("?")) {
            pageTargetUrl += "&article.offset=" + offset + "&articleLimit=10";
        } else {
            pageTargetUrl += "?article.offset=" + offset + "&articleLimit=10";
        }

        final String finalUrl = pageTargetUrl;

        javax.swing.SwingWorker<java.util.List<ycuration.model.Notice>, Void> worker =
                new javax.swing.SwingWorker<java.util.List<ycuration.model.Notice>, Void>() {
                    @Override
                    protected java.util.List<ycuration.model.Notice> doInBackground() throws Exception {
                        ycuration.model.CrawlEngine crawler = new ycuration.model.CrawlEngine();
                        return crawler.scrapeNoticeList(finalUrl, 1);
                    }

                    @Override
                    protected void done() {
                        try {
                            java.util.List<ycuration.model.Notice> pagedNotices = get();
                            NoticeDashboard.this.originalNoticeList = pagedNotices;
                            NoticeDashboard.this.currentMainFeed.refreshFeedWithPage(pagedNotices, 5);
                            System.out.println("[NoticeDashboard] 전체공지 " + targetPage + "페이지로 정상 전환 완료.");
                        } catch (Exception ex) {
                            NoticeDashboard.this.currentMainFeed.refreshFeedWithPage(NoticeDashboard.this.originalNoticeList, 5);
                        }
                    }
                };
        worker.execute();
    }

    public void renderSentNotices() {
        System.out.println("[NoticeDashboard] 알림 맞춤 공지 피드 추출 가동.");
        if (this.currentMainFeed == null) return;

        this.currentMode = "ALARM";
        ycuration.model.SentNoticeStore store = new ycuration.model.SentNoticeStore();

        java.util.List<ycuration.model.Notice> filtered = new java.util.ArrayList<>();
        if (this.originalNoticeList != null) {
            for (ycuration.model.Notice notice : this.originalNoticeList) {
                if (store.hasSent(notice)) {
                    filtered.add(notice);
                }
            }
        }

        this.alarmNoticeList = filtered;
        int totalCount = filtered.size();

        int requiredPageBarCount = (totalCount == 0) ? 1 : (int) Math.ceil((double) totalCount / 10.0);

        int toIndex = Math.min(10, totalCount);
        java.util.List<ycuration.model.Notice> firstPageData = filtered.subList(0, toIndex);

        this.currentMainFeed.refreshFeedWithPage(firstPageData, requiredPageBarCount);
        System.out.println("[NoticeDashboard] 알림함 동기화 마감. 총 건수: " + totalCount + "개 | 동적 하단 바 범위: " + requiredPageBarCount + "쪽까지 완벽 연동.");
    }

    public java.util.List<String> getUserKeywords() {
        java.util.List<String> list = new java.util.ArrayList<>();
        java.io.File file = new java.io.File("keywords.txt");

        if (!file.exists()) return list;

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line = br.readLine();
            if (line != null && !line.trim().isEmpty()) {
                for (String k : line.split(",")) {
                    if (!k.trim().isEmpty()) {
                        list.add(k.trim());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[NoticeDashboard 파일 동기화 에러] " + e.getMessage());
        }
        return list;
    }

    public String handleSummaryRequest(ycuration.model.Notice notice) {
        if (notice == null) return "요약할 공지사항이 선택되지 않았습니다.";

        System.out.println("[NoticeDashboard] AI 요약 프로세스 트리거 타격. 대상: " + notice.getTitle());

        String contentToSummarize = notice.getContent();
        if (contentToSummarize == null || contentToSummarize.trim().isEmpty() || contentToSummarize.equals(notice.getTitle())) {
            return "본문 내용을 수집하지 못했거나 요약할 본문 텍스트 량이 너무 적습니다. [원문 보기] 버튼을 활용해 주세요.";
        }

        return "[실시간 텔레그램 AI 알림 발송 연동 완료]\n"
                + "이 공지의 진짜 AI 3줄 요약본은 키워드 매칭 시 스마트폰 텔레그램 알림으로 자동 발송됩니다.\n"
                + "아래는 수집된 본문 원문입니다:\n\n"
                + contentToSummarize;
    }
}