package ycuration.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.util.ArrayList;

public class KeywordSettingView {
    private JDialog dialog;
    private JFrame parentFrame;
    private String currentUserId;
    private JTextField keywordField;
    private JPanel chipContainer;
    private JLabel activeTitleLabel;

    private JTextField tokenField;
    private JTextField chatIdField;

    private final Color COLOR_NAV_BG = Color.WHITE;
    private final Color COLOR_BODY_BG = new Color(245, 247, 250);
    private final Color COLOR_CARD_BG = Color.WHITE;
    private final Color COLOR_PRIMARY = new Color(26, 54, 124);
    private final Color COLOR_TEXT_MAIN = new Color(33, 37, 41);
    private final Color COLOR_TEXT_MUTED = new Color(127, 140, 141);
    private final Color COLOR_CHIP_BG = new Color(225, 238, 254);

    public KeywordSettingView(JFrame parent, String userId) {
        this.parentFrame = parent;
        this.currentUserId = userId;
    }

    public void displayWindow() {
        dialog = new JDialog(parentFrame, "", true);
        dialog.setUndecorated(true);
        dialog.setSize(750, 600);
        dialog.setLayout(new BorderLayout());
        dialog.setShape(new RoundRectangle2D.Double(0, 0, dialog.getWidth(), dialog.getHeight(), 20, 20));

        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(COLOR_NAV_BG);
        navBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 235, 240)),
                BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        JLabel navTitle = new JLabel("키워드 관리 및 서버 연동 (Keyword & Server config)");
        navTitle.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        navTitle.setForeground(COLOR_PRIMARY);
        navBar.add(navTitle, BorderLayout.WEST);

        JButton closeBtn = new JButton("공지 화면으로 돌아가기");
        closeBtn.setBackground(COLOR_PRIMARY);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setOpaque(true);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        closeBtn.addActionListener(e -> dialog.dispose());
        navBar.add(closeBtn, BorderLayout.EAST);

        dialog.add(navBar, BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBackground(COLOR_BODY_BG);
        bodyPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel mainCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        mainCard.setOpaque(false);
        mainCard.setLayout(null);

        JLabel tgGuideTitle = new JLabel("실시간 텔레그램 알림 연동 상세 가이드");
        tgGuideTitle.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        tgGuideTitle.setForeground(new Color(192, 57, 43));
        tgGuideTitle.setBounds(30, 25, 450, 25);
        mainCard.add(tgGuideTitle);

        JLabel tgGuideText1 = new JLabel("1. 텔레그램 @BotFather 검색 -> /newbot 전송 -> 봇 이름 설정 후 나오는 [API Token] 복사");
        tgGuideText1.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        tgGuideText1.setForeground(COLOR_TEXT_MAIN);
        tgGuideText1.setBounds(30, 55, 600, 20);
        mainCard.add(tgGuideText1);

        // 💡 [제일 중요한 필수 단계 시각적 강조]
        JLabel tgGuideText2 = new JLabel("2. 방금 만든 내 봇을 검색하여 대화방 입장 -> 하단의 [시작] 버튼을 반드시 클릭! (필수)");
        tgGuideText2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        tgGuideText2.setForeground(new Color(41, 128, 185)); // 파란색 굵은 글씨로 경고 강조
        tgGuideText2.setBounds(30, 75, 600, 20);
        mainCard.add(tgGuideText2);

        JLabel tgGuideText3 = new JLabel("3. 텔레그램 @userinfobot 검색 -> [시작] 클릭 후 본인 고유의 [Chat ID (숫자)] 복사");
        tgGuideText3.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        tgGuideText3.setForeground(COLOR_TEXT_MAIN);
        tgGuideText3.setBounds(30, 95, 600, 20);
        mainCard.add(tgGuideText3);

        // UI 겹침 방지를 위해 폼 입력칸 전체 좌표 25px씩 하강 조치
        JLabel tokenLabel = new JLabel("Bot Token :");
        tokenLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        tokenLabel.setBounds(30, 130, 90, 25);
        mainCard.add(tokenLabel);

        tokenField = new JTextField();
        setupStyledField(tokenField);
        tokenField.setBounds(120, 125, 250, 35);
        mainCard.add(tokenField);

        JLabel chatLabel = new JLabel("Chat ID :");
        chatLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        chatLabel.setBounds(390, 130, 80, 25);
        mainCard.add(chatLabel);

        chatIdField = new JTextField();
        setupStyledField(chatIdField);
        chatIdField.setBounds(470, 125, 170, 35);
        mainCard.add(chatIdField);

        JButton saveTgBtn = new JButton("연동 저장");
        saveTgBtn.setBounds(540, 170, 100, 35);
        saveTgBtn.setBackground(new Color(46, 204, 113));
        saveTgBtn.setForeground(Color.WHITE);
        saveTgBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        saveTgBtn.setFocusPainted(false);
        saveTgBtn.setBorderPainted(false);
        saveTgBtn.setOpaque(true);
        saveTgBtn.addActionListener(e -> saveTelegramData());
        mainCard.add(saveTgBtn);

        JPanel separator = new JPanel();
        separator.setBackground(new Color(230, 235, 240));
        separator.setBounds(30, 220, 610, 2);
        mainCard.add(separator);

        JLabel mainTitle = new JLabel("알림 수신 키워드 등록");
        mainTitle.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        mainTitle.setForeground(COLOR_TEXT_MAIN);
        mainTitle.setBounds(30, 240, 300, 25);
        mainCard.add(mainTitle);

        JLabel subTitle = new JLabel("키워드를 입력해 두면 관련 공지가 등록되는 즉시 스마트폰 알림이 발송됩니다.");
        subTitle.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        subTitle.setForeground(COLOR_TEXT_MUTED);
        subTitle.setBounds(30, 267, 600, 20);
        mainCard.add(subTitle);

        keywordField = new JTextField();
        setupStyledField(keywordField);
        keywordField.setBounds(30, 300, 510, 42);
        keywordField.addActionListener(e -> handleAddKeyword());
        mainCard.add(keywordField);

        JButton addBtn = new JButton("+ 추가");
        addBtn.setBounds(550, 300, 90, 42);
        addBtn.setBackground(COLOR_PRIMARY);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        addBtn.setFocusPainted(false);
        addBtn.setBorderPainted(false);
        addBtn.setOpaque(true);
        addBtn.addActionListener(e -> handleAddKeyword());
        mainCard.add(addBtn);

        activeTitleLabel = new JLabel("현재 감시 중인 키워드 (0)");
        activeTitleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        activeTitleLabel.setForeground(COLOR_TEXT_MAIN);
        activeTitleLabel.setBounds(30, 370, 300, 20);
        mainCard.add(activeTitleLabel);

        chipContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        chipContainer.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(chipContainer);
        scrollPane.setBounds(30, 400, 610, 90);
        scrollPane.setBorder(null);
        mainCard.add(scrollPane);

        loadTelegramData();
        refreshKeywordChips();

        bodyPanel.add(mainCard, BorderLayout.CENTER);
        dialog.add(bodyPanel, BorderLayout.CENTER);

        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
    }

    private void setupStyledField(JTextField field) {
        field.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        field.setBackground(new Color(245, 246, 248));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 228, 232), 1, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
    }

    private void showCustomDialog(String title, String message, boolean isError) {
        JDialog customDialog = new JDialog(dialog, "", true);
        customDialog.setUndecorated(true);
        customDialog.setSize(340, 180);
        customDialog.setLayout(null);
        customDialog.getContentPane().setBackground(Color.WHITE);
        customDialog.setShape(new RoundRectangle2D.Double(0, 0, customDialog.getWidth(), customDialog.getHeight(), 16, 16));

        JLabel iconLabel = new JLabel(isError ? "[ ! ]" : "[ OK ]", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 18)); // 글자가 들어가므로 폰트 크기를 조금 조절
        iconLabel.setForeground(isError ? java.awt.Color.RED : new java.awt.Color(39, 174, 96));

        iconLabel.setBounds(20, 25, 50, 40); // 글자 폭을 고려해 필요시 width만 살짝 조정
        customDialog.add(iconLabel);

        Color titleColor = isError ? new Color(192, 57, 43) : COLOR_PRIMARY;
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        titleLabel.setForeground(titleColor);
        titleLabel.setBounds(75, 22, 200, 22);
        customDialog.add(titleLabel);

        JLabel msgLabel = new JLabel("<html><body style='font-family:맑은 고딕; font-size:11px; color:#7f8c8d;'>" + message.replace("\n", "<br>") + "</body></html>");
        msgLabel.setBounds(75, 48, 240, 40);
        customDialog.add(msgLabel);

        JButton confirmBtn = new JButton("확인");
        confirmBtn.setBounds(25, 105, 290, 40);
        confirmBtn.setBackground(COLOR_PRIMARY);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setOpaque(true);
        confirmBtn.addActionListener(ev -> customDialog.dispose());
        customDialog.add(confirmBtn);

        customDialog.setLocationRelativeTo(dialog);
        customDialog.setVisible(true);
    }

    private void saveTelegramData() {
        String token = tokenField.getText().trim();
        String chatId = chatIdField.getText().trim();

        if (token.isEmpty() || chatId.isEmpty()) {
            showCustomDialog("입력 오류", "토큰과 Chat ID를 모두 입력해주세요.", true);
            return;
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter("telegram_config.txt"))) {
            pw.println(token + "," + chatId);
            showCustomDialog("연동 완료", "서버 연동 정보가 저장되었습니다.\n지금부터 알림 엔진이 가동됩니다.", false);
        } catch (Exception e) {
            showCustomDialog("시스템 오류", "설정 저장 실패: " + e.getMessage(), true);
        }
    }

    private void loadTelegramData() {
        File tgFile = new File("telegram_config.txt");
        if (tgFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(tgFile))) {
                String line = br.readLine();
                if (line != null && line.contains(",")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        tokenField.setText(parts[0].trim());
                        chatIdField.setText(parts[1].trim());
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private void handleAddKeyword() {
        String word = keywordField.getText().trim();
        if (!word.isEmpty()) {
            appendKeywordToFile(word);
            keywordField.setText("");
            refreshKeywordChips();
        }
    }

    private void refreshKeywordChips() {
        chipContainer.removeAll();
        ArrayList<String> list = loadKeywords();
        activeTitleLabel.setText("현재 감시 중인 키워드 (" + list.size() + ")");

        for (String keyword : list) {
            JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COLOR_CHIP_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                    g2.dispose();
                }
            };
            chip.setOpaque(false);

            JLabel textLabel = new JLabel(keyword);
            textLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            textLabel.setForeground(COLOR_PRIMARY);
            chip.add(textLabel);

            JButton delBtn = new JButton("✕");
            delBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            delBtn.setForeground(COLOR_PRIMARY);
            delBtn.setBorder(null);
            delBtn.setContentAreaFilled(false);
            delBtn.setFocusPainted(false);
            delBtn.addActionListener(e -> {
                removeKeywordFromFile(keyword);
                refreshKeywordChips();
            });
            chip.add(delBtn);

            chipContainer.add(chip);
        }
        chipContainer.revalidate();
        chipContainer.repaint();
    }

    private ArrayList<String> loadKeywords() {
        ArrayList<String> list = new ArrayList<>();
        File file = new File("keywords.txt");
        if (!file.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line != null && !line.trim().isEmpty()) {
                for (String k : line.split(",")) {
                    if (!k.trim().isEmpty()) list.add(k.trim());
                }
            }
        } catch (Exception e) {}
        return list;
    }

    private void appendKeywordToFile(String newKeyword) {
        ArrayList<String> list = loadKeywords();
        if (!list.contains(newKeyword)) {
            list.add(newKeyword);
            saveListToFile(list);
        }
    }

    private void removeKeywordFromFile(String target) {
        ArrayList<String> list = loadKeywords();
        list.remove(target);
        saveListToFile(list);
    }

    private void saveListToFile(ArrayList<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String k : list) {
            if (sb.length() > 0) sb.append(",");
            sb.append(k);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("keywords.txt"))) {
            bw.write(sb.toString());
        } catch (IOException e) {}
    }
}