package ycuration.view;

import ycuration.model.Notice;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class MainFeedView {
    private JFrame frame;
    private String currentUserId;
    private List<Notice> liveNotices;
    private JPanel feedPanel;

    private int currentPageNum = 1;
    private int maxPageLimit = 5;

    private boolean isMyNoticeTab = false;

    private final Color COLOR_BG = new Color(240, 244, 248);
    private final Color COLOR_CARD_BG = Color.WHITE;
    private final Color COLOR_PRIMARY = new Color(26, 54, 124);
    private final Color COLOR_TEXT_MAIN = new Color(33, 37, 41);
    private final Color COLOR_TEXT_MUTED = new Color(108, 117, 125);

    public MainFeedView(String userId, List<Notice> notices) {
        this.currentUserId = userId;
        this.liveNotices = notices;
    }

    public void showCardFeed() {
        frame = new JFrame("Y-Curation Notice Portal");
        frame.setSize(640, 780);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(COLOR_BG);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(COLOR_CARD_BG);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel searchLine = new JPanel(new BorderLayout(10, 0));
        searchLine.setBackground(COLOR_CARD_BG);

        JTextField searchField = new JTextField();
        searchField.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        searchField.setBackground(new Color(245, 246, 248));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 228, 232), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        JButton searchButton = new JButton("검색");
        styleButton(searchButton, COLOR_PRIMARY, Color.WHITE);

        searchLine.add(searchField, BorderLayout.CENTER);
        searchLine.add(searchButton, BorderLayout.EAST);
        headerPanel.add(searchLine);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel chipBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chipBar.setBackground(COLOR_CARD_BG);

        String[] depts = {"영대 소식", "컴퓨터학부", "장학 소식"};
        JComboBox<String> deptCombo = new JComboBox<>(depts);

        deptCombo.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        deptCombo.setBackground(Color.WHITE);
        deptCombo.setForeground(COLOR_PRIMARY);
        deptCombo.setPreferredSize(new Dimension(140, 32));
        deptCombo.setFocusable(false);

        deptCombo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton("▼");
                button.setFont(new Font("맑은 고딕", Font.BOLD, 10));
                button.setForeground(COLOR_PRIMARY);
                button.setBorder(null);
                button.setContentAreaFilled(false);
                button.setFocusPainted(false);
                return button;
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, c.getWidth(), c.getHeight());
                hasFocus = false;
                super.paint(g, c);
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(Color.WHITE);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });

        deptCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index == -1) {
                    label.setBackground(Color.WHITE);
                    label.setForeground(COLOR_PRIMARY);
                    label.setFont(new Font("맑은 고딕", Font.BOLD, 13));
                } else {
                    label.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                    if (isSelected) {
                        label.setBackground(new Color(240, 244, 248));
                        label.setForeground(COLOR_PRIMARY);
                    } else {
                        label.setBackground(Color.WHITE);
                        label.setForeground(COLOR_TEXT_MAIN);
                    }
                }
                label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return label;
            }
        });

        deptCombo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 225, 230), 1, true),
                BorderFactory.createEmptyBorder(2, 4, 2, 2)
        ));
        deptCombo.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnAll = new JButton("전체 공지");
        styleButton(btnAll, COLOR_PRIMARY, Color.WHITE);

        JButton btnCurated = new JButton("내 알림함");
        styleButton(btnCurated, new Color(240, 244, 248), COLOR_TEXT_MAIN);

        JButton btnKeywordConfig = new JButton("알림 키워드 설정");
        styleButton(btnKeywordConfig, new Color(240, 244, 248), COLOR_TEXT_MAIN);

        chipBar.add(deptCombo);
        chipBar.add(btnAll);
        chipBar.add(btnCurated);
        chipBar.add(btnKeywordConfig);
        headerPanel.add(chipBar);

        frame.add(headerPanel, BorderLayout.NORTH);

        feedPanel = new JPanel(new GridBagLayout());
        feedPanel.setBackground(COLOR_BG);
        feedPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JScrollPane scrollPane = new JScrollPane(feedPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.add(createPaginationPanel(), BorderLayout.SOUTH);

        renderCards(this.liveNotices);

        java.awt.event.ActionListener searchAction = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                isMyNoticeTab = false;
                String word = searchField.getText().trim();
                ycuration.controller.NoticeDashboard.getInstance().handleSearchRequest(word);
            }
        };

        searchButton.addActionListener(searchAction);
        searchField.addActionListener(searchAction);

        deptCombo.addActionListener(e -> {
            isMyNoticeTab = false;
            String selected = (String) deptCombo.getSelectedItem();
            String targetUrl = "https://www.yu.ac.kr/main/intro/yu-news.do?mode=list";

            if ("컴퓨터학부".equals(selected)) {
                targetUrl = "https://www.yu.ac.kr/computer/notice/notice.do?mode=list&boardConfigId=14654";
            } else if ("장학 소식".equals(selected)) {
                targetUrl = "https://www.yu.ac.kr/scholar/notice/notice.do?mode=list&boardConfigId=14850";
            }

            System.out.println("[MainFeedView] 카테고리 전환 -> " + selected + " | URL: " + targetUrl);

            this.currentPageNum = 1;
            this.maxPageLimit = 5;

            ycuration.controller.NoticeDashboard.getInstance().setCurrentDeptUrl(targetUrl);
            ycuration.controller.NoticeDashboard.getInstance().changePage(1);
        });

        btnAll.addActionListener(e -> {
            isMyNoticeTab = false;
            searchField.setText("");
            btnAll.setBackground(COLOR_PRIMARY);
            btnAll.setForeground(Color.WHITE);
            btnCurated.setBackground(new Color(240, 244, 248));
            btnCurated.setForeground(COLOR_TEXT_MAIN);
            this.currentPageNum = 1;
            this.maxPageLimit = 5;
            ycuration.controller.NoticeDashboard.getInstance().handleSearchRequest("");
        });

        btnCurated.addActionListener(e -> {
            isMyNoticeTab = true;
            searchField.setText("");
            btnCurated.setBackground(COLOR_PRIMARY);
            btnCurated.setForeground(Color.WHITE);
            btnAll.setBackground(new Color(240, 244, 248));
            btnAll.setForeground(COLOR_TEXT_MAIN);
            this.currentPageNum = 1;
            ycuration.controller.NoticeDashboard.getInstance().renderSentNotices();
        });

        btnKeywordConfig.addActionListener(e -> {
            ycuration.view.KeywordSettingView configView = new ycuration.view.KeywordSettingView(frame, currentUserId);
            configView.displayWindow();
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void renderCards(List<Notice> notices) {
        feedPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 15, 0);

        if (notices == null || notices.isEmpty()) {
            String emptyMessage = isMyNoticeTab ? "아직 받은 알람이 없습니다." : "검색 결과와 일치하는 공지가 없습니다.";

            JLabel emptyLabel = new JLabel(emptyMessage, SwingConstants.CENTER);
            emptyLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.NONE;
            feedPanel.add(emptyLabel, gbc);
        } else {
            for (Notice notice : notices) {
                JPanel card = createNoticeCard(notice);
                card.setAlignmentX(Component.CENTER_ALIGNMENT);
                feedPanel.add(card, gbc);
                gbc.gridy++;
            }
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            feedPanel.add(Box.createGlue(), gbc);
        }
        feedPanel.revalidate();
        feedPanel.repaint();
    }

    public void refreshFeed(List<Notice> filteredNotices) {
        this.liveNotices = filteredNotices;
        renderCards(filteredNotices);
    }

    public void showLoadingState() {
        feedPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;

        JLabel loadingLabel = new JLabel("공지사항을 불러오는 중입니다...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        loadingLabel.setForeground(COLOR_PRIMARY);

        feedPanel.add(loadingLabel, gbc);
        feedPanel.revalidate();
        feedPanel.repaint();
    }

    public void refreshFeedWithPage(List<Notice> filteredNotices, int limitOrPageNum) {
        this.maxPageLimit = limitOrPageNum;
        this.liveNotices = filteredNotices;
        renderCards(filteredNotices);

        BorderLayout layout = (BorderLayout) frame.getContentPane().getLayout();
        Component southComponent = layout.getLayoutComponent(BorderLayout.SOUTH);
        if (southComponent != null) {
            frame.remove(southComponent);
        }

        frame.add(createPaginationPanel(), BorderLayout.SOUTH);
        frame.revalidate();
        frame.repaint();
    }

    private JPanel createNoticeCard(Notice notice) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(COLOR_CARD_BG);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        Color defaultBorderColor = new Color(230, 235, 240);
        Color hoverBorderColor = COLOR_PRIMARY;

        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(defaultBorderColor, 1, true),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        card.setMinimumSize(new Dimension(580, 100));
        card.setPreferredSize(new Dimension(580, 100));
        card.setMaximumSize(new Dimension(580, 100));

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                clickNoticeCard(0, notice.getOriginalUrl());
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(250, 252, 255));
                card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(hoverBorderColor, 1, true),
                        BorderFactory.createEmptyBorder(18, 20, 18, 20)
                ));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                card.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(COLOR_CARD_BG);
                card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(defaultBorderColor, 1, true),
                        BorderFactory.createEmptyBorder(18, 20, 18, 20)
                ));
                card.repaint();
            }
        });

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(COLOR_CARD_BG);

        JLabel titleLabel = new JLabel(notice.getTitle());
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        titleLabel.setForeground(COLOR_TEXT_MAIN);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        JLabel dateLabel = new JLabel("작성일: " + notice.getDate() + "  |  영남대학교 포털광장");
        dateLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        dateLabel.setForeground(COLOR_TEXT_MUTED);
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(dateLabel);

        card.add(contentPanel, BorderLayout.CENTER);

        JButton viewBtn = new JButton("원문 보기");
        viewBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        viewBtn.setForeground(COLOR_PRIMARY);
        viewBtn.setBackground(COLOR_CARD_BG);
        viewBtn.setBorder(null);
        viewBtn.setContentAreaFilled(false);
        viewBtn.setFocusPainted(false);
        viewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewBtn.addActionListener(e -> clickNoticeCard(0, notice.getOriginalUrl()));

        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setBackground(COLOR_CARD_BG);
        btnWrapper.add(viewBtn, BorderLayout.SOUTH);
        card.add(btnWrapper, BorderLayout.EAST);

        contentPanel.setOpaque(false);
        btnWrapper.setOpaque(false);

        return card;
    }

    private JPanel createPaginationPanel() {
        JPanel pagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        pagePanel.setBackground(COLOR_BG);

        for (int i = 1; i <= this.maxPageLimit; i++) {
            final int pageNum = i;
            JButton pageBtn = new JButton(String.valueOf(i));

            pageBtn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
            pageBtn.setPreferredSize(new Dimension(38, 38));
            pageBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            pageBtn.setFocusPainted(false);
            pageBtn.setOpaque(true);

            if (pageNum == this.currentPageNum) {
                pageBtn.setForeground(COLOR_PRIMARY);
                pageBtn.setBackground(Color.WHITE);
                pageBtn.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARY, 2, true));
            } else {
                pageBtn.setForeground(COLOR_TEXT_MUTED);
                pageBtn.setBackground(Color.WHITE);
                pageBtn.setBorder(BorderFactory.createLineBorder(new Color(225, 228, 232), 1, true));

                pageBtn.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        pageBtn.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARY, 1, true));
                        pageBtn.setForeground(COLOR_PRIMARY);
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        pageBtn.setBorder(BorderFactory.createLineBorder(new Color(225, 228, 232), 1, true));
                        pageBtn.setForeground(COLOR_TEXT_MUTED);
                    }
                });
            }

            pageBtn.addActionListener(e -> {
                this.currentPageNum = pageNum;
                ycuration.controller.NoticeDashboard.getInstance().changePage(pageNum);
            });

            pagePanel.add(pageBtn);
        }
        return pagePanel;
    }

    public void clickNoticeCard(int noticeId, String url) {
        if (url == null || url.trim().isEmpty()) return;
        String targetUrl = url.trim();

        if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
            if (!targetUrl.startsWith("/")) targetUrl = "/" + targetUrl;
            targetUrl = "https://www.yu.ac.kr" + targetUrl;
        }

        if (java.awt.Desktop.isDesktopSupported()) {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(targetUrl));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "브라우저 연동 실패:\n" + targetUrl, "연결 에러", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void closeWindow() {
        if (frame != null) frame.dispose();
    }

    private void styleButton(JButton btn, Color background, Color foreground) {
        btn.setBackground(background);
        btn.setForeground(foreground);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }


    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {

        @Override
        protected void installDefaults() {
            super.installDefaults();
            scrollbar.setPreferredSize(new Dimension(8, 0));
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(new Color(240, 244, 248));
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(180, 188, 196));
            g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1, thumbBounds.width - 2, thumbBounds.height - 2, 6, 6);
            g2.dispose();
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton(); // 상단 삼각형 화살표 삭제
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton(); // 하단 삼각형 화살표 삭제
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
    }
}