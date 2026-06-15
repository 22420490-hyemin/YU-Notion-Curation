package ycuration.model;

public class Notice {
    private int noticeId;
    private String title;
    private String content;
    private String date;
    private String originalUrl;
    private String[] summaryLines;

    // 생성자 (Constructor)
    public Notice(int noticeId, String title, String content, String date, String originalUrl) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.date = date;
        this.originalUrl = originalUrl;
        this.summaryLines = new String[3];
    }

    // Getter and Setter Methods
    public int getNoticeId() { return noticeId; }
    public void setNoticeId(int noticeId) { this.noticeId = noticeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

}
